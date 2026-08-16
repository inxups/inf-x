package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * MITE black ghoul: extinguishes light, heals in darkness, blinds and weakens victims, and is
 * instantly killed by sunlight unless spared by silver, magic or sunlight-resistant damage.
 */
public final class Shadow extends InfxZombieBase {
    public Shadow(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes().add(Attributes.ARMOR, 0.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected boolean breaksDoors() {
        return false;
    }

    @Override
    protected boolean picksUpLoot() {
        return false;
    }

    @Override
    protected boolean targetsAnimals() {
        return true;
    }

    @Override
    protected boolean isSilentType() {
        return true;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float amount) {
        if (!MobDamageRules.silverMagicGateAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean doHurtTarget(@NonNull ServerLevel level, @NonNull Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0), this);
            living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0), this);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (level.isBrightOutside() && level.canSeeSky(blockPosition()) && !level.isRaining()) {
            // InfX shadows take 1000 sunlight damage: certain death, no helmet protection.
            hurtServer(level, damageSources().genericKill(), 1000.0F);
        } else if (tickCount % 40 == 0) {
            int darknessHeal = (int) ((0.4F - getLightLevelDependentMagicValue()) * 10.0F);
            if (darknessHeal > 0) {
                heal(darknessHeal);
            }
        }
        if ((getLastHurtByMob() == null || tickCount - getLastHurtByMobTimestamp() > 100)
                && level.getNearestPlayer(this, 4.0) == null) {
            disableNearbyLight(level);
        }
    }

    @Override
    protected @NonNull SoundEvent getAmbientSound() {
        return InfXSounds.SHADOW_AMBIENT.get();
    }

    @Override
    protected @NonNull SoundEvent getHurtSound(@NonNull DamageSource source) {
        return InfXSounds.SHADOW_HURT.get();
    }

    @Override
    protected @NonNull SoundEvent getDeathSound() {
        return InfXSounds.SHADOW_DEATH.get();
    }
}
