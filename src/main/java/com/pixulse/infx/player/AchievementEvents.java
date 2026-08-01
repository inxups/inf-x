package com.pixulse.infx.player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.StructureGenerationGates;
import com.pixulse.infx.world.WorldData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

/** Records the first player/day for every INFX advancement and updates world milestones. */
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
        long day = StructureGenerationGates.day(player.level());
        data.recordFirst(path, player.getScoreboardName(), day);
        if (path.equals("the_end2")) data.markEndConquered();
        StructureGenerationGates.refresh(player.level());
    }
}
