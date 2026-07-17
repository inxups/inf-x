package com.pixulse.infx.crafting;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class CraftingOutputDistributor {
    private CraftingOutputDistributor() {}

    public static <T> void giveOrDrop(T result, Predicate<T> addToInventory, Consumer<T> dropAtPlayer) {
        if (!addToInventory.test(result)) {
            dropAtPlayer.accept(result);
        }
    }
}
