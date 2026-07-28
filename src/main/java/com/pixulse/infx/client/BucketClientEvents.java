package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.MiteBucketItem;
import com.pixulse.infx.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * MITE ctrl_is_down for buckets. An empty bucket takes the liquid cell it scoops from; a filled one
 * places a permanent source for 100 XP. Both need the server to know Ctrl was held, so the use is
 * cancelled client-side and replayed server-side under the flag.
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class BucketClientEvents {
    private BucketClientEvents() {}

    @SubscribeEvent
    private static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isUseItem()
                || minecraft.player == null
                || !(minecraft.player.getItemInHand(event.getHand()).getItem() instanceof MiteBucketItem bucket)
                || !controlDown(minecraft)
                || !ctrlIsMeaningful(bucket, minecraft)) {
            return;
        }
        ClientPacketDistributor.sendToServer(
                new Network.PlaceFluidSourcePayload(event.getHand() == InteractionHand.OFF_HAND));
        event.setCanceled(true);
        event.setSwingHand(true);
    }

    /** Empty buckets always honour Ctrl; filled ones only when the source can actually be paid for. */
    private static boolean ctrlIsMeaningful(MiteBucketItem bucket, Minecraft minecraft) {
        return switch (bucket.contents()) {
            case EMPTY -> true;
            case WATER, LAVA -> MiteBucketItem.canPlaceAsSource(minecraft.player, true);
            case MILK, STONE -> false;
        };
    }

    private static boolean controlDown(Minecraft minecraft) {
        long window = minecraft.getWindow().handle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
}
