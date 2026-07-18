package com.pixulse.infx.entity;

import com.pixulse.infx.world.R196MoonPhase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** R196 husbandry rules layered onto vanilla animals through public events and goals. */
public final class R196AnimalEvents {
    private static final String MILK_DAY = "infx_cow_milk_day";
    private static final String MILK_UNITS = "infx_cow_milk_units";
    private static final String NEXT_FEATHER = "infx_chicken_next_feather";
    private static final String HORSE_RETRY = "infx_horse_tame_retry";
    private static final long FEATHER_INTERVAL = 96_000L;
    private static final long HORSE_RETRY_TICKS = 4_000L;

    private R196AnimalEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196AnimalEvents::onJoin);
        gameBus.addListener(R196AnimalEvents::beforeAnimalTick);
        gameBus.addListener(R196AnimalEvents::onAnimalTick);
        gameBus.addListener(R196AnimalEvents::onBreeding);
        gameBus.addListener(R196AnimalEvents::onEntityInteract);
        gameBus.addListener(R196AnimalEvents::onDamage);
        gameBus.addListener(R196AnimalEvents::onDrops);
        gameBus.addListener(R196AnimalEvents::onMount);
    }

    private static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel) || !(event.getEntity() instanceof Animal animal)) return;
        if (!animal.getPersistentData().getBooleanOr(R196Livestock.GOALS_ADDED, false)) {
            animal.goalSelector.addGoal(2, new R196Livestock.NeedsGoal(animal));
            animal.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                    animal,
                    Mob.class,
                    mob -> mob instanceof Enemy,
                    10.0F,
                    1.15,
                    1.4,
                    entity -> entity.isAlive()));
            if (animal instanceof AbstractHorse horse) {
                horse.goalSelector.addGoal(3, new AvoidEntityGoal<>(
                        horse,
                        Player.class,
                        player -> !horse.isTamed(),
                        10.0F,
                        1.1,
                        1.35,
                        entity -> !entity.isSpectator()));
            }
            animal.getPersistentData().putBoolean(R196Livestock.GOALS_ADDED, true);
        }
        if (animal instanceof Cow cow) {
            var health = cow.getAttribute(Attributes.MAX_HEALTH);
            if (health != null && health.getBaseValue() < 20.0) {
                health.setBaseValue(20.0);
                cow.setHealth(20.0F);
            }
        }
    }

    private static void onAnimalTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Animal animal) || !(entity.level() instanceof ServerLevel level)) return;
        if (animal.tickCount % 100 == 0) R196Livestock.update(level, animal);
        if (animal instanceof Chicken chicken) updateChicken(level, chicken);
    }

    private static void beforeAnimalTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Chicken chicken
                && shouldDelayEgg(
                        !chicken.isBaby(),
                        chicken.isChickenJockey(),
                        R196Livestock.isProductive(chicken),
                        chicken.eggTime)) {
            chicken.eggTime = 1_200;
        }
    }

    static boolean shouldDelayEgg(boolean adult, boolean jockey, boolean productive, int eggTime) {
        return adult && !jockey && !productive && eggTime <= 1;
    }

    public static void updateChicken(ServerLevel level, Chicken chicken) {
        if (chicken.isBaby() || chicken.isChickenJockey()) return;
        if (!R196Livestock.isProductive(chicken)) {
            chicken.eggTime = Math.max(chicken.eggTime, 1_200);
        } else if (R196MoonPhase.at(level) == R196MoonPhase.FULL && chicken.tickCount % 2 == 0) {
            chicken.eggTime--;
        } else if (R196MoonPhase.at(level) == R196MoonPhase.NEW && chicken.tickCount % 2 == 0) {
            chicken.eggTime++;
        }

        long now = level.getGameTime();
        long next = chicken.getPersistentData().getLong(NEXT_FEATHER).orElse(0L);
        if (next == 0L) {
            chicken.getPersistentData().putLong(NEXT_FEATHER, now + FEATHER_INTERVAL);
        } else if (now >= next && R196Livestock.isProductive(chicken)) {
            chicken.spawnAtLocation(level, Items.FEATHER);
            chicken.getPersistentData().putLong(NEXT_FEATHER, now + FEATHER_INTERVAL);
        }
    }

    private static void onBreeding(BabyEntitySpawnEvent event) {
        if (!(event.getParentA() instanceof Animal first)
                || !(event.getParentB() instanceof Animal second)
                || !(first.level() instanceof ServerLevel level)) {
            return;
        }
        if (!R196Livestock.canBreed(level, first) || !R196Livestock.canBreed(level, second)) {
            event.setCanceled(true);
        }
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getTarget() instanceof Animal animal && animal.isFood(event.getItemStack())) {
            R196Livestock.markFed(animal, level.getGameTime());
        }
        if (event.getTarget() instanceof Cow cow && event.getItemStack().is(Items.BUCKET) && !cow.isBaby()) {
            if (!takeMilk(level, cow, 4)) deny(event);
        } else if (event.getTarget() instanceof Sheep sheep && event.getItemStack().is(Items.SHEARS)) {
            if (!R196Livestock.isProductive(sheep)) deny(event);
        }
    }

    static boolean takeMilk(ServerLevel level, Cow cow, int units) {
        if (!R196Livestock.isProductive(cow)) return false;
        long day = level.getOverworldClockTime() / 24_000L;
        var data = cow.getPersistentData();
        if (data.getLong(MILK_DAY).orElse(Long.MIN_VALUE) != day) {
            data.putLong(MILK_DAY, day);
            data.putInt(MILK_UNITS, 0);
        }
        int used = data.getInt(MILK_UNITS).orElse(0);
        if (used + units > 4) return false;
        data.putInt(MILK_UNITS, used + units);
        return true;
    }

    private static void deny(PlayerInteractEvent.EntityInteractSpecific event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    private static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || event.getInflictedDamage() <= 0.0F) return;
        if (event.getEntity() instanceof Animal animal) R196Livestock.panic(level, animal);
        if (event.getEntity() instanceof Chicken chicken) {
            long accelerated = level.getGameTime() + 1_200L;
            long current = chicken.getPersistentData().getLong(NEXT_FEATHER).orElse(Long.MAX_VALUE);
            chicken.getPersistentData().putLong(NEXT_FEATHER, Math.min(current, accelerated));
        }
        if (event.getEntity() instanceof Sheep sheep
                && (event.getSource().is(DamageTypeTags.IS_FIRE)
                        || event.getSource().typeHolder().unwrapKey()
                                .map(key -> key.identifier().getPath().contains("acid"))
                                .orElse(false))) {
            sheep.setSheared(true);
        }
    }

    private static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (event.getEntity() instanceof IronGolem golem) {
            event.getDrops().removeIf(drop -> drop.getItem().is(Items.IRON_INGOT));
            event.getDrops().add(drop(level, golem, new ItemStack(Items.IRON_NUGGET, 2 + golem.getRandom().nextInt(4))));
        } else if (event.getEntity() instanceof Sheep sheep) {
            if (sheep.getRandom().nextBoolean()) event.getDrops().add(drop(level, sheep, new ItemStack(Items.LEATHER)));
        } else if (event.getEntity() instanceof AbstractHorse horse) {
            event.getDrops().add(drop(level, horse, new ItemStack(Items.BEEF, 1 + horse.getRandom().nextInt(3))));
        }
    }

    private static ItemEntity drop(ServerLevel level, Entity source, ItemStack stack) {
        return new ItemEntity(level, source.getX(), source.getY(), source.getZ(), stack);
    }

    private static void onMount(EntityMountEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntityMounting() instanceof Player)
                || !(event.getEntityBeingMounted() instanceof AbstractHorse horse)
                || horse.isTamed()) {
            return;
        }
        long now = level.getGameTime();
        if (event.isMounting() && horse.getPersistentData().getLong(HORSE_RETRY).orElse(0L) > now) {
            event.setCanceled(true);
        } else if (event.isDismounting()) {
            horse.getPersistentData().putLong(HORSE_RETRY, now + HORSE_RETRY_TICKS);
        }
    }

    static long horseRetryTicks() {
        return HORSE_RETRY_TICKS;
    }
}
