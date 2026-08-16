package com.pixulse.infx.entity;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * MITE revenant: a hardier undead that always spawns in a full rusted-iron kit and, being
 * always-smart, can dig through any block bare-handed.
 */
public final class Revenant extends InfxZombieBase {
    public Revenant(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        xpReward = 15;
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes().add(Attributes.ARMOR, 0.0)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 7.0);
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
    protected boolean zombifiesVillagers() {
        return true;
    }

    /** Revenants are the InfX mobs that are smart from birth and dig bare-handed. */
    @Override
    protected boolean digsBareHanded() {
        return true;
    }

    @Override
    protected void afterFinalizeSpawn(ServerLevel level) {
        equipRevenantKit(level);
    }

    private void equipRevenantKit(ServerLevel level) {
        float tension = MonsterTactics.difficultyTension(level, blockPosition());
        int bound = 2 + (tension >= 0.15F ? 1 : 0) + (tension >= 0.35F ? 1 : 0);
        int roll = random.nextInt(bound);
        EquipmentType weapon = roll <= 1
                ? EquipmentType.SWORD
                : roll == 2 && tension >= 0.15F ? EquipmentType.BATTLE_AXE : EquipmentType.WAR_HAMMER;
        MonsterTactics.equip(level, this, EquipmentSlot.MAINHAND, InfxMaterial.RUSTED_IRON, weapon, tension);
        MonsterTactics.equip(level, this, EquipmentSlot.HEAD, InfxMaterial.RUSTED_IRON, EquipmentType.HELMET, tension);
        MonsterTactics.equip(
                level, this, EquipmentSlot.CHEST, InfxMaterial.RUSTED_IRON, EquipmentType.CHESTPLATE, tension);
        MonsterTactics.equip(
                level, this, EquipmentSlot.LEGS, InfxMaterial.RUSTED_IRON, EquipmentType.LEGGINGS, tension);
        MonsterTactics.equip(level, this, EquipmentSlot.FEET, InfxMaterial.RUSTED_IRON, EquipmentType.BOOTS, tension);
    }
}
