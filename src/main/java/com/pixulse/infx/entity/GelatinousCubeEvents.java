package com.pixulse.infx.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.item.equipment.CorrosionRules;
import com.pixulse.infx.item.equipment.CorrosionType;
import com.pixulse.infx.registry.InfXSounds;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Server-side contact, item corrosion, and loot rules for INFX gelatinous cubes. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class GelatinousCubeEvents {
    private static final int CONTACT_INTERVAL = 20;

    private GelatinousCubeEvents() {}

    /** MITE: burning slimes die without splitting into smaller cubes. */
    @SubscribeEvent
    public static void onMobSplit(MobSplitEvent event) {
        if (event.getParent() instanceof Slime slime && slime.isOnFire()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!InfXConfig.INSTANCE.mobs.enabled.getValue()
                || !(event.getEntity() instanceof InfxSlime slime)
                || !(slime.level() instanceof ServerLevel level)
                || slime.tickCount % CONTACT_INTERVAL != 0) {
            return;
        }

        boolean corroded = dissolveTouchedBlocks(level, slime);
        if (InfXConfig.INSTANCE.mobs.gelatinousItemCorrosion.getValue()) {
            corroded |= dissolveTouchedItems(level, slime);
        }
        if (corroded) {
            playCorrosionFizz(level, slime, slime.getRandom());
        }
        if (InfXConfig.INSTANCE.mobs.gelatinousItemCorrosion.getValue() && slime.getTarget() == null) {
            seekDissolvableItem(level, slime);
        }
    }

    private static boolean dissolveTouchedBlocks(ServerLevel level, InfxSlime slime) {
        CorrosionType type = slime.variant().corrosionType();
        boolean corroded = false;
        for (BlockPos pos : BlockPos.betweenClosed(slime.getBoundingBox().inflate(0.01))) {
            BlockState state = level.getBlockState(pos);
            int period = GelatinousCubeRules.dissolvePeriod(state, type);
            if (period == GelatinousCubeRules.INSTANT) {
                corroded |= GelatinousCubeRules.dissolveOnContact(level, pos, type, null);
                slime.clearDissolvingBlock(pos);
            } else if (period > 0) {
                if (slime.advanceDissolvingBlock(pos, period)) {
                    corroded |= level.destroyBlock(pos, false);
                }
            } else {
                slime.clearDissolvingBlock(pos);
            }
        }
        return corroded;
    }

    public static void playCorrosionFizz(ServerLevel level, BlockPos pos, RandomSource random) {
        level.playSound(
                null,
                pos,
                InfXSounds.GELATINOUS_CUBE_CORROSION.get(),
                SoundSource.HOSTILE,
                0.5F,
                2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
    }

    public static void playCorrosionFizz(ServerLevel level, Entity entity, RandomSource random) {
        level.playSound(
                null,
                entity.getX(),
                entity.getY() + entity.getBbHeight() * 0.5D,
                entity.getZ(),
                InfXSounds.GELATINOUS_CUBE_CORROSION.get(),
                SoundSource.HOSTILE,
                0.7F,
                1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
    }

    private static boolean dissolveTouchedItems(ServerLevel level, InfxSlime slime) {
        boolean corroded = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, slime.getBoundingBox())) {
            corroded |= CorrosionRules.damageItemEntity(item, slime.variant().corrosionType(), 1.0F);
        }
        return corroded;
    }

    private static void seekDissolvableItem(ServerLevel level, InfxSlime slime) {
        level.getEntitiesOfClass(
                        ItemEntity.class,
                        slime.getBoundingBox().inflate(8.0),
                        item -> CorrosionRules.isHarmedBy(item.getItem(), slime.variant().corrosionType()))
                .stream()
                .min(Comparator.comparingDouble(slime::distanceToSqr)).ifPresent(nearest -> slime.getNavigation().moveTo(nearest, 1.0));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!InfXConfig.INSTANCE.mobs.enabled.getValue()
                || !InfXConfig.INSTANCE.mobs.gelatinousItemCorrosion.getValue()
                || event.getHealthDamage() <= 0.0F
                || !(event.getEntity() instanceof InfxSlime slime)
                || !(slime.level() instanceof ServerLevel level)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player) {
            return;
        }
        if (CorrosionRules.damageHeldItem(
                player, slime.variant().corrosionType(), slime.variant().damageMultiplier())) {
            playCorrosionFizz(level, player, slime.getRandom());
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof InfxSlime slime) || !(slime.level() instanceof ServerLevel level)) {
            return;
        }
        event.getDrops().clear();
        if (slime.getSize() == 1) {
            event.getDrops().add(new ItemEntity(
                    level, slime.getX(), slime.getY(), slime.getZ(), slime.gelatinousSphere().getDefaultInstance()));
        }
    }
}
