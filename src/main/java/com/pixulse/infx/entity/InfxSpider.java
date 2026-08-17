package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXSounds;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Spider replacement and the four INFX spider variants. */
public final class InfxSpider extends Spider implements InfxMob {
    private static final double MODERN_SPIDER_MOVEMENT_SPEED = 0.30;
    private static final double DEMON_SPIDER_SPEED_MULTIPLIER = 1.25;
    private static final int WEB_TARGET_LEAD_TICKS = 10;

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
    private int websRemaining;

    public InfxSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        websRemaining = variant() == Variant.PHASE ? 0 : initialWebCount(variant(), random.nextInt(4));
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
        return switch (EntityVariant.path(this)) {
            case "infx_cave_spider" -> Variant.CAVE_SPIDER;
            case "black_widow_spider" -> Variant.BLACK_WIDOW;
            case "demon_spider" -> Variant.DEMON;
            case "wood_spider" -> Variant.WOOD;
            case "phase_spider" -> Variant.PHASE;
            default -> Variant.SPIDER;
        };
    }

    @Override
    protected @NonNull SoundEvent getAmbientSound() {
        return variant() == Variant.DEMON ? InfXSounds.DEMON_SPIDER_AMBIENT.get() : super.getAmbientSound();
    }

    @Override
    protected @NonNull SoundEvent getHurtSound(@NonNull DamageSource source) {
        return variant() == Variant.DEMON ? InfXSounds.DEMON_SPIDER_HURT.get() : super.getHurtSound(source);
    }

    @Override
    protected @NonNull SoundEvent getDeathSound() {
        return variant() == Variant.DEMON ? InfXSounds.DEMON_SPIDER_DEATH.get() : super.getDeathSound();
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        // The replacement and cave spider are vanilla 26.1 entities and therefore use the
        // modern 0.30 attribute; only the separate demon spider keeps its explicit boost.
        AttributeSupplier.Builder builder = Spider.createAttributes().add(Attributes.FOLLOW_RANGE, 16.0);
        return switch (variant) {
            case SPIDER -> builder
                    .add(Attributes.MAX_HEALTH, 12.0)
                    .add(Attributes.MOVEMENT_SPEED, movementSpeed(variant))
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
            case CAVE_SPIDER -> builder
                    .add(Attributes.MAX_HEALTH, 16.0)
                    .add(Attributes.MOVEMENT_SPEED, movementSpeed(variant))
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
            case BLACK_WIDOW, WOOD -> builder
                    .add(Attributes.MAX_HEALTH, 6.0)
                    .add(Attributes.MOVEMENT_SPEED, movementSpeed(variant))
                    .add(Attributes.ATTACK_DAMAGE, 1.0);
            case DEMON -> builder
                    .add(Attributes.MAX_HEALTH, 18.0)
                    .add(Attributes.MOVEMENT_SPEED, movementSpeed(variant))
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case PHASE -> builder
                    .add(Attributes.MAX_HEALTH, 6.0)
                    .add(Attributes.MOVEMENT_SPEED, movementSpeed(variant))
                    .add(Attributes.ATTACK_DAMAGE, 3.0);
        };
    }

    static double movementSpeed(Variant variant) {
        return switch (variant) {
            // Spider and Cave Spider replace the vanilla 26.1 entities and must retain their
            // 0.30 baseline. Demon Spider is a separate InfX variant and keeps its explicit
            // 25% custom boost.
            case SPIDER, CAVE_SPIDER -> MODERN_SPIDER_MOVEMENT_SPEED;
            case DEMON -> MODERN_SPIDER_MOVEMENT_SPEED * DEMON_SPIDER_SPEED_MULTIPLIER;
            case BLACK_WIDOW, WOOD, PHASE -> MODERN_SPIDER_MOVEMENT_SPEED;
        };
    }

    /** InfX arachnids have a finite 0-3 web stock; phase spiders do not carry webs. */
    static int initialWebCount(Variant variant, int randomRoll) {
        if (variant == Variant.PHASE) {
            return 0;
        }
        int webs = Mth.clamp(randomRoll, 0, 3);
        return webs > 0 && variant != Variant.CAVE_SPIDER && variant != Variant.DEMON ? webs - 1 : webs;
    }

    static int webThrowInterval(Variant variant) {
        return variant == Variant.CAVE_SPIDER || variant == Variant.DEMON ? 200 : 500;
    }

    static boolean shouldThrowWebAtTick(Variant variant, int tickCount, int entityId) {
        return Math.floorMod(tickCount + entityId * 47, webThrowInterval(variant)) == 0;
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinOldAiReach(this, target, AttackRanges.OLD_AI_REACH);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof LeapAtTargetGoal);
        goalSelector.addGoal(3, new InfxArachnidLeapGoal(this));
        if (variant() != Variant.SPIDER) {
            // InfX: only the base spider turns peaceful in daylight; the variants ignore light
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
            @NonNull ServerLevelAccessor level,
            @NonNull DifficultyInstance difficulty,
            @NonNull EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData) {
        if (groupData == null) {
            SpiderEffectsGroupData effects = new SpiderEffectsGroupData();
            if (variant() == Variant.SPIDER
                    && level.getDifficulty() == Difficulty.HARD
                    && random.nextFloat() < 0.10F * MonsterTactics.difficultyTension(
                            level.getLevel(), blockPosition())) {
                // InfX buff table: speed 1/2, strength 1/4, regeneration 1/4 — never invisibility.
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
            // InfX: variants never carry jockeys; base-spider jockeys are INFX skeletons,
            // or longdead in the Underworld.
            passenger.discard();
            if (variant() == Variant.SPIDER) {
                EntityType<InfxSkeleton> jockeyType = level.getLevel().dimension() == Underworld.LEVEL
                        ? InfXEntityTypes.LONGDEAD.get()
                        : InfXEntityTypes.INFX_SKELETON.get();
                InfxSkeleton jockey = jockeyType.create(level.getLevel(), EntitySpawnReason.JOCKEY);
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
    public boolean doHurtTarget(@NonNull ServerLevel level, @NonNull Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (!hurt || !(target instanceof LivingEntity living)) {
            return hurt;
        }

        switch (variant()) {
            case CAVE_SPIDER -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 480, 0), this);
            case BLACK_WIDOW -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 960, 0), this);
            // InfX demon spiders poison and slow but never ignite on melee; their fire comes
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
    protected void customServerAiStep(@NonNull ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = getTarget();
        if (target == null || !MonsterEvents.withinFollowRange(this, target)) {
            return;
        }

        if (variant() == Variant.PHASE && tickCount % 10 == 0 && random.nextInt(3) == 0 && distanceToSqr(target) > 9.0) {
            teleportAlongPath();
        }

        if (websRemaining > 0
                && shouldThrowWebAtTick(variant(), tickCount, getId())
                && distanceToSqr(target) <= 64.0
                && canHitTargetWithWeb(level, target)
                && throwWeb(level, target)) {
            websRemaining--;
        }
    }

    /** MITE first checks the eye ray, then the target's lower body when the eye ray is blocked. */
    private boolean canHitTargetWithWeb(ServerLevel level, LivingEntity target) {
        Vec3 origin = getEyePosition();
        Vec3 endpoint = target.getEyePosition();
        if (isBlocked(level, origin, endpoint)) {
            endpoint = target.position().add(0.0D, target.getBbHeight() * 0.25D, 0.0D);
            if (isBlocked(level, origin, endpoint)) {
                return false;
            }
        }
        AABB searchArea = getBoundingBox().expandTowards(endpoint.subtract(origin)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                level,
                this,
                origin,
                endpoint,
                searchArea,
                candidate -> candidate.isAlive() && EntitySelector.CAN_BE_PICKED.test(candidate),
                0.0F);
        return hit != null && hit.getEntity() == target;
    }

    private boolean isBlocked(ServerLevel level, Vec3 origin, Vec3 endpoint) {
        return level.clip(new ClipContext(origin, endpoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
                        .getType()
                == HitResult.Type.BLOCK;
    }

    private boolean throwWeb(ServerLevel level, LivingEntity target) {
        InfxWebProjectile web = new InfxWebProjectile(level, this, variant() == Variant.DEMON || isOnFire());
        Vec3 movement = target.getKnownMovement();
        double x = target.getX() + movement.x * WEB_TARGET_LEAD_TICKS - web.getX();
        double y = target.getEyeY() - web.getY();
        double z = target.getZ() + movement.z * WEB_TARGET_LEAD_TICKS - web.getZ();
        double horizontalLead = Math.sqrt(x * x + z * z) * 0.2D;
        web.shoot(x, y + horizontalLead, z, 0.8F, 0.0F);
        if (!level.addFreshEntity(web)) {
            return false;
        }
        playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
        return true;
    }

    private boolean teleportAlongPath() {
        Path path = getNavigation().getPath();
        if (path == null || path.isDone()) {
            return false;
        }
        int remaining = path.getNodeCount() - path.getNextNodeIndex();
        int advancement = phasePathAdvancement(remaining, random.nextInt(Math.max(1, remaining)));
        if (advancement == 0) {
            return false;
        }
        int targetIndex = Math.min(path.getNodeCount() - 1, path.getNextNodeIndex() + advancement);
        BlockPos node = path.getNodePos(targetIndex);
        if (Vec3.atBottomCenterOf(node).distanceToSqr(position()) <= 3.0D
                || !randomTeleport(node.getX() + 0.5D, node.getY(), node.getZ() + 0.5D, true)) {
            return false;
        }
        path.setNextNodeIndex(Math.min(path.getNodeCount(), path.getNextNodeIndex() + advancement - 1));
        return true;
    }

    static int phasePathAdvancement(int remainingNodes, int randomRoll) {
        if (remainingNodes <= 1) {
            return 0;
        }
        return Mth.clamp(randomRoll, 1, Math.min(4, remainingNodes - 1));
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        if (variant() == Variant.PHASE && phaseEvasions > 0 && consumesPhaseEvasion(source)) {
            phaseEvasions--;
            Entity threat = source.getDirectEntity();
            if (threat == null) {
                threat = source.getEntity();
            }
            if (threat == null) {
                threat = this;
            }
            if (teleportAwayFrom(threat, level)) {
                return false;
            }
        }
        return super.hurtServer(level, source, damage);
    }

    static boolean consumesPhaseEvasion(DamageSource source) {
        return phaseEvasionEligible(
                source.is(net.minecraft.tags.DamageTypeTags.IS_FALL),
                source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE),
                source.is(net.neoforged.neoforge.common.NeoForgeMod.POISON_DAMAGE));
    }

    static boolean phaseEvasionEligible(boolean fallDamage, boolean fireDamage, boolean poisonDamage) {
        return !fallDamage && !fireDamage && !poisonDamage;
    }

    private boolean teleportAwayFrom(Entity threat, ServerLevel level) {
        int originX = blockPosition().getX();
        int originY = blockPosition().getY();
        int originZ = blockPosition().getZ();
        for (int attempt = 0; attempt < 64; attempt++) {
            int dx = random.nextInt(11) - 5;
            int dy = random.nextInt(9) - 4;
            int dz = random.nextInt(11) - 5;
            if (Math.abs(dx) < 3 && Math.abs(dz) < 3) {
                continue;
            }
            double x = originX + dx + 0.5D;
            double y = originY + dy;
            double z = originZ + dz + 0.5D;
            double threatX = x - threat.getX();
            double threatZ = z - threat.getZ();
            if (threatX * threatX + threatZ * threatZ < 9.0D || !randomTeleport(x, y, z, true)) {
                continue;
            }
            double followRange = getAttributeValue(Attributes.FOLLOW_RANGE);
            Player nearest = level.getNearestPlayer(this, followRange);
            if (nearest != null && hasLineOfSight(nearest)) {
                setTarget(nearest);
            }
            this.level().playSound(null, xo, yo, zo, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1.0F, 1.0F);
            playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (variant() == Variant.PHASE && phaseEvasions < maxPhaseEvasions && tickCount % 100 == 0) {
            phaseEvasions++;
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel level, @NonNull DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (killedByPlayer) {
            for (int webs = websRemaining; webs > 0; webs--) {
                spawnAtLocation(level, Items.STRING.getDefaultInstance());
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("infx.phase_evasions", phaseEvasions);
        output.putInt("infx.phase_max_evasions", maxPhaseEvasions);
        output.putInt("infx.webs_remaining", websRemaining);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        phaseEvasions = input.getIntOr("infx.phase_evasions", phaseEvasions);
        maxPhaseEvasions = input.getIntOr("infx.phase_max_evasions", maxPhaseEvasions);
        websRemaining = variant() == Variant.PHASE
                ? 0
                : Mth.clamp(input.getIntOr("infx.webs_remaining", websRemaining), 0, 3);
    }
}
