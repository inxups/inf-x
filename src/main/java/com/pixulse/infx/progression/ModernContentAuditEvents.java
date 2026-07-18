package com.pixulse.infx.progression;

import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Runtime audit gates for trades and vanilla loot tables not controlled by recipe data. */
public final class ModernContentAuditEvents {
    private static final Set<Identifier> EMPTY_TABLES = Set.of(
            Identifier.withDefaultNamespace("chests/spawn_bonus_chest"),
            Identifier.withDefaultNamespace("gameplay/piglin_bartering"));
    private static final Set<String> EMPTY_CHEST_PREFIXES = Set.of(
            "chests/ancient_city",
            "chests/bastion_",
            "chests/buried_treasure",
            "chests/end_city_treasure",
            "chests/igloo_chest",
            "chests/pillager_outpost",
            "chests/ruined_portal",
            "chests/shipwreck_",
            "chests/trail_ruins_",
            "chests/trial_chambers/",
            "chests/underwater_ruin_",
            "chests/village/",
            "chests/woodland_mansion");

    private ModernContentAuditEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(ModernContentAuditEvents::onLootTableLoad);
        gameBus.addListener(ModernContentAuditEvents::removeVillagers);
    }

    private static void onLootTableLoad(LootTableLoadEvent event) {
        if (isExplicitlyDisabledLootTable(event.getName())) event.setCanceled(true);
    }

    private static void removeVillagers(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof AbstractVillager) {
            event.setCanceled(true);
        }
    }

    public static boolean isExplicitlyDisabledLootTable(Identifier id) {
        if (EMPTY_TABLES.contains(id)) return true;
        if (!id.getNamespace().equals("minecraft")) return false;
        return EMPTY_CHEST_PREFIXES.stream().anyMatch(id.getPath()::startsWith);
    }
}
