package com.pixulse.infx.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;

/**
 * MITE's clay golem is a separate natural monster that shares the earth-elemental material and
 * heat machinery. Fire and lava permanently harden it rather than creating a magma state.
 */
public final class R196ClayGolem extends R196EarthElemental {
    public R196ClayGolem(EntityType<? extends IronGolem> type, Level level) {
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
