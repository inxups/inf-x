package com.pixulse.infx.entity;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.equipment.QualitySystem;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Zombified piglin replacement that is hostile at close range without provocation. */
public final class InfxZombifiedPiglin extends ZombifiedPiglin implements InfxMob {
    private static final Identifier CHASE_SPEED_ID = Identifier.fromNamespaceAndPath("infx", "pigman_chase_speed");
    private static final Identifier VANILLA_CHASE_SPEED_ID = Identifier.withDefaultNamespace("attacking");
    private static final double MODERN_BASE_MOVEMENT_SPEED = 0.23;
    private static final double CHASE_SPEED_BONUS = 0.05;
    private static final AttributeModifier CHASE_SPEED =
            new AttributeModifier(
                    CHASE_SPEED_ID,
                    CHASE_SPEED_BONUS,
                    AttributeModifier.Operation.ADD_VALUE);

    public InfxZombifiedPiglin(EntityType<? extends ZombifiedPiglin> type, Level level) {
        super(type, level);
        // InfX pig zombies are worth triple the base experience.
        xpReward = 15;
        disableVanillaZombieAbilities();
    }

    public static AttributeSupplier.Builder attributes() {
        return ZombifiedPiglin.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, MODERN_BASE_MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinOldAiReach(this, target, AttackRanges.OLD_AI_REACH);
    }

    /** Pig zombies use their fixed golden kit rather than modern zombie door-breaking or item pickup. */
    private void disableVanillaZombieAbilities() {
        setCanBreakDoors(false);
        setCanPickUpLoot(false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // InfX pig zombies notice unprovoked players only within 6 blocks; anger extends to 24.
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                (target, level) -> {
                    double limit = isAngry() ? 24.0 : 6.0;
                    return distanceToSqr(target) <= limit * limit;
                }));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            @NonNull ServerLevelAccessor level,
            @NonNull DifficultyInstance difficulty,
            @NonNull EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        // InfX has no baby pig zombies and no chicken jockeys.
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, new Zombie.ZombieGroupData(false, false));
        setBaby(false);
        AttributeInstance reinforcements = getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (reinforcements != null) {
            reinforcements.removeModifiers();
            reinforcements.setBaseValue(0.0);
        }
        // Zombie#finalizeSpawn re-rolls both flags, so restore the pig-zombie restrictions.
        disableVanillaZombieAbilities();
        return data;
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        // Preserve the restriction for pig zombies loaded from worlds saved before this fix.
        disableVanillaZombieAbilities();
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    /** InfX pig zombies always carry a worn golden weapon: sword 2, axe 1, pickaxe 1. */
    @Override
    public void populateDefaultEquipmentSlots(RandomSource random, @NonNull DifficultyInstance difficulty) {
        int roll = random.nextInt(4);
        EquipmentType type = roll <= 1
                ? EquipmentType.SWORD
                : roll == 2 ? EquipmentType.AXE : EquipmentType.PICKAXE;
        ItemStack weapon = InfXItems.catalog().equipment(InfxMaterial.GOLD, type).holder().toStack();
        QualitySystem.applySelectedQuality(weapon, QualitySystem.toCode(Quality.POOR));
        setItemSlot(EquipmentSlot.MAINHAND, weapon);
    }

    @Override
    protected void customServerAiStep(@NonNull ServerLevel level) {
        super.customServerAiStep(level);
        // Keep the target-specific lifecycle, but use the modern +0.05 fighting bonus.
        // The legacy 1.9x conversion made the 0.23 base speed visibly too fast in 26.2.
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(VANILLA_CHASE_SPEED_ID);
        boolean chasing = getTarget() != null;
        if (chasing && !speed.hasModifier(CHASE_SPEED_ID)) {
            speed.addTransientModifier(CHASE_SPEED);
        } else if (!chasing && speed.hasModifier(CHASE_SPEED_ID)) {
            speed.removeModifier(CHASE_SPEED_ID);
        }
    }

    static double chasingMovementSpeed(double baseMovementSpeed) {
        return baseMovementSpeed + CHASE_SPEED_BONUS;
    }
}
