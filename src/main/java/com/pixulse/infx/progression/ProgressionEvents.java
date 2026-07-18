package com.pixulse.infx.progression;

import com.pixulse.infx.InfiniteX;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class ProgressionEvents {
    private static final Identifier ACQUIRE_IRON = InfiniteX.id("progression/acquire_iron");
    private static final String SMELTED_IRON_CRITERION = "smelted_iron";

    private ProgressionEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(ProgressionEvents::onItemSmelted);
    }

    private static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getAmountRemoved() <= 0
                || !event.getSmelting().is(Items.IRON_INGOT)) {
            return;
        }

        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(ACQUIRE_IRON);
        if (advancement != null) {
            player.getAdvancements().award(advancement, SMELTED_IRON_CRITERION);
        }
    }
}
