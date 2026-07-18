package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Skeleton replacement plus Longdead and both Bone Lord variants. */
public final class R196Skeleton extends Skeleton implements R196Mob {
    public enum Variant {
        SKELETON,
        LONGDEAD,
        BONE_LORD,
        ANCIENT_BONE_LORD
    }

    private int summonedTroops;

    public R196Skeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
        setCanPickUpLoot(true);
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "longdead" -> Variant.LONGDEAD;
            case "bone_lord" -> Variant.BONE_LORD;
            case "ancient_bone_lord" -> Variant.ANCIENT_BONE_LORD;
            default -> Variant.SKELETON;
        };
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        AttributeSupplier.Builder builder = AbstractSkeleton.createAttributes();
        return switch (variant) {
            case SKELETON -> builder
                    .add(Attributes.MAX_HEALTH, 6.0)
                    .add(Attributes.FOLLOW_RANGE, 100.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.30)
                    .add(Attributes.ATTACK_DAMAGE, 3.0);
            case LONGDEAD -> builder
                    .add(Attributes.MAX_HEALTH, 12.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.29)
                    .add(Attributes.ATTACK_DAMAGE, 6.0)
                    .add(Attributes.ARMOR, 1.0);
            case BONE_LORD -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.26)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case ANCIENT_BONE_LORD -> builder
                    .add(Attributes.MAX_HEALTH, 24.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.27)
                    .add(Attributes.ATTACK_DAMAGE, 8.0)
                    .add(Attributes.ARMOR, 2.0);
        };
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        switch (variant()) {
            case SKELETON -> {
                if (random.nextFloat() < 0.25F) {
                    setItemSlot(EquipmentSlot.MAINHAND, equipment(R196Material.WOOD, R196EquipmentType.CLUB));
                }
            }
            case LONGDEAD -> equip(R196Material.ANCIENT_METAL, false);
            case BONE_LORD -> equip(R196Material.RUSTED_IRON, true);
            case ANCIENT_BONE_LORD -> equip(R196Material.ANCIENT_METAL, true);
        }
        reassessWeaponGoal();
        return result;
    }

    private void equip(R196Material material, boolean plateArmor) {
        R196EquipmentType weapon = random.nextBoolean()
                ? R196EquipmentType.SWORD
                : R196EquipmentType.WAR_HAMMER;
        setItemSlot(EquipmentSlot.MAINHAND, equipment(material, weapon));
        setItemSlot(
                EquipmentSlot.HEAD,
                equipment(material, plateArmor ? R196EquipmentType.HELMET : R196EquipmentType.CHAINMAIL_HELMET));
        setItemSlot(
                EquipmentSlot.CHEST,
                equipment(material, plateArmor ? R196EquipmentType.CHESTPLATE : R196EquipmentType.CHAINMAIL_CHESTPLATE));
        setItemSlot(
                EquipmentSlot.LEGS,
                equipment(material, plateArmor ? R196EquipmentType.LEGGINGS : R196EquipmentType.CHAINMAIL_LEGGINGS));
        setItemSlot(
                EquipmentSlot.FEET,
                equipment(material, plateArmor ? R196EquipmentType.BOOTS : R196EquipmentType.CHAINMAIL_BOOTS));
    }

    private static ItemStack equipment(R196Material material, R196EquipmentType type) {
        return ModItems.catalog().equipment(material, type).holder().toStack();
    }

    @Override
    public void reassessWeaponGoal() {
        super.reassessWeaponGoal();
        if (level() != null && !level().isClientSide() && isHolding(stack -> stack.getItem() instanceof BowItem)) {
            goalSelector.removeAllGoals(goal -> goal instanceof RangedBowAttackGoal<?>);
            goalSelector.addGoal(4, new RangedBowAttackGoal<>(this, 1.0, 20, 100.0F));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        ItemStack bow = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
        ItemStack ammunition = getProjectile(bow);
        AbstractArrow arrow = getArrow(ammunition, power, bow);
        if (bow.getItem() instanceof ProjectileWeaponItem weapon) {
            arrow = weapon.customArrow(arrow, ammunition, bow);
        }

        double currentX = target.getX() - getX();
        double currentZ = target.getZ() - getZ();
        double horizontalDistance = Math.sqrt(currentX * currentX + currentZ * currentZ);
        double flightTicks = Math.min(60.0, horizontalDistance / 1.6);
        double xd = target.getX() + target.getDeltaMovement().x * flightTicks - getX();
        double zd = target.getZ() + target.getDeltaMovement().z * flightTicks - getZ();
        double predictedDistance = Math.sqrt(xd * xd + zd * zd);
        double yd = target.getY(1.0 / 3.0) + target.getDeltaMovement().y * Math.min(10.0, flightTicks) - arrow.getY();
        if (level() instanceof ServerLevel level) {
            Projectile.spawnProjectileUsingShoot(
                    arrow,
                    level,
                    ammunition,
                    xd,
                    yd + predictedDistance * 0.2F,
                    zd,
                    1.6F,
                    14 - level.getDifficulty().getId() * 4);
        }
        playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof AbstractArrow) {
            if (source.getEntity() instanceof AbstractSkeleton) {
                return false;
            }
            if (source.getEntity() instanceof Player) {
                damage *= 0.5F;
            }
        }
        if (source.isDirect() && source.getEntity() instanceof LivingEntity attacker) {
            var equipment = ModItems.catalog().equipment(attacker.getMainHandItem());
            if (equipment != null
                    && (equipment.key().type() == R196EquipmentType.CUDGEL
                            || equipment.key().type() == R196EquipmentType.CLUB
                            || equipment.key().type() == R196EquipmentType.WAR_HAMMER)) {
                damage *= 1.5F;
            }
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level) || tickCount % 20 != 0 || !isBoneLord()) {
            return;
        }

        var target = getTarget();
        for (AbstractSkeleton skeleton : level.getEntitiesOfClass(
                AbstractSkeleton.class, getBoundingBox().inflate(16.0, 8.0, 16.0))) {
            if (skeleton == this || !hasLineOfSight(skeleton)) {
                continue;
            }
            skeleton.heal(1.0F);
            skeleton.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 0), this);
            if (target != null && skeleton.getTarget() == null) {
                skeleton.setTarget(target);
            }
        }

        if (target != null && summonedTroops < 6 && distanceToSqr(target) <= 256.0
                && random.nextInt(8) < 7 - summonedTroops) {
            summonTroop(level);
        }
    }

    private boolean isBoneLord() {
        return variant() == Variant.BONE_LORD || variant() == Variant.ANCIENT_BONE_LORD;
    }

    private void summonTroop(ServerLevel level) {
        EntityType<R196Skeleton> troopType = variant() == Variant.ANCIENT_BONE_LORD
                ? ModEntityTypes.LONGDEAD.get()
                : ModEntityTypes.R196_SKELETON.get();
        R196Skeleton troop = troopType.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (troop == null) {
            return;
        }

        double x = getX() + random.nextInt(9) - 4;
        double z = getZ() + random.nextInt(9) - 4;
        troop.snapTo(x, getY(), z, random.nextFloat() * 360.0F, 0.0F);
        troop.finalizeSpawn(level, level.getCurrentDifficultyAt(troop.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        troop.setTarget(getTarget());
        if (level.noCollision(troop) && level.addFreshEntity(troop)) {
            summonedTroops++;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("R196SummonedTroops", summonedTroops);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        summonedTroops = input.getIntOr("R196SummonedTroops", 0);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if ((variant() == Variant.LONGDEAD || variant() == Variant.ANCIENT_BONE_LORD)
                && random.nextFloat() < (killedByPlayer ? 0.50F : 0.25F)) {
            spawnAtLocation(level, ModItems.ANCIENT_METAL_INGOT.toStack());
        }
    }
}
