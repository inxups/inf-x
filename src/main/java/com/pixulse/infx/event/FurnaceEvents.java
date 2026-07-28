package com.pixulse.infx.event;

import com.pixulse.infx.data.furnace.FurnaceHeatPolicy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class FurnaceEvents {
    private FurnaceEvents() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getEntity().level().getBlockState(event.getPos());
        if (FurnaceHeatPolicy.isMouthBlocked(event.getEntity().level(), event.getPos(), state)) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
}
