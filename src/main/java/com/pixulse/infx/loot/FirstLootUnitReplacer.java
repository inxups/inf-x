package com.pixulse.infx.loot;

import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class FirstLootUnitReplacer {
    private FirstLootUnitReplacer() {}

    public static <T> boolean replace(
            List<T> loot,
            Predicate<? super T> isBaseDrop,
            ToIntFunction<? super T> count,
            ObjIntConsumer<T> setCount,
            T replacement) {
        for (int index = 0; index < loot.size(); index++) {
            T stack = loot.get(index);
            int stackCount = count.applyAsInt(stack);
            if (stackCount <= 0 || !isBaseDrop.test(stack)) {
                continue;
            }
            if (stackCount == 1) {
                loot.set(index, replacement);
            } else {
                setCount.accept(stack, stackCount - 1);
                loot.add(index, replacement);
            }
            return true;
        }
        return false;
    }
}
