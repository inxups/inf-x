package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.network.R196Network;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Ctrl-use explicitly chooses the projectile branch while normal use prioritizes eating. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class R196EggClientEvents {
    private R196EggClientEvents() {}

    @SubscribeEvent
    private static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isUseItem() || minecraft.player == null
                || !minecraft.player.getItemInHand(event.getHand()).is(Items.EGG)
                || !controlDown(minecraft)) return;
        ClientPacketDistributor.sendToServer(
                new R196Network.EggThrowPayload(event.getHand() == InteractionHand.OFF_HAND));
        event.setCanceled(true);
    }

    private static boolean controlDown(Minecraft minecraft) {
        long window = minecraft.getWindow().handle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
}
