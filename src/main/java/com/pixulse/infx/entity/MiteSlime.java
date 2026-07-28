package com.pixulse.infx.entity;

import com.pixulse.infx.item.equipment.CorrosionRules;
import com.pixulse.infx.item.equipment.CorrosionType;
import com.pixulse.infx.item.GelatinousSphereItem;
import com.pixulse.infx.registry.InfXItems;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.NonNull;

/** Vanilla slime replacement and the four corrosive R196 gelatinous cubes. */
public final class MiteSlime extends Slime implements MiteMob {
    private static final double MODERN_BASE_MOVEMENT_SPEED = 0.20;
    private static final double MODERN_MOVEMENT_SPEED_PER_SIZE = 0.10;
    private static final double OOZE_CRAWL_SPEED = 0.05;

    public enum Variant {
        SLIME(1, CorrosionType.PEPSIN, 16.0, GelatinousSphereItem.Color.GREEN),
        JELLY(2, CorrosionType.PEPSIN, 16.0, GelatinousSphereItem.Color.OCHRE),
        BLOB(3, CorrosionType.PEPSIN, 16.0, GelatinousSphereItem.Color.CRIMSON),
        OOZE(3, CorrosionType.ACID, 32.0, GelatinousSphereItem.Color.GRAY),
        PUDDING(4, CorrosionType.ACID, 16.0, GelatinousSphereItem.Color.BLACK);

        private final int damageMultiplier;
        private final CorrosionType corrosionType;
        private final double followRange;
        private final GelatinousSphereItem.Color landingParticleColor;

        Variant(
                int damageMultiplier,
                CorrosionType corrosionType,
                double followRange,
                GelatinousSphereItem.Color landingParticleColor) {
            this.damageMultiplier = damageMultiplier;
            this.corrosionType = corrosionType;
            this.followRange = followRange;
            this.landingParticleColor = landingParticleColor;
        }

        public int damageMultiplier() {
            return damageMultiplier;
        }

        public CorrosionType corrosionType() {
            return corrosionType;
        }

        public double followRange() {
            return followRange;
        }

        public GelatinousSphereItem.Color landingParticleColor() {
            return landingParticleColor;
        }
    }

    private final Map<BlockPos, Integer> dissolvingBlocks = new HashMap<>();

