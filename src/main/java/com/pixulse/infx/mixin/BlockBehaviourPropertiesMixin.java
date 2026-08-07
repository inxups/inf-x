package com.pixulse.infx.mixin;

import com.pixulse.infx.data.harvest.MiteBlockHardness;
import com.pixulse.infx.data.harvest.PlantHardness;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2 constructs vanilla blocks during bootstrap, before a mod registry event can alter their
 * state caches. Adjust only mapped MITE block counterparts while their properties receive the
 * vanilla registry key and before the block constructor creates those cached states.
 */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockBehaviourPropertiesMixin {
    @Inject(method = "setId", at = @At("RETURN"))
    private void infx$applyMiteHardness(
            ResourceKey<Block> id,
            CallbackInfoReturnable<BlockBehaviour.Properties> callback) {
        if (PlantHardness.appliesTo(id.identifier())) {
            ((BlockBehaviour.Properties) (Object) this).destroyTime(PlantHardness.destroyTime(id.identifier()));
        } else if (MiteBlockHardness.appliesTo(id.identifier())) {
            ((BlockBehaviour.Properties) (Object) this)
                    .destroyTime(MiteBlockHardness.destroyTime(id.identifier()));
        }
    }
}
