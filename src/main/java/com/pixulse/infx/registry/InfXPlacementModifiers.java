package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.InfXUnderworldLushRegionPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers placement filters used by InfiniteX world generation. */
public final class InfXPlacementModifiers {
    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<InfXUnderworldLushRegionPlacement>>
            UNDERWORLD_LUSH_REGION = PLACEMENT_MODIFIERS.register(
                    "underworld_lush_region",
                    () -> () -> InfXUnderworldLushRegionPlacement.CODEC);

    private InfXPlacementModifiers() {}

    public static void register(IEventBus modBus) {
        PLACEMENT_MODIFIERS.register(modBus);
    }
}
