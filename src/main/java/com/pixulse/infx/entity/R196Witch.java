package com.pixulse.infx.entity;

import com.pixulse.infx.curse.R196CurseManager;
import com.pixulse.infx.curse.R196CurseType;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModSounds;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Swamp-hut miniboss witch with a curse and a one-time wolf-pack summon. */
public final class R196Witch extends Witch implements R196Mob {
    private static final float INDIRECT_MAGIC_DEFENSE = 10.0F;

    private boolean summonedWolves;
    private int summonWolvesAt = -1;
    private int curseRandomSeed;

    public R196Witch(EntityType<? extends Witch> type, Level level) {
        super(type, level);
        // MITE witches are worth four times the base experience.
        xpReward = 20;
        if (!level.isClientSide()) {
            curseRandomSeed = new Random().nextInt();
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return Witch.createAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    /** MITE witches are homebodies that never despawn. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.WITCH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WITCH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WITCH_DEATH.get();
    }

    static boolean hasIndirectMagicDefense(DamageSource source) {
        return !source.isDirect() && source.is(DamageTypes.INDIRECT_MAGIC);
    }

    static float magicDefenseReduction(DamageSource source, float damage) {
        if (!hasIndirectMagicDefense(source)) {
            return 0.0F;
        }
        // MITE protection is flat and leaves one point of incoming damage.
        return Math.min(INDIRECT_MAGIC_DEFENSE, Math.max(0.0F, damage - 1.0F));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof net.minecraft.world.entity.ai.goal.RangedAttackGoal);
        goalSelector.addGoal(2, new MiteHardLimitedRangedAttackGoal(
                this, 1.0, 60, (float) R196AttackRanges.WITCH_RANGED_REACH));
        targetSelector.removeAllGoals(goal -> true);
        targetSelector.addGoal(1, new CurseHurtByTargetGoal(this));
        targetSelector.addGoal(2, new CurseNearestPlayerGoal(this));
    }

    /** The target goal makes the one-in-four roll before calling this method. */
    private void cursePlayer(ServerPlayer player) {
        R196CurseManager.addPending(
                player,
                this,
                R196CurseType.forWitch(curseRandomSeed, player.getScoreboardName()));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        // MITE: the first player hit triggers a single wolf-pack summon 60 ticks later.
        if (hurt && !summonedWolves && summonWolvesAt < 0 && source.getEntity() instanceof Player) {
            summonWolvesAt = tickCount + 60;
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel level) {
            R196CurseManager.removeForWitch(level.getServer(), getUUID());
        }
        super.die(source);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        var target = getTarget();
        if (target == null) {
            return;
        }
        if (summonWolvesAt >= 0 && tickCount >= summonWolvesAt) {
            summonWolvesAt = -1;
            summonedWolves = true;
            int pack = 1 + random.nextInt(3);
            for (int i = 0; i < pack; i++) {
                summonWolfNear(level, target);
            }
        }
    }

    /** MITE summons plain hostile wolves 8-16 blocks around the witch's target. */
    private void summonWolfNear(ServerLevel level, LivingEntity target) {
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = 8.0 + random.nextDouble() * 8.0;
            double x = target.getX() + Math.cos(angle) * distance;
            double z = target.getZ() + Math.sin(angle) * distance;
            R196VanillaWolf wolf = ModEntityTypes.R196_WOLF.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (wolf == null) {
                return;
            }
            wolf.snapTo(x, target.getY(), z, random.nextFloat() * 360.0F, 0.0F);
            if (level.noCollision(wolf)) {
                level.addFreshEntity(wolf);
                wolf.setTarget(target);
                return;
            }
            wolf.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("R196SummonedWolves", summonedWolves);
        output.putInt("R196CurseRandomSeed", curseRandomSeed);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        summonedWolves = input.getBooleanOr("R196SummonedWolves", false);
        curseRandomSeed = input.getIntOr("R196CurseRandomSeed", curseRandomSeed);
    }

    private static final class CurseHurtByTargetGoal extends HurtByTargetGoal {
        private final R196Witch witch;

        private CurseHurtByTargetGoal(R196Witch witch) {
            super(witch);
            this.witch = witch;
        }

        @Override
        public boolean canUse() {
            boolean suitable = super.canUse();
            if (suitable
                    && witch.getLastHurtByMob() instanceof ServerPlayer player
                    && witch.getRandom().nextInt(4) == 0) {
                witch.cursePlayer(player);
            }
            return suitable;
        }
    }

    private static final class CurseNearestPlayerGoal extends NearestAttackableTargetGoal<Player> {
        private static final double VERTICAL_SEARCH_RANGE = 6.0D;
        private final R196Witch witch;
        /*
         * MITE's EntityAITarget rejects a player only when that player's game-mode
         * capabilities disable damage.  TargetingConditions.forCombat() is stricter
         * in 26.2: it also rejects an entity whose root Invulnerable flag is set.
         * That flag can be present on an otherwise-survival player, so use a
         * non-combat query for curse delivery and retain the normal combat check
         * only when choosing the witch's actual attack target.
         */
        private final TargetingConditions curseConditions = TargetingConditions.forNonCombat()
                .selector((target, level) -> target instanceof Player player
                        && !player.getAbilities().invulnerable);

        private CurseNearestPlayerGoal(R196Witch witch) {
            super(witch, Player.class, 0, true, false, null);
            this.witch = witch;
        }

        @Override
        protected void findTarget() {
            target = scanForNearestTarget();
        }

        private @Nullable Player scanForNearestTarget() {
            ServerLevel level = getServerLevel(witch);
            double range = getFollowDistance();
            // Player targeting in 26.2 uses ServerLevel's player collection rather than the
            // section-entity query. Mirror that path so every connected player is considered.
            var candidates = level.getNearbyPlayers(
                    curseConditions.range(range),
                    witch,
                    witch.getBoundingBox().inflate(range, VERTICAL_SEARCH_RANGE, range));
            for (Player candidate : candidates) {
                if (candidate instanceof ServerPlayer player && witch.getRandom().nextInt(4) == 0) {
                    witch.cursePlayer(player);
                }
            }
            return candidates.stream()
                    // Mob#setTarget still rejects non-attackable modern targets. Do not let one
                    // such player prevent the witch from choosing another valid combat target.
                    .filter(witch::canAttack)
                    .min(Comparator.comparingDouble(witch::distanceToSqr))
                    .orElse(null);
        }

        @Override
        public void tick() {
            if (witch.getRandom().nextInt(40) == 0) {
                Player nearest = scanForNearestTarget();
                if (nearest != null
                        && nearest != witch.getTarget()
                        && witch.getSensing().hasLineOfSight(nearest)) {
                    target = nearest;
                    start();
                }
            }
            super.tick();
        }
    }
}
