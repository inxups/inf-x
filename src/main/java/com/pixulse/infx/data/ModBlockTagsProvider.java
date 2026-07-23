package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196MiningFamily;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.tag.ModTags;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

final class ModBlockTagsProvider extends BlockTagsProvider {
    ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        addVanillaMiningTags();
        addEffectiveToolTags();
        addPortableBlocks();
        addHarvestLevels();
        addWorldgenTags();
        addGelatinousCubeTags();
        ModBlocks.R196_FLOWERS.forEach(flower -> tag(BlockTags.FLOWERS).add(flower.getKey()));
    }

    private void addWorldgenTags() {
        tag(ModTags.Blocks.UNDERWORLD_CARVER_REPLACEABLES)
                .addTag(BlockTags.OVERWORLD_CARVER_REPLACEABLES)
                .add(blockKey(Blocks.BEDROCK));
    }

    private void addGelatinousCubeTags() {
        tag(ModTags.Blocks.PEPSIN_DISSOLVABLE)
                .addTag(BlockTags.WOOL)
                .addTag(BlockTags.WOOL_CARPETS)
                .addTag(BlockTags.CANDLE_CAKES)
                .add(blockKey(Blocks.CAKE), blockKey(Blocks.TRIPWIRE));

        tag(ModTags.Blocks.ACID_DISSOLVES_INSTANTLY)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.WOOL)
                .addTag(BlockTags.WOOL_CARPETS)
                .addTag(BlockTags.CANDLE_CAKES)
                .add(blockKey(Blocks.CAKE));

        tag(ModTags.Blocks.ACID_DISSOLVES_GRADUALLY)
                .addTag(BlockTags.DOORS)
                .addTag(BlockTags.PRESSURE_PLATES)
                .addTag(BlockTags.RAILS)
                .addTag(BlockTags.WOODEN_BUTTONS)
                .addTag(BlockTags.ALL_SIGNS)
                .addTag(BlockTags.BEDS)
                .addTag(BlockTags.TRAPDOORS)
                .addTag(BlockTags.FENCES)
                .addTag(BlockTags.FENCE_GATES)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(Tags.Blocks.CHESTS)
                .add(
                        blockKey(Blocks.LEVER),
                        blockKey(Blocks.LADDER),
                        blockKey(Blocks.PISTON),
                        blockKey(Blocks.STICKY_PISTON),
                        blockKey(Blocks.PISTON_HEAD),
                        blockKey(Blocks.MOVING_PISTON),
                        blockKey(Blocks.CACTUS),
                        blockKey(Blocks.MELON),
                        blockKey(Blocks.PUMPKIN),
                        blockKey(Blocks.REPEATER),
                        blockKey(Blocks.ENCHANTING_TABLE),
                        blockKey(Blocks.SKELETON_SKULL),
                        blockKey(Blocks.COMPARATOR),
                        blockKey(Blocks.DAYLIGHT_DETECTOR),
                        blockKey(Blocks.HOPPER),
                        blockKey(Blocks.HAY_BLOCK),
                        blockKey(Blocks.CAULDRON),
                        blockKey(Blocks.COCOA),
                        blockKey(Blocks.TRIPWIRE_HOOK));
    }

    private void addVanillaMiningTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.HARDENED_CLAY_FURNACE.getKey())
                .add(ModBlocks.OBSIDIAN_FURNACE.getKey())
                .add(ModBlocks.NETHERRACK_FURNACE.getKey())
                .add(ModBlocks.SILVER_ORE.getKey())
                .add(ModBlocks.MITHRIL_ORE.getKey())
                .add(ModBlocks.ADAMANTIUM_ORE.getKey())
                .add(ModBlocks.SILVER_BLOCK.getKey())
                .add(ModBlocks.ANCIENT_METAL_BLOCK.getKey())
                .add(ModBlocks.MITHRIL_BLOCK.getKey())
                .add(ModBlocks.ADAMANTIUM_BLOCK.getKey())
                .add(ModBlocks.MITHRIL_RUNE_STONE.getKey())
                .add(ModBlocks.ADAMANTIUM_RUNE_STONE.getKey())
                .add(ModBlocks.INFESTED_NETHERRACK.getKey());
        ModBlocks.METAL_ANVILS.forEach(anvil -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(anvil.getKey()));
        ModBlocks.ENCHANTING_TABLES.forEach(table -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(table.getKey()));
        ModBlocks.METAL_SAFES.forEach(safe -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(safe.getKey()));
        ModBlocks.WORKBENCHES.forEach(workbench -> tag(BlockTags.MINEABLE_WITH_AXE).add(workbench.getKey()));
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.NETHER_GRAVEL.getKey());
    }

    private void addEffectiveToolTags() {
        TagAppender<Block> pickaxe = tag(ModTags.Blocks.effectiveWith(R196MiningFamily.PICKAXE))
                .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(BlockTags.FLOWER_POTS)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(Tags.Blocks.SKULLS)
                .addTag(BlockTags.CORAL_BLOCKS)
                .addTag(BlockTags.CORALS)
                .addTag(BlockTags.WALL_CORALS)
                .addTag(BlockTags.BUTTONS)
                .add(
                        blockKey(Blocks.BEACON),
                        blockKey(Blocks.CLAY),
                        blockKey(Blocks.GLOWSTONE),
                        blockKey(Blocks.LADDER),
                        blockKey(Blocks.REDSTONE_WIRE),
                        blockKey(Blocks.REDSTONE_TORCH),
                        blockKey(Blocks.REDSTONE_WALL_TORCH),
                        blockKey(Blocks.TORCH),
                        blockKey(Blocks.WALL_TORCH),
                        blockKey(Blocks.SOUL_TORCH),
                        blockKey(Blocks.SOUL_WALL_TORCH),
                        blockKey(Blocks.COPPER_TORCH),
                        blockKey(Blocks.COPPER_WALL_TORCH),
                        blockKey(Blocks.LEVER),
                        blockKey(Blocks.REPEATER),
                        blockKey(Blocks.COMPARATOR),
                        blockKey(Blocks.TRIPWIRE),
                        blockKey(Blocks.TRIPWIRE_HOOK),
                        ModBlocks.CLAY_FURNACE.getKey(),
                        ModBlocks.LARGE_CLAY_OVEN.getKey());
        addMatching(pickaxe, ModBlockTagsProvider::isInfested);

        TagAppender<Block> axe = tag(ModTags.Blocks.effectiveWith(R196MiningFamily.AXE))
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(BlockTags.TERRACOTTA)
                .addTag(BlockTags.GLAZED_TERRACOTTA)
                .addTag(BlockTags.ICE)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.SANDSTONE_SLABS)
                .addTag(Tags.Blocks.PUMPKINS)
                .add(
                        blockKey(Blocks.BEACON),
                        blockKey(Blocks.CLAY),
                        blockKey(Blocks.CACTUS),
                        blockKey(Blocks.CACTUS_FLOWER),
                        blockKey(Blocks.GLOWSTONE),
                        blockKey(Blocks.MELON),
                        blockKey(Blocks.LADDER),
                        blockKey(Blocks.PACKED_MUD),
                        blockKey(Blocks.SUGAR_CANE),
                        ModBlocks.CLAY_FURNACE.getKey(),
                        ModBlocks.LARGE_CLAY_OVEN.getKey(),
                        ModBlocks.HARDENED_CLAY_FURNACE.getKey(),
                        ModBlocks.INFESTED_NETHERRACK.getKey());
        addMatching(axe, id -> isInfested(id) || isMudBrick(id));

        tag(ModTags.Blocks.AXE_HALF_SPEED).addTag(Tags.Blocks.SANDSTONE_BLOCKS);

        TagAppender<Block> shovel = tag(ModTags.Blocks.effectiveWith(R196MiningFamily.SHOVEL))
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(BlockTags.CANDLE_CAKES)
                .add(
                        blockKey(Blocks.CAKE),
                        blockKey(Blocks.CARROTS),
                        blockKey(Blocks.POTATOES),
                        blockKey(Blocks.BEETROOTS),
                        ModBlocks.CLAY_FURNACE.getKey(),
                        ModBlocks.LARGE_CLAY_OVEN.getKey(),
                        ModBlocks.SANDSTONE_FURNACE.getKey(),
                        ModBlocks.INFESTED_NETHERRACK.getKey());
        addMatching(shovel, ModBlockTagsProvider::isInfested);
        tag(ModTags.Blocks.METAL_SHOVEL_EFFECTIVE).addTag(Tags.Blocks.GLASS_BLOCKS);

        tag(ModTags.Blocks.effectiveWith(R196MiningFamily.HOE))
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addTag(BlockTags.CANDLE_CAKES)
                .add(
                        blockKey(Blocks.CAKE),
                        blockKey(Blocks.CARROTS),
                        blockKey(Blocks.POTATOES),
                        blockKey(Blocks.BEETROOTS),
                        ModBlocks.SANDSTONE_FURNACE.getKey());

        tag(ModTags.Blocks.effectiveWith(R196MiningFamily.SCYTHE))
                .addTag(BlockTags.CROPS)
                .add(
                        blockKey(Blocks.SHORT_GRASS),
                        blockKey(Blocks.TALL_GRASS),
                        blockKey(Blocks.FERN),
                        blockKey(Blocks.LARGE_FERN),
                        blockKey(Blocks.BUSH),
                        blockKey(Blocks.FIREFLY_BUSH),
                        blockKey(Blocks.SHORT_DRY_GRASS),
                        blockKey(Blocks.TALL_DRY_GRASS));

        tag(ModTags.Blocks.effectiveWith(R196MiningFamily.CUDGEL))
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(BlockTags.ICE)
                .addTag(BlockTags.CORAL_BLOCKS)
                .addTag(BlockTags.CORALS)
                .addTag(BlockTags.WALL_CORALS)
                .addTag(BlockTags.CANDLE_CAKES)
                .addTag(Tags.Blocks.PUMPKINS)
                .add(
                        blockKey(Blocks.BEACON),
                        blockKey(Blocks.CAKE),
                        blockKey(Blocks.GLOWSTONE),
                        blockKey(Blocks.MELON));

        addPlantCuttingTags(tag(ModTags.Blocks.effectiveWith(R196MiningFamily.SWORD)))
                .addTag(BlockTags.SWORD_EFFICIENT)
                .addTag(BlockTags.SWORD_INSTANTLY_MINES)
                .addTag(BlockTags.WOOL_CARPETS)
                .addTag(Tags.Blocks.PUMPKINS)
                .add(blockKey(Blocks.MELON));
        addPlantCuttingTags(tag(ModTags.Blocks.effectiveWith(R196MiningFamily.SHEARS)))
                .addTag(BlockTags.SHEARS_EXTREME_BREAKING_SPEED)
                .addTag(BlockTags.SHEARS_MAJOR_BREAKING_SPEED)
                .addTag(BlockTags.SHEARS_MINOR_BREAKING_SPEED)
                .add(blockKey(Blocks.TRIPWIRE));

        tag(ModTags.Blocks.WAR_HAMMER_EFFECTIVE)
                .addTag(BlockTags.CANDLE_CAKES)
                .addTag(Tags.Blocks.PUMPKINS)
                .add(blockKey(Blocks.CAKE), blockKey(Blocks.MELON));
        tag(ModTags.Blocks.NO_EFFECTIVE_TOOL)
                .addTag(BlockTags.ANVIL)
                .add(
                        blockKey(Blocks.PISTON),
                        blockKey(Blocks.STICKY_PISTON),
                        blockKey(Blocks.PISTON_HEAD),
                        blockKey(Blocks.MOVING_PISTON));
        ModBlocks.METAL_ANVILS.forEach(anvil -> tag(ModTags.Blocks.NO_EFFECTIVE_TOOL).add(anvil.getKey()));
    }

    private TagAppender<Block> addPlantCuttingTags(TagAppender<Block> appender) {
        return appender
                .addTag(BlockTags.BEDS)
                .addTag(BlockTags.BANNERS)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.WOOL)
                .addTag(BlockTags.CROPS)
                .addTag(BlockTags.FLOWERS)
                .addTag(vanillaTag("saplings"))
                .addTag(BlockTags.WART_BLOCKS)
                .add(
                        blockKey(Blocks.COBWEB),
                        blockKey(Blocks.VINE),
                        blockKey(Blocks.SUGAR_CANE),
                        blockKey(Blocks.CAVE_VINES),
                        blockKey(Blocks.CAVE_VINES_PLANT),
                        blockKey(Blocks.WEEPING_VINES),
                        blockKey(Blocks.WEEPING_VINES_PLANT),
                        blockKey(Blocks.TWISTING_VINES),
                        blockKey(Blocks.TWISTING_VINES_PLANT),
                        blockKey(Blocks.BROWN_MUSHROOM),
                        blockKey(Blocks.RED_MUSHROOM),
                        blockKey(Blocks.LILY_PAD),
                        blockKey(Blocks.COCOA),
                        blockKey(Blocks.NETHER_WART),
                        blockKey(Blocks.HAY_BLOCK),
                        blockKey(Blocks.DEAD_BUSH),
                        blockKey(Blocks.AZALEA),
                        blockKey(Blocks.FLOWERING_AZALEA),
                        blockKey(Blocks.SWEET_BERRY_BUSH),
                        blockKey(Blocks.NETHER_SPROUTS),
                        blockKey(Blocks.CRIMSON_ROOTS),
                        blockKey(Blocks.WARPED_ROOTS),
                        blockKey(Blocks.HANGING_ROOTS),
                        blockKey(Blocks.SMALL_DRIPLEAF),
                        blockKey(Blocks.BIG_DRIPLEAF),
                        blockKey(Blocks.BIG_DRIPLEAF_STEM),
                        blockKey(Blocks.SEAGRASS),
                        blockKey(Blocks.TALL_SEAGRASS),
                        blockKey(Blocks.KELP),
                        blockKey(Blocks.KELP_PLANT),
                        blockKey(Blocks.BAMBOO),
                        blockKey(Blocks.BAMBOO_SAPLING),
                        blockKey(Blocks.CHORUS_PLANT),
                        blockKey(Blocks.CHORUS_FLOWER),
                        blockKey(Blocks.SHORT_GRASS),
                        blockKey(Blocks.TALL_GRASS),
                        blockKey(Blocks.FERN),
                        blockKey(Blocks.LARGE_FERN),
                        blockKey(Blocks.BUSH),
                        blockKey(Blocks.FIREFLY_BUSH),
                        blockKey(Blocks.SHORT_DRY_GRASS),
                        blockKey(Blocks.TALL_DRY_GRASS));
    }

    private void addPortableBlocks() {
        TagAppender<Block> portable = tag(ModTags.Blocks.PORTABLE_HAND_HARVEST)
                .addTag(BlockTags.ANVIL)
                .addTag(BlockTags.BEDS)
                .addTag(BlockTags.CAULDRONS)
                .addTag(BlockTags.ALL_SIGNS)
                .addTag(BlockTags.BANNERS)
                .addTag(BlockTags.SHULKER_BOXES)
                .addTag(BlockTags.COPPER_CHESTS)
                .addTag(Tags.Blocks.SKULLS)
                .add(
                        blockKey(Blocks.CRAFTING_TABLE),
                        blockKey(Blocks.CHEST),
                        blockKey(Blocks.TRAPPED_CHEST),
                        blockKey(Blocks.ENDER_CHEST),
                        blockKey(Blocks.FURNACE),
                        blockKey(Blocks.BLAST_FURNACE),
                        blockKey(Blocks.SMOKER),
                        blockKey(Blocks.DISPENSER),
                        blockKey(Blocks.DROPPER),
                        blockKey(Blocks.BREWING_STAND),
                        blockKey(Blocks.HOPPER),
                        blockKey(Blocks.JUKEBOX),
                        blockKey(Blocks.NOTE_BLOCK),
                        blockKey(Blocks.DAYLIGHT_DETECTOR),
                        blockKey(Blocks.ENCHANTING_TABLE),
                        blockKey(Blocks.BEACON),
                        blockKey(Blocks.COMMAND_BLOCK),
                        blockKey(Blocks.REPEATING_COMMAND_BLOCK),
                        blockKey(Blocks.CHAIN_COMMAND_BLOCK),
                        blockKey(Blocks.DRAGON_EGG),
                        blockKey(Blocks.LADDER),
                        blockKey(Blocks.TNT),
                        blockKey(Blocks.BARREL),
                        blockKey(Blocks.CRAFTER),
                        blockKey(Blocks.LECTERN),
                        blockKey(Blocks.LOOM),
                        blockKey(Blocks.CARTOGRAPHY_TABLE),
                        blockKey(Blocks.FLETCHING_TABLE),
                        blockKey(Blocks.SMITHING_TABLE),
                        blockKey(Blocks.STONECUTTER),
                        blockKey(Blocks.GRINDSTONE),
                        blockKey(Blocks.BELL),
                        blockKey(Blocks.BEEHIVE),
                        blockKey(Blocks.BEE_NEST),
                        blockKey(Blocks.DECORATED_POT),
                        blockKey(Blocks.CHISELED_BOOKSHELF),
                        blockKey(Blocks.CONDUIT));
        ModBlocks.WORKBENCHES.forEach(block -> portable.add(block.getKey()));
        ModBlocks.FURNACES.forEach(block -> portable.add(block.getKey()));
        ModBlocks.METAL_ANVILS.forEach(block -> portable.add(block.getKey()));
        ModBlocks.METAL_SAFES.forEach(block -> portable.add(block.getKey()));
        ModBlocks.ENCHANTING_TABLES.forEach(block -> portable.add(block.getKey()));
    }

    private void addHarvestLevels() {
        TagAppender<Block> level0 = tag(ModTags.Blocks.requiredLevel(0))
                .addTag(BlockTags.RAILS)
                .addTag(BlockTags.STONE_BUTTONS)
                .addTag(BlockTags.CORAL_BLOCKS)
                .addTag(BlockTags.CORALS)
                .addTag(BlockTags.WALL_CORALS)
                .addTag(BlockTags.ANVIL)
                .add(
                        blockKey(Blocks.COAL_BLOCK),
                        blockKey(Blocks.BONE_BLOCK),
                        blockKey(Blocks.PISTON),
                        blockKey(Blocks.STICKY_PISTON),
                        blockKey(Blocks.PISTON_HEAD),
                        blockKey(Blocks.MOVING_PISTON),
                        ModBlocks.CLAY_FURNACE.getKey(),
                        ModBlocks.LARGE_CLAY_OVEN.getKey(),
                        ModBlocks.SANDSTONE_FURNACE.getKey(),
                        ModBlocks.INFESTED_NETHERRACK.getKey());
        addMatching(level0, id -> isCoral(id) || isInfested(id));
        ModBlocks.METAL_ANVILS.forEach(block -> level0.add(block.getKey()));

        TagAppender<Block> level1 = tag(ModTags.Blocks.requiredLevel(1))
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.BAMBOO_BLOCKS)
                .addTag(BlockTags.TERRACOTTA)
                .addTag(BlockTags.GLAZED_TERRACOTTA)
                .addTag(BlockTags.ICE)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.SANDSTONE_SLABS)
                .add(
                        blockKey(Blocks.MANGROVE_ROOTS),
                        blockKey(Blocks.MUDDY_MANGROVE_ROOTS),
                        blockKey(Blocks.PACKED_MUD),
                        ModBlocks.HARDENED_CLAY_FURNACE.getKey());
        addMatching(level1, ModBlockTagsProvider::isMudBrick);

        tag(ModTags.Blocks.requiredLevel(2))
                .add(
                        ModBlocks.SILVER_ORE.getKey(),
                        ModBlocks.NETHERRACK_FURNACE.getKey());

        TagAppender<Block> level3 = tag(ModTags.Blocks.requiredLevel(3))
                .addTag(Tags.Blocks.ORES_EMERALD)
                .addTag(BlockTags.COPPER_CHESTS)
                .addTag(BlockTags.COPPER_GOLEM_STATUES)
                .addTag(BlockTags.COPPER)
                .addTag(BlockTags.CAULDRONS)
                .add(
                        blockKey(Blocks.OBSIDIAN),
                        blockKey(Blocks.CRYING_OBSIDIAN),
                        blockKey(Blocks.RESPAWN_ANCHOR),
                        blockKey(Blocks.GOLD_BLOCK),
                        blockKey(Blocks.REDSTONE_BLOCK),
                        blockKey(Blocks.RAW_GOLD_BLOCK),
                        blockKey(Blocks.RAW_COPPER_BLOCK),
                        blockKey(Blocks.IRON_BARS),
                        blockKey(Blocks.IRON_DOOR),
                        blockKey(Blocks.IRON_TRAPDOOR),
                        blockKey(Blocks.IRON_CHAIN),
                        blockKey(Blocks.LANTERN),
                        blockKey(Blocks.SOUL_LANTERN),
                        blockKey(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE),
                        blockKey(Blocks.BREWING_STAND),
                        blockKey(Blocks.HOPPER),
                        blockKey(Blocks.HEAVY_CORE),
                        ModBlocks.MITHRIL_ORE.getKey(),
                        ModBlocks.SILVER_BLOCK.getKey(),
                        ModBlocks.MITHRIL_RUNE_STONE.getKey(),
                        ModBlocks.ADAMANTIUM_RUNE_STONE.getKey(),
                        ModBlocks.OBSIDIAN_FURNACE.getKey(),
                        ModBlocks.COPPER_SAFE.getKey(),
                        ModBlocks.SILVER_SAFE.getKey(),
                        ModBlocks.GOLD_SAFE.getKey());
        addMatching(level3, ModBlockTagsProvider::isDenseCopper);

        tag(ModTags.Blocks.requiredLevel(4))
                .addTag(Tags.Blocks.ORES_DIAMOND)
                .add(
                        blockKey(Blocks.EMERALD_BLOCK),
                        blockKey(Blocks.IRON_BLOCK),
                        blockKey(Blocks.RAW_IRON_BLOCK),
                        blockKey(Blocks.ANCIENT_DEBRIS),
                        blockKey(Blocks.LODESTONE),
                        ModBlocks.ADAMANTIUM_ORE.getKey(),
                        ModBlocks.ANCIENT_METAL_BLOCK.getKey(),
                        ModBlocks.IRON_SAFE.getKey(),
                        ModBlocks.ANCIENT_METAL_SAFE.getKey());

        tag(ModTags.Blocks.requiredLevel(5))
                .add(
                        blockKey(Blocks.DIAMOND_BLOCK),
                        blockKey(Blocks.NETHERITE_BLOCK),
                        ModBlocks.MITHRIL_BLOCK.getKey(),
                        ModBlocks.MITHRIL_SAFE.getKey());
        tag(ModTags.Blocks.requiredLevel(6))
                .add(ModBlocks.ADAMANTIUM_BLOCK.getKey(), ModBlocks.ADAMANTIUM_SAFE.getKey());
    }

    private void addMatching(TagAppender<Block> appender, Predicate<Identifier> predicate) {
        BuiltInRegistries.BLOCK.keySet().stream()
                .filter(predicate)
                .map(id -> ResourceKey.create(Registries.BLOCK, id))
                .forEach(appender::add);
    }

    private static boolean isCoral(Identifier id) {
        return isMinecraft(id) && id.getPath().contains("coral");
    }

    private static boolean isMudBrick(Identifier id) {
        return isMinecraft(id) && id.getPath().startsWith("mud_brick");
    }

    private static boolean isInfested(Identifier id) {
        return isMinecraft(id) && id.getPath().startsWith("infested_");
    }

    private static boolean isDenseCopper(Identifier id) {
        if (!isMinecraft(id)) {
            return false;
        }
        String path = id.getPath();
        if (path.startsWith("waxed_")) {
            path = path.substring("waxed_".length());
        }
        for (String weathering : new String[] {"exposed_", "weathered_", "oxidized_"}) {
            if (path.startsWith(weathering)) {
                path = path.substring(weathering.length());
                break;
            }
        }
        return path.equals("copper_block")
                || path.equals("chiseled_copper")
                || path.equals("copper_bulb")
                || path.equals("copper_grate")
                || path.equals("copper_golem_statue")
                || path.equals("cut_copper")
                || path.equals("cut_copper_slab")
                || path.equals("cut_copper_stairs");
    }

    private static boolean isMinecraft(Identifier id) {
        return id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE);
    }

    private static ResourceKey<Block> blockKey(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
    }

    private static TagKey<Block> vanillaTag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace(path));
    }
}
