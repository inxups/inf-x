package com.pixulse.infx.crafting;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * MITE R196 crafting difficulty and workbench-level rules adapted to the
 * modern 26.2 recipe API.
 *
 * <p>MITE stores a difficulty on each recipe at registration time.  Modern
 * ingredients can be tags and special recipes can produce an output based on
 * the actual stacks, so the adapter computes the value for the matched input
 * on the server.  This is equivalent for ordinary recipes and is more precise
 * for tag/special recipes.</p>
 */
public final class MiteCraftingRules {
    public static final float MINIMUM_DIFFICULTY = 25.0F;
    public static final float LEATHER_DIFFICULTY = 100.0F;
    public static final float BLAZE_ROD_DIFFICULTY = 200.0F;
    private static final float BLOCK_DIFFICULTY_SCALE = 100.0F;
    private static final float DEFAULT_BLOCK_DIFFICULTY = 25.0F;
    private static final float UNBREAKABLE_BLOCK_DIFFICULTY = 25_600.0F;
    private static final double LEVEL_BONUS_PER_LEVEL = 0.02D;

    private MiteCraftingRules() {}

    /**
     * Returns the R196 component value for one occupied crafting slot.
     *
     * <p>Recipe slots, rather than the stack count in a slot, are counted. A
     * vanilla crafting recipe consumes one item from each occupied slot; this
     * also mirrors MITE's expansion of counted shapeless components into
     * one-item entries.</p>
     */
    public static float componentDifficulty(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0.0F;
        }

        Item item = stack.getItem();
        String path = itemPath(item);

        // Values explicitly present in the R196 source.  Keep these before
        // the block/material heuristics because modern blocks often have a
        // different destroy time from their 1.6.4 counterparts.
        float exact = exactComponentDifficulty(path, item);
        if (exact > 0.0F) {
            return exact;
        }

        float material = materialComponentDifficulty(path);
        if (material > 0.0F) {
            return material;
        }

        if (item instanceof BlockItem blockItem) {
            float storage = storageBlockDifficulty(path);
            if (storage > 0.0F) {
                return storage;
            }
            float destroyTime = blockItem.getBlock().defaultDestroyTime();
            if (!Float.isFinite(destroyTime) || destroyTime < 0.0F) {
                return UNBREAKABLE_BLOCK_DIFFICULTY;
            }
            return Math.max(DEFAULT_BLOCK_DIFFICULTY, destroyTime * BLOCK_DIFFICULTY_SCALE);
        }

