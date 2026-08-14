package com.pixulse.infx;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.render.screen.ConfigSelectScreen;
import com.pixulse.infx.config.InfXClientConfig;
import com.pixulse.infx.config.InfXConfig;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Registers client preferences and the Jupiter-backed Mods configuration screen. */
@Mod(value = InfiniteX.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class InfXClient {
    public InfXClient() {
        ConfigManager.getInstance().registerConfigHandler(InfXClientConfig.INSTANCE);
    }

    @SubscribeEvent
    public static void registerConfigScreen(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (container, parent) -> ConfigSelectScreen.builder(Component.literal("InfiniteX"), parent)
                        .server(InfXConfig.INSTANCE)
                        .client(InfXClientConfig.INSTANCE)
                        .build()));
    }
}
