package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196BucketItem;
import com.pixulse.infx.network.R196Network;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Ctrl-use places a fluid source when the player has enough total XP. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class R196BucketClientEvents {
    private R196BucketClientEvents() {}

    @SubscribeEvent
    private static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isUseItem()
                || minecraft.player == null
                || !(minecraft.player.getItemInHand(event.getHand()).getItem() instanceof R196BucketItem bucket)
                || (bucket.contents() != R196BucketItem.Contents.WATER
                        && bucket.contents() != R196BucketItem.Contents.LAVA)
                || !controlDown(minecraft)
                || !R196BucketItem.canPlaceAsSource(minecraft.player, true)) {
            return;
        }
        ClientPacketDistributor.sendToServer(
                new R196Network.PlaceFluidSourcePayload(event.getHand() == InteractionHand.OFF_HAND));
        event.setCanceled(true);
        event.setSwingHand(true);
    }

    private static boolean controlDown(Minecraft minecraft) {
        long window = minecraft.getWindow().handle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
}
