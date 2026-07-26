package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

/** Zombie-shaped R196 mobs, including the replacement zombie and five new variants. */
public final class R196Zombie extends Zombie implements R196Mob {
    public enum Variant {
        ZOMBIE,
        INVISIBLE_STALKER,
        GHOUL,
        SHADOW,
        WIGHT,
        REVENANT
    }

    public R196Zombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        setCanBreakDoors(breaksDoors());
        setCanPickUpLoot(variant() == Variant.ZOMBIE);
        if (variant() == Variant.INVISIBLE_STALKER) {
            setInvisible(true);
        }
        xpReward = switch (variant()) {
            case ZOMBIE -> xpReward;
            case INVISIBLE_STALKER, GHOUL, SHADOW, WIGHT -> 10;
            case REVENANT -> 15;
        };
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "invisible_stalker" -> Variant.INVISIBLE_STALKER;
            case "ghoul" -> Variant.GHOUL;
            case "shadow" -> Variant.SHADOW;
            case "wight" -> Variant.WIGHT;
            case "revenant" -> Variant.REVENANT;
            default -> Variant.ZOMBIE;
        };
    }

    /** MITE only gives the break-door task to zombies and ghouls. */
    private boolean breaksDoors() {
        return variant() == Variant.ZOMBIE || variant() == Variant.GHOUL;
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        AttributeSupplier.Builder builder = Zombie.createAttributes().add(Attributes.ARMOR, 0.0);
        return switch (variant) {
            case ZOMBIE -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.23)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case INVISIBLE_STALKER -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.23)
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
            case GHOUL -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.28)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case SHADOW -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.23)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case WIGHT -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.25)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case REVENANT -> builder
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.26)
                    .add(Attributes.ATTACK_DAMAGE, 7.0);
        };
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        // MITE zombies never spawn as babies, so chicken jockeys cannot appear either.
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, new ZombieGroupData(false, false));
        setBaby(false);
        AttributeInstance reinforcements = getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (reinforcements != null) {
            reinforcements.removeModifiers();
            reinforcements.setBaseValue(0.0);
        }
        setCanBreakDoors(breaksDoors());
        setCanPickUpLoot(variant() == Variant.ZOMBIE);
        if (variant() == Variant.REVENANT) {
            equipRevenantKit(level.getLevel());
        }
        return data;
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // MITE only arms the plain zombie; the revenant receives its fixed kit in finalizeSpawn.
        if (variant() == Variant.ZOMBIE) {
            super.populateDefaultEquipmentSlots(random, difficulty);
        }
    }

    /** MITE revenants always spawn in full rusted-iron armor with a weighted rusted weapon. */
    private void equipRevenantKit(ServerLevel level) {
        long day = R196MonsterTactics.survivalDay(level);
        int bound = 2 + (day >= 10L ? 1 : 0) + (day >= 20L ? 1 : 0);
        int roll = random.nextInt(bound);
        R196EquipmentType weapon = roll <= 1
                ? R196EquipmentType.SWORD
                : roll == 2 && day >= 10L ? R196EquipmentType.BATTLE_AXE : R196EquipmentType.WAR_HAMMER;
        R196MonsterTactics.equip(level, this, EquipmentSlot.MAINHAND, R196Material.RUSTED_IRON, weapon, day);
        R196MonsterTactics.equip(level, this, EquipmentSlot.HEAD, R196Material.RUSTED_IRON, R196EquipmentType.HELMET, day);
        R196MonsterTactics.equip(
                level, this, EquipmentSlot.CHEST, R196Material.RUSTED_IRON, R196EquipmentType.CHESTPLATE, day);
        R196MonsterTactics.equip(
                level, this, EquipmentSlot.LEGS, R196Material.RUSTED_IRON, R196EquipmentType.LEGGINGS, day);
        R196MonsterTactics.equip(level, this, EquipmentSlot.FEET, R196Material.RUSTED_IRON, R196EquipmentType.BOOTS, day);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (variant() == Variant.SHADOW && !R196MobDamageRules.silverMagicGateAccepts(source)) {
            return false;
        }
        if (variant() == Variant.WIGHT && !R196MobDamageRules.wightAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return switch (variant()) {
            case GHOUL -> ModSounds.GHOUL_AMBIENT.get();
            case SHADOW -> ModSounds.SHADOW_AMBIENT.get();
            case WIGHT -> ModSounds.WIGHT_AMBIENT.get();
            case INVISIBLE_STALKER -> ModSounds.INVISIBLE_STALKER_AMBIENT.get();
            case REVENANT, ZOMBIE -> super.getAmbientSound();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (variant()) {
            case GHOUL -> ModSounds.GHOUL_HURT.get();
            case SHADOW -> ModSounds.SHADOW_HURT.get();
            case WIGHT -> ModSounds.WIGHT_HURT.get();
            case INVISIBLE_STALKER -> ModSounds.INVISIBLE_STALKER_HURT.get();
            case REVENANT, ZOMBIE -> super.getHurtSound(source);
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (variant()) {
            case GHOUL -> ModSounds.GHOUL_DEATH.get();
            case SHADOW -> ModSounds.SHADOW_DEATH.get();
            case WIGHT -> ModSounds.WIGHT_DEATH.get();
            case INVISIBLE_STALKER -> ModSounds.INVISIBLE_STALKER_DEATH.get();
            case REVENANT, ZOMBIE -> super.getDeathSound();
        };
    }

    /** MITE stalkers and shadows move silently. */
    @Override
    protected float getSoundVolume() {
        return variant() == Variant.INVISIBLE_STALKER || variant() == Variant.SHADOW ? 0.2F : super.getSoundVolume();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (variant() == Variant.INVISIBLE_STALKER || variant() == Variant.SHADOW) {
            return;
        }
        super.playStepSound(pos, state);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (!hurt || !(target instanceof LivingEntity living)) {
            return hurt;
        }

        switch (variant()) {
            case GHOUL -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
            case SHADOW -> {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0), this);
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0), this);
            }
            case WIGHT -> {
                if (living instanceof Player player && random.nextFloat() < 0.4F) {
                    player.giveExperiencePoints(-Math.max(20, (player.experienceLevel + 1) * 10));
                }
            }
            case INVISIBLE_STALKER, REVENANT, ZOMBIE -> {
            }
        }
        // MITE ghouls are the only family member that heals from feeding on flesh.
        if (variant() == Variant.GHOUL && living instanceof Animal && !living.isAlive()) {
            heal(getMaxHealth() * 0.5F);
        }
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (variant() == Variant.INVISIBLE_STALKER && !isInvisible()) {
            setInvisible(true);
        }
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (variant() == Variant.SHADOW) {
            if (level.isBrightOutside() && level.canSeeSky(blockPosition()) && !level.isRaining()) {
                // MITE shadows take 1000 sunlight damage: certain death, no helmet protection.
                hurtServer(level, damageSources().genericKill(), 1000.0F);
            } else if (tickCount % 40 == 0) {
                int darknessHeal = (int) ((0.4F - getLightLevelDependentMagicValue()) * 10.0F);
                if (darknessHeal > 0) {
                    heal(darknessHeal);
                }
            }
        }
        if ((variant() == Variant.INVISIBLE_STALKER || variant() == Variant.SHADOW)
                && (getLastHurtByMob() == null || tickCount - getLastHurtByMobTimestamp() > 100)
                && level.getNearestPlayer(this, 4.0) == null) {
            disableNearbyLight(level);
        }
        if (tickCount % 20 == 0 && isOnFire() && random.nextFloat() < 0.15F) {
            igniteNearbyBlock(level);
        }
    }

    private void disableNearbyLight(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 3, 1))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.TORCH)
                    || state.is(Blocks.WALL_TORCH)
                    || state.is(Blocks.REDSTONE_TORCH)
                    || state.is(Blocks.REDSTONE_WALL_TORCH)) {
                level.destroyBlock(pos, true, this);
                return;
            }
            if (state.is(Blocks.JACK_O_LANTERN)) {
                BlockState pumpkin = Blocks.CARVED_PUMPKIN.defaultBlockState();
                if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                    pumpkin = pumpkin.setValue(
                            HorizontalDirectionalBlock.FACING, state.getValue(HorizontalDirectionalBlock.FACING));
                }
                level.setBlockAndUpdate(pos, pumpkin);
                spawnAtLocation(level, new ItemStack(Items.TORCH));
                return;
            }
        }
    }

    private void igniteNearbyBlock(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos pos = blockPosition().relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        if (!level.isEmptyBlock(pos)) {
            return;
        }
        BlockState fire = BaseFireBlock.getState(level, pos);
        if (fire.canSurvive(level, pos)) {
            level.setBlockAndUpdate(pos, fire);
        }
    }
}
