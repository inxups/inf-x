package com.pixulse.infx.event;

import com.pixulse.infx.data.harvest.HarvestSpeedRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.block.SafeBlock;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.registry.tag.InfXBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class HarvestEvents {
    private HarvestEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyBreakSpeedRules(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        BlockPos pos = event.getPosition().orElse(null);
        float hardness = pos == null ? -1.0F : state.getDestroySpeed(player.level(), pos);
        boolean portable = isPortable(player, state, pos);
        float speed = HarvestSpeedRules.adjustedBreakSpeed(
                player,
                event.getNewSpeed(),
                hardness,
                portable);
        // SafeEvents grants owners harvest capability so vanilla already uses its /30 divisor.
        if (portable && !(state.getBlock() instanceof SafeBlock)) {
            speed = HarvestSpeedRules.compensatePortableHandSpeed(
                    speed,
                    state.requiresCorrectToolForDrops(),
                    player.hasCorrectToolForDrops(state));
        }
        event.setNewSpeed(speed);
    }

    private static boolean isPortable(Player player, BlockState state, @Nullable BlockPos pos) {
        if (!state.is(InfXBlockTags.PORTABLE_HAND_HARVEST)) {
            return false;
        }
        if (!(state.getBlock() instanceof SafeBlock)) {
            return true;
        }
        return pos != null
                && player.level().getBlockEntity(pos) instanceof SafeBlockEntity safe
                && safe.isPortableTo(player);
    }

}
