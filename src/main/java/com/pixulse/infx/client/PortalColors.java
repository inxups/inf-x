package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jspecify.annotations.NonNull;

/** Client tints for InfiniteX portal surfaces. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class PortalColors {
    static final int NETHER_PORTAL_TINT = 0xFFFF3B30;
    static final int RUNEGATE_OVERWORLD_TINT = RunegateColors.tintFor(Level.OVERWORLD);
    static final int RUNEGATE_UNDERWORLD_TINT = RunegateColors.tintFor(Underworld.LEVEL);
    static final int RUNEGATE_NETHER_TINT = RunegateColors.tintFor(Level.NETHER);

    private PortalColors() {}

    @SubscribeEvent
    public static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.constant(NETHER_PORTAL_TINT)), InfXBlocks.NETHER_PORTAL.get());
        event.register(
                List.of(runegateTint()),
                InfXBlocks.UNDERWORLD_PORTAL.get(),
                InfXBlocks.RETURN_SPAWN_PORTAL.get());
    }

    static BlockTintSource runegateTint() {
        return new BlockTintSource() {
            @Override
            public int color(@NonNull BlockState state) {
                return RUNEGATE_OVERWORLD_TINT;
            }

            @Override
            public int colorInWorld(
                    @NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
                return level instanceof Level world
                        ? RunegateColors.tintFor(world.dimension())
                        : color(state);
            }
        };
    }
}
