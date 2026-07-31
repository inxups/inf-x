package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jspecify.annotations.NonNull;

/** Client tints for InfiniteX portal surfaces, resolved from their destination. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class PortalColors {
    private PortalColors() {}

    @SubscribeEvent
    public static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(
                List.of(destinationTint()),
                InfXBlocks.NETHER_PORTAL.get(),
                InfXBlocks.UNDERWORLD_PORTAL.get(),
                InfXBlocks.RETURN_SPAWN_PORTAL.get());
    }

    static BlockTintSource destinationTint() {
        return new BlockTintSource() {
            @Override
            public int color(@NonNull BlockState state) {
                return PortalDestinationColors.tintFor(state);
            }

            @Override
            public int colorInWorld(
                    @NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
                return level instanceof Level world
                        ? PortalDestinationColors.tintFor(state, world.dimension())
                        : color(state);
            }
        };
    }
}
