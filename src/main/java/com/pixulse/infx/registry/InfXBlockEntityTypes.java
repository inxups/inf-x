package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.entity.MiteFurnaceBlockEntity;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

public final class InfXBlockEntityTypes {
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MiteFurnaceBlockEntity>> FURNACE =
            TYPES.register("furnace", () -> new BlockEntityType<>(
                    MiteFurnaceBlockEntity::new,
                    InfXBlocks.CLAY_FURNACE.get(),
                    InfXBlocks.LARGE_CLAY_OVEN.get(),
                    InfXBlocks.SANDSTONE_FURNACE.get(),
                    InfXBlocks.HARDENED_CLAY_FURNACE.get(),
                    InfXBlocks.OBSIDIAN_FURNACE.get(),
                    InfXBlocks.NETHERRACK_FURNACE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalAnvilBlockEntity>> METAL_ANVIL =
            TYPES.register("metal_anvil", () -> new BlockEntityType<>(
                    MetalAnvilBlockEntity::new,
                    InfXBlocks.COPPER_ANVIL.get(),
                    InfXBlocks.SILVER_ANVIL.get(),
                    InfXBlocks.GOLD_ANVIL.get(),
                    InfXBlocks.IRON_ANVIL.get(),
                    InfXBlocks.ANCIENT_METAL_ANVIL.get(),
                    InfXBlocks.MITHRIL_ANVIL.get(),
                    InfXBlocks.ADAMANTIUM_ANVIL.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SafeBlockEntity>> SAFE =
            TYPES.register("safe", () -> new BlockEntityType<>(
                    SafeBlockEntity::new,
                    InfXBlocks.COPPER_SAFE.get(),
                    InfXBlocks.SILVER_SAFE.get(),
                    InfXBlocks.GOLD_SAFE.get(),
                    InfXBlocks.IRON_SAFE.get(),
                    InfXBlocks.ANCIENT_METAL_SAFE.get(),
                    InfXBlocks.MITHRIL_SAFE.get(),
                    InfXBlocks.ADAMANTIUM_SAFE.get()));

    private InfXBlockEntityTypes() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(InfXBlockEntityTypes::addVanillaBlockEntityBlocks);
    }

    private static void addVanillaBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BlockEntityType.ENCHANTING_TABLE,
                InfXBlocks.EMERALD_ENCHANTING_TABLE.get(),
                InfXBlocks.DIAMOND_ENCHANTING_TABLE.get());
    }
}
