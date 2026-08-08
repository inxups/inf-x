package com.pixulse.infx.compat.jade;

import com.pixulse.infx.InfiniteX;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade (Waila) integration entry point, discovered by Jade through {@link WailaPlugin}.
 *
 * <p>Registers {@link InfXHarvestToolProvider} so the harvest-tool line reads InfX mining
 * rules (families, tiers and the server-side mining gate) instead of vanilla tags. The
 * vanilla class is only loaded when Jade is present.
 */
@WailaPlugin(InfiniteX.MOD_ID)
public final class InfXWailaPlugin implements IWailaPlugin {
    @Override
    public void registerClient(@NonNull IWailaClientRegistration registration) {
        registration.registerBlockComponent(InfXHarvestToolProvider.INSTANCE, Block.class);
    }
}
