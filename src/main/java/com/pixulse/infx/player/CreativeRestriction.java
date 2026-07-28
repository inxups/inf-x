package com.pixulse.infx.player;

import net.minecraft.network.chat.Component;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Prevents normal R196 survival worlds from being switched into creative mode. */
public final class CreativeRestriction {
    private CreativeRestriction() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(CreativeRestriction::onGameModeChange);
    }

    private static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getNewGameMode() == GameType.CREATIVE
                && !(event.getEntity().level().getServer() instanceof GameTestServer)) {
            event.setCanceled(true);
            event.getEntity().sendSystemMessage(Component.translatable("message.infx.creative_disabled"));
        }
    }
}
