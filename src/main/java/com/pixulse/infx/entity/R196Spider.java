package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModSounds;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Spider replacement and the four R196 spider variants. */
public final class R196Spider extends Spider implements R196Mob {
    public enum Variant {
        SPIDER,
        CAVE_SPIDER,
        BLACK_WIDOW,
        DEMON,
        WOOD,
        PHASE
    }

    private int phaseEvasions;
    private int maxPhaseEvasions;

    public R196Spider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        if (variant() == Variant.PHASE) {
            maxPhaseEvasions = random.nextInt(3) + 2;
            phaseEvasions = maxPhaseEvasions;
        }
        xpReward = switch (variant()) {
            case SPIDER, WOOD -> xpReward;
            case CAVE_SPIDER, PHASE -> 10;
            case DEMON -> 15;
            case BLACK_WIDOW -> 8;
        };
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "r196_cave_spider" -> Variant.CAVE_SPIDER;
            case "black_widow_spider" -> Variant.BLACK_WIDOW;
            case "demon_spider" -> Variant.DEMON;
            case "wood_spider" -> Variant.WOOD;
            case "phase_spider" -> Variant.PHASE;
            default -> Variant.SPIDER;
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return variant() == Variant.DEMON ? ModSounds.DEMON_SPIDER_AMBIENT.get() : super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return variant() == Variant.DEMON ? ModSounds.DEMON_SPIDER_HURT.get() : super.getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return variant() == Variant.DEMON ? ModSounds.DEMON_SPIDER_DEATH.get() : super.getDeathSound();
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        AttributeSupplier.Builder builder = Spider.createAttributes().add(Attributes.FOLLOW_RANGE, 28.0);
        return switch (variant) {
            case SPIDER -> builder
                    .add(Attributes.MAX_HEALTH, 12.0)
                    .add(Attributes.MOVEMENT_SPEED, 1.0)
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
            case CAVE_SPIDER -> builder
                    .add(Attributes.MAX_HEALTH, 16.0)
                    .add(Attributes.MOVEMENT_SPEED, 1.0)
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
            case BLACK_WIDOW -> builder
                    .add(Attributes.MAX_HEALTH, 6.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.80)
                    .add(Attributes.ATTACK_DAMAGE, 1.0);
            case DEMON -> builder
                    .add(Attributes.MAX_HEALTH, 18.0)
                    .add(Attributes.MOVEMENT_SPEED, 1.0)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case WOOD -> builder
                    .add(Attributes.MAX_HEALTH, 6.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.80)
                    .add(Attributes.ATTACK_DAMAGE, 1.0);
            case PHASE -> builder
                    .add(Attributes.MAX_HEALTH, 6.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.80)
                    .add(Attributes.ATTACK_DAMAGE, 3.0);
        };
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        if (variant() != Variant.SPIDER) {
            // MITE: only the base spider turns peaceful in daylight; the variants ignore light
            // both when acquiring targets and when continuing an attack.
            targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal<?>);
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
            goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal);
            goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true) {
                @Override
                public boolean canUse() {
                    return super.canUse() && !mob.isVehicle();
                }
            });
        }
        goalSelector.addGoal(2, new AvoidEntityGoal<>(
                this,
                Player.class,
                player -> getHealth() <= getMaxHealth() * 0.25F,
                8.0F,
                1.0,
                1.4,
                net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Chicken.class, true));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData) {
        if (groupData == null) {
            SpiderEffectsGroupData effects = new SpiderEffectsGroupData();
            if (variant() == Variant.SPIDER
                    && level.getDifficulty() == Difficulty.HARD
                    && random.nextFloat() < 0.1F * difficulty.getSpecialMultiplier()) {
                // MITE buff table: speed 1/2, strength 1/4, regeneration 1/4 — never invisibility.
                int roll = random.nextInt(4);
                effects.effect = roll <= 1
                        ? MobEffects.SPEED
                        : roll == 2 ? MobEffects.STRENGTH : MobEffects.REGENERATION;
            }
            groupData = effects;
        }
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        for (Entity passenger : List.copyOf(getPassengers())) {
            if (!(passenger instanceof AbstractSkeleton)) {
                continue;
            }
            // MITE: variants never carry jockeys; base-spider jockeys are R196 skeletons,
            // or longdead in the Underworld.
            passenger.discard();
            if (variant() == Variant.SPIDER) {
                EntityType<R196Skeleton> jockeyType = level.getLevel().dimension() == Underworld.LEVEL
                        ? ModEntityTypes.LONGDEAD.get()
                        : ModEntityTypes.R196_SKELETON.get();
                R196Skeleton jockey = jockeyType.create(level.getLevel(), EntitySpawnReason.JOCKEY);
                if (jockey != null) {
                    jockey.snapTo(getX(), getY(), getZ(), getYRot(), 0.0F);
                    jockey.finalizeSpawn(level, difficulty, spawnReason, null);
                    jockey.startRiding(this, false, false);
                }
            }
        }
        return data;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (!hurt || !(target instanceof LivingEntity living)) {
            return hurt;
        }

        switch (variant()) {
            case CAVE_SPIDER -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 480, 0), this);
            case BLACK_WIDOW -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 960, 0), this);
            // MITE demon spiders poison and slow but never ignite on melee; their fire comes
            // from burning webs only.
            case DEMON -> {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 480, 0), this);
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
            }
            case WOOD -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 240, 0), this);
            case PHASE, SPIDER -> {
            }
        }
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = getTarget();
        if (target == null) {
            return;
        }

        if (variant() == Variant.PHASE && tickCount % 10 == 0 && random.nextInt(3) == 0
                && distanceToSqr(target) > 9.0) {
            teleportToward(target);
        }

        if ((variant() == Variant.SPIDER || variant() == Variant.CAVE_SPIDER || variant() == Variant.DEMON)
                && tickCount % 80 == 0
                && distanceToSqr(target) >= 9.0
                && distanceToSqr(target) <= 144.0
                && hasLineOfSight(target)) {
            snareTarget(level, target);
        }
    }

    private void snareTarget(ServerLevel level, LivingEntity target) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos pos = target.blockPosition();
        if (level.isEmptyBlock(pos)) {
            level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
            if (variant() == Variant.DEMON) {
                target.igniteForSeconds(6.0F);
            }
        }
    }

    private boolean teleportToward(LivingEntity target) {
        double distance = Math.max(1.0, distanceTo(target));
        double x = getX() + (target.getX() - getX()) / distance * Math.min(4.0, distance - 1.0);
        double z = getZ() + (target.getZ() - getZ()) / distance * Math.min(4.0, distance - 1.0);
        return randomTeleport(x, target.getY(), z, true);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // MITE phase spiders always evade while they have evasions left, jumping at least three
        // blocks sideways and away from the threat, then reacquire a player within 24 blocks.
        if (variant() == Variant.PHASE && phaseEvasions > 0 && source.getEntity() != null) {
            for (int attempt = 0; attempt < 64; attempt++) {
                int dx = random.nextInt(11) - 5;
                int dy = random.nextInt(9) - 4;
                int dz = random.nextInt(11) - 5;
                if (Math.abs(dx) < 3 && Math.abs(dz) < 3) {
                    continue;
                }
                double x = getX() + dx;
                double y = getY() + dy;
                double z = getZ() + dz;
                if (source.getEntity().distanceToSqr(x, y, z) < 9.0) {
                    continue;
                }
                if (randomTeleport(x, y, z, true)) {
                    phaseEvasions--;
                    Player nearest = level.getNearestPlayer(this, 24.0);
                    if (nearest != null) {
                        setTarget(nearest);
                    }
                    return false;
                }
            }
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (variant() == Variant.PHASE && phaseEvasions < maxPhaseEvasions && tickCount % 100 == 0) {
            phaseEvasions++;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("R196PhaseEvasions", phaseEvasions);
        output.putInt("R196PhaseMaxEvasions", maxPhaseEvasions);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        phaseEvasions = input.getIntOr("R196PhaseEvasions", phaseEvasions);
        maxPhaseEvasions = input.getIntOr("R196PhaseMaxEvasions", maxPhaseEvasions);
    }
}