    public MiteSlime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
        if (usesCrawlAi(variant())) {
            // CubeMobMoveControl only advances by jumping. Oozes use the normal ground
            // controller so they can creep across terrain without inheriting that hop.
            this.moveControl = new MoveControl(this);
        }
    }

    public Variant variant() {
        return switch (EntityVariant.path(this)) {
            case "jelly" -> Variant.JELLY;
            case "blob" -> Variant.BLOB;
            case "ooze" -> Variant.OOZE;
            case "pudding" -> Variant.PUDDING;
            default -> Variant.SLIME;
        };
    }

    public static AttributeSupplier.Builder attributes() {
        return attributes(Variant.SLIME);
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, variant.followRange())
                .add(Attributes.MOVEMENT_SPEED, movementSpeedFor(variant, 1));
    }

    static double attackDamageForSize(Variant variant, int size) {
        return size * variant.damageMultiplier();
    }

    static int experienceForSize(Variant variant, int size) {
        return size * (variant.damageMultiplier() + (variant.corrosionType() == CorrosionType.ACID ? 1 : 0));
    }

    static double movementSpeedForSize(int size) {
        // MITE's old cube AI used a fixed 0.1 movement throttle; the inherited 0.7
        // attribute was not a modern-speed value.  Keep 26.2's cube size curve.
        return MODERN_BASE_MOVEMENT_SPEED + MODERN_MOVEMENT_SPEED_PER_SIZE * Math.max(1, size);
    }

    static double movementSpeedFor(Variant variant, int size) {
        return usesCrawlAi(variant) ? OOZE_CRAWL_SPEED : movementSpeedForSize(size);
    }

    static boolean usesCrawlAi(Variant variant) {
        return variant == Variant.OOZE;
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        if (variant() == Variant.OOZE) {
            size = Math.min(size, 2);
        }
        super.setSize(size, updateHealth);
        var movementSpeed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(movementSpeedFor(variant(), getSize()));
        }
        var attackDamage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamageForSize(variant(), getSize()));
        }
        this.xpReward = experienceForSize(variant(), getSize());
    }

    @Override
    protected boolean isDealsDamage() {
        return isEffectiveAi();
    }

    /** The acid ooze crawls with ground navigation instead of the bouncing cube goals. */
    @Override
    protected void registerGoals() {
        if (!usesCrawlAi(variant())) {
            super.registerGoals();
            addR196TargetingGoals();
            return;
        }

        goalSelector.addGoal(0, new FloatGoal(this));
        // Keep the crawl goal for pursuit only. MITE ooze damage is dispatched by the
        // slime collision callback, never by a hitbox-expanded melee-goal check.
        goalSelector.addGoal(2, new OozePursuitGoal(this));
        goalSelector.addGoal(5, new OozeRandomMovementGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        addR196TargetingGoals();
    }

    /** Suppress any fallback jump request from ground navigation while the ooze crawls. */
    @Override
    public void jumpFromGround() {
        if (!usesCrawlAi(variant())) {
            super.jumpFromGround();
        }
    }

    /** MITE cubes never climb ladders; only the ooze creeps up walls it presses against. */
    @Override
    public boolean onClimbable() {
        return variant() == Variant.OOZE && horizontalCollision;
    }

    /** MITE jump cadence: 40-120 ticks at rest, an effective 10 while chasing (30 / 3). */
    @Override
    protected int getJumpDelay() {
        return getTarget() != null ? 30 : random.nextInt(81) + 40;
    }

    /** MITE spawn sizes are uniform 1/2/4 with no difficulty bias. */
    @Override
    public @org.jspecify.annotations.Nullable SpawnGroupData finalizeSpawn(
            @NonNull ServerLevelAccessor level,
            @NonNull DifficultyInstance difficulty,
            @NonNull EntitySpawnReason spawnReason,
            @org.jspecify.annotations.Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        // Unlike vanilla's difficulty-biased size roll, MITE uses a uniform 1/2/4 choice.
        setSize(1 << level.getRandom().nextInt(3), true);
        return result;
    }

    /** MITE cubes damage the animals and villagers they collide with, not only players. */
    @Override
    public void push(@NonNull Entity entity) {
        super.push(entity);
        if ((entity instanceof Animal || entity instanceof Villager) && isDealsDamage()) {
            dealDamage((LivingEntity) entity);
        }
    }

    private void addR196TargetingGoals() {
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        if (usesCrawlAi(variant())) {
            // Recreate the vanilla Slime targets because the ooze uses its own ground goals.
            this.targetSelector.addGoal(
                    1,
                    new NearestAttackableTargetGoal<>(
                            this,
                            Player.class,
                            10,
                            true,
                            false,
                            (target, level) -> Math.abs(target.getY() - this.getY()) <= 4.0));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        }
    }

    /** Ground pursuit used by the acid ooze in 26.1.2, where MeleeAttackGoal requires PathfinderMob. */
    private static final class OozePursuitGoal extends Goal {
        private final MiteSlime slime;

        private OozePursuitGoal(MiteSlime slime) {
            this.slime = slime;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = slime.getTarget();
            return target != null && target.isAlive() && slime.canAttack(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = slime.getTarget();
            return target != null && target.isAlive() && slime.canAttack(target);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = slime.getTarget();
            if (target != null) {
                slime.lookAt(target, 10.0F, 10.0F);
                slime.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
            }
        }

        @Override
        public void stop() {
            slime.getMoveControl().setWait();
        }
    }

    /** Lightweight replacement for WaterAvoidingRandomStrollGoal, which also requires PathfinderMob. */
    private static final class OozeRandomMovementGoal extends Goal {
        private final MiteSlime slime;
        private double wantedX;
        private double wantedZ;
        private int time;

        private OozeRandomMovementGoal(MiteSlime slime) {
            this.slime = slime;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return slime.getTarget() == null
                    && !slime.isPassenger()
                    && (slime.onGround() || slime.isInWater() || slime.isInLava())
                    && slime.getRandom().nextInt(40) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return slime.getTarget() == null && time > 0
                    && slime.distanceToSqr(wantedX, slime.getY(), wantedZ) > 1.0D;
        }

        @Override
        public void start() {
            double angle = slime.getRandom().nextDouble() * (Math.PI * 2.0D);
            double distance = 4.0D + slime.getRandom().nextDouble() * 4.0D;
            wantedX = slime.getX() + Math.cos(angle) * distance;
            wantedZ = slime.getZ() + Math.sin(angle) * distance;
            time = 40 + slime.getRandom().nextInt(40);
        }

        @Override
        public void tick() {
            slime.getMoveControl().setWantedPosition(wantedX, slime.getY(), wantedZ, 1.0D);
            time--;
        }

        @Override
        public void stop() {
            slime.getMoveControl().setWait();
        }
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        float health = target.getHealth();
        super.dealDamage(target);
        if (target.getHealth() >= health) {
            return;
        }
        Variant variant = variant();
        if (target instanceof net.minecraft.server.level.ServerPlayer player) {
            CorrosionRules.damageInventory(
                    player,
                    variant.corrosionType(),
                    0.05F * getSize(),
                    (float) attackDamageForSize(variant, getSize()));
        }
        if (variant == Variant.BLOB) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
        }
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        if (!acceptsDamage(level, source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    public Item gelatinousSphere() {
        return gelatinousSphere(variant().landingParticleColor());
    }

    /** Modern equivalent of MITE's five colored slime-ball squish particles. */
    @Override
    protected @NonNull ParticleOptions getParticleType() {
        return new ItemParticleOption(
                ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(gelatinousSphere().getDefaultInstance()));
    }

    private static Item gelatinousSphere(GelatinousSphereItem.Color color) {
        return switch (color) {
            case GREEN -> InfXItems.GREEN_GELATINOUS_SPHERE.get();
            case OCHRE -> InfXItems.OCHRE_GELATINOUS_SPHERE.get();
            case CRIMSON -> InfXItems.CRIMSON_GELATINOUS_SPHERE.get();
            case GRAY -> InfXItems.GRAY_GELATINOUS_SPHERE.get();
            case BLACK -> InfXItems.BLACK_GELATINOUS_SPHERE.get();
        };
    }

    boolean advanceDissolvingBlock(BlockPos pos, int period) {
        int progress = dissolvingBlocks.merge(pos.immutable(), getSize() * 20, Integer::sum);
        if (progress < period) {
            return false;
        }
        dissolvingBlocks.remove(pos);
        return true;
    }

    void clearDissolvingBlock(BlockPos pos) {
        dissolvingBlocks.remove(pos);
    }

    private boolean acceptsDamage(ServerLevel level, DamageSource source) {
        Variant variant = variant();
        if (variant != Variant.OOZE && variant != Variant.PUDDING) {
            return true;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                || source.getDirectEntity() instanceof Snowball) {
            return true;
        }
        // MITE counts every enchanted weapon (melee or bow) as magic, the main way to kill these.
        if (MobDamageRules.hasMagicAspect(source)) {
            return true;
        }
        return variant == Variant.PUDDING
                && (source.is(DamageTypeTags.IS_FIRE)
                        || MobDamageRules.hasFireEnchantment(level, MobDamageRules.resolveWeapon(source)));
    }
}
