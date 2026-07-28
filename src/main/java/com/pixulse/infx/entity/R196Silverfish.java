package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

/** Explosive, venomous and paralyzing R196 silverfish variants. */
public final class R196Silverfish extends Silverfish implements R196Mob {
    public enum Variant {
        NETHERSPAWN,
        COPPERSPINE,
        HOARY
    }

    private boolean exploded;
    private int nextFizzTick;
    private MiteWakeUpFriendsGoal miteWakeUpFriendsGoal;

    public R196Silverfish(EntityType<? extends Silverfish> type, Level level) {
        super(type, level);
        // MITE silverfish variants are worth double the base experience.
        xpReward = 10;
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "copperspine" -> Variant.COPPERSPINE;
            case "hoary_silverfish" -> Variant.HOARY;
            default -> Variant.NETHERSPAWN;
        };
    }

    public static AttributeSupplier.Builder attributes() {
        // The legacy 0.6 value is consumed by MITE's old-AI throttle.  These variants
        // otherwise match vanilla silverfish movement, so keep the modern 0.25 baseline.
        return Silverfish.createAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return R196AttackRanges.withinOldAiReach(this, target, R196AttackRanges.SILVERFISH_REACH);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // MITE silverfish neither burrow into blocks nor use the modern 50% ally-release roll.
        goalSelector.removeAllGoals(goal -> {
            String name = goal.getClass().getSimpleName();
            return name.equals("SilverfishMergeWithStoneGoal") || name.equals("SilverfishWakeUpFriendsGoal");
        });
        miteWakeUpFriendsGoal = new MiteWakeUpFriendsGoal(this);
        goalSelector.addGoal(3, miteWakeUpFriendsGoal);
        targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal<?>);
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false, (target, level) -> distanceToSqr(target) <= 64.0));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof LivingEntity living) {
            if (variant() == Variant.COPPERSPINE) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 480, 0), this);
            } else if (variant() == Variant.HOARY) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
            }
        }
        return hurt;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (variant() == Variant.NETHERSPAWN && source.getDirectEntity() instanceof Snowball) {
            damage = Math.max(damage, 2.0F);
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt
                && isAlive()
                && miteWakeUpFriendsGoal != null
                && (source.getEntity() != null || source.is(DamageTypes.MAGIC))) {
            miteWakeUpFriendsGoal.notifyHurt();
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // MITE netherspawn fizz while wet every 2-8 ticks, always losing a point in water and
        // one roll in four in rain — roughly one point every five ticks when submerged.
        if (variant() == Variant.NETHERSPAWN && level() instanceof ServerLevel level
                && isInWaterOrRain() && tickCount >= nextFizzTick) {
            nextFizzTick = tickCount + 2 + random.nextInt(7);
            playSound(SoundEvents.FIRE_EXTINGUISH, 0.7F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
            if (isInWater() || random.nextInt(4) == 0) {
                hurtServer(level, damageSources().drown(), 1.0F);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!exploded && variant() == Variant.NETHERSPAWN && level() instanceof ServerLevel level
                && !isInWaterOrRain()
                && !isInLava()
                && !source.is(DamageTypeTags.IS_DROWNING)
                && !(source.getDirectEntity() instanceof Snowball)) {
            exploded = true;
            level.explode(
                    this,
                    getX(),
                    getY() + getBbHeight() * 0.25,
                    getZ(),
                    1.0F,
                    true,
                    Level.ExplosionInteraction.MOB);
        }
        super.die(source);
    }

    static boolean isNetherspawnExplosionProtected(BlockState state) {
        return state.is(Blocks.NETHERRACK)
                || state.is(Blocks.NETHER_QUARTZ_ORE)
                || state.is(Blocks.NETHER_GOLD_ORE)
                || state.is(Blocks.GOLD_ORE)
                || state.is(Blocks.DEEPSLATE_GOLD_ORE);
    }

    private static final class MiteWakeUpFriendsGoal extends Goal {
        private final R196Silverfish silverfish;
        private int ticksUntilWake;

        private MiteWakeUpFriendsGoal(R196Silverfish silverfish) {
            this.silverfish = silverfish;
        }

        private void notifyHurt() {
            if (ticksUntilWake == 0) {
                ticksUntilWake = adjustedTickDelay(20);
            }
        }

        @Override
        public boolean canUse() {
            return ticksUntilWake > 0;
        }

        @Override
        public void tick() {
            if (--ticksUntilWake > 0 || !(silverfish.level() instanceof ServerLevel level)) {
                return;
            }

            BlockPos origin = silverfish.blockPosition();
            for (int yOffset = 0; yOffset <= 5 && yOffset >= -5; yOffset = (yOffset <= 0 ? 1 : 0) - yOffset) {
                for (int xOffset = 0; xOffset <= 10 && xOffset >= -10; xOffset = (xOffset <= 0 ? 1 : 0) - xOffset) {
                    for (int zOffset = 0; zOffset <= 10 && zOffset >= -10; zOffset = (zOffset <= 0 ? 1 : 0) - zOffset) {
                        BlockPos pos = origin.offset(xOffset, yOffset, zOffset);
                        BlockState state = level.getBlockState(pos);
                        if (!(state.getBlock() instanceof InfestedBlock infestedBlock)) {
                            continue;
                        }
                        if (level.getGameRules().get(GameRules.MOB_GRIEFING)) {
                            level.destroyBlock(pos, true, silverfish);
                        } else {
                            level.setBlock(pos, infestedBlock.hostStateByInfested(state), 3);
                        }
                        if (silverfish.getRandom().nextInt(4) == 0) {
                            return;
                        }
                    }
                }
            }
        }
    }
}
