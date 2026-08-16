package com.pixulse.infx.entity;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Skeleton replacement plus Longdead, Guardian and both Bone Lord variants. */
public final class InfxSkeleton extends Skeleton implements InfxMob {
    public static final double ORDINARY_MAX_HEALTH = 6.0;
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
        LONGDEAD_GUARDIAN,
        BONE_LORD,
        ANCIENT_BONE_LORD
    }

    /** Material and weapon selected for an ordinary skeleton-family spawn. */
    public record OrdinarySkeletonWeapon(InfxMaterial material, EquipmentType type) {}

    private int summonedTroops;
    private int inspiredUntil;
    private int boneRepairCooldownUntil;
    private @Nullable InfxHardCappedBowAttackGoal<InfxSkeleton> bowGoal;

    public InfxSkeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
        setCanPickUpLoot(true);
        xpReward = switch (variant()) {
            case SKELETON -> xpReward;
            case LONGDEAD, BONE_LORD -> 15;
            case LONGDEAD_GUARDIAN, ANCIENT_BONE_LORD -> 30;
        };
    }

    public Variant variant() {
        return switch (EntityVariant.path(this)) {
            case "longdead" -> Variant.LONGDEAD;
            case "longdead_guardian" -> Variant.LONGDEAD_GUARDIAN;
            case "bone_lord" -> Variant.BONE_LORD;
            case "ancient_bone_lord" -> Variant.ANCIENT_BONE_LORD;
            default -> Variant.SKELETON;
        };
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        AttributeSupplier.Builder builder = AbstractSkeleton.createAttributes();
        return switch (variant) {
            case SKELETON -> builder
                    .add(Attributes.MAX_HEALTH, ORDINARY_MAX_HEALTH)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    // The replacement is a 26.1 AbstractSkeleton: its modern baseline is 0.25.
                    .add(Attributes.MOVEMENT_SPEED, 0.25)
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
            case LONGDEAD -> builder
                    .add(Attributes.MAX_HEALTH, 12.0)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.29)
                    .add(Attributes.ATTACK_DAMAGE, 6.0)
                    .add(Attributes.ARMOR, 1.0);
            case LONGDEAD_GUARDIAN -> builder
                    .add(Attributes.MAX_HEALTH, 24.0)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.29)
                    .add(Attributes.ATTACK_DAMAGE, 8.0)
                    .add(Attributes.ARMOR, 2.0);
            case BONE_LORD -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.26)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case ANCIENT_BONE_LORD -> builder
                    .add(Attributes.MAX_HEALTH, 24.0)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.27)
                    .add(Attributes.ATTACK_DAMAGE, 8.0);
        };
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinNewAiReach(this, target);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            @NonNull ServerLevelAccessor level,
            @NonNull DifficultyInstance difficulty,
            @NonNull EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        // InfX skeletons always pick up loot; the vanilla finalizeSpawn re-rolls it at 55%.
        setCanPickUpLoot(true);
        switch (variant()) {
            case SKELETON -> {
                OrdinarySkeletonWeapon weapon =
                        ordinarySpawnWeapon(
                                random.nextFloat(), MonsterTactics.difficultyTension(level.getLevel(), blockPosition()));
                setItemSlot(
                        EquipmentSlot.MAINHAND,
                        equipment(weapon.material(), weapon.type()));
            }
            // InfX longdead carry an ancient-metal bow or sword at even odds; guardians spawn
            // with a bow and swap to a dagger in melee (see swapGuardianWeaponForRange).
            case LONGDEAD -> equip(
                    InfxMaterial.ANCIENT_METAL,
                    false,
                    random.nextBoolean() ? EquipmentType.BOW : EquipmentType.SWORD);
            case LONGDEAD_GUARDIAN -> equip(InfxMaterial.ANCIENT_METAL, false, EquipmentType.BOW);
            case BONE_LORD -> equip(InfxMaterial.RUSTED_IRON, true, lordWeapon(level.getLevel()));
            case ANCIENT_BONE_LORD -> equip(InfxMaterial.ANCIENT_METAL, true, lordWeapon(level.getLevel()));
        }
        if (variant() == Variant.LONGDEAD || variant() == Variant.LONGDEAD_GUARDIAN) {
            // InfX longdead drop their armor pieces at a quarter of the usual chance.
            for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            }) {
                setDropChance(slot, 0.021F);
            }
        }
        reassessWeaponGoal();
        return result;
    }

    /** InfX bone lords weight rusted swords 2, battle axes 1 past low tension, war hammers 1 past medium tension. */
    private EquipmentType lordWeapon(ServerLevel level) {
        float tension = MonsterTactics.difficultyTension(level, blockPosition());
        int bound = 2 + (tension >= 0.15F ? 1 : 0) + (tension >= 0.35F ? 1 : 0);
        int roll = random.nextInt(bound);
        if (roll <= 1) {
            return EquipmentType.SWORD;
        }
        return roll == 2 && tension >= 0.15F ? EquipmentType.BATTLE_AXE : EquipmentType.WAR_HAMMER;
    }

    private void equip(InfxMaterial material, boolean plateArmor, EquipmentType weapon) {
        setItemSlot(EquipmentSlot.MAINHAND, equipment(material, weapon));
        setItemSlot(
                EquipmentSlot.HEAD,
                equipment(material, plateArmor ? EquipmentType.HELMET : EquipmentType.CHAINMAIL_HELMET));
        setItemSlot(
                EquipmentSlot.CHEST,
                equipment(material, plateArmor ? EquipmentType.CHESTPLATE : EquipmentType.CHAINMAIL_CHESTPLATE));
        setItemSlot(
                EquipmentSlot.LEGS,
                equipment(material, plateArmor ? EquipmentType.LEGGINGS : EquipmentType.CHAINMAIL_LEGGINGS));
        setItemSlot(
                EquipmentSlot.FEET,
                equipment(material, plateArmor ? EquipmentType.BOOTS : EquipmentType.CHAINMAIL_BOOTS));
    }

    private static ItemStack equipment(InfxMaterial material, EquipmentType type) {
        return InfXItems.catalog().equipment(material, type).holder().toStack();
    }

    /** Uses the shared 25% melee, 75% ranged spawn split for ordinary skeleton variants. */
    public static OrdinarySkeletonWeapon ordinarySpawnWeapon(float roll, float tension) {
        if (roll >= 0.25F) {
            return new OrdinarySkeletonWeapon(InfxMaterial.WOOD, EquipmentType.BOW);
        }
        if (tension >= 0.3F) {
            return new OrdinarySkeletonWeapon(InfxMaterial.RUSTED_IRON, EquipmentType.SWORD);
        }
        if (tension >= 0.2F) {
            return new OrdinarySkeletonWeapon(InfxMaterial.RUSTED_IRON, EquipmentType.DAGGER);
        }
        if (tension >= 0.1F) {
            return new OrdinarySkeletonWeapon(InfxMaterial.WOOD, EquipmentType.CLUB);
        }
        return new OrdinarySkeletonWeapon(InfxMaterial.WOOD, EquipmentType.CUDGEL);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        // InfX skeletons never spawn with vanilla armor. The plain skeleton defaults to a wooden
        // bow before finalizeSpawn applies its shared current-day profile.
        if (variant() == Variant.SKELETON) {
            setItemSlot(EquipmentSlot.MAINHAND, equipment(InfxMaterial.WOOD, EquipmentType.BOW));
        }
    }

    /** InfX skeletons walk to dropped bones to heal while hurt; combat goals keep their priority. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new MoveToBoneRepairGoal(this));
    }

    @Override
    public void reassessWeaponGoal() {
        super.reassessWeaponGoal();
        goalSelector.getAvailableGoals().stream()
                .filter(candidate -> candidate.getGoal() instanceof InfxHardCappedBowAttackGoal<?>
                        || candidate.getGoal() instanceof RangedBowAttackGoal<?>)
                .map(candidate -> candidate.getGoal())
                .toList()
                .forEach(goalSelector::removeGoal);
        bowGoal = null;
        if (level() != null && !level().isClientSide() && isHolding(stack -> stack.getItem() instanceof BowItem)) {
            // InfX skeletons fire once every 60 ticks (40 while inspired) out to 30 blocks.
            bowGoal = new InfxHardCappedBowAttackGoal<>(
                    this, 1.0, 60, (float) AttackRanges.SKELETON_RANGED_REACH);
            goalSelector.addGoal(4, bowGoal);
        }
    }

    @Override
    public void performRangedAttack(@NonNull LivingEntity target, float power) {
        ItemStack bow = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
        // InfX skeletons loose rusted-iron arrows; longdead loose ancient-metal arrows.
        ItemStack ammunition = equipment(
                variant() == Variant.LONGDEAD || variant() == Variant.LONGDEAD_GUARDIAN
                        ? InfxMaterial.ANCIENT_METAL
                        : InfxMaterial.RUSTED_IRON,
                EquipmentType.ARROW);
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
            float uncertainty = arrowInaccuracy(level.getDifficulty().getId());
            if (ballisticAim != null) {
                Projectile.spawnProjectile(
                        arrow,
                        level,
                        ammunition,
                        projectile -> projectile.shoot(
                                ballisticAim.x(), ballisticAim.y(), ballisticAim.z(), ARROW_SPEED, uncertainty));
            } else {
                spawnFallbackArrow(
                        arrow,
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

    private void spawnFallbackArrow(
            AbstractArrow arrow,
            ServerLevel level,
            ItemStack ammunition,
            LivingEntity target,
            Vec3 knownMovement,
            Vec3 physicalMovement,
            float uncertainty) {
        InfxRangedAim aim = calculateRangedAim(
                getX(),
                getZ(),
                target.getX(),
                target.getZ(),
                knownMovement,
                physicalMovement,
                arrow.getRandom().nextFloat());
        double predictedDistance = Math.sqrt(aim.horizontalDistanceSqr());
        double yd = target.getY(1.0 / 3.0) - arrow.getY();
        double verticalCorrection = verticalCorrection(aim.horizontalDistanceSqr(), target.getY() - getY());
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

    /** InfX's random horizontal lead, using the player input vector when the server has one. */
    static InfxRangedAim calculateRangedAim(
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
        float leadTicks = leadTicks(currentDistanceSqr, randomSample);
        double xd = targetX + predictionMotion(knownMovement.x, physicalMovement.x) * leadTicks - shooterX;
        double zd = targetZ + predictionMotion(knownMovement.z, physicalMovement.z) * leadTicks - shooterZ;
        return new InfxRangedAim(xd, zd, xd * xd + zd * zd, leadTicks);
    }

    static float leadTicks(double horizontalDistanceSqr, float randomSample) {
        return (float) Math.pow(horizontalDistanceSqr, 0.44D) * (0.5F + randomSample);
    }

    static float arrowInaccuracy(int difficultyId) {
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

    static double verticalCorrection(double horizontalDistanceSqr, double targetHeightDifference) {
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
        // Entity#getKnownMovement is the modern public equivalent of InfX's last received player motion.
        return Math.abs(knownMovement) <= 1.0D ? knownMovement : physicalMovement;
    }

    record InfxRangedAim(double x, double z, double horizontalDistanceSqr, float leadTicks) {}

    record BallisticAim(double x, double y, double z, double flightTicks) {}

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypes.CACTUS)) {
            // MITE skeletons are never harmed by cactus.
            return false;
        }
        if (source.getDirectEntity() instanceof AbstractArrow) {
            if (source.getEntity() instanceof AbstractSkeleton) {
                return false;
            }
            // InfX quarters arrow damage against skeletons regardless of the shooter.
            damage *= 0.25F;
        }
        if (source.isDirect() && source.getEntity() instanceof LivingEntity attacker) {
            var equipment = InfXItems.catalog().equipment(attacker.getMainHandItem());
            if (equipment != null
                    && (equipment.key().type() == EquipmentType.CUDGEL
                            || equipment.key().type() == EquipmentType.CLUB
                            || equipment.key().type() == EquipmentType.WAR_HAMMER)) {
                // InfX doubles blunt-weapon damage against the skeleton family.
                damage *= 2.0F;
            }
        }
        return super.hurtServer(level, source, damage);
    }

    /** InfX has no powder-snow conversion; longdead and bone lords must never become strays. */
    @Override
    public void startFreezeConversion(int time) {}

    /** InfX lord inspiration: +50% base melee damage and faster shooting for a short window. */
    public boolean isInspired() {
        return tickCount < inspiredUntil;
    }

    void inspire() {
        inspiredUntil = tickCount + 60;
    }

    /** MITE repair gating: hurt and past the 400-tick pickup cooldown. */
    boolean canRepairFromBone() {
        return getHealth() < getMaxHealth() && tickCount >= boneRepairCooldownUntil;
    }

    /** MITE {@code onRepairItemPickup}: consumes one bone to heal 50% of maximum health. */
    public boolean tryRepairFromBone(ItemStack stack) {
        if (!stack.is(Items.BONE) || !canRepairFromBone()) {
            return false;
        }
        stack.shrink(1);
        heal(getMaxHealth() * 0.5F);
        boneRepairCooldownUntil = tickCount + 400;
        playSound(SoundEvents.ITEM_PICKUP, 0.2F, (random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    getX(),
                    getY() + getBbHeight() * 0.5,
                    getZ(),
                    6,
                    0.4,
                    0.2,
                    0.4,
                    0.0);
        }
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (tickCount % 10 == 0) {
            swapGuardianWeaponForRange();
        }
        if (tickCount % 20 != 0) {
            return;
        }
        if (bowGoal != null) {
            // MITE frenzy: blood-moon and bone-lord inspiration each cut the ranged cooldown by 1.5×.
            int interval = (int) (60 / (MonsterEvents.isBloodMoonFrenzied(level) ? 1.5F : 1.0F)
                    / (isInspired() ? 1.5F : 1.0F));
            bowGoal.setAttackInterval(interval);
        }
        if (!isBoneLord()) {
            return;
        }

        var target = getTarget();
        if (target != null && !MonsterEvents.withinFollowRange(this, target)) {
            target = null;
        }
        for (AbstractSkeleton skeleton : level.getEntitiesOfClass(
                AbstractSkeleton.class, getBoundingBox().inflate(16.0, 8.0, 16.0))) {
            if (skeleton == this
                    || distanceToSqr(skeleton) > 16.0 * 16.0
                    || !hasLineOfSight(skeleton)) {
                continue;
            }
            skeleton.heal(1.0F);
            if (skeleton instanceof InfxSkeleton troop) {
                troop.inspire();
            }
            if (target != null
                    && skeleton.getTarget() == null
                    && MonsterEvents.withinFollowRange(skeleton, target)) {
                skeleton.setTarget(target);
            }
        }

        if (target != null && summonedTroops < 6 && MonsterEvents.withinFollowRange(this, target)
                && random.nextInt(8) < 7 - summonedTroops) {
            summonTroop(level);
        }
    }

    /** MITE longdead guardian: melee dagger inside 5 blocks, bow beyond 6, rechecked every 10 ticks. */
    public void swapGuardianWeaponForRange() {
        if (variant() != Variant.LONGDEAD_GUARDIAN) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null || !hasLineOfSight(target)) {
            return;
        }
        boolean holdingBow = isHolding(stack -> stack.getItem() instanceof BowItem);
        double distance = distanceTo(target);
        EquipmentType desired = holdingBow && distance < 5.0
                ? EquipmentType.DAGGER
                : !holdingBow && distance > 6.0 ? EquipmentType.BOW : null;
        if (desired != null) {
            setItemSlot(EquipmentSlot.MAINHAND, equipment(InfxMaterial.ANCIENT_METAL, desired));
            reassessWeaponGoal();
        }
    }

    private boolean isBoneLord() {
        return variant() == Variant.BONE_LORD || variant() == Variant.ANCIENT_BONE_LORD;
    }

    private void summonTroop(ServerLevel level) {
        EntityType<InfxSkeleton> troopType = variant() == Variant.ANCIENT_BONE_LORD
                ? InfXEntityTypes.LONGDEAD.get()
                : InfXEntityTypes.INFX_SKELETON.get();
        InfxSkeleton troop = troopType.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (troop == null) {
            return;
        }

        double x = getX() + random.nextInt(9) - 4;
        double z = getZ() + random.nextInt(9) - 4;
        troop.snapTo(x, getY(), z, random.nextFloat() * 360.0F, 0.0F);
        troop.finalizeSpawn(level, level.getCurrentDifficultyAt(troop.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        LivingEntity target = getTarget();
        if (target != null && MonsterEvents.withinFollowRange(troop, target)) {
            troop.setTarget(target);
        }
        if (level.noCollision(troop) && level.addFreshEntity(troop)) {
            summonedTroops++;
        }
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("infx.summoned_troops", summonedTroops);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        summonedTroops = input.getIntOr("infx.summoned_troops", 0);
    }
}
