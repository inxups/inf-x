package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.MiningFamily;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.tag.InfXBlockTags;

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
import org.jspecify.annotations.NonNull;

final class ModBlockTagsProvider extends KeyTagsProvider<Block> {
    ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BLOCK, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        addVanillaMiningTags();
        addEffectiveToolTags();
        addPortableBlocks();
        addHarvestLevels();
        addCropTags();
        addGelatinousCubeTags();
        addCurseTags();
    }

    private void addCropTags() {
        InfXBlocks.INFX_CROPS.forEach(crop -> {
            tag(BlockTags.CROPS).add(crop.getKey());
            tag(BlockTags.MAINTAINS_FARMLAND).add(crop.getKey());
        });
        tag(BlockTags.SUPPORTS_CROPS)
                .add(blockKey(Blocks.FARMLAND), InfXBlocks.FERTILE_FARMLAND.getKey());
    }

    private void addCurseTags() {
        tag(InfXBlockTags.CURSE_VINES)
                .addTag(BlockTags.CAVE_VINES)
                .add(
                        blockKey(Blocks.VINE),
                        blockKey(Blocks.WEEPING_VINES),
                        blockKey(Blocks.WEEPING_VINES_PLANT),
                        blockKey(Blocks.TWISTING_VINES),
                        blockKey(Blocks.TWISTING_VINES_PLANT));

        tag(InfXBlockTags.CURSE_PLANTS)
                .addTag(BlockTags.CROPS)
                .addTag(BlockTags.FLOWERS)
                .addTag(vanillaTag("saplings"))
                .add(
                        blockKey(Blocks.SHORT_GRASS),
                        blockKey(Blocks.TALL_GRASS),
                        blockKey(Blocks.FERN),
                        blockKey(Blocks.LARGE_FERN),
                        blockKey(Blocks.DEAD_BUSH),
                        blockKey(Blocks.BROWN_MUSHROOM),
                        blockKey(Blocks.RED_MUSHROOM),
                        blockKey(Blocks.SUGAR_CANE),
                        blockKey(Blocks.BAMBOO_SAPLING),
                        blockKey(Blocks.NETHER_SPROUTS),
                        blockKey(Blocks.CRIMSON_ROOTS),
                        blockKey(Blocks.WARPED_ROOTS),
                        blockKey(Blocks.CRIMSON_FUNGUS),
                        blockKey(Blocks.WARPED_FUNGUS),
                        blockKey(Blocks.SWEET_BERRY_BUSH),
                        InfXBlocks.BLUEBERRY_BUSH.getKey(),
                        blockKey(Blocks.LILY_PAD),
                        blockKey(Blocks.SEAGRASS),
                        blockKey(Blocks.TALL_SEAGRASS));
    }

    private void addGelatinousCubeTags() {
        tag(InfXBlockTags.PEPSIN_DISSOLVABLE)
                .addTag(BlockTags.WOOL)
                .addTag(BlockTags.WOOL_CARPETS)
                .addTag(BlockTags.CANDLE_CAKES)
                .add(blockKey(Blocks.CAKE), blockKey(Blocks.TRIPWIRE));

        tag(InfXBlockTags.ACID_DISSOLVES_INSTANTLY)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.WOOL)
                .addTag(BlockTags.WOOL_CARPETS)
                .addTag(BlockTags.CANDLE_CAKES)
                .add(blockKey(Blocks.CAKE));

        tag(InfXBlockTags.ACID_DISSOLVES_GRADUALLY)
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
                .add(InfXBlocks.HARDENED_CLAY_FURNACE.getKey())
                .add(InfXBlocks.OBSIDIAN_FURNACE.getKey())
                .add(InfXBlocks.NETHERRACK_FURNACE.getKey())
                .add(InfXBlocks.SILVER_ORE.getKey())
                .add(InfXBlocks.DEEPSLATE_SILVER_ORE.getKey())
                .add(InfXBlocks.MITHRIL_ORE.getKey())
                .add(InfXBlocks.DEEPSLATE_MITHRIL_ORE.getKey())
                .add(InfXBlocks.ADAMANTIUM_ORE.getKey())
                .add(InfXBlocks.DEEPSLATE_ADAMANTIUM_ORE.getKey())
                .add(InfXBlocks.SILVER_BLOCK.getKey())
                .add(InfXBlocks.ANCIENT_METAL_BLOCK.getKey())
                .add(InfXBlocks.MITHRIL_BLOCK.getKey())
                .add(InfXBlocks.ADAMANTIUM_BLOCK.getKey())
                .add(InfXBlocks.MITHRIL_RUNE_STONE.getKey())
                .add(InfXBlocks.ADAMANTIUM_RUNE_STONE.getKey())
                .add(InfXBlocks.INFESTED_NETHERRACK.getKey());
        InfXBlocks.METAL_ANVILS.forEach(anvil -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(anvil.getKey()));
        InfXBlocks.ENCHANTING_TABLES.forEach(table -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(table.getKey()));
        InfXBlocks.METAL_SAFES.forEach(safe -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(safe.getKey()));
        InfXBlocks.WORKBENCHES.forEach(workbench -> tag(BlockTags.MINEABLE_WITH_AXE).add(workbench.getKey()));
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(InfXBlocks.GRAVEL.getKey(), InfXBlocks.NETHER_GRAVEL.getKey());
    }

    private void addEffectiveToolTags() {
        TagAppender<ResourceKey<Block>, Block> pickaxe = tag(InfXBlockTags.effectiveWith(MiningFamily.PICKAXE))
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
                        InfXBlocks.CLAY_FURNACE.getKey());
        addMatching(pickaxe, ModBlockTagsProvider::isInfested);

        TagAppender<ResourceKey<Block>, Block> axe = tag(InfXBlockTags.effectiveWith(MiningFamily.AXE))
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(BlockTags.TERRACOTTA)
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
                        InfXBlocks.CLAY_FURNACE.getKey(),
                        InfXBlocks.HARDENED_CLAY_FURNACE.getKey(),
                        InfXBlocks.INFESTED_NETHERRACK.getKey());
        addMatching(axe, id -> isInfested(id) || isMudBrick(id) || isGlazedTerracotta(id));

        tag(InfXBlockTags.AXE_HALF_SPEED).addTag(Tags.Blocks.SANDSTONE_BLOCKS);

        TagAppender<ResourceKey<Block>, Block> shovel = tag(InfXBlockTags.effectiveWith(MiningFamily.SHOVEL))
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(BlockTags.CANDLE_CAKES)
                .add(
                        blockKey(Blocks.CAKE),
                        blockKey(Blocks.CARROTS),
                        blockKey(Blocks.POTATOES),
                        blockKey(Blocks.BEETROOTS),
                        InfXBlocks.CLAY_FURNACE.getKey(),
                        InfXBlocks.SANDSTONE_FURNACE.getKey(),
                        InfXBlocks.INFESTED_NETHERRACK.getKey());
        addMatching(shovel, ModBlockTagsProvider::isInfested);
        tag(InfXBlockTags.METAL_SHOVEL_EFFECTIVE).addTag(Tags.Blocks.GLASS_BLOCKS);

        tag(InfXBlockTags.effectiveWith(MiningFamily.HOE))
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addTag(BlockTags.CANDLE_CAKES)
                .add(
                        blockKey(Blocks.CAKE),
                        blockKey(Blocks.CARROTS),
                        blockKey(Blocks.POTATOES),
                        blockKey(Blocks.BEETROOTS),
                        InfXBlocks.SANDSTONE_FURNACE.getKey());

        tag(InfXBlockTags.effectiveWith(MiningFamily.SCYTHE))
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

        tag(InfXBlockTags.effectiveWith(MiningFamily.CUDGEL))
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

        addPlantCuttingTags(tag(InfXBlockTags.effectiveWith(MiningFamily.SWORD)))
                .addTag(BlockTags.SWORD_EFFICIENT)
                .addTag(BlockTags.SWORD_INSTANTLY_MINES)
                .addTag(BlockTags.WOOL_CARPETS)
                .addTag(Tags.Blocks.PUMPKINS)
                .add(blockKey(Blocks.MELON));
        addPlantCuttingTags(tag(InfXBlockTags.effectiveWith(MiningFamily.SHEARS)))
                .add(blockKey(Blocks.GLOW_LICHEN), blockKey(Blocks.TRIPWIRE));

        tag(InfXBlockTags.WAR_HAMMER_EFFECTIVE)
                .addTag(BlockTags.CANDLE_CAKES)
                .addTag(Tags.Blocks.PUMPKINS)
                .add(blockKey(Blocks.CAKE), blockKey(Blocks.MELON));
        tag(InfXBlockTags.NO_EFFECTIVE_TOOL)
                .addTag(BlockTags.ANVIL)
                .add(
                        blockKey(Blocks.PISTON),
                        blockKey(Blocks.STICKY_PISTON),
                        blockKey(Blocks.PISTON_HEAD),
                        blockKey(Blocks.MOVING_PISTON));
        InfXBlocks.METAL_ANVILS.forEach(anvil -> tag(InfXBlockTags.NO_EFFECTIVE_TOOL).add(anvil.getKey()));
    }

    private TagAppender<ResourceKey<Block>, Block> addPlantCuttingTags(
            TagAppender<ResourceKey<Block>, Block> appender) {
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
                        InfXBlocks.BLUEBERRY_BUSH.getKey(),
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
        TagAppender<ResourceKey<Block>, Block> portable = tag(InfXBlockTags.PORTABLE_HAND_HARVEST)
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
        InfXBlocks.WORKBENCHES.forEach(block -> portable.add(block.getKey()));
        InfXBlocks.FURNACES.forEach(block -> portable.add(block.getKey()));
        InfXBlocks.METAL_ANVILS.forEach(block -> portable.add(block.getKey()));
        InfXBlocks.METAL_SAFES.forEach(block -> portable.add(block.getKey()));
        InfXBlocks.ENCHANTING_TABLES.forEach(block -> portable.add(block.getKey()));
    }

    private void addHarvestLevels() {
        TagAppender<ResourceKey<Block>, Block> level0 = tag(InfXBlockTags.requiredLevel(0))
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
                        InfXBlocks.CLAY_FURNACE.getKey(),
                        InfXBlocks.SANDSTONE_FURNACE.getKey(),
                        InfXBlocks.INFESTED_NETHERRACK.getKey());
        addMatching(level0, id -> isCoral(id) || isInfested(id));
        InfXBlocks.METAL_ANVILS.forEach(block -> level0.add(block.getKey()));

        TagAppender<ResourceKey<Block>, Block> level1 = tag(InfXBlockTags.requiredLevel(1))
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.BAMBOO_BLOCKS)
                .addTag(BlockTags.TERRACOTTA)
                .addTag(BlockTags.ICE)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.SANDSTONE_SLABS)
                .add(
                        blockKey(Blocks.MANGROVE_ROOTS),
                        blockKey(Blocks.MUDDY_MANGROVE_ROOTS),
                        blockKey(Blocks.PACKED_MUD),
                        InfXBlocks.GRAVEL.getKey(),
                        InfXBlocks.HARDENED_CLAY_FURNACE.getKey());
        addMatching(level1, id -> isMudBrick(id) || isGlazedTerracotta(id));

        tag(InfXBlockTags.requiredLevel(2))
                .add(
                        InfXBlocks.SILVER_ORE.getKey(),
                        InfXBlocks.DEEPSLATE_SILVER_ORE.getKey(),
                        InfXBlocks.NETHERRACK_FURNACE.getKey());

        TagAppender<ResourceKey<Block>, Block> level3 = tag(InfXBlockTags.requiredLevel(3))
                .addTag(Tags.Blocks.ORES_EMERALD)
                .addTag(BlockTags.COPPER_CHESTS)
                .addTag(BlockTags.COPPER_GOLEM_STATUES)
                .addTag(BlockTags.COPPER)
                .addTag(BlockTags.CAULDRONS)
                .add(
                        blockKey(Blocks.OBSIDIAN),
                        blockKey(Blocks.CRYING_OBSIDIAN),
                        blockKey(Blocks.NETHERRACK),
                        blockKey(Blocks.CRIMSON_NYLIUM),
                        blockKey(Blocks.WARPED_NYLIUM),
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
                        InfXBlocks.MITHRIL_ORE.getKey(),
                        InfXBlocks.DEEPSLATE_MITHRIL_ORE.getKey(),
                        InfXBlocks.SILVER_BLOCK.getKey(),
                        InfXBlocks.MITHRIL_RUNE_STONE.getKey(),
                        InfXBlocks.ADAMANTIUM_RUNE_STONE.getKey(),
                        InfXBlocks.OBSIDIAN_FURNACE.getKey(),
                        InfXBlocks.COPPER_SAFE.getKey(),
                        InfXBlocks.SILVER_SAFE.getKey(),
                        InfXBlocks.GOLD_SAFE.getKey());
        addMatching(level3, ModBlockTagsProvider::isDenseCopper);

        tag(InfXBlockTags.requiredLevel(4))
                .addTag(Tags.Blocks.ORES_DIAMOND)
                .add(
                        blockKey(Blocks.EMERALD_BLOCK),
                        blockKey(Blocks.IRON_BLOCK),
                        blockKey(Blocks.RAW_IRON_BLOCK),
                        blockKey(Blocks.ANCIENT_DEBRIS),
                        blockKey(Blocks.LODESTONE),
                        InfXBlocks.ADAMANTIUM_ORE.getKey(),
                        InfXBlocks.DEEPSLATE_ADAMANTIUM_ORE.getKey(),
                        InfXBlocks.ANCIENT_METAL_BLOCK.getKey(),
                        InfXBlocks.IRON_SAFE.getKey(),
                        InfXBlocks.ANCIENT_METAL_SAFE.getKey());

        tag(InfXBlockTags.requiredLevel(5))
                .add(
                        blockKey(Blocks.DIAMOND_BLOCK),
                        blockKey(Blocks.NETHERITE_BLOCK),
                        InfXBlocks.MITHRIL_BLOCK.getKey(),
                        InfXBlocks.MITHRIL_SAFE.getKey());
        tag(InfXBlockTags.requiredLevel(6))
                .add(InfXBlocks.ADAMANTIUM_BLOCK.getKey(), InfXBlocks.ADAMANTIUM_SAFE.getKey());
    }

    private void addMatching(
            TagAppender<ResourceKey<Block>, Block> appender, Predicate<Identifier> predicate) {
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

    private static boolean isGlazedTerracotta(Identifier id) {
        return isMinecraft(id) && id.getPath().endsWith("_glazed_terracotta");
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
