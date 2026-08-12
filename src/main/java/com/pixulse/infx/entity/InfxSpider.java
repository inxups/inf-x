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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Spider replacement and the four INFX spider variants. */
public final class InfxSpider extends Spider implements InfxMob {
    private static final double MODERN_SPIDER_MOVEMENT_SPEED = 0.30;
    private static final double INFX_ARACHNID_SPEED_MULTIPLIER = 1.25;
    private static final double MAX_PHASE_CHASE_VERTICAL_DISTANCE = 2.0;

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
        // Legacy arachnids use the old AI pathing formula, where the attribute is a
        // forward-input multiplier under a fixed 0.1 throttle.  Its 1.0/0.8 values cannot
        // be assigned directly to 26.2's movement attribute.  Preserve only the source's
        // 25% base/cave/demon boost over the vanilla arachnid family.
        AttributeSupplier.Builder builder = Spider.createAttributes().add(Attributes.FOLLOW_RANGE, 28.0);
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
            case SPIDER, CAVE_SPIDER, DEMON -> MODERN_SPIDER_MOVEMENT_SPEED * INFX_ARACHNID_SPEED_MULTIPLIER;
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

    static boolean canPhaseChaseAcrossVerticalDistance(double verticalDistance) {
        return Math.abs(verticalDistance) <= MAX_PHASE_CHASE_VERTICAL_DISTANCE;
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
                    && random.nextFloat() < 0.1F * difficulty.getSpecialMultiplier()) {
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
        if (target == null) {
            return;
        }

        if (variant() == Variant.PHASE && tickCount % 10 == 0 && random.nextInt(3) == 0 && distanceToSqr(target) > 9.0) {
            teleportToward(target);
        }

        if (websRemaining > 0
                && shouldThrowWebAtTick(variant(), tickCount, getId())
                && distanceToSqr(target) <= 64.0
                && hasLineOfSight(target)
                && snareTarget(level, target)) {
            websRemaining--;
        }
    }

    /**
     * The complete EntityWeb projectile is not yet available in 26.2.  Keep the existing block-web
     * approximation, but consume the same finite stock and use the source targeting cadence.
     */
    private boolean snareTarget(ServerLevel level, LivingEntity target) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return false;
        }
        BlockPos pos = target.blockPosition();
        if (!level.isEmptyBlock(pos)) {
            return false;
        }
        level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
        if (variant() == Variant.DEMON) {
            target.igniteForSeconds(6.0F);
        }
        return true;
    }

    private boolean teleportToward(LivingEntity target) {
        if (!canPhaseChaseAcrossVerticalDistance(target.getY() - getY())) {
            return false;
        }
        double distance = Math.max(1.0, distanceTo(target));
        double x = getX() + (target.getX() - getX()) / distance * Math.min(4.0, distance - 1.0);
        double z = getZ() + (target.getZ() - getZ()) / distance * Math.min(4.0, distance - 1.0);
        return randomTeleport(x, target.getY(), z, true);
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
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
                    this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
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
        output.putInt("R196PhaseEvasions", phaseEvasions);
        output.putInt("R196PhaseMaxEvasions", maxPhaseEvasions);
        output.putInt("R196WebsRemaining", websRemaining);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        phaseEvasions = input.getIntOr("R196PhaseEvasions", phaseEvasions);
        maxPhaseEvasions = input.getIntOr("R196PhaseMaxEvasions", maxPhaseEvasions);
        websRemaining = variant() == Variant.PHASE
                ? 0
                : Mth.clamp(input.getIntOr("R196WebsRemaining", websRemaining), 0, 3);
    }
}
