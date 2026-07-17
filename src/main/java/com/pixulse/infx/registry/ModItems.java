package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.item.R196ToolItem;
import com.pixulse.infx.material.R196Material;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(InfiniteX.MOD_ID);

    public static final DeferredItem<BlockItem> FLINT_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.FLINT_WORKBENCH);
    public static final DeferredItem<BlockItem> COPPER_WORKBENCH =
            ITEMS.registerSimpleBlockItem(ModBlocks.COPPER_WORKBENCH);

    private static final R196Catalog CATALOG = R196Catalog.register(ITEMS);

    public static final DeferredItem<Item> FLINT_CHIP = CATALOG.raw("flint_chip").holderAs(Item.class);
    public static final DeferredItem<Item> SINEW = CATALOG.raw("sinew").holderAs(Item.class);
    public static final DeferredItem<Item> OBSIDIAN_SHARD = CATALOG.raw("obsidian_shard").holderAs(Item.class);
    public static final DeferredItem<Item> EMERALD_SHARD = CATALOG.raw("emerald_shard").holderAs(Item.class);
    public static final DeferredItem<Item> SILVER_NUGGET = CATALOG.raw("silver_nugget").holderAs(Item.class);
    public static final DeferredItem<Item> MITHRIL_NUGGET = CATALOG.raw("mithril_nugget").holderAs(Item.class);
    public static final DeferredItem<Item> ADAMANTIUM_NUGGET = CATALOG.raw("adamantium_nugget").holderAs(Item.class);
    public static final DeferredItem<R196ToolItem> FLINT_HATCHET =
            CATALOG.equipment(R196Material.FLINT, R196EquipmentType.HATCHET).holderAs(R196ToolItem.class);
    public static final DeferredItem<R196ToolItem> COPPER_PICKAXE =
            CATALOG.equipment(R196Material.COPPER, R196EquipmentType.PICKAXE).holderAs(R196ToolItem.class);

    private ModItems() {}

    public static R196Catalog catalog() {
        return CATALOG;
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
