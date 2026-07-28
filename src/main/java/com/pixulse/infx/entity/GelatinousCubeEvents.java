package com.pixulse.infx.entity;

import com.pixulse.infx.equipment.CorrosionRules;
import com.pixulse.infx.equipment.CorrosionType;
import com.pixulse.infx.registry.ModSounds;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Server-side contact, item corrosion, and loot rules for R196 gelatinous cubes. */
public final class GelatinousCubeEvents {
    private static final int CONTACT_INTERVAL = 20;

    private GelatinousCubeEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(GelatinousCubeEvents::onEntityTick);
        gameBus.addListener(GelatinousCubeEvents::onLivingDamage);
        gameBus.addListener(GelatinousCubeEvents::onLivingDrops);
    }

    private static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof MiteSlime slime)
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

    private static void dissolveTouchedBlocks(ServerLevel level, MiteSlime slime) {
        CorrosionType type = slime.variant().corrosionType();
        boolean playedGrassCorrosionSound = false;
        for (BlockPos pos : BlockPos.betweenClosed(slime.getBoundingBox().inflate(0.01))) {
            BlockState state = level.getBlockState(pos);
            int period = GelatinousCubeRules.dissolvePeriod(state, type);
            if (period == GelatinousCubeRules.INSTANT) {
                boolean dissolved = GelatinousCubeRules.dissolveOnContact(level, pos, type, null);
                if (!playedGrassCorrosionSound
                        && dissolved
                        && GelatinousCubeRules.isAcidScorchableGround(state, type)) {
                    playAcidCorrosionFizz(level, pos, slime.getRandom());
                    playedGrassCorrosionSound = true;
                }
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

    static void playAcidCorrosionFizz(ServerLevel level, BlockPos pos, RandomSource random) {
        level.playSound(
                null,
                pos,
                ModSounds.GELATINOUS_CUBE_CORROSION.get(),
                SoundSource.HOSTILE,
                0.5F,
                2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
    }

    private static void dissolveTouchedItems(ServerLevel level, MiteSlime slime) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, slime.getBoundingBox())) {
            CorrosionRules.damageItemEntity(item, slime.variant().corrosionType(), 1.0F);
        }
    }

    private static void seekDissolvableItem(ServerLevel level, MiteSlime slime) {
        ItemEntity nearest = level.getEntitiesOfClass(
                        ItemEntity.class,
                        slime.getBoundingBox().inflate(8.0),
                        item -> CorrosionRules.isHarmedBy(item.getItem(), slime.variant().corrosionType()))
                .stream()
                .min(Comparator.comparingDouble(slime::distanceToSqr))
                .orElse(null);
        if (nearest != null) {
            slime.getNavigation().moveTo(nearest, 1.0);
        }
    }

    private static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() <= 0.0F
                || !(event.getEntity() instanceof MiteSlime slime)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player) {
            return;
        }
        CorrosionRules.damageHeldItem(
                player, slime.variant().corrosionType(), slime.variant().damageMultiplier());
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof MiteSlime slime) || !(slime.level() instanceof ServerLevel level)) {
            return;
        }
        event.getDrops().clear();
        if (slime.getSize() == 1) {
            event.getDrops().add(new ItemEntity(
                    level, slime.getX(), slime.getY(), slime.getZ(), slime.gelatinousSphere().getDefaultInstance()));
        }
    }
}
