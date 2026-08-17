package com.pixulse.infx.entity;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.equipment.QualitySystem;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.world.BoneLordSummonRegistry;
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
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Structure and spawn-egg MITE wither skeleton replacement. Natural Nether spawns remain vanilla. */
public final class InfxWitherSkeleton extends WitherSkeleton implements InfxMob, BoneRepairingSkeleton, BoneLordInspired {
    private static final int BONE_REPAIR_COOLDOWN_TICKS = 400;
    private static final int BONE_LORD_INSPIRE_TICKS = 20;

    private int boneRepairCooldownUntil;
    private int inspiredUntil;

    public InfxWitherSkeleton(EntityType<? extends WitherSkeleton> type, Level level) {
        super(type, level);
        setCanPickUpLoot(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinNewAiReach(this, target);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new MoveToBoneRepairGoal<>(this));
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, poorIronSword());
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            @NonNull ServerLevelAccessor level,
            @NonNull DifficultyInstance difficulty,
            @NonNull EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        setCanPickUpLoot(true);
        setItemSlot(EquipmentSlot.MAINHAND, poorIronSword());
        setDropChance(EquipmentSlot.MAINHAND, 0.085F);
        reassessWeaponGoal();
        return result;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        if (source.is(DamageTypes.CACTUS)) {
            return false;
        }
        if (source.getDirectEntity() instanceof AbstractArrow) {
            if (source.getEntity() instanceof AbstractSkeleton) {
                return false;
            }
            damage *= 0.25F;
        }
        if (source.isDirect() && source.getEntity() instanceof LivingEntity attacker) {
            var equipment = InfXItems.catalog().equipment(attacker.getMainHandItem());
            if (equipment != null
                    && (equipment.key().type() == EquipmentType.CUDGEL
                            || equipment.key().type() == EquipmentType.CLUB
                            || equipment.key().type() == EquipmentType.WAR_HAMMER)) {
                damage *= 2.0F;
            }
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean canRepairFromBone() {
        return getHealth() < getMaxHealth() && tickCount >= boneRepairCooldownUntil;
    }

    @Override
    public boolean tryRepairFromBone(ItemStack stack) {
        if (!stack.is(Items.BONE) || !canRepairFromBone()) {
            return false;
        }
        stack.shrink(1);
        heal(getMaxHealth() * 0.5F);
        boneRepairCooldownUntil = tickCount + BONE_REPAIR_COOLDOWN_TICKS;
        playSound(SoundEvents.ITEM_PICKUP, 0.2F, (random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    getX(),
                    getY() + getBbHeight() * 0.5D,
                    getZ(),
                    6,
                    0.4D,
                    0.2D,
                    0.4D,
                    0.0D);
        }
        return true;
    }

    @Override
    public boolean isInspired() {
        return tickCount < inspiredUntil;
    }

    @Override
    public void inspire() {
        inspiredUntil = tickCount + BONE_LORD_INSPIRE_TICKS;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence()
                || level() instanceof ServerLevel level
                        && BoneLordSummonRegistry.get(level).isTracked(getUUID());
    }

    private static ItemStack poorIronSword() {
        ItemStack weapon = InfXItems.catalog().equipment(InfxMaterial.IRON, EquipmentType.SWORD).holder().toStack();
        QualitySystem.applySelectedQuality(weapon, QualitySystem.toCode(Quality.POOR));
        return weapon;
    }
}
