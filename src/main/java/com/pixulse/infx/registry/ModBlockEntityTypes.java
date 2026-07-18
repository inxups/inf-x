package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.entity.R196FurnaceBlockEntity;
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

    private ModBlockEntityTypes() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
