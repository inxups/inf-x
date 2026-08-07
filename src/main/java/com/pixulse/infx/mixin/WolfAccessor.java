package com.pixulse.infx.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Bridges the private wolf variant/sound/collar setters so INFX wolf replacements can inherit
 * them when breeding, exactly like the vanilla {@link Wolf#getBreedOffspring} logic.
 */
@Mixin(Wolf.class)
public interface WolfAccessor {
    @Invoker("getVariant")
    Holder<WolfVariant> infx$getVariant();

    @Invoker("setVariant")
    void infx$setVariant(Holder<WolfVariant> variant);

    @Invoker("getSoundVariant")
    Holder<WolfSoundVariant> infx$getSoundVariant();

    @Invoker("setSoundVariant")
    void infx$setSoundVariant(Holder<WolfSoundVariant> soundVariant);

    @Invoker("setCollarColor")
    void infx$setCollarColor(DyeColor color);
}
