package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.MetalAnvilBlock;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/** MITE R196 stack limits for block inventory objects and their modern structural equivalents. */
public final class R196BlockStackLimits {
    private static final int DEFAULT_BLOCK_LIMIT = 4;
    private static final Set<Item> STANDALONE_SIXTEEN = Set.of(
            Items.SUGAR_CANE,
            Items.REPEATER,
            Items.COMPARATOR,
            Items.BREWING_STAND,
            Items.FLOWER_POT);
    private static final Set<Item> STANDALONE_EIGHT = Set.of(
            Items.CAKE,
            Items.MELON,
            Items.CARVED_PUMPKIN,
            Items.JACK_O_LANTERN);
    private static final Set<Item> NON_BLOCK_INVENTORY_OBJECTS = Set.of(Items.NETHER_WART);

    private R196BlockStackLimits() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(R196BlockStackLimits::modifyDefaultComponents);
    }

    private static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (limit(item, 64).isEmpty()) continue;
            event.modify(item, (components, context, modifiedItem) -> {
                int currentLimit = components.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
                limit(modifiedItem, currentLimit)
                        .ifPresent(limit -> components.set(DataComponents.MAX_STACK_SIZE, limit));
            });
        }
    }

    static OptionalInt limit(Item item, int currentLimit) {
        if (!(item instanceof BlockItem blockItem)) return OptionalInt.empty();

        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        if (!isOwnedNamespace(itemId) || !itemId.equals(blockId) || NON_BLOCK_INVENTORY_OBJECTS.contains(item)) {
            return OptionalInt.empty();
        }

        int miteLimit = sourceLimit(item, blockItem.getBlock(), itemId.getPath());
        int safeLimit = item == Items.CAKE
                ? miteLimit
                : Math.min(miteLimit, currentLimit);
        return OptionalInt.of(safeLimit);
    }

    private static boolean isOwnedNamespace(Identifier id) {
        return id.getNamespace().equals("minecraft") || id.getNamespace().equals(InfiniteX.MOD_ID);
    }

    private static int sourceLimit(Item item, Block block, String path) {
        // BlockFurnace/BlockAnvil and the standalone ItemDoor/ItemBed use one slot in R196.
        if (block instanceof AbstractFurnaceBlock
                || block instanceof AnvilBlock
                || block instanceof MetalAnvilBlock
                || block instanceof DoorBlock
                || block instanceof BedBlock) {
            return 1;
        }
        // ItemBlock inherits these overrides from BlockPane, BlockSapling and BlockTorch;
        // signs, skulls and ItemReed-backed placers retain Item's R196 default of sixteen.
        if (STANDALONE_SIXTEEN.contains(item)
                || block instanceof IronBarsBlock
                || block instanceof SaplingBlock
                || block instanceof BaseTorchBlock
                || block instanceof SignBlock
                || block instanceof AbstractSkullBlock) {
            return 16;
        }
        // These are the R196 eight-stack block subclasses plus the explicit cake item override.
        if (STANDALONE_EIGHT.contains(item)
                || path.endsWith("_planks")
                || path.endsWith("_wool")
                || block instanceof CarpetBlock
                || block instanceof BasePressurePlateBlock
                || block instanceof FenceBlock
                || block instanceof LadderBlock
                || block instanceof PumpkinBlock
                || block instanceof BaseRailBlock
                || block instanceof SlabBlock
                || block instanceof VineBlock
                || block instanceof WallBlock
                || block instanceof GrowingPlantBlock) {
            return 8;
        }
        // BlockPlant and BlockSnow override the four-stack Block default with thirty-two.
        if (block instanceof VegetationBlock || block instanceof SnowLayerBlock) {
            return 32;
        }
        return DEFAULT_BLOCK_LIMIT;
    }
}
