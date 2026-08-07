package com.pixulse.infx.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Bridges Pig's private variant setter for breeding inheritance. */
@Mixin(Pig.class)
public interface PigAccessor {
    @Invoker("setVariant")
    void infx$setVariant(Holder<PigVariant> variant);
}
