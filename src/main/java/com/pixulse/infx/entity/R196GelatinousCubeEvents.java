package com.pixulse.infx.entity;

import com.pixulse.infx.equipment.R196CorrosionRules;
import com.pixulse.infx.equipment.R196CorrosionType;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Server-side contact, item corrosion, and loot rules for R196 gelatinous cubes. */
public final class R196GelatinousCubeEvents {
    private static final int CONTACT_INTERVAL = 20;

    private R196GelatinousCubeEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196GelatinousCubeEvents::onEntityTick);
        gameBus.addListener(R196GelatinousCubeEvents::onLivingDamage);
        gameBus.addListener(R196GelatinousCubeEvents::onLivingDrops);
    }

    private static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof R196Slime slime)
                || !(slime.level() instanceof ServerLevel level)
                || slime.tickCount % CONTACT_INTERVAL != 0) {
            return;
        }

        dissolveTouchedBlocks(level, slime);
        dissolveTouchedItems(level, slime);
        if (slime.getTarget() == null) {
            seekDissolvableItem(level, slime);
        }
    }

    private static void dissolveTouchedBlocks(ServerLevel level, R196Slime slime) {
        R196CorrosionType type = slime.variant().corrosionType();
        for (BlockPos pos : BlockPos.betweenClosed(slime.getBoundingBox().inflate(0.01))) {
            int period = R196GelatinousCubeRules.dissolvePeriod(level, pos, type);
            if (period == R196GelatinousCubeRules.INSTANT) {
                R196GelatinousCubeRules.dissolveOnContact(level, pos, type, null);
                slime.clearDissolvingBlock(pos);
            } else if (period > 0) {
                if (slime.advanceDissolvingBlock(pos, period)) {
                    level.destroyBlock(pos, false);
                }
            } else {
                slime.clearDissolvingBlock(pos);
            }
        }
    }

    private static void dissolveTouchedItems(ServerLevel level, R196Slime slime) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, slime.getBoundingBox())) {
            R196CorrosionRules.damageItemEntity(item, slime.variant().corrosionType(), 1.0F);
        }
    }

    private static void seekDissolvableItem(ServerLevel level, R196Slime slime) {
        ItemEntity nearest = level.getEntitiesOfClass(
                        ItemEntity.class,
                        slime.getBoundingBox().inflate(8.0),
                        item -> R196CorrosionRules.isHarmedBy(item.getItem(), slime.variant().corrosionType()))
                .stream()
                .min(Comparator.comparingDouble(slime::distanceToSqr))
                .orElse(null);
        if (nearest != null) {
            slime.getNavigation().moveTo(nearest, 1.0);
        }
    }

    private static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() <= 0.0F
                || !(event.getEntity() instanceof R196Slime slime)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player) {
            return;
        }
        R196CorrosionRules.damageHeldItem(
                player, slime.variant().corrosionType(), slime.variant().damageMultiplier());
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof R196Slime slime) || !(slime.level() instanceof ServerLevel level)) {
            return;
        }
        event.getDrops().clear();
        if (slime.getSize() == 1) {
            event.getDrops().add(new ItemEntity(
                    level, slime.getX(), slime.getY(), slime.getZ(), slime.gelatinousSphere().getDefaultInstance()));
        }
    }
}
