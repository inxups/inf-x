package com.pixulse.infx.event;

import com.pixulse.infx.world.MoonPhase;
import com.pixulse.infx.config.InfXConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.world.SpawnGate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-side INFX lunar spawn, weather, sleep, fishing and taming rules. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class MoonEvents {
    private MoonEvents() {}

    @SubscribeEvent
    public static void limitHostileSpawn(FinalizeSpawnEvent event) {
        if (!InfXConfig.INSTANCE.mobs.enabled.getValue()
                || !InfXConfig.INSTANCE.mobs.moonSpawnGating.getValue()
                || !(event.getLevel().getLevel() instanceof ServerLevel level)
                || event.getSpawnType() != EntitySpawnReason.NATURAL) {
            return;
        }
        boolean enemy = event.getEntity() instanceof Enemy;
        boolean skyExposed = level.canSeeSky(event.getEntity().blockPosition());
        if (!SpawnGate.shouldSpawnNatural(
                level,
                event.getEntity().getType(),
                event.getEntity().getType().getCategory(),
                enemy,
                skyExposed)) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void tickLevel(LevelTickEvent.Post event) {
        if (!InfXConfig.INSTANCE.world.enabled.getValue()
                || !InfXConfig.INSTANCE.world.moonEvents.getValue()
                || !(event.getLevel() instanceof ServerLevel level)
                || !MoonPhase.isOverworld(level)
                || level.getGameTime() % 200 != 0) {
            return;
        }
        if (MoonPhase.BLUE.isActiveInOverworld(level)) {
            setWeather(level, false, false, 6_000);
        } else if (isBloodMoonThunderWindow(level)) {
            // MITE forces a thunderstorm from noon, lasting 13,000 ticks (noon→19:00).
            long remaining = MoonPhase.bloodMoonStormRemainingTicks(level.getOverworldClockTime());
            if (remaining > 0) {
                setWeather(level, true, true, (int) remaining);
            }
        }
    }

    /** MITE blood-moon storm window: the whole day from noon (World.java:8675-8680). */
    public static boolean isBloodMoonThunderWindow(ServerLevel level) {
        return MoonPhase.isBloodMoonThunderWindow(level.getOverworldClockTime());
    }

    @SubscribeEvent
    public static void modifyFishing(ItemFishedEvent event) {
        if (!InfXConfig.INSTANCE.world.enabled.getValue()
                || !InfXConfig.INSTANCE.world.lunarFishing.getValue()
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        // ItemFishedEvent is raised after vanilla has chosen the catch. Keep this as a scoped
        // INFX loot bonus; strict InfX fishing would need to replace FishingHook's bite timing.
        MoonPhase phase = MoonPhase.isOverworld(player.level())
                ? MoonPhase.at(player.level())
                : MoonPhase.NORMAL;
        double multiplier = phase.fishingMultiplier();
        long time = Math.floorMod(player.level().getOverworldClockTime(), MoonPhase.DAY_TICKS);
        if (time < 2_000L || time > 11_000L && time < 14_000L) multiplier *= 1.5D;
        if (player.level().isRainingAt(event.getHookEntity().blockPosition())) multiplier *= 1.5D;
        if (multiplier > 1.0D && !event.getDrops().isEmpty()
                && player.getRandom().nextDouble() < Math.min(1.0D, multiplier - 1.0D)) {
            event.getDrops().add(event.getDrops().getFirst().copy());
        }
        consumeWormBait(player);
    }

    private static void consumeWormBait(ServerPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(InfXItems.WORM.get())) continue;
            stack.consume(1, player);
            return;
        }
    }

    @SubscribeEvent
    public static void modifyTaming(AnimalTameEvent event) {
        if (!InfXConfig.INSTANCE.world.enabled.getValue()
                || !InfXConfig.INSTANCE.world.lunarTaming.getValue()
                || !(event.getAnimal().level() instanceof ServerLevel level)
                || !(event.getAnimal() instanceof Wolf)) {
            return;
        }
        boolean bloodMoonNight = MoonPhase.BLOOD.isActiveInOverworldAtNight(level);
        boolean blueMoonNight = MoonPhase.BLUE.isActiveInOverworldAtNight(level);
        if (bloodMoonNight && level.getRandom().nextFloat() < 0.75F) {
            event.setCanceled(true);
        } else if (!blueMoonNight && level.getRandom().nextFloat() < 0.25F) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void applyLunarBuffs(PlayerTickEvent.Post event) {
        if (!InfXConfig.INSTANCE.world.enabled.getValue()
                || !InfXConfig.INSTANCE.world.lunarBuffs.getValue()
                || !(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % 100 != 0) {
            return;
        }
        long time = player.level().getOverworldClockTime();
        MoonPhase phase = MoonPhase.atTime(time);
        // Refresh the effect's remaining duration to the end of the current lunar day so it
        // lapses on its own at the phase boundary — no removeEffect that could cull an
        // externally granted luck/unluck. addEffect's merge keeps the longer (equal-expiry)
        // instance, so re-application never shortens the live duration.
        int remaining = (int) Math.max(1L, MoonPhase.DAY_TICKS - Math.floorMod(time, MoonPhase.DAY_TICKS));
        if (phase == MoonPhase.BLOOD) {
            player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, remaining, 0, false, true, true));
        } else if (phase == MoonPhase.BLUE) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, remaining, 0, false, true, true));
        }
    }

    @SubscribeEvent
    public static void makeBloodMoonWolvesHostile(EntityTickEvent.Post event) {
        if (!InfXConfig.INSTANCE.world.enabled.getValue()
                || !InfXConfig.INSTANCE.world.moonEvents.getValue()
                || !(event.getEntity() instanceof Wolf wolf)
                || wolf.isTame()
                || !(wolf.level() instanceof ServerLevel level)
                || !MoonPhase.BLOOD.isActiveInOverworldAtNight(level)
                || wolf.tickCount % 20 != 0) {
            return;
        }
        Player target = level.getNearestPlayer(wolf, 32.0D);
        if (target != null && !target.isCreative() && !target.isSpectator()) wolf.setTarget(target);
    }

    public static boolean isFoggy(Level level) {
        return InfXConfig.INSTANCE.world.enabled.getValue()
                && InfXConfig.INSTANCE.world.moonEvents.getValue()
                && MoonPhase.isOverworld(level)
                && isFoggy(level.getOverworldClockTime());
    }

    public static boolean isFoggy(long overworldClockTime) {
        long day = MoonPhase.dayAt(overworldClockTime);
        long time = Math.floorMod(overworldClockTime, MoonPhase.DAY_TICKS);
        return (day % 9L == 0L && time < 8_000L)
                || MoonPhase.atDay(day) == MoonPhase.PHANTOM && MoonPhase.isNightTime(overworldClockTime);
    }

    private static void setWeather(ServerLevel level, boolean raining, boolean thundering, int duration) {
        var weather = level.getWeatherData();
        weather.setClearWeatherTime(raining ? 0 : duration);
        weather.setRainTime(duration);
        weather.setThunderTime(duration);
        weather.setRaining(raining);
        weather.setThundering(thundering);
    }
}
