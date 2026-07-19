package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.tag.ModTags;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

final class ModBlockTagsProvider extends BlockTagsProvider {
    ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
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
                .add(ModBlocks.ADAMANTIUM_RUNE_STONE.getKey());
        ModBlocks.METAL_ANVILS.forEach(anvil -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(anvil.getKey()));
        ModBlocks.ENCHANTING_TABLES.forEach(table -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(table.getKey()));
        ModBlocks.METAL_SAFES.forEach(safe -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(safe.getKey()));
        tag(ModTags.Blocks.RESTRICTED_HARVEST)
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.MINEABLE_WITH_HOE)
                .addTag(BlockTags.CROPS);
        var portable = tag(ModTags.Blocks.PORTABLE_HAND_HARVEST);
        ModBlocks.WORKBENCHES.forEach(block -> portable.add(block.getKey()));
        ModBlocks.FURNACES.forEach(block -> portable.add(block.getKey()));
        tag(ModTags.Blocks.requiredTier(HarvestTier.FLINT))
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.MINEABLE_WITH_HOE)
                .addTag(BlockTags.CROPS);
        tag(ModTags.Blocks.requiredTier(HarvestTier.COPPER)).addTag(BlockTags.MINEABLE_WITH_PICKAXE);
        tag(ModTags.Blocks.requiredTier(HarvestTier.IRON))
                .add(net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getResourceKey(net.minecraft.world.level.block.Blocks.EMERALD_ORE).orElseThrow())
                .add(net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getResourceKey(net.minecraft.world.level.block.Blocks.DEEPSLATE_EMERALD_ORE).orElseThrow())
                .add(ModBlocks.OBSIDIAN_FURNACE.getKey())
                .add(ModBlocks.MITHRIL_ORE.getKey())
                .add(ModBlocks.ANCIENT_METAL_BLOCK.getKey())
                .add(ModBlocks.ANCIENT_METAL_ANVIL.getKey());
        tag(ModTags.Blocks.requiredTier(HarvestTier.COPPER))
                .add(ModBlocks.COPPER_SAFE.getKey())
                .add(ModBlocks.SILVER_SAFE.getKey())
                .add(ModBlocks.GOLD_SAFE.getKey());
        tag(ModTags.Blocks.requiredTier(HarvestTier.IRON)).add(ModBlocks.IRON_SAFE.getKey());
        tag(ModTags.Blocks.requiredTier(HarvestTier.ANCIENT_METAL))
                .add(ModBlocks.MITHRIL_BLOCK.getKey())
                .add(ModBlocks.MITHRIL_ANVIL.getKey())
                .add(ModBlocks.MITHRIL_RUNE_STONE.getKey());
        tag(ModTags.Blocks.requiredTier(HarvestTier.ANCIENT_METAL)).add(ModBlocks.ANCIENT_METAL_SAFE.getKey());
        tag(ModTags.Blocks.requiredTier(HarvestTier.MITHRIL))
                .add(net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getResourceKey(net.minecraft.world.level.block.Blocks.DIAMOND_ORE).orElseThrow())
                .add(net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getResourceKey(net.minecraft.world.level.block.Blocks.DEEPSLATE_DIAMOND_ORE).orElseThrow())
                .add(ModBlocks.ADAMANTIUM_ORE.getKey())
                .add(ModBlocks.ADAMANTIUM_BLOCK.getKey())
                .add(ModBlocks.ADAMANTIUM_ANVIL.getKey())
                .add(ModBlocks.ADAMANTIUM_RUNE_STONE.getKey());
        tag(ModTags.Blocks.requiredTier(HarvestTier.MITHRIL)).add(ModBlocks.MITHRIL_SAFE.getKey());
        tag(ModTags.Blocks.requiredTier(HarvestTier.ADAMANTIUM)).add(ModBlocks.ADAMANTIUM_SAFE.getKey());
    }
}
