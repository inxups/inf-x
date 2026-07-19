package com.pixulse.infx.world;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class R196PhysicsRules {
    private R196PhysicsRules() {}

    public static boolean isLoose(BlockState state) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MUD)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.DIRT_PATH)
                || state.is(BlockTags.SAND)
                || state.is(Blocks.GRAVEL);
    }

    public static float fallDamageMultiplier(BlockState landing) {
        if (landing.is(Blocks.SPONGE) || landing.is(Blocks.WET_SPONGE)) return 0.2F;
        if (landing.is(BlockTags.WOOL) || landing.is(Blocks.SNOW_BLOCK)) return 0.4F;
        if (landing.is(BlockTags.BEDS) || landing.is(BlockTags.LEAVES)) return 0.55F;
        if (landing.is(BlockTags.SAND)
                || landing.is(Blocks.FARMLAND)
                || landing.is(Blocks.HAY_BLOCK)
                || landing.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || landing.is(Blocks.RED_MUSHROOM_BLOCK)) return 0.7F;
        if (landing.is(Blocks.DIRT)
                || landing.is(Blocks.GRASS_BLOCK)
                || landing.is(BlockTags.CROPS)) return 0.85F;
        return 1.0F;
    }

    public static float snowLayerMultiplier(BlockState state) {
        return state.is(Blocks.SNOW) && state.hasProperty(SnowLayerBlock.LAYERS)
                ? Math.max(0.35F, 1.0F - state.getValue(SnowLayerBlock.LAYERS) * 0.08F)
                : 1.0F;
    }

    public static int explosionWear(double distance, float radius) {
        if (radius <= 0 || distance >= radius) return 0;
        return Math.max(1, (int) Math.ceil((radius - distance) / radius * 24.0D));
    }
}
