package com.pixulse.infx.event;

import com.pixulse.infx.world.MoonPhase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.registry.InfXItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
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

/** Server-side R196 lunar spawn, weather, sleep, fishing and taming rules. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class MoonEvents {
    private MoonEvents() {}

    @SubscribeEvent
    public static void limitHostileSpawn(FinalizeSpawnEvent event) {
        if (!(event.getLevel().getLevel() instanceof ServerLevel level)
                || event.getSpawnType() != EntitySpawnReason.NATURAL) {
            return;
        }
        MobCategory category = event.getEntity().getType().getCategory();
        if (category == MobCategory.CREATURE) {
            if (MoonPhase.at(level) != MoonPhase.BLUE || level.getGameTime() % 400L != 0L) {
                event.setSpawnCancelled(true);
            }
            return;
        }
        if (category == MobCategory.AMBIENT
                || category == MobCategory.WATER_CREATURE
                || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE) {
            if (level.getGameTime() % 400L != 0L) event.setSpawnCancelled(true);
            return;
        }
        if (!(event.getEntity() instanceof Enemy)) return;
        if (level.dimension() != Level.OVERWORLD) {
            if (level.getRandom().nextInt(4) != 0) event.setSpawnCancelled(true);
            return;
        }
        MoonPhase phase = MoonPhase.at(level);
        if (!isDay(level) && level.canSeeSky(event.getEntity().blockPosition())) {
            if (phase == MoonPhase.BLUE
                    || level.getRandom().nextInt(phase.outdoorHostileSpawnDenominator()) != 0) {
                event.setSpawnCancelled(true);
            }
        }
    }

    @SubscribeEvent
    public static void tickLevel(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || level.getGameTime() % 200 != 0) {
            return;
        }
        MoonPhase phase = MoonPhase.at(level);
        if (phase == MoonPhase.BLUE) {
            setWeather(level, false, false);
        } else if (phase == MoonPhase.BLOOD && isDay(level)) {
            setWeather(level, true, true);
        }
    }

    @SubscribeEvent
    public static void modifyFishing(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MoonPhase phase = MoonPhase.at(player.level());
        double multiplier = phase.fishingMultiplier();
        long time = Math.floorMod(player.level().getOverworldClockTime(), 24_000L);
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
        if (!(event.getAnimal().level() instanceof ServerLevel level)
                || !(event.getAnimal() instanceof Wolf)) {
            return;
        }
        MoonPhase phase = MoonPhase.at(level);
        if (phase == MoonPhase.BLOOD && level.getRandom().nextFloat() < 0.75F) {
            event.setCanceled(true);
        } else if (phase != MoonPhase.BLUE && level.getRandom().nextFloat() < 0.25F) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void makeBloodMoonWolvesHostile(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Wolf wolf)
                || wolf.isTame()
                || !(wolf.level() instanceof ServerLevel level)
                || MoonPhase.at(level) != MoonPhase.BLOOD
                || wolf.tickCount % 20 != 0) {
            return;
        }
        Player target = level.getNearestPlayer(wolf, 32.0D);
        if (target != null && !target.isCreative() && !target.isSpectator()) wolf.setTarget(target);
    }

    public static boolean isFoggy(long overworldClockTime) {
        long day = Math.max(1L, overworldClockTime / 24_000L + 1L);
        long time = Math.floorMod(overworldClockTime, 24_000L);
        return (day % 9L == 0L && time < 8_000L) || MoonPhase.atDay(day) == MoonPhase.PHANTOM;
    }

    private static boolean isDay(ServerLevel level) {
        return Math.floorMod(level.getOverworldClockTime(), 24_000L) < 12_000L;
    }

    private static void setWeather(ServerLevel level, boolean raining, boolean thundering) {
        var weather = level.getWeatherData();
        weather.setClearWeatherTime(raining ? 0 : 6_000);
        weather.setRainTime(6_000);
        weather.setThunderTime(6_000);
        weather.setRaining(raining);
        weather.setThundering(thundering);
    }
}
