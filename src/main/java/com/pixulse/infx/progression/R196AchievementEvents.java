package com.pixulse.infx.progression;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.R196VillageProgression;
import com.pixulse.infx.world.R196WorldData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

/** Records and broadcasts the first player/day for every R196 advancement. */
public final class R196AchievementEvents {
    private R196AchievementEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196AchievementEvents::onAdvancementEarned);
    }

    private static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var id = event.getAdvancement().id();
        if (!id.getNamespace().equals(InfiniteX.MOD_ID) || !id.getPath().startsWith("progression/")) return;
        String path = id.getPath().substring("progression/".length());
        R196WorldData data = R196WorldData.get(player.level());
        long day = R196VillageProgression.day(player.level());
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
