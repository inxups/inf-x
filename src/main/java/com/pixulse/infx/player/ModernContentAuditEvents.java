package com.pixulse.infx.player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Runtime audit gates for trades and vanilla loot tables not controlled by recipe data. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModernContentAuditEvents {
    private static final Set<Identifier> EMPTY_TABLES = Set.of(
            Identifier.withDefaultNamespace("chests/spawn_bonus_chest"),
            Identifier.withDefaultNamespace("gameplay/piglin_bartering"));

    private ModernContentAuditEvents() {}

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (isExplicitlyDisabledLootTable(event.getName())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void removeVillagers(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof AbstractVillager) {
            event.setCanceled(true);
        }
    }

    public static boolean isExplicitlyDisabledLootTable(Identifier id) {
        return EMPTY_TABLES.contains(id);
    }
}
