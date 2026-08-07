package com.pixulse.infx.item;

import com.pixulse.infx.item.material.InfxMaterial;
import java.util.Objects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.entity.monster.Strider;

/**
 * InfX warped-fungus-on-a-stick equivalent: the modern counterpart of the
 * carrot-on-a-stick, with one stick per fishing-hook material for steering
 * striders. The vanilla strider entity is used, so the base class behavior
 * (one durability point of damage per boost) is kept.
 */
public final class InfxWarpedFungusOnAStickItem extends FoodOnAStickItem<Strider> {
    private static final int STRIDER_BOOST_DAMAGE = 1;
    private final InfxMaterial hookMaterial;

    public InfxWarpedFungusOnAStickItem(InfxMaterial hookMaterial, Properties properties) {
        super(EntityType.STRIDER, STRIDER_BOOST_DAMAGE, properties);
        this.hookMaterial = Objects.requireNonNull(hookMaterial, "hookMaterial");
    }

    public InfxMaterial hookMaterial() {
        return hookMaterial;
    }
}
