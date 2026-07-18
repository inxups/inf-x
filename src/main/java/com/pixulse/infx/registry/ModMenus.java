package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.menu.TimedWorkbenchMenu;
import com.pixulse.infx.menu.MetalAnvilMenu;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, InfiniteX.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<MetalAnvilMenu>> METAL_ANVIL = MENUS.register(
            "metal_anvil",
            () -> IMenuTypeExtension.create(MetalAnvilMenu::client));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> FLINT_WORKBENCH = MENUS.register(
            "flint_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.FLINT, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> COPPER_WORKBENCH = MENUS.register(
            "copper_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.COPPER, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> SILVER_WORKBENCH = MENUS.register(
            "silver_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.SILVER, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> GOLD_WORKBENCH = MENUS.register(
            "gold_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.GOLD, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> IRON_WORKBENCH = MENUS.register(
            "iron_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.IRON, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> ANCIENT_METAL_WORKBENCH =
            MENUS.register(
                    "ancient_metal_workbench",
                    () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> TimedWorkbenchMenu.client(
                            containerId, inventory, BenchTier.ANCIENT_METAL, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> MITHRIL_WORKBENCH = MENUS.register(
            "mithril_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.MITHRIL, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> ADAMANTIUM_WORKBENCH =
            MENUS.register(
                    "adamantium_workbench",
                    () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> TimedWorkbenchMenu.client(
                            containerId, inventory, BenchTier.ADAMANTIUM, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> OBSIDIAN_WORKBENCH = MENUS.register(
            "obsidian_workbench",
            () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
                    TimedWorkbenchMenu.client(containerId, inventory, BenchTier.OBSIDIAN, buffer)));

    public static final List<DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>>> WORKBENCHES = List.of(
            FLINT_WORKBENCH,
            COPPER_WORKBENCH,
            SILVER_WORKBENCH,
            GOLD_WORKBENCH,
            IRON_WORKBENCH,
            ANCIENT_METAL_WORKBENCH,
            MITHRIL_WORKBENCH,
            ADAMANTIUM_WORKBENCH,
            OBSIDIAN_WORKBENCH);

    private ModMenus() {}

    public static DeferredHolder<MenuType<?>, MenuType<TimedWorkbenchMenu>> workbench(BenchTier tier) {
        return switch (tier) {
            case FLINT -> FLINT_WORKBENCH;
            case COPPER -> COPPER_WORKBENCH;
            case SILVER -> SILVER_WORKBENCH;
            case GOLD -> GOLD_WORKBENCH;
            case IRON -> IRON_WORKBENCH;
            case ANCIENT_METAL -> ANCIENT_METAL_WORKBENCH;
            case MITHRIL -> MITHRIL_WORKBENCH;
            case ADAMANTIUM -> ADAMANTIUM_WORKBENCH;
            case OBSIDIAN -> OBSIDIAN_WORKBENCH;
            case HAND -> throw new IllegalArgumentException("Hand crafting has no workbench menu");
        };
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
