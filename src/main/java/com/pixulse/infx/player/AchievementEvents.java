package com.pixulse.infx.player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.VillageProgression;
import com.pixulse.infx.world.WorldData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

/** Records and broadcasts the first player/day for every R196 advancement. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class AchievementEvents {
    private AchievementEvents() {}

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var id = event.getAdvancement().id();
        if (!id.getNamespace().equals(InfiniteX.MOD_ID) || !id.getPath().startsWith("progression/")) return;
        String path = id.getPath().substring("progression/".length());
        WorldData data = WorldData.get(player.level());
        long day = VillageProgression.day(player.level());
        if (data.recordFirst(path, player.getScoreboardName(), day)) {
            player.level().getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable(
                            "message.infx.world_first",
                            player.getDisplayName(),
                            Component.translatable("advancements.infx." + path + ".title"),
                            day),
                    false);
        }
        if (path.equals("the_end2")) data.markEndConquered();
    }
}
