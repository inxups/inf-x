package com.pixulse.infx.furnace;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class FurnaceEvents {
    private FurnaceEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(FurnaceEvents::onRightClickBlock);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getEntity().level().getBlockState(event.getPos());
        if (FurnaceHeatPolicy.isMouthBlocked(event.getEntity().level(), event.getPos(), state)) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
}
