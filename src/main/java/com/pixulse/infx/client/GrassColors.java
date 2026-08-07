package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.agriculture.GrassTrampling;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jspecify.annotations.NonNull;

/** Client grass tint: biome color blended toward InfX manure brown when trampled. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class GrassColors {
    private GrassColors() {}

    @SubscribeEvent
    public static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(trampledGrassBlock()), Blocks.GRASS_BLOCK);
    }

    static BlockTintSource trampledGrassBlock() {
        return new BlockTintSource() {
            @Override
            public int color(@NonNull BlockState state) {
                return GrassColor.getDefaultColor();
            }

            @Override
            public int colorInWorld(@NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
                int biome = BiomeColors.getAverageGrassColor(level, pos);
                int tramplings = clientTramplings(level, pos);
                return GrassTrampling.blendColor(biome, tramplings);
            }

            @Override
            public int colorAsTerrainParticle(@NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
                return -1;
            }
        };
    }

    private static int clientTramplings(BlockAndTintGetter level, BlockPos pos) {
        if (!(level instanceof Level world)) {
            return 0;
        }
        return GrassTrampling.getTramplings(world, pos);
    }
}
