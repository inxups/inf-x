package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModMobEffects;
import com.pixulse.infx.registry.ModSounds;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
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
    private @Nullable UUID cursedPlayer;

    public R196Witch(EntityType<? extends Witch> type, Level level) {
        super(type, level);
        // MITE witches are worth four times the base experience.
        xpReward = 20;
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
        goalSelector.removeAllGoals(goal -> goal instanceof RangedAttackGoal);
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 10.0F));
    }

    /** MITE witches curse a newly targeted player one time in four, lifted when the witch dies. */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target instanceof Player player
                && cursedPlayer == null
                && !player.hasEffect(ModMobEffects.WITCH_CURSE)
                && random.nextInt(4) == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 6000, 0), this);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0), this);
            player.addEffect(new MobEffectInstance(ModMobEffects.WITCH_CURSE, 6000, 0), this);
            cursedPlayer = player.getUUID();
        }
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
        super.die(source);
        if (cursedPlayer != null
                && level() instanceof ServerLevel level
                && level.getPlayerByUUID(cursedPlayer) instanceof Player player) {
            player.removeEffect(ModMobEffects.WITCH_CURSE);
        }
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
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        summonedWolves = input.getBooleanOr("R196SummonedWolves", false);
    }
}
