package com.pixulse.infx.player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.server.ServerTestModePolicy;

import net.minecraft.network.chat.Component;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Prevents normal INFX survival worlds from being switched into creative mode. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class CreativeRestriction {
    private CreativeRestriction() {}

    @SubscribeEvent
    public static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        MinecraftServer server = event.getEntity().level().getServer();
        if (ServerTestModePolicy.effectiveTestMode(server)) return;
        if (event.getNewGameMode() == GameType.CREATIVE && !(server instanceof GameTestServer)) {
            event.setCanceled(true);
            event.getEntity().sendSystemMessage(Component.translatable("message.infx.creative_disabled"));
        }
    }
}
