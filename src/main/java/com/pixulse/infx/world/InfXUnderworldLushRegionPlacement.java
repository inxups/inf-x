package com.pixulse.infx.world;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.registry.InfXPlacementModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/** Filters placed features to the seed-stable lush Underworld regions. */
public final class InfXUnderworldLushRegionPlacement extends PlacementFilter {
    public static final MapCodec<InfXUnderworldLushRegionPlacement> CODEC =
            MapCodec.unit(InfXUnderworldLushRegionPlacement::new);
    public static final InfXUnderworldLushRegionPlacement INSTANCE = new InfXUnderworldLushRegionPlacement();

    private InfXUnderworldLushRegionPlacement() {}

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        return InfXUnderworldLushRegion.isLushRegion(context.getLevel().getSeed(), chunkX, chunkZ);
    }

    @Override
    public PlacementModifierType<?> type() {
        return InfXPlacementModifiers.UNDERWORLD_LUSH_REGION.get();
    }
}
