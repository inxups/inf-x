package com.pixulse.infx.event.server;

import com.pixulse.infx.InfiniteX;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** InfX block drops that differ from vanilla behavior. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class BlockDropEvents {
    private BlockDropEvents() {}

    /** InfX coal ore yields no experience when mined. */
    @SubscribeEvent
    public static void removeCoalExperience(BlockDropsEvent event) {
        if (event.getState().is(Blocks.COAL_ORE) || event.getState().is(Blocks.DEEPSLATE_COAL_ORE)) {
            event.setDroppedExperience(0);
        }
    }

    /**
     * InfX grass plants destroyed with no cause (flowing water, bucket pour, sourceless explosions)
     * must not shower wheat seeds; only player and livestock harvesting keeps the drop.
     */
    @SubscribeEvent
    public static void suppressUncausedGrassSeedDrops(BlockDropsEvent event) {
        if (event.getBreaker() == null && isSeedGrass(event.getState())) {
            event.setCanceled(true);
        }
    }

    static boolean isSeedGrass(BlockState state) {
        return state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN);
    }
}
