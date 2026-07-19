package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.R196MoonEvents;
import com.pixulse.infx.world.R196MoonPhase;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Client fog and color treatment for blood, blue, yellow, phantom moons and fog days. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class R196MoonClientEvents {
    private R196MoonClientEvents() {}

    @SubscribeEvent
    private static void renderFog(ViewportEvent.RenderFog event) {
        var level = Minecraft.getInstance().level;
        if (level != null && R196MoonEvents.isFoggy(level.getOverworldClockTime())) {
            event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), 8.0F));
            event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), 48.0F));
        }
    }

    @SubscribeEvent
    private static void fogColor(ViewportEvent.ComputeFogColor event) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        R196MoonPhase phase = R196MoonPhase.atTime(level.getOverworldClockTime());
        float red = event.getRed();
        float green = event.getGreen();
        float blue = event.getBlue();
        switch (phase) {
            case BLOOD -> {
                event.setRed(Math.min(1.0F, red * 1.35F + 0.15F));
                event.setGreen(green * 0.55F);
                event.setBlue(blue * 0.55F);
            }
            case BLUE -> {
                event.setRed(red * 0.65F);
                event.setGreen(Math.min(1.0F, green * 0.9F + 0.08F));
                event.setBlue(Math.min(1.0F, blue * 1.4F + 0.15F));
            }
            case YELLOW -> {
                event.setRed(Math.min(1.0F, red * 1.2F + 0.08F));
                event.setGreen(Math.min(1.0F, green * 1.05F + 0.04F));
                event.setBlue(blue * 0.7F);
            }
            case PHANTOM -> {
                event.setRed(Math.min(1.0F, red * 0.9F + 0.12F));
                event.setGreen(green * 0.75F);
                event.setBlue(Math.min(1.0F, blue * 1.25F + 0.12F));
            }
            default -> {
            }
        }
    }
}
