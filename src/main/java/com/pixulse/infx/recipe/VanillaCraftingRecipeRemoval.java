package com.pixulse.infx.recipe;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pixulse.infx.InfiniteX;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;

/**
 * Removes Minecraft's weapon and tool crafting recipes before recipe
 * deserialization.
 *
 * <p>All other vanilla recipes (planks, food, armor, smelting, ...) are
 * restored and matched through the standard crafting type; their INFX
 * difficulty and workbench tier come from {@link RecipeRules} overrides or
 * {@link InfxCraftingRules} inference. Weapons and tools stay disabled
 * because InfiniteX provides its own equipment line for every material.
 * Tool-adjacent special recipes (grid repair, shield decoration, tipped
 * arrows) are removed with them.</p>
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class VanillaCraftingRecipeRemoval {
    private static final String MINECRAFT_NAMESPACE = "minecraft";

    /** Vanilla weapon and tool crafting recipes that remain disabled. */
    private static final Set<String> RETAINED_WEAPON_TOOL_RECIPES = Set.of(
            // Melee and ranged weapons.
            "wooden_sword",
            "stone_sword",
            "copper_sword",
            "iron_sword",
            "golden_sword",
            "diamond_sword",
            "wooden_spear",
            "stone_spear",
            "copper_spear",
            "iron_spear",
            "golden_spear",
            "diamond_spear",
            "bow",
            "arrow",
            "crossbow",
            "mace",
            // Tools.
            "wooden_axe",
            "wooden_pickaxe",
            "wooden_shovel",
            "wooden_hoe",
            "stone_axe",
            "stone_pickaxe",
            "stone_shovel",
            "stone_hoe",
            "copper_axe",
            "copper_pickaxe",
            "copper_shovel",
            "copper_hoe",
            "iron_axe",
            "iron_pickaxe",
            "iron_shovel",
            "iron_hoe",
            "golden_axe",
            "golden_pickaxe",
            "golden_shovel",
            "golden_hoe",
            "diamond_axe",
            "diamond_pickaxe",
            "diamond_shovel",
            "diamond_hoe",
            "shears",
            "fishing_rod",
            "flint_and_steel",
            "brush",
            "spyglass",
            "carrot_on_a_stick",
            "warped_fungus_on_a_stick",
            "shield");

    /**
     * Special crafting serializers that are weapon/tool adjacent and stay
     * disabled together with the weapon/tool recipes.
     */
    private static final Set<String> RETAINED_SPECIAL_SERIALIZERS = Set.of(
            "crafting_special_repairitem",
            "crafting_special_shielddecoration",
            "crafting_imbue");

    private VanillaCraftingRecipeRemoval() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void removeRecipes(ModifyRecipeJsonsEvent event) {
        int removed = removeVanillaCraftingRecipes(event.getRecipeJsons());
        InfiniteX.LOGGER.info("Removed {} vanilla weapon/tool crafting recipes", removed);
    }

    static int removeVanillaCraftingRecipes(Map<Identifier, JsonElement> recipes) {
        int initialSize = recipes.size();
        recipes.entrySet().removeIf(entry -> isVanillaCraftingRecipe(entry.getKey(), entry.getValue()));
        return initialSize - recipes.size();
    }

    static boolean isVanillaCraftingRecipe(Identifier recipeId, JsonElement recipeJson) {
        if (!MINECRAFT_NAMESPACE.equals(recipeId.getNamespace()) || !recipeJson.isJsonObject()) {
            return false;
        }
        JsonObject object = recipeJson.getAsJsonObject();
        JsonElement typeElement = object.get("type");
        if (typeElement == null || !typeElement.isJsonPrimitive()
                || !typeElement.getAsJsonPrimitive().isString()) {
            return false;
        }
        Identifier type = Identifier.tryParse(typeElement.getAsString());
        if (type == null || !MINECRAFT_NAMESPACE.equals(type.getNamespace())) {
            return false;
        }
        String serializer = type.getPath();
        if (RETAINED_SPECIAL_SERIALIZERS.contains(serializer)) {
            return true;
        }
        return (serializer.equals("crafting_shaped") || serializer.equals("crafting_shapeless"))
                && RETAINED_WEAPON_TOOL_RECIPES.contains(recipeId.getPath());
    }
}
