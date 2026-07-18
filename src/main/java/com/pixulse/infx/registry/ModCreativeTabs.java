package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, InfiniteX.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.infx"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(ModItems.FLINT_CHIP::toStack)
                    .displayItems((parameters, output) -> {
                        ModItems.catalog().rawEntries().forEach(entry -> output.accept(entry.holder().value()));
                        ModItems.WORKBENCHES.forEach(workbench -> output.accept(workbench.value()));
                        ModItems.FURNACES.forEach(furnace -> output.accept(furnace.value()));
                        ModItems.catalog().equipmentEntries().forEach(entry -> output.accept(entry.holder().value()));
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
