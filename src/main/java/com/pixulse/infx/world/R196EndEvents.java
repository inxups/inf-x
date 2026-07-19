package com.pixulse.infx.world;

import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Restores the survival End chain and R196 crystal/dragon constraints. */
public final class R196EndEvents {
    private R196EndEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196EndEvents::restrictCrystalAttack);
        gameBus.addListener(R196EndEvents::restrictCrystalDamage);
        gameBus.addListener(R196EndEvents::restoreDragonOnReload);
    }

    private static void restrictCrystalAttack(AttackEntityEvent event) {
        if (event.getTarget() instanceof EndCrystal && !hasAdamantiumCrystalTool(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void restrictCrystalDamage(EntityInvulnerabilityCheckEvent event) {
        if (!(event.getEntity() instanceof EndCrystal)) return;
        boolean validMelee = event.getSource().getEntity() instanceof Player player
                && event.getSource().getDirectEntity() == player
                && hasAdamantiumCrystalTool(player);
        event.setInvulnerable(!validMelee);
    }

    public static boolean hasAdamantiumCrystalTool(Player player) {
        var entry = ModItems.catalog().equipment(player.getMainHandItem());
        return entry != null
                && entry.key().material() == R196Material.ADAMANTIUM
                && (entry.key().type() == R196EquipmentType.PICKAXE
                        || entry.key().type() == R196EquipmentType.WAR_HAMMER);
    }

    private static void restoreDragonOnReload(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getTo().equals(Level.END) || !(event.getEntity().level() instanceof ServerLevel end)) return;
        if (end.players().size() > 1) return;
        for (EnderDragon dragon : end.getDragons()) {
            dragon.setHealth(Math.max(dragon.getHealth(), dragon.getMaxHealth() * 0.5F));
        }
    }
}
