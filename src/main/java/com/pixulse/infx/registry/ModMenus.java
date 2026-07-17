package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.menu.TimedWorkbenchMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, InfiniteX.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> FLINT_WORKBENCH = MENUS.register(
            "flint_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.FLINT, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> COPPER_WORKBENCH = MENUS.register(
            "copper_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.COPPER, buffer)));

    private ModMenus() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
