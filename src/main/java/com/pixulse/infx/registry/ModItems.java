package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.ToolWearCalculator;
import com.pixulse.infx.item.R196ToolItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(InfiniteX.MOD_ID);

    public static final DeferredItem<Item> FLINT_CHIP = ITEMS.registerSimpleItem("flint_chip");
    public static final DeferredItem<Item> SINEW = ITEMS.registerSimpleItem("sinew");
    public static final DeferredItem<Item> OBSIDIAN_SHARD = ITEMS.registerSimpleItem("obsidian_shard");
    public static final DeferredItem<Item> EMERALD_SHARD = ITEMS.registerSimpleItem("emerald_shard");
    public static final DeferredItem<Item> SILVER_NUGGET = ITEMS.registerSimpleItem("silver_nugget");
    public static final DeferredItem<Item> MITHRIL_NUGGET = ITEMS.registerSimpleItem("mithril_nugget");
    public static final DeferredItem<Item> ADAMANTIUM_NUGGET = ITEMS.registerSimpleItem("adamantium_nugget");

    public static final DeferredItem<BlockItem> FLINT_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.FLINT_WORKBENCH);
    public static final DeferredItem<BlockItem> COPPER_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.COPPER_WORKBENCH);

    public static final DeferredItem<R196ToolItem> FLINT_HATCHET = ITEMS.registerItem(
            "flint_hatchet",
            properties -> new R196ToolItem(ToolWearCalculator.FLINT_HATCHET_DECAY, properties),
            properties -> ModToolProperties.flintHatchet(properties).repairable(Items.FLINT));

    public static final DeferredItem<R196ToolItem> COPPER_PICKAXE = ITEMS.registerItem(
            "copper_pickaxe",
            properties -> new R196ToolItem(ToolWearCalculator.PICKAXE_DECAY, properties),
            ModToolProperties::copperPickaxe);

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
