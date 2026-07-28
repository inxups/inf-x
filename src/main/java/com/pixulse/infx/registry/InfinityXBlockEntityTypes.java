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

public final class InfinityXBlockEntityTypes {
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, InfiniteX.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MiteFurnaceBlockEntity>> FURNACE =
            TYPES.register("furnace", () -> new BlockEntityType<>(
                    MiteFurnaceBlockEntity::new,
                    InfinityXBlocks.CLAY_FURNACE.get(),
                    InfinityXBlocks.LARGE_CLAY_OVEN.get(),
                    InfinityXBlocks.SANDSTONE_FURNACE.get(),
                    InfinityXBlocks.HARDENED_CLAY_FURNACE.get(),
                    InfinityXBlocks.OBSIDIAN_FURNACE.get(),
                    InfinityXBlocks.NETHERRACK_FURNACE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalAnvilBlockEntity>> METAL_ANVIL =
            TYPES.register("metal_anvil", () -> new BlockEntityType<>(
                    MetalAnvilBlockEntity::new,
                    InfinityXBlocks.COPPER_ANVIL.get(),
                    InfinityXBlocks.SILVER_ANVIL.get(),
                    InfinityXBlocks.GOLD_ANVIL.get(),
                    InfinityXBlocks.IRON_ANVIL.get(),
                    InfinityXBlocks.ANCIENT_METAL_ANVIL.get(),
                    InfinityXBlocks.MITHRIL_ANVIL.get(),
                    InfinityXBlocks.ADAMANTIUM_ANVIL.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SafeBlockEntity>> SAFE =
            TYPES.register("safe", () -> new BlockEntityType<>(
                    SafeBlockEntity::new,
                    InfinityXBlocks.COPPER_SAFE.get(),
                    InfinityXBlocks.SILVER_SAFE.get(),
                    InfinityXBlocks.GOLD_SAFE.get(),
                    InfinityXBlocks.IRON_SAFE.get(),
                    InfinityXBlocks.ANCIENT_METAL_SAFE.get(),
                    InfinityXBlocks.MITHRIL_SAFE.get(),
                    InfinityXBlocks.ADAMANTIUM_SAFE.get()));

    private InfinityXBlockEntityTypes() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(InfinityXBlockEntityTypes::addVanillaBlockEntityBlocks);
    }

    private static void addVanillaBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BlockEntityType.ENCHANTING_TABLE,
                InfinityXBlocks.EMERALD_ENCHANTING_TABLE.get(),
                InfinityXBlocks.DIAMOND_ENCHANTING_TABLE.get());
    }
}
