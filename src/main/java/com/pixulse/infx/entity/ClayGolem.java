package com.pixulse.infx.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * InfX's clay golem is a separate natural monster that shares the earth-elemental material and
 * heat machinery. Fire and lava permanently harden it rather than creating a magma state.
 */
public final class ClayGolem extends EarthElemental {
    public ClayGolem(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setForm(Form.CLAY_NORMAL);
    }

    public static AttributeSupplier.Builder attributes() {
        return baseAttributes(6.0, 0.0);
    }

    @Override
    protected boolean isClayGolem() {
        return true;
    }

    /** Clay hardens in heat but never becomes a fire-spreading magma elemental. */
    @Override
    public boolean isMagma() {
        return false;
    }
}
