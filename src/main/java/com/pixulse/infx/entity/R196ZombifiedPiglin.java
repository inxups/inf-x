package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/** Zombified piglin replacement that is hostile at close range without provocation. */
public final class R196ZombifiedPiglin extends ZombifiedPiglin implements R196Mob {
    private static final Identifier MITE_CHASE_SPEED_ID = Identifier.fromNamespaceAndPath("infx", "mite_chase_speed");
    private static final AttributeModifier MITE_CHASE_SPEED =
            new AttributeModifier(MITE_CHASE_SPEED_ID, 0.45, AttributeModifier.Operation.ADD_VALUE);

    public R196ZombifiedPiglin(EntityType<? extends ZombifiedPiglin> type, Level level) {
        super(type, level);
        // MITE pig zombies are worth triple the base experience.
        xpReward = 15;
    }

    public static AttributeSupplier.Builder attributes() {
        return ZombifiedPiglin.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.50)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // MITE pig zombies notice unprovoked players only within 6 blocks; anger extends to 24.
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
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        // MITE has no baby pig zombies and no chicken jockeys.
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, new Zombie.ZombieGroupData(false, false));
        setBaby(false);
        AttributeInstance reinforcements = getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (reinforcements != null) {
            reinforcements.removeModifiers();
            reinforcements.setBaseValue(0.0);
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

    /** MITE pig zombies always carry a worn golden weapon: sword 2, axe 1, pickaxe 1. */
    @Override
    public void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        int roll = random.nextInt(4);
        R196EquipmentType type = roll <= 1
                ? R196EquipmentType.SWORD
                : roll == 2 ? R196EquipmentType.AXE : R196EquipmentType.PICKAXE;
        setItemSlot(
                EquipmentSlot.MAINHAND,
                ModItems.catalog().equipment(R196Material.GOLD, type).holder().toStack());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        // MITE: +0.45 speed whenever a pig zombie has a target, nearly doubling its pace.
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean chasing = getTarget() != null;
        if (chasing && !speed.hasModifier(MITE_CHASE_SPEED_ID)) {
            speed.addTransientModifier(MITE_CHASE_SPEED);
        } else if (!chasing && speed.hasModifier(MITE_CHASE_SPEED_ID)) {
            speed.removeModifier(MITE_CHASE_SPEED_ID);
        }
    }
}
