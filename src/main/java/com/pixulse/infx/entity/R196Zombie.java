package com.pixulse.infx.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Zombie-shaped R196 mobs, including the replacement zombie and five new variants. */
public final class R196Zombie extends Zombie implements R196Mob {
    public enum Variant {
        ZOMBIE,
        INVISIBLE_STALKER,
        GHOUL,
        SHADOW,
        WIGHT,
        REVENANT
    }

    public R196Zombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        setCanBreakDoors(true);
        setCanPickUpLoot(true);
        if (variant() == Variant.INVISIBLE_STALKER) {
            setInvisible(true);
        }
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "invisible_stalker" -> Variant.INVISIBLE_STALKER;
            case "ghoul" -> Variant.GHOUL;
            case "shadow" -> Variant.SHADOW;
            case "wight" -> Variant.WIGHT;
            case "revenant" -> Variant.REVENANT;
            default -> Variant.ZOMBIE;
        };
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        AttributeSupplier.Builder builder = Zombie.createAttributes();
        return switch (variant) {
            case ZOMBIE -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.23)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case INVISIBLE_STALKER -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.23)
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
            case GHOUL -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.28)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case SHADOW -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.23)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case WIGHT -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.25)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case REVENANT -> builder
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.26)
                    .add(Attributes.ATTACK_DAMAGE, 7.0);
        };
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (!hurt || !(target instanceof LivingEntity living)) {
            return hurt;
        }

        switch (variant()) {
            case INVISIBLE_STALKER -> living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0), this);
            case GHOUL -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
            case SHADOW -> {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0), this);
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0), this);
            }
            case WIGHT -> {
                living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 1), this);
                if (living instanceof Player player && random.nextFloat() < 0.4F) {
                    player.giveExperiencePoints(-Math.max(20, (player.experienceLevel + 1) * 10));
                }
            }
            case REVENANT, ZOMBIE -> {
            }
        }
        if (living instanceof Animal && !living.isAlive()) {
            heal(Math.min(4.0F, living.getMaxHealth() * 0.25F));
        }
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (variant() == Variant.INVISIBLE_STALKER && !isInvisible()) {
            setInvisible(true);
        }
        if (variant() == Variant.SHADOW && level() instanceof ServerLevel level) {
            if (level.isBrightOutside() && level.canSeeSky(blockPosition())) {
                igniteForSeconds(4.0F);
            } else if (tickCount % 40 == 0 && getHealth() < getMaxHealth()) {
                heal(1.0F);
            }
        }
        if (level() instanceof ServerLevel level && tickCount % 20 == 0) {
            if ((variant() == Variant.INVISIBLE_STALKER || variant() == Variant.SHADOW)
                    && level.getNearestPlayer(this, 4.0) == null) {
                disableNearbyLight(level);
            }
            if (isOnFire() && random.nextFloat() < 0.15F) {
                igniteNearbyBlock(level);
            }
        }
    }

    private void disableNearbyLight(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 3, 1))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.TORCH)
                    || state.is(Blocks.WALL_TORCH)
                    || state.is(Blocks.REDSTONE_TORCH)
                    || state.is(Blocks.REDSTONE_WALL_TORCH)) {
                level.destroyBlock(pos, true, this);
                return;
            }
            if (state.is(Blocks.JACK_O_LANTERN)) {
                BlockState pumpkin = Blocks.CARVED_PUMPKIN.defaultBlockState();
                if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                    pumpkin = pumpkin.setValue(
                            HorizontalDirectionalBlock.FACING, state.getValue(HorizontalDirectionalBlock.FACING));
                }
                level.setBlockAndUpdate(pos, pumpkin);
                spawnAtLocation(level, new ItemStack(Items.TORCH));
                return;
            }
        }
    }

    private void igniteNearbyBlock(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos pos = blockPosition().relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        if (!level.isEmptyBlock(pos)) {
            return;
        }
        BlockState fire = BaseFireBlock.getState(level, pos);
        if (fire.canSurvive(level, pos)) {
            level.setBlockAndUpdate(pos, fire);
        }
    }
}
