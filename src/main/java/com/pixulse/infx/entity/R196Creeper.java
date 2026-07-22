package com.pixulse.infx.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/** Creeper replacement and the terrain-breaking Infernal Creeper. */
public final class R196Creeper extends Creeper implements R196Mob {
    public enum Variant {
        CREEPER,
        INFERNAL
    }

    public R196Creeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    private boolean amplifyingExplosion;

    public Variant variant() {
        return R196EntityVariant.path(this).equals("infernal_creeper") ? Variant.INFERNAL : Variant.CREEPER;
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        return Creeper.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, variant == Variant.INFERNAL ? 2.0 : 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    boolean isAmplifyingExplosion() {
        return amplifyingExplosion;
    }

    void setAmplifyingExplosion(boolean amplifyingExplosion) {
        this.amplifyingExplosion = amplifyingExplosion;
    }
}
