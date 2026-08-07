package com.pixulse.infx.mixin;

import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Bridges Horse's private coat/markings setter for breeding inheritance. */
@Mixin(Horse.class)
public interface HorseAccessor {
    @Invoker("setVariantAndMarkings")
    void infx$setVariantAndMarkings(Variant variant, Markings markings);
}
