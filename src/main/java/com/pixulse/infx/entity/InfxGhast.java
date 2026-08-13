package com.pixulse.infx.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;

/** Ghast replacement retaining a one-hundred-block ranged awareness radius. */
public final class InfxGhast extends Ghast implements InfxMob {
    public InfxGhast(EntityType<? extends Ghast> type, Level level) {
        super(type, level);
        // InfX ghasts are worth double the base experience.
        xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Ghast.createAttributes();
    }

    /** InfX ghast cries carry at twice the modern volume. */
    @Override
    protected float getSoundVolume() {
        return 10.0F;
    }
}
