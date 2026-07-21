package com.pixulse.infx.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    public R196Spider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        if (variant() == Variant.PHASE) {
            phaseEvasions = random.nextInt(3) + 2;
        }
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
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (!hurt || !(target instanceof LivingEntity living)) {
            return hurt;
        }

        switch (variant()) {
            case CAVE_SPIDER -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 240, 0), this);
            case BLACK_WIDOW -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 960, 0), this);
            case DEMON -> {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 480, 0), this);
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
                living.igniteForSeconds(4.0F);
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
        if (variant() == Variant.PHASE && phaseEvasions > 0 && source.getEntity() != null
                && random.nextFloat() < 0.75F) {
            for (int attempt = 0; attempt < 16; attempt++) {
                double x = getX() + random.nextInt(11) - 5;
                double y = getY() + random.nextInt(9) - 4;
                double z = getZ() + random.nextInt(11) - 5;
                if (randomTeleport(x, y, z, true)) {
                    phaseEvasions--;
                    return false;
                }
            }
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (variant() == Variant.PHASE && phaseEvasions < 4 && tickCount % 100 == 0) {
            phaseEvasions++;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("R196PhaseEvasions", phaseEvasions);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        phaseEvasions = input.getIntOr("R196PhaseEvasions", phaseEvasions);
    }
}
