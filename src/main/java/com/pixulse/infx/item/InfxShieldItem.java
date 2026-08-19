package com.pixulse.infx.item;

import net.minecraft.world.item.Item;

/**
 * Offhand shield. Blocking is fully component-driven in 26.x: any item carrying
 * {@link net.minecraft.core.component.DataComponents#BLOCKS_ATTACKS} automatically raises and
 * blocks on use (see vanilla {@code Item.use} / {@code getUseAnimation}), so this class only
 * needs to carry its {@link EquipmentKey} for naming and lookups. The {@code BlocksAttacks}
 * component itself is built per-material in {@link ItemProperties#shield}.
 */
public class InfxShieldItem extends Item {
    private final EquipmentKey key;

    public InfxShieldItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }
}
