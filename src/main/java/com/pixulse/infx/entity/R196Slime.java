package com.pixulse.infx.entity;

import com.pixulse.infx.equipment.R196CorrosionRules;
import com.pixulse.infx.equipment.R196CorrosionType;
import com.pixulse.infx.item.R196GelatinousSphereItem;
import com.pixulse.infx.registry.ModItems;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/** Vanilla slime replacement and the four corrosive R196 gelatinous cubes. */
public final class R196Slime extends Slime implements R196Mob {
    private static final double MODERN_BASE_MOVEMENT_SPEED = 0.20;
    private static final double MODERN_MOVEMENT_SPEED_PER_SIZE = 0.10;
    private static final double OOZE_CRAWL_SPEED = 0.07;

    public enum Variant {
        SLIME(1, R196CorrosionType.PEPSIN, 16.0, R196GelatinousSphereItem.Color.GREEN),
        JELLY(2, R196CorrosionType.PEPSIN, 16.0, R196GelatinousSphereItem.Color.OCHRE),
        BLOB(3, R196CorrosionType.PEPSIN, 16.0, R196GelatinousSphereItem.Color.CRIMSON),
        OOZE(3, R196CorrosionType.ACID, 32.0, R196GelatinousSphereItem.Color.GRAY),
        PUDDING(4, R196CorrosionType.ACID, 16.0, R196GelatinousSphereItem.Color.BLACK);

        private final int damageMultiplier;
        private final R196CorrosionType corrosionType;
        private final double followRange;
        private final R196GelatinousSphereItem.Color landingParticleColor;

        Variant(
                int damageMultiplier,
                R196CorrosionType corrosionType,
                double followRange,
                R196GelatinousSphereItem.Color landingParticleColor) {
            this.damageMultiplier = damageMultiplier;
            this.corrosionType = corrosionType;
            this.followRange = followRange;
            this.landingParticleColor = landingParticleColor;
        }

        public int damageMultiplier() {
            return damageMultiplier;
        }

        public R196CorrosionType corrosionType() {
            return corrosionType;
        }

        public double followRange() {
            return followRange;
        }

        public R196GelatinousSphereItem.Color landingParticleColor() {
            return landingParticleColor;
        }
    }

    private final Map<BlockPos, Integer> dissolvingBlocks = new HashMap<>();

    public R196Slime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
        if (usesCrawlAi(variant())) {
            // CubeMobMoveControl only advances by jumping. Oozes use the normal ground
            // controller so they can creep across terrain without inheriting that hop.
            this.moveControl = new MoveControl<>(this);
        }
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
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
        return size * (variant.damageMultiplier() + (variant.corrosionType() == R196CorrosionType.ACID ? 1 : 0));
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
            return;
        }

        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        addTargetingGoals();
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
    protected void setSpawnSize(ServerLevelAccessor level, DifficultyInstance difficulty) {
        setSize(1 << level.getRandom().nextInt(3), true);
    }

    /** MITE cubes damage the animals and villagers they collide with, not only players. */
    @Override
    public void push(Entity entity) {
        super.push(entity);
        if ((entity instanceof Animal || entity instanceof Villager) && isDealsDamage()) {
            dealDamage((LivingEntity) entity);
        }
    }

    @Override
    protected void addTargetingGoals() {
        super.addTargetingGoals();
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
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
            R196CorrosionRules.damageInventory(
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
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
    protected ParticleOptions getParticleType() {
        return new ItemParticleOption(
                ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(gelatinousSphere().getDefaultInstance()));
    }

    private static Item gelatinousSphere(R196GelatinousSphereItem.Color color) {
        return switch (color) {
            case GREEN -> ModItems.GREEN_GELATINOUS_SPHERE.get();
            case OCHRE -> ModItems.OCHRE_GELATINOUS_SPHERE.get();
            case CRIMSON -> ModItems.CRIMSON_GELATINOUS_SPHERE.get();
            case GRAY -> ModItems.GRAY_GELATINOUS_SPHERE.get();
            case BLACK -> ModItems.BLACK_GELATINOUS_SPHERE.get();
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
        if (R196MobDamageRules.hasMagicAspect(source)) {
            return true;
        }
        return variant == Variant.PUDDING
                && (source.is(DamageTypeTags.IS_FIRE)
                        || R196MobDamageRules.hasFireEnchantment(level, R196MobDamageRules.resolveWeapon(source)));
    }
}
