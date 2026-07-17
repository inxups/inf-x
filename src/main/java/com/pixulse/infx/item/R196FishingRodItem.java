package com.pixulse.infx.item;

import net.minecraft.world.item.FishingRodItem;

public final class R196FishingRodItem extends FishingRodItem {
    private final R196EquipmentKey key;

    public R196FishingRodItem(R196EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public R196EquipmentKey key() {
        return key;
    }
}
