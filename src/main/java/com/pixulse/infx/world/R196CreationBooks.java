package com.pixulse.infx.world;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

public final class R196CreationBooks {
    public static final String AUTHOR = "Father Phoonzang";
    public static final List<String> TITLES = List.of(
            "Boat", "Crypt", "Crystal", "Dragon", "Globe", "Serpent", "Sphinx", "Star", "Temple");

    private R196CreationBooks() {}

    public static ItemStack create(int index) {
        String title = TITLES.get(index);
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(
                DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(
                        Filterable.passThrough(title),
                        AUTHOR,
                        0,
                        List.of(Filterable.passThrough(Component.translatable(
                                "book.infx.creation." + title.toLowerCase(java.util.Locale.ROOT)))),
                        true));
        return stack;
    }

    public static int index(String author, String title) {
        return AUTHOR.equals(author) ? TITLES.indexOf(title) : -1;
    }

    public static boolean complete(int mask) {
        int all = (1 << TITLES.size()) - 1;
        return (mask & all) == all;
    }
}
