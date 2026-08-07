package com.pixulse.infx.entity;

import com.pixulse.infx.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** INFX squid replacement that hunts prey in water and applies its original slow. */
public final class InfxSquid extends Squid implements InfxMob {
    private static final String BOAT_SQUID_HITS = "infx_squid_hits";
    private static final String BOAT_SQUID_LAST_TICK = "infx_squid_last_tick";
    private static final int BOAT_DESTROY_HITS = 6;
    private static final int BOAT_HIT_DECAY_TICKS = 200;

    public InfxSquid(EntityType<? extends Squid> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Squid.createAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    /** InfX squid grant no experience. */
    @Override
    public int getBaseExperienceReward(@NonNull ServerLevel level) {
        return 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        // InfX squid keep the peace on blue-moon nights.
        if (MoonPhase.BLUE.isActiveInOverworldAtNight(level)) {
            return;
        }
        LivingEntity target = nearestPrey(level);
        if (target == null) {
            return;
        }

        Vec3 delta = target.getEyePosition().subtract(position());
        double distance = delta.length();
        if (distance > 0.001) {
            setDeltaMovement(getDeltaMovement().scale(0.5).add(delta.normalize().scale(0.20)));
        }
        if (target instanceof Player player
                && !(player.getVehicle() instanceof AbstractBoat)
                && distanceTo(target) < 1.0F) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 2), this);
        }
    }

    @Override
    protected void doPush(@NonNull Entity entity) {
        if (ramPursuedBoat(entity)) {
            return;
        }
        if (level() instanceof ServerLevel level
                && entity instanceof Animal animal
                && canPreyUpon(level, animal)
                && hasLineOfSight(animal)) {
            animal.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 2), this);
        }
        super.doPush(entity);
    }

    /**
     * Modern living entities initiate a collision through {@link #doPush(Entity)}, which then
     * calls {@code boat.push(this)}.  Hook before that delegation so the InfX six-hit rule runs
     * on natural squid-to-boat collisions rather than only on an explicit test call.
     */
    private boolean ramPursuedBoat(Entity entity) {
        if (!(level() instanceof ServerLevel level) || !(entity instanceof AbstractBoat boat)) {
            return false;
        }

        // InfX keeps this counter on the boat, so separate squid attacks combine and the
        // damage survives unloading. It decays on the boat's id-offset 200-tick cadence.
        LivingEntity target = nearestPrey(level);
        if (target != null && target.getVehicle() == boat) {
            int hits = squidRamHits(boat);
            if (hits + 1 >= BOAT_DESTROY_HITS) {
                boat.hurtServer(level, level.damageSources().mobAttack(this), 5.0F);
                return boat.isRemoved();
            } else {
                boat.getPersistentData().putInt(BOAT_SQUID_HITS, hits + 1);
            }
        }
        return false;
    }

    private static int squidRamHits(AbstractBoat boat) {
        var data = boat.getPersistentData();
        int currentTick = boat.tickCount + boat.getId() * 47;
        int previousTick = data.getIntOr(BOAT_SQUID_LAST_TICK, currentTick);
        int hits = data.getIntOr(BOAT_SQUID_HITS, 0);
        if (currentTick >= previousTick) {
            int elapsedDecayIntervals = Math.floorDiv(currentTick, BOAT_HIT_DECAY_TICKS)
                    - Math.floorDiv(previousTick, BOAT_HIT_DECAY_TICKS);
            hits = Math.max(0, hits - elapsedDecayIntervals);
        }
        data.putInt(BOAT_SQUID_LAST_TICK, currentTick);
        return hits;
    }

    private @Nullable LivingEntity nearestPrey(ServerLevel level) {
        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        getBoundingBox().inflate(16.0),
                        candidate -> candidate != this
                                && candidate.isAlive()
                                && canPreyUpon(level, candidate)
                                && hasLineOfSight(candidate))
                .stream()
                .min((left, right) -> Double.compare(distanceToSqr(left), distanceToSqr(right)))
                .orElse(null);
    }

    private static boolean canPreyUpon(ServerLevel level, LivingEntity candidate) {
        if (MoonPhase.BLUE.isActiveInOverworldAtNight(level)) {
            return false;
        }
        if (candidate instanceof Player player) {
            return !player.isCreative()
                    && !player.isSpectator()
                    && (!(player.getVehicle() instanceof AbstractBoat) || MoonPhase.isNight(level))
                    && !isPlayerNotInOrAboveDeepWater(player);
        }
        // InfX's Entity#isTrueAnimal accepts land animals but explicitly excludes hellhounds.
        return candidate instanceof Animal
                && !(candidate instanceof InfxWolf wolf && wolf.variant() == InfxWolf.Variant.HELLHOUND)
                && isInOrAboveWater(candidate);
    }

    private static boolean isInOrAboveWater(LivingEntity entity) {
        if (entity.isInWater() || entity.getVehicle() instanceof AbstractBoat) {
            return true;
        }
        BlockPos position = entity.blockPosition();
        return entity.level().getFluidState(position).is(FluidTags.WATER)
                || entity.level().getFluidState(position.below()).is(FluidTags.WATER);
    }

    private static boolean isPlayerNotInOrAboveDeepWater(Player player) {
        if (player.isPassenger()) {
            return false;
        }
        Level level = player.level();
        BlockPos position = BlockPos.containing(player.getEyePosition());
        if (level.getFluidState(position).is(FluidTags.WATER)) {
            return false;
        }
        if (player.onGround()) {
            return true;
        }

        position = position.below();
        if (level.getFluidState(position).is(FluidTags.WATER)) {
            return !level.getFluidState(position.below()).is(FluidTags.WATER);
        }
        position = position.below();
        return level.getFluidState(position).is(FluidTags.WATER)
                && !level.getFluidState(position.below()).is(FluidTags.WATER);
    }

}
