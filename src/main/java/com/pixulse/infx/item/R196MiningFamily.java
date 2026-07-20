package com.pixulse.infx.item;

import com.pixulse.infx.tag.ModTags;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum R196MiningFamily {
    NONE,
    PICKAXE,
    SHOVEL,
    AXE,
    HOE,
    SCYTHE,
    CUDGEL,
    SWORD,
    SHEARS;

    public String path() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public Tool createTool(R196EquipmentKey key) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        float speed = key.miningSpeed();
        return switch (this) {
            case PICKAXE -> standard(blocks, key, BlockTags.MINEABLE_WITH_PICKAXE);
            case SHOVEL -> standard(blocks, key, BlockTags.MINEABLE_WITH_SHOVEL);
            case AXE -> standard(blocks, key, BlockTags.MINEABLE_WITH_AXE);
            case HOE -> standard(blocks, key, BlockTags.MINEABLE_WITH_HOE);
            case SCYTHE -> new Tool(
                    List.of(
                            Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.CROPS), speed),
                            Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_HOE), speed)),
                    1.0F,
                    0,
                    true);
            case CUDGEL -> new Tool(
                    List.of(Tool.Rule.minesAndDrops(
                            blocks.getOrThrow(ModTags.Blocks.effectiveWith(CUDGEL)), speed)),
                    1.0F,
                    0,
                    true);
            case SWORD -> new Tool(
                    List.of(
                            Tool.Rule.minesAndDrops(
                                    HolderSet.direct(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.COBWEB)), 15.0F),
                            Tool.Rule.overrideSpeed(
                                    blocks.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE),
                            Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), speed)),
                    1.0F,
                    0,
                    true);
            case SHEARS -> new Tool(
                    List.of(
                            Tool.Rule.minesAndDrops(
                                    HolderSet.direct(BuiltInRegistries.BLOCK.wrapAsHolder(Blocks.COBWEB)), 15.0F),
                            Tool.Rule.overrideSpeed(
                                    blocks.getOrThrow(BlockTags.SHEARS_EXTREME_BREAKING_SPEED), 15.0F),
                            Tool.Rule.overrideSpeed(
                                    blocks.getOrThrow(BlockTags.SHEARS_MAJOR_BREAKING_SPEED), speed),
                            Tool.Rule.overrideSpeed(
                                    blocks.getOrThrow(BlockTags.SHEARS_MINOR_BREAKING_SPEED), speed)),
                    1.0F,
                    0,
                    true);
            case NONE -> new Tool(List.of(), 1.0F, 0, true);
        };
    }

    private static Tool standard(
            HolderGetter<Block> blocks, R196EquipmentKey key, TagKey<Block> minesEfficiently) {
        return new Tool(
                List.of(Tool.Rule.minesAndDrops(blocks.getOrThrow(minesEfficiently), key.miningSpeed())),
                1.0F,
                0,
                true);
    }
}
