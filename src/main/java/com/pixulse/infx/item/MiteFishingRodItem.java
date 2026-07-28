package com.pixulse.infx.item;

import net.minecraft.world.item.FishingRodItem;

public final class MiteFishingRodItem extends FishingRodItem {
    private final EquipmentKey key;

    public MiteFishingRodItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }
}
