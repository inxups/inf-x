package com.pixulse.infx.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Restores the survival End chain and R196 crystal/dragon constraints. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class EndEvents {
    private EndEvents() {}

    @SubscribeEvent
    public static void restrictCrystalAttack(AttackEntityEvent event) {
        if (event.getTarget() instanceof EndCrystal && !hasAdamantiumCrystalTool(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void restrictCrystalDamage(EntityInvulnerabilityCheckEvent event) {
        if (!(event.getEntity() instanceof EndCrystal)) return;
        boolean validMelee = event.getSource().getEntity() instanceof Player player
                && event.getSource().getDirectEntity() == player
                && hasAdamantiumCrystalTool(player);
        event.setInvulnerable(!validMelee);
    }

    public static boolean hasAdamantiumCrystalTool(Player player) {
        var entry = InfXItems.catalog().equipment(player.getMainHandItem());
        return entry != null
                && entry.key().material() == MiteMaterial.ADAMANTIUM
                && (entry.key().type() == EquipmentType.PICKAXE
                        || entry.key().type() == EquipmentType.WAR_HAMMER);
    }

    @SubscribeEvent
    public static void restoreDragonOnReload(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getTo().equals(Level.END) || !(event.getEntity().level() instanceof ServerLevel end)) return;
        if (end.players().size() > 1) return;
        for (EnderDragon dragon : end.getDragons()) {
            dragon.setHealth(Math.max(dragon.getHealth(), dragon.getMaxHealth() * 0.5F));
        }
    }
}
