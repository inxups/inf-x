package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/** Restores InfX stack limits for vanilla items that are not ordinary block items. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class InfXVanillaItemStackLimits {
    private static final int DEFAULT_ITEM_LIMIT = 16;

    private static final Set<Item> STACK_ONE = Set.of(Items.WRITTEN_BOOK);
    private static final Set<Item> STACK_FOUR = Set.of(
            Items.MUSHROOM_STEW,
            Items.RABBIT_STEW,
            Items.BEETROOT_SOUP,
            Items.SUSPICIOUS_STEW);
    private static final Set<Item> STACK_EIGHT = Set.of(
            Items.BUCKET,
            Items.GLASS_BOTTLE,
            Items.BRICK,
            Items.NETHER_BRICK,
            Items.RESIN_BRICK,
            Items.IRON_INGOT,
            Items.COPPER_INGOT,
            Items.GOLD_INGOT,
            Items.NETHERITE_INGOT,
            Items.PUMPKIN_PIE);
    private static final Set<Item> STACK_THIRTY_TWO = Set.of(Items.DIAMOND, Items.EMERALD);
    private static final Set<Item> STACK_SIXTY_FOUR = Set.of(
            Items.WHEAT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.MELON_SEEDS,
            Items.TORCHFLOWER_SEEDS,
            Items.PITCHER_POD,
            Items.BEETROOT_SEEDS,
            Items.NETHER_WART,
            Items.PAPER,
            Items.GOLD_NUGGET,
            Items.IRON_NUGGET,
            Items.COPPER_NUGGET,
            Items.AMETHYST_SHARD,
            Items.PRISMARINE_SHARD,
            Items.ECHO_SHARD,
            Items.DISC_FRAGMENT_5);

    private InfXVanillaItemStackLimits() {}

    @SubscribeEvent
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
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
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        if (!isVanilla(itemId) || item == Items.AIR || item == Items.STICK || item == Items.BONE) {
            return OptionalInt.empty();
        }

        if (item instanceof BlockItem blockItem) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
            if (itemId.equals(blockId) && item != Items.NETHER_WART) {
                return OptionalInt.empty();
            }
        }

        int limit = sourceLimit(item);
        if (STACK_FOUR.contains(item)) {
            // InfX's ItemBowl uses four slots for every filled bowl meal.
            return OptionalInt.of(limit);
        }
        return OptionalInt.of(Math.min(limit, currentLimit));
    }

    private static boolean isVanilla(Identifier id) {
        return id != null && id.getNamespace().equals("minecraft");
    }

    private static int sourceLimit(Item item) {
        if (STACK_ONE.contains(item)) return 1;
        if (STACK_FOUR.contains(item)) return 4;
        if (STACK_EIGHT.contains(item)) return 8;
        if (STACK_THIRTY_TWO.contains(item)) return 32;
        if (STACK_SIXTY_FOUR.contains(item)) return 64;
        return DEFAULT_ITEM_LIMIT;
    }
}
