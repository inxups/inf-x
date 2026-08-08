package com.pixulse.infx.event.server;

import com.pixulse.infx.InfiniteX;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** InfX ore drops that differ from vanilla experience behavior. */
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
}
