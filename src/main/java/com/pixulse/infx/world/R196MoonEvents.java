package com.pixulse.infx.world;

import com.pixulse.infx.entity.R196MonsterEvents;
import com.pixulse.infx.enchantment.R196Enchantments;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Server-side R196 lunar spawn, weather, sleep, fishing and taming rules. */
public final class R196MoonEvents {
    private static final String BONUS_SPAWN = "infx_moon_bonus_spawn";

    private R196MoonEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196MoonEvents::limitHostileSpawn);
        gameBus.addListener(R196MoonEvents::addHostileSpawn);
        gameBus.addListener(R196MoonEvents::tickLevel);
        gameBus.addListener(R196MoonEvents::preventBloodMoonSleep);
        gameBus.addListener(R196MoonEvents::modifyFishing);
        gameBus.addListener(R196MoonEvents::modifyTaming);
        gameBus.addListener(R196MoonEvents::makeBloodMoonWolvesHostile);
    }

    private static void limitHostileSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Enemy)
                || !(event.getLevel().getLevel() instanceof ServerLevel level)
                || event.getSpawnType() != EntitySpawnReason.NATURAL) {
            return;
        }
        R196MoonPhase phase = R196MoonPhase.at(level);
        if (phase == R196MoonPhase.BLUE && !isDay(level) && level.canSeeSky(event.getEntity().blockPosition())) {
            event.setSpawnCancelled(true);
            return;
        }
        if (phase.hostileSpawnRate() < 1.0D && level.getRandom().nextDouble() >= phase.hostileSpawnRate()) {
            event.setSpawnCancelled(true);
        }
    }

    private static void addHostileSpawn(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Monster original)
                || original.getSpawnType() != EntitySpawnReason.NATURAL
                || original.getPersistentData().getBooleanOr(BONUS_SPAWN, false)) {
            return;
        }
        double extra = R196MoonPhase.at(level).hostileSpawnRate() - 1.0D;
        if (extra <= 0.0D || level.getRandom().nextDouble() >= Math.min(1.0D, extra)) return;
        level.getServer().execute(() -> {
            var replacementType = R196MonsterEvents.replacementFor(original.getType());
            var created = replacementType == null
                    ? original.getType().create(level, EntitySpawnReason.EVENT)
                    : replacementType.create(level, EntitySpawnReason.EVENT);
            if (!(created instanceof Mob copy)) return;
            copy.getPersistentData().putBoolean(BONUS_SPAWN, true);
            copy.snapTo(
                    original.getX() + level.getRandom().nextInt(5) - 2,
                    original.getY(),
                    original.getZ() + level.getRandom().nextInt(5) - 2,
                    original.getYRot(),
                    original.getXRot());
            if (level.noCollision(copy)) level.addFreshEntity(copy);
        });
    }

    private static void tickLevel(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || level.getGameTime() % 200 != 0) {
            return;
        }
        R196MoonPhase phase = R196MoonPhase.at(level);
        if (phase == R196MoonPhase.BLUE) {
            setWeather(level, false, false);
            if (level.getGameTime() % 1_200 == 0) replenishAnimals(level);
        } else if (phase == R196MoonPhase.BLOOD && isDay(level)) {
            setWeather(level, true, true);
            spawnDaylightThreat(level);
        }
    }

    private static void spawnDaylightThreat(ServerLevel level) {
        if (level.players().isEmpty()) return;
        ServerPlayer player = level.players().get(level.getRandom().nextInt(level.players().size()));
        if (level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(64.0D)).size() >= 40) return;
        int x = player.getBlockX() + (level.getRandom().nextBoolean() ? 1 : -1) * (24 + level.getRandom().nextInt(25));
        int z = player.getBlockZ() + (level.getRandom().nextBoolean() ? 1 : -1) * (24 + level.getRandom().nextInt(25));
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        EntityType<? extends Monster> type = level.getRandom().nextBoolean()
                ? ModEntityTypes.R196_CREEPER.get()
                : ModEntityTypes.R196_SPIDER.get();
        Monster monster = type.create(level, EntitySpawnReason.EVENT);
        if (monster == null) return;
        monster.snapTo(x + 0.5D, y, z + 0.5D, 0, 0);
        monster.getPersistentData().putBoolean(BONUS_SPAWN, true);
        if (level.noCollision(monster)) level.addFreshEntity(monster);
    }

    private static void replenishAnimals(ServerLevel level) {
        List<EntityType<? extends Animal>> animals = List.of(
                EntityTypes.COW, EntityTypes.PIG, EntityTypes.SHEEP, EntityTypes.CHICKEN);
        for (ServerPlayer player : level.players()) {
            if (level.getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(48.0D)).size() >= 12) continue;
            int x = player.getBlockX() + level.getRandom().nextInt(49) - 24;
            int z = player.getBlockZ() + level.getRandom().nextInt(49) - 24;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            Animal animal = animals.get(level.getRandom().nextInt(animals.size())).create(level, EntitySpawnReason.EVENT);
            if (animal == null) continue;
            animal.snapTo(x + 0.5D, y, z + 0.5D, 0, 0);
            if (level.noCollision(animal)) level.addFreshEntity(animal);
        }
    }

    private static void preventBloodMoonSleep(CanPlayerSleepEvent event) {
        if (R196MoonPhase.at(event.getEntity().level()).allowsSleep()) return;
        event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
    }

    private static void modifyFishing(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        R196MoonPhase phase = R196MoonPhase.at(player.level());
        double multiplier = phase.fishingMultiplier();
        int baiting = R196Enchantments.level(player.level(), player.getMainHandItem(), ModEnchantments.BAITING);
        if (baiting == 0) {
            baiting = R196Enchantments.level(player.level(), player.getOffhandItem(), ModEnchantments.BAITING);
        }
        multiplier *= 1.0D + baiting * 0.25D;
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
            if (!stack.is(ModItems.WORM.get())) continue;
            stack.consume(1, player);
            return;
        }
    }

    private static void modifyTaming(AnimalTameEvent event) {
        if (!(event.getAnimal().level() instanceof ServerLevel level)
                || !(event.getAnimal() instanceof Wolf)) {
            return;
        }
        R196MoonPhase phase = R196MoonPhase.at(level);
        if (phase == R196MoonPhase.BLOOD && level.getRandom().nextFloat() < 0.75F) {
            event.setCanceled(true);
        } else if (phase != R196MoonPhase.BLUE && level.getRandom().nextFloat() < 0.25F) {
            event.setCanceled(true);
        }
    }

    private static void makeBloodMoonWolvesHostile(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Wolf wolf)
                || wolf.isTame()
                || !(wolf.level() instanceof ServerLevel level)
                || R196MoonPhase.at(level) != R196MoonPhase.BLOOD
                || wolf.tickCount % 20 != 0) {
            return;
        }
        Player target = level.getNearestPlayer(wolf, 32.0D);
        if (target != null && !target.isCreative() && !target.isSpectator()) wolf.setTarget(target);
    }

    public static boolean isFoggy(long overworldClockTime) {
        long day = Math.max(1L, overworldClockTime / 24_000L + 1L);
        long time = Math.floorMod(overworldClockTime, 24_000L);
        return (day % 9L == 0L && time < 8_000L) || R196MoonPhase.atDay(day) == R196MoonPhase.PHANTOM;
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
