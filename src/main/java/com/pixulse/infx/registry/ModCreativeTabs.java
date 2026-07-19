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
                        ModItems.ORES.forEach(ore -> output.accept(ore.value()));
                        ModItems.METAL_STORAGE_BLOCKS.forEach(block -> output.accept(block.value()));
                        ModItems.WORKBENCHES.forEach(workbench -> output.accept(workbench.value()));
                        ModItems.FURNACES.forEach(furnace -> output.accept(furnace.value()));
                        ModItems.METAL_ANVILS.forEach(anvil -> output.accept(anvil.value()));
                        ModItems.ENCHANTING_TABLES.forEach(table -> output.accept(table.value()));
                        ModItems.METAL_SAFES.forEach(safe -> output.accept(safe.value()));
                        ModItems.R196_FLOWERS.forEach(flower -> output.accept(flower.value()));
                        ModItems.FULLTEXT_BLOCKS.forEach(block -> output.accept(block.value()));
                        ModItems.R196_BUCKETS.forEach(bucket -> output.accept(bucket.value()));
                        output.accept(ModItems.BOTTLE_OF_DISENCHANTING.value());
                        ModItems.R196_RECORDS.forEach(record -> output.accept(record.value()));
                        output.accept(ModItems.FLOUR.value());
                        output.accept(ModItems.WATER_BOWL.value());
                        ModItems.R196_FOODS.forEach(food -> output.accept(food.value()));
                        ModItems.catalog().equipmentEntries().forEach(entry -> output.accept(entry.holder().value()));
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
