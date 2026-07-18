package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.entity.R196FurnaceBlockEntity;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntityTypes {
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<R196FurnaceBlockEntity>> FURNACE =
            TYPES.register("furnace", () -> new BlockEntityType<>(
                    R196FurnaceBlockEntity::new,
                    ModBlocks.CLAY_FURNACE.get(),
                    ModBlocks.SANDSTONE_FURNACE.get(),
                    ModBlocks.HARDENED_CLAY_FURNACE.get(),
                    ModBlocks.OBSIDIAN_FURNACE.get(),
                    ModBlocks.NETHERRACK_FURNACE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalAnvilBlockEntity>> METAL_ANVIL =
            TYPES.register("metal_anvil", () -> new BlockEntityType<>(
                    MetalAnvilBlockEntity::new,
                    ModBlocks.COPPER_ANVIL.get(),
                    ModBlocks.SILVER_ANVIL.get(),
                    ModBlocks.GOLD_ANVIL.get(),
                    ModBlocks.IRON_ANVIL.get(),
                    ModBlocks.ANCIENT_METAL_ANVIL.get(),
                    ModBlocks.MITHRIL_ANVIL.get(),
                    ModBlocks.ADAMANTIUM_ANVIL.get()));

    private ModBlockEntityTypes() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