        // R196 assigns 25 to the large family of simple drops (seeds, dyes,
        // food, paper, sticks, etc.).  New 26.2 items without an old MITE
        // counterpart use the same conservative leaf value instead of making
        // them free or bypassing the timed-crafting path.
        return MINIMUM_DIFFICULTY;
    }

    /** Sums occupied input slots, as MITE's RecipeHelper does. */
    public static float recipeDifficulty(CraftingInput input) {
        float result = 0.0F;
        for (ItemStack stack : input.items()) {
            result += componentDifficulty(stack);
        }
        return Math.max(MINIMUM_DIFFICULTY, result);
    }

    /**
     * Computes a representative difficulty for recipe displays/JEI when no
     * concrete input is available. The cheapest member of a tag is used,
     * matching MITE's lowest-producible-component convention.
     */
    public static float representativeDifficulty(CraftingRecipe recipe) {
        float result = 0.0F;
        for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
            Optional<Float> cheapest = ingredientItems(ingredient)
                    .map(holder -> componentDifficulty(new ItemStack(holder.value())))
                    .min(Float::compare);
            result += cheapest.orElse(MINIMUM_DIFFICULTY);
        }
        return Math.max(MINIMUM_DIFFICULTY, result);
    }

    /**
     * Builds a profile for a vanilla 26.2 crafting recipe and a concrete
     * matched input.
     */
    public static CraftingProfile profile(CraftingRecipe recipe, CraftingInput input) {
        ItemStack output = assembleSafely(recipe, input);
        BenchTier gridTier = gridTier(input);
        boolean materialGated = hasMaterialGate(output, input);
        BenchTier materialTier = materialTier(output);
        BenchTier required = materialGated ? max(gridTier, materialTier) : gridTier;
        return new CraftingProfile(required, recipeDifficulty(input), materialGated);
    }

    /** Profile used by client recipe displays where there is no concrete grid. */
    public static CraftingProfile displayProfile(CraftingRecipe recipe) {
        BenchTier gridTier = gridTier(recipe);
        ItemStack output = displayOutput(recipe);
        BenchTier materialTier = materialTier(output);
        boolean materialGated = hasMaterialGate(output, representativeInputs(recipe));
        BenchTier required = materialGated ? max(gridTier, materialTier) : gridTier;
        return new CraftingProfile(required, representativeDifficulty(recipe), materialGated);
    }

    /** R196's per-level crafting reduction: +2% per displayed level. */
    public static double levelModifier(int level) {
        return level * LEVEL_BONUS_PER_LEVEL;
    }

    /**
     * Returns the workbench bonus used by MITE's EntityPlayerSP. Ordinary
     * recipes receive 20% on any workbench; material-gated recipes use the
     * actual workbench tier. Hand crafting has no bench bonus.
     */
    public static double benchModifier(BenchTier actualBench, boolean materialGated) {
        if (!actualBench.isWorkbench()) {
            return 0.0D;
        }
        return materialGated ? actualBench.speedBonus() : BenchTier.FLINT.speedBonus();
    }

    private static ItemStack assembleSafely(CraftingRecipe recipe, CraftingInput input) {
        try {
            ItemStack output = recipe.assemble(input);
            return output == null ? ItemStack.EMPTY : output;
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static BenchTier gridTier(CraftingRecipe recipe) {
        if (recipe instanceof ShapedRecipe shaped) {
            return shaped.getWidth() <= 2 && shaped.getHeight() <= 2
                    ? BenchTier.HAND
                    : BenchTier.FLINT;
        }
        if (recipe instanceof ShapelessRecipe) {
            return recipe.placementInfo().ingredients().size() <= 4
                    ? BenchTier.HAND
                    : BenchTier.FLINT;
        }
        // Modern special recipes expose either a crafting display or a
        // placement contract.  Use the largest displayed shape; this keeps
        // two-item repairs/dyes available in the 2x2 inventory grid while
        // retaining the 3x3 requirement for map and decorated-pot recipes.
        return recipe.display().stream()
                        .map(MiteCraftingRules::displayGridTier)
                        .max(MiteCraftingRules::compareTier)
                        .orElse(recipe.placementInfo().ingredients().size() <= 4
                                ? BenchTier.HAND
                                : BenchTier.FLINT);
    }

    private static BenchTier gridTier(CraftingInput input) {
        return input.width() <= 2 && input.height() <= 2 ? BenchTier.HAND : BenchTier.FLINT;
    }

    private static BenchTier materialTier(ItemStack stack) {
        return stack == null || stack.isEmpty() ? BenchTier.HAND : materialTier(stack.getItem());
    }

    private static boolean hasMaterialGate(ItemStack output, CraftingInput input) {
        return hasMaterialGate(output, input == null ? Stream.empty() : input.items().stream());
    }

    private static boolean hasMaterialGate(ItemStack output, Stream<ItemStack> inputs) {
        if (output.isEmpty()) {
            return false;
        }
        String path = itemPath(output.getItem());
        // RecipeHelper's explicit exceptions: ingot/nugget conversions and
        // shard/gem conversions are material preparation, not metalworking.
        // A bucket made from another bucket is likewise a container conversion.
        if (path.endsWith("_ingot") || path.endsWith("_nugget") || path.endsWith("_shard")
                || path.equals("diamond") || path.equals("emerald") || path.equals("quartz")
                || path.equals("nether_quartz") || path.equals("flint") || path.equals("obsidian")) {
            return false;
        }
        if (path.equals("bucket") || path.endsWith("_bucket")) {
            return !inputs.anyMatch(stack -> !stack.isEmpty() && itemPath(stack.getItem()).contains("bucket"));
        }
        if (path.contains("knife")) {
            return false;
        }

        // MITE marks planks as wood products even though wood is not a metal;
        // in modern InfiniteX this is the flint-level woodworking gate.
        if (path.contains("plank") || path.endsWith("_workbench")) {
            return true;
        }

        // The output, rather than an arbitrary ingredient, determines MITE's
        // hardness check.  This prevents iron rails or redstone components
        // from being promoted solely because their recipe happens to consume
        // an ingot, while still covering modern metal hardware by name.
        if (materialTier(output) != BenchTier.HAND && isMaterialProduct(path)) {
            return true;
        }
        return false;
    }

    private static boolean isMaterialProduct(String path) {
        if (path.endsWith("_block") || path.endsWith("_bars") || path.endsWith("_door")
                || path.endsWith("_trapdoor") || path.endsWith("_grate") || path.endsWith("_lantern")
                || path.endsWith("_bulb") || path.endsWith("_chest") || path.endsWith("_anvil")
                || path.contains("pickaxe") || path.contains("shovel") || path.contains("axe")
                || path.contains("hoe") || path.contains("sword") || path.contains("armor")
                || path.contains("helmet") || path.contains("chestplate") || path.contains("leggings")
                || path.contains("boots") || path.contains("shears") || path.contains("minecart")
                || path.contains("cauldron") || path.contains("compass") || path.equals("clock")
                || path.contains("flint_and_steel") || path.contains("chain")) {
            return true;
        }
        // A material-bearing output which has no modern naming convention is
        // still treated as a metal product.  This covers third-party items
        // that expose a material in their registry path without requiring a
        // hard dependency on their classes.
        return path.contains("copper") || path.contains("silver") || path.contains("gold")
                || path.contains("iron") || path.contains("steel") || path.contains("bronze")
                || path.contains("ancient_metal") || path.contains("mithril")
                || path.contains("adamantium") || path.contains("netherite")
                || path.contains("diamond") || path.contains("emerald") || path.contains("quartz");
    }

    private static BenchTier displayGridTier(RecipeDisplay display) {
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            return shaped.width() <= 2 && shaped.height() <= 2 ? BenchTier.HAND : BenchTier.FLINT;
        }
        if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            return shapeless.ingredients().size() <= 4 ? BenchTier.HAND : BenchTier.FLINT;
        }
        return BenchTier.FLINT;
    }

    private static ItemStack displayOutput(CraftingRecipe recipe) {
        for (RecipeDisplay display : recipe.display()) {
            ItemStack output = displayOutput(display.result());
            if (!output.isEmpty()) {
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    private static Stream<ItemStack> representativeInputs(CraftingRecipe recipe) {
        return recipe.placementInfo().ingredients().stream()
                .flatMap(ingredient -> ingredientItems(ingredient).limit(1))
                .map(holder -> new ItemStack(holder.value()));
    }

    private static Stream<net.minecraft.core.Holder<Item>> ingredientItems(Ingredient ingredient) {
        if (ingredient.isCustom()) {
            var custom = ingredient.getCustomIngredient();
            return custom == null ? Stream.empty() : custom.items();
        }
        return ingredient.getValues().stream();
    }

    private static ItemStack displayOutput(SlotDisplay display) {
        if (display instanceof SlotDisplay.ItemSlotDisplay item) {
            return new ItemStack(item.item().value());
        }
        if (display instanceof SlotDisplay.ItemStackSlotDisplay stack) {
            return stack.stack().create();
        }
        if (display instanceof SlotDisplay.Composite composite) {
            for (SlotDisplay child : composite.contents()) {
                ItemStack result = displayOutput(child);
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        if (display instanceof SlotDisplay.WithRemainder remainder) {
            return displayOutput(remainder.input());
        }
        return ItemStack.EMPTY;
    }

    private static BenchTier materialTier(Item item) {
        return materialTier(itemPath(item));
    }

    private static BenchTier materialTier(String rawPath) {
        String path = rawPath.toLowerCase(Locale.ROOT);
        if (path.contains("adamantium") || path.contains("netherite")) {
            return BenchTier.ADAMANTIUM;
        }
        if (path.contains("mithril") || path.contains("diamond")) {
            return path.contains("diamond") ? BenchTier.ANCIENT_METAL : BenchTier.MITHRIL;
        }
        if (path.contains("ancient_metal") || path.contains("ancientmetal")) {
            return BenchTier.ANCIENT_METAL;
        }
        if (path.contains("rusted_iron") || path.contains("rustediron")) {
            return BenchTier.COPPER;
        }
        if (path.contains("iron") || path.contains("steel") || path.contains("emerald")) {
            return BenchTier.IRON;
        }
        if (path.contains("copper") || path.contains("silver") || path.contains("gold")
                || path.contains("bronze") || path.contains("quartz")) {
            return BenchTier.COPPER;
        }
        if (path.contains("obsidian")) {
            return BenchTier.OBSIDIAN;
        }
        if (path.contains("flint") || path.contains("wood") || path.contains("leather")
                || path.contains("plank") || path.equals("crafting_table")) {
            return BenchTier.FLINT;
        }
        return BenchTier.HAND;
    }

    private static float exactComponentDifficulty(String path, Item item) {
        return switch (path) {
            case "stick", "string", "feather", "gunpowder", "wheat", "paper", "glowstone_dust",
                    "blaze_powder", "snowball", "wheat_seeds", "egg", "bowl", "bucket", "redstone",
                    "flour", "sinew", "coin", "coal", "charcoal", "sugar", "clay_ball",
                    "lapis_lazuli", "dye", "experience_bottle" -> 25.0F;
            case "leather", "bone", "nether_star", "ender_pearl", "potion", "splash_potion",
                    "lingering_potion", "brick", "nether_brick", "map", "filled_map", "milk_bucket",
                    "sugar_cane", "reeds", "slime_ball" -> LEATHER_DIFFICULTY;
            case "arrow", "spectral_arrow", "tipped_arrow" -> 40.0F;
            case "blaze_rod" -> BLAZE_ROD_DIFFICULTY;
            case "flint" -> 100.0F;
            case "glass" -> 200.0F;
            case "coal_block" -> 120.0F;
            case "lapis_block" -> 300.0F;
            case "obsidian" -> 240.0F;
            case "emerald", "diamond" -> 100.0F * (path.equals("diamond") ? 16.0F : 8.0F);
            case "quartz", "nether_quartz" -> 900.0F;
            default -> {
                if (item == Items.AIR) {
                    yield 0.0F;
                }
                yield 0.0F;
            }
        };
    }

    private static float materialComponentDifficulty(String rawPath) {
        String path = rawPath.toLowerCase(Locale.ROOT);
        if (path.endsWith("_nugget")) {
            return materialIngotDifficulty(path.substring(0, path.length() - "_nugget".length())) / 9.0F;
        }
        if (path.endsWith("_shard")) {
            String base = path.substring(0, path.length() - "_shard".length());
            float whole = switch (base) {
                case "flint" -> 100.0F;
                case "quartz", "nether_quartz" -> 900.0F;
                case "glass" -> 200.0F;
                case "obsidian" -> 200.0F;
                case "diamond" -> 1600.0F;
                default -> materialIngotDifficulty(base);
            };
            return whole > 0.0F ? whole / (base.equals("flint") ? 4.0F : 9.0F) : 0.0F;
        }
        if (path.endsWith("_ingot")) {
            return materialIngotDifficulty(path.substring(0, path.length() - "_ingot".length()));
        }
        if (path.endsWith("_chain")) {
            return materialIngotDifficulty(path.substring(0, path.length() - "_chain".length())) * 4.0F / 9.0F;
        }
        if (path.endsWith("_coin")) {
            return 25.0F;
        }
        return 0.0F;
    }

    private static float storageBlockDifficulty(String rawPath) {
        String path = rawPath.toLowerCase(Locale.ROOT);
        if (!path.endsWith("_block")) {
            return 0.0F;
        }
        String material = path.substring(0, path.length() - "_block".length());
        if (material.startsWith("raw_")) {
            material = material.substring("raw_".length());
        }
        float ingot = materialIngotDifficulty(material);
        if (ingot > 0.0F) {
            return ingot * 9.0F;
        }
        return switch (material) {
            case "diamond" -> 1600.0F * 9.0F;
            case "emerald" -> 800.0F * 9.0F;
            case "quartz", "nether_quartz" -> 900.0F * 4.0F;
            case "glass" -> 200.0F;
            case "coal", "charcoal", "lapis_lazuli" -> 25.0F * 9.0F;
            default -> 0.0F;
        };
    }

    private static float materialIngotDifficulty(String material) {
        String path = material.toLowerCase(Locale.ROOT);
        if (path.contains("adamantium") || path.contains("netherite")) return 25_600.0F;
        if (path.contains("mithril")) return 6_400.0F;
        if (path.contains("ancient_metal") || path.contains("ancientmetal")) return 1_600.0F;
        if (path.contains("rusted")) return 400.0F;
        if (path.contains("iron") || path.contains("steel")) return 800.0F;
        if (path.contains("copper") || path.contains("silver") || path.contains("gold")
                || path.contains("bronze")) return 400.0F;
        return 0.0F;
    }

    private static String itemPath(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static BenchTier max(BenchTier first, BenchTier second) {
        return compareTier(first, second) >= 0 ? first : second;
    }

    private static int compareTier(BenchTier first, BenchTier second) {
        return Integer.compare(first.capability(), second.capability());
    }
}
