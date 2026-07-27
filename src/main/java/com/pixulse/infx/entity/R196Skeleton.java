package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Skeleton replacement plus Longdead and both Bone Lord variants. */
public final class R196Skeleton extends Skeleton implements R196Mob {
    private static final float ARROW_SPEED = 1.8F;
    private static final double ARROW_AIR_DRAG = 0.99F;
    private static final double VANILLA_ARROW_GRAVITY = 0.05D;
    private static final double ARROW_GRAVITY = 0.04D;
    private static final double MIN_INTERCEPT_TICKS = 0.05D;
    private static final double MAX_INTERCEPT_TICKS = 60.0D;
    private static final int INTERCEPT_SEARCH_STEPS = 240;
    private static final int INTERCEPT_REFINEMENT_STEPS = 24;

    public enum Variant {
        SKELETON,
        LONGDEAD,
        BONE_LORD,
        ANCIENT_BONE_LORD
    }

    private int summonedTroops;
    private int inspiredUntil;
    private @Nullable RangedBowAttackGoal<R196Skeleton> bowGoal;

    public R196Skeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
        setCanPickUpLoot(true);
        xpReward = switch (variant()) {
            case SKELETON -> xpReward;
            case LONGDEAD, BONE_LORD -> 15;
            case ANCIENT_BONE_LORD -> 30;
        };
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
                    .add(Attributes.FOLLOW_RANGE, 32.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.30)
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
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
                    .add(Attributes.ATTACK_DAMAGE, 8.0);
        };
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        // MITE skeletons always pick up loot; the vanilla finalizeSpawn re-rolls it at 55%.
        setCanPickUpLoot(true);
        switch (variant()) {
            case SKELETON -> {
                if (random.nextFloat() < 0.25F) {
                    setItemSlot(EquipmentSlot.MAINHAND, equipment(R196Material.WOOD, R196EquipmentType.CLUB));
                }
            }
            // MITE longdead carry an ancient-metal bow or sword at even odds.
            case LONGDEAD -> equip(
                    R196Material.ANCIENT_METAL,
                    false,
                    random.nextBoolean() ? R196EquipmentType.BOW : R196EquipmentType.SWORD);
            case BONE_LORD -> equip(R196Material.RUSTED_IRON, true, lordWeapon(level.getLevel()));
            case ANCIENT_BONE_LORD -> equip(R196Material.ANCIENT_METAL, true, lordWeapon(level.getLevel()));
        }
        if (variant() == Variant.LONGDEAD) {
            // MITE longdead drop their armor pieces at a quarter of the usual chance.
            for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            }) {
                setDropChance(slot, 0.021F);
            }
        }
        reassessWeaponGoal();
        return result;
    }

    /** MITE bone lords weight rusted swords 2, battle axes 1 from day 10, war hammers 1 from day 20. */
    private R196EquipmentType lordWeapon(ServerLevel level) {
        long day = R196MonsterTactics.survivalDay(level);
        int bound = 2 + (day >= 10L ? 1 : 0) + (day >= 20L ? 1 : 0);
        int roll = random.nextInt(bound);
        if (roll <= 1) {
            return R196EquipmentType.SWORD;
        }
        return roll == 2 && day >= 10L ? R196EquipmentType.BATTLE_AXE : R196EquipmentType.WAR_HAMMER;
    }

    private void equip(R196Material material, boolean plateArmor, R196EquipmentType weapon) {
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
            // MITE skeletons fire once every 60 ticks (40 while inspired) out to 30 blocks.
            bowGoal = new RangedBowAttackGoal<>(this, 1.0, 60, 30.0F);
            goalSelector.addGoal(4, bowGoal);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        ItemStack bow = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
        // MITE skeletons loose rusted-iron arrows; longdead loose ancient-metal arrows.
        ItemStack ammunition = equipment(
                variant() == Variant.LONGDEAD ? R196Material.ANCIENT_METAL : R196Material.RUSTED_IRON,
                R196EquipmentType.ARROW);
        AbstractArrow arrow = getArrow(ammunition, power, bow);
        if (bow.getItem() instanceof ProjectileWeaponItem weapon) {
            arrow = weapon.customArrow(arrow, ammunition, bow);
        }

        Vec3 knownMovement = target.getKnownMovement();
        Vec3 physicalMovement = target.getDeltaMovement();
        double targetMotionX = predictionMotion(knownMovement.x, physicalMovement.x);
        double targetMotionZ = predictionMotion(knownMovement.z, physicalMovement.z);
        BallisticAim ballisticAim = calculateBallisticIntercept(
                target.getX() - getX(),
                target.getY(1.0 / 3.0) - arrow.getY(),
                target.getZ() - getZ(),
                targetMotionX,
                targetMotionZ);
        if (level() instanceof ServerLevel level) {
            AbstractArrow launchedArrow = arrow;
            float uncertainty = miteArrowInaccuracy(level.getDifficulty().getId());
            if (ballisticAim != null) {
                Projectile.spawnProjectile(
                        launchedArrow,
                        level,
                        ammunition,
                        projectile -> projectile.shoot(
                                ballisticAim.x(), ballisticAim.y(), ballisticAim.z(), ARROW_SPEED, uncertainty));
            } else {
                spawnMiteFallbackArrow(
                        launchedArrow,
                        level,
                        ammunition,
                        target,
                        knownMovement,
                        physicalMovement,
                        uncertainty);
            }
        }
        playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
    }

    private void spawnMiteFallbackArrow(
            AbstractArrow arrow,
            ServerLevel level,
            ItemStack ammunition,
            LivingEntity target,
            Vec3 knownMovement,
            Vec3 physicalMovement,
            float uncertainty) {
        MiteRangedAim aim = calculateMiteRangedAim(
                getX(),
                getZ(),
                target.getX(),
                target.getZ(),
                knownMovement,
                physicalMovement,
                arrow.getRandom().nextFloat());
        double predictedDistance = Math.sqrt(aim.horizontalDistanceSqr());
        double yd = target.getY(1.0 / 3.0) - arrow.getY();
        double verticalCorrection = miteVerticalCorrection(aim.horizontalDistanceSqr(), target.getY() - getY());
        Projectile.spawnProjectile(
                arrow,
                level,
                ammunition,
                projectile -> {
                    projectile.shoot(aim.x(), yd + predictedDistance * 0.2F, aim.z(), ARROW_SPEED, uncertainty);
                    // EntityArrow applies this after setting its heading; preserve that ordering on fallback.
                    projectile.setDeltaMovement(projectile.getDeltaMovement().add(0.0, verticalCorrection, 0.0));
                });
    }

    /**
     * Finds the low-arc intercept that follows the modern arrow's exact per-tick drag and gravity model.
     * Vertical target motion is deliberately not projected: player input is horizontal and entity vertical
     * movement depends on collision and gravity during flight.
     */
    static @Nullable BallisticAim calculateBallisticIntercept(
            double targetX, double targetY, double targetZ, double targetMotionX, double targetMotionZ) {
        if (!Double.isFinite(targetX)
                || !Double.isFinite(targetY)
                || !Double.isFinite(targetZ)
                || !Double.isFinite(targetMotionX)
                || !Double.isFinite(targetMotionZ)) {
            return null;
        }

        double previousTime = MIN_INTERCEPT_TICKS;
        double previousError = interceptSpeedSqr(
                previousTime, targetX, targetY, targetZ, targetMotionX, targetMotionZ)
                - ARROW_SPEED * ARROW_SPEED;
        for (int step = 1; step <= INTERCEPT_SEARCH_STEPS; step++) {
            double time = MAX_INTERCEPT_TICKS * step / INTERCEPT_SEARCH_STEPS;
            double error = interceptSpeedSqr(time, targetX, targetY, targetZ, targetMotionX, targetMotionZ)
                    - ARROW_SPEED * ARROW_SPEED;
            if (Double.isFinite(error) && previousError > 0.0D && error <= 0.0D) {
                double interceptTime = refineInterceptTime(
                        previousTime, time, targetX, targetY, targetZ, targetMotionX, targetMotionZ);
                return ballisticAimAt(
                        interceptTime, targetX, targetY, targetZ, targetMotionX, targetMotionZ);
            }
            previousTime = time;
            previousError = error;
        }
        return null;
    }

    private static double refineInterceptTime(
            double lower,
            double upper,
            double targetX,
            double targetY,
            double targetZ,
            double targetMotionX,
            double targetMotionZ) {
        for (int step = 0; step < INTERCEPT_REFINEMENT_STEPS; step++) {
            double middle = (lower + upper) * 0.5D;
            double error = interceptSpeedSqr(middle, targetX, targetY, targetZ, targetMotionX, targetMotionZ)
                    - ARROW_SPEED * ARROW_SPEED;
            if (error > 0.0D) {
                lower = middle;
            } else {
                upper = middle;
            }
        }
        return (lower + upper) * 0.5D;
    }

    private static BallisticAim ballisticAimAt(
            double time,
            double targetX,
            double targetY,
            double targetZ,
            double targetMotionX,
            double targetMotionZ) {
        double travelScale = arrowTravelScale(time);
        double verticalDrop = arrowVerticalDrop(time, travelScale);
        return new BallisticAim(
                (targetX + targetMotionX * time) / travelScale,
                (targetY + verticalDrop) / travelScale,
                (targetZ + targetMotionZ * time) / travelScale,
                time);
    }

    private static double interceptSpeedSqr(
            double time, double targetX, double targetY, double targetZ, double targetMotionX, double targetMotionZ) {
        BallisticAim aim = ballisticAimAt(time, targetX, targetY, targetZ, targetMotionX, targetMotionZ);
        return aim.x() * aim.x() + aim.y() * aim.y() + aim.z() * aim.z();
    }

    private static double arrowTravelScale(double time) {
        return (1.0D - Math.pow(ARROW_AIR_DRAG, time)) / (1.0D - ARROW_AIR_DRAG);
    }

    private static double arrowVerticalDrop(double time, double travelScale) {
        return ARROW_GRAVITY / (1.0D - ARROW_AIR_DRAG) * (time - travelScale);
    }

    /** MITE's random horizontal lead, using the player input vector when the server has one. */
    static MiteRangedAim calculateMiteRangedAim(
            double shooterX,
            double shooterZ,
            double targetX,
            double targetZ,
            Vec3 knownMovement,
            Vec3 physicalMovement,
            float randomSample) {
        double currentX = targetX - shooterX;
        double currentZ = targetZ - shooterZ;
        double currentDistanceSqr = currentX * currentX + currentZ * currentZ;
        float leadTicks = miteLeadTicks(currentDistanceSqr, randomSample);
        double xd = targetX + predictionMotion(knownMovement.x, physicalMovement.x) * leadTicks - shooterX;
        double zd = targetZ + predictionMotion(knownMovement.z, physicalMovement.z) * leadTicks - shooterZ;
        return new MiteRangedAim(xd, zd, xd * xd + zd * zd, leadTicks);
    }

    static float miteLeadTicks(double horizontalDistanceSqr, float randomSample) {
        return (float) Math.pow(horizontalDistanceSqr, 0.44D) * (0.5F + randomSample);
    }

    static float miteArrowInaccuracy(int difficultyId) {
        return (14 - difficultyId * 4) * 1.5F;
    }

    static float skeletonArrowSpeed() {
        return ARROW_SPEED;
    }

    static double skeletonArrowGravity() {
        return ARROW_GRAVITY;
    }

    static double skeletonArrowGravityCompensation() {
        return VANILLA_ARROW_GRAVITY - ARROW_GRAVITY;
    }

    static double miteVerticalCorrection(double horizontalDistanceSqr, double targetHeightDifference) {
        double correction = horizontalDistanceSqr * 0.0005D * horizontalDistanceSqr * 0.0005D - 0.05D;
        if (horizontalDistanceSqr > 576.0D) {
            correction += 0.06D;
        }
        if (targetHeightDifference > 5.0D) {
            correction += (targetHeightDifference - 5.0D) * 0.025D * (1.2D - horizontalDistanceSqr * 0.0005D);
        }
        return correction;
    }

    private static double predictionMotion(double knownMovement, double physicalMovement) {
        // Entity#getKnownMovement is the modern public equivalent of MITE's last received player motion.
        return Math.abs(knownMovement) <= 1.0D ? knownMovement : physicalMovement;
    }

    record MiteRangedAim(double x, double z, double horizontalDistanceSqr, float leadTicks) {}

    record BallisticAim(double x, double y, double z, double flightTicks) {}

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof AbstractArrow) {
            if (source.getEntity() instanceof AbstractSkeleton) {
                return false;
            }
            // MITE quarters arrow damage against skeletons regardless of the shooter.
            damage *= 0.25F;
        }
        if (source.isDirect() && source.getEntity() instanceof LivingEntity attacker) {
            var equipment = ModItems.catalog().equipment(attacker.getMainHandItem());
            if (equipment != null
                    && (equipment.key().type() == R196EquipmentType.CUDGEL
                            || equipment.key().type() == R196EquipmentType.CLUB
                            || equipment.key().type() == R196EquipmentType.WAR_HAMMER)) {
                // MITE doubles blunt-weapon damage against the skeleton family.
                damage *= 2.0F;
            }
        }
        return super.hurtServer(level, source, damage);
    }

    /** MITE has no powder-snow conversion; longdead and bone lords must never become strays. */
    @Override
    public void startFreezeConversion(int time) {}

    /** MITE lord inspiration: +50% base melee damage and faster shooting for a short window. */
    public boolean isInspired() {
        return tickCount < inspiredUntil;
    }

    void inspire() {
        inspiredUntil = tickCount + 60;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level) || tickCount % 20 != 0) {
            return;
        }
        if (bowGoal != null) {
            bowGoal.setMinAttackInterval(isInspired() ? 40 : 60);
        }
        if (!isBoneLord()) {
            return;
        }

        var target = getTarget();
        for (AbstractSkeleton skeleton : level.getEntitiesOfClass(
                AbstractSkeleton.class, getBoundingBox().inflate(16.0, 8.0, 16.0))) {
            if (skeleton == this || !hasLineOfSight(skeleton)) {
                continue;
            }
            skeleton.heal(1.0F);
            if (skeleton instanceof R196Skeleton troop) {
                troop.inspire();
            }
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
