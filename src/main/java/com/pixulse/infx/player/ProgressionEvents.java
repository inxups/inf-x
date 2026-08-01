package com.pixulse.infx.player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.world.CreationBooks;
import com.pixulse.infx.world.Underworld;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Awards the INFX stage-line advancement criteria that cannot use recipe triggers. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ProgressionEvents {
    private static final String CREATION_BOOKS_READ = "infx_creation_books_read";

    private ProgressionEvents() {}

    public static void award(ServerPlayer player, String path, String criterion) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements()
                .get(InfiniteX.id("progression/" + path));
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmountRemoved() <= 0) {
            return;
        }
        ItemStack result = event.getSmelting();
        if (result.is(Items.IRON_INGOT)) award(player, "iron_age", "smelted_iron");
        if (result.is(InfXItems.MITHRIL_INGOT)) award(player, "mithril_age", "smelted_mithril");
        if (result.is(InfXItems.ADAMANTIUM_INGOT)) award(player, "adamantium_age", "smelted_adamantium");
    }

    @SubscribeEvent
    public static void onWrittenBookOpened(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getItemStack().is(Items.WRITTEN_BOOK)) {
            return;
        }
        var content = event.getItemStack().get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (content == null) return;
        int index = creationBookIndex(content.author(), content.title().raw());
        if (index < 0) return;

        var data = player.getPersistentData();
        int previous = data.getInt(CREATION_BOOKS_READ).orElse(0);
        int updated = previous | 1 << index;
        if (updated == previous) return;
        data.putInt(CREATION_BOOKS_READ, updated);
        player.giveExperiencePoints(100);
        if (allCreationBooksRead(updated)) {
            award(player, "enlightenment", "read_nine_books");
        }
    }

    static int creationBookIndex(String author, String title) {
        return CreationBooks.index(author, title);
    }

    static boolean allCreationBooksRead(int mask) {
        return CreationBooks.complete(mask);
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getTo().equals(Level.END)) {
            award(player, "the_end", "entered_end");
        } else if (completedEndReturn(player, event.getFrom())) {
            award(player, "the_end2", "returned_from_end");
        } else if (event.getTo().equals(Underworld.LEVEL)) {
            award(player, "underworld", "entered_underworld");
        } else if (event.getTo().equals(Level.NETHER)) {
            award(player, "nether", "entered_nether");
        }
    }

    private static boolean completedEndReturn(
            ServerPlayer player, net.minecraft.resources.ResourceKey<Level> from) {
        var end = player.level().getServer().getLevel(Level.END);
        var fight = end == null ? null : end.getDragonFight();
        return shouldAwardEndReturn(
                from.equals(Level.END),
                player.seenCredits,
                fight != null && fight.hasPreviouslyKilledDragon());
    }

    static boolean shouldAwardEndReturn(boolean fromEnd, boolean seenCredits, boolean dragonKilled) {
        return fromEnd && seenCredits && dragonKilled;
    }
}
