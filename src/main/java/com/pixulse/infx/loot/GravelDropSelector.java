package com.pixulse.infx.loot;

import java.util.function.IntUnaryOperator;

public final class GravelDropSelector {
    private GravelDropSelector() {}

    public static GravelDrop select(int fortune, IntUnaryOperator nextInt) {
        int supportedFortune = Math.clamp(fortune, 0, 3);
        if (nextInt.applyAsInt(12 - supportedFortune * 2) > 2) {
            return GravelDrop.GRAVEL;
        }
        if (nextInt.applyAsInt(3) > 0) {
            return nextInt.applyAsInt(16) == 0 ? GravelDrop.FLINT : GravelDrop.FLINT_CHIP;
        }
        if (nextInt.applyAsInt(3) > 0) {
            return GravelDrop.COPPER_NUGGET;
        }
        if (nextInt.applyAsInt(3) > 0) {
            return GravelDrop.SILVER_NUGGET;
        }
        if (nextInt.applyAsInt(3) > 0) {
            return GravelDrop.GOLD_NUGGET;
        }
        if (nextInt.applyAsInt(3) > 0) {
            return GravelDrop.OBSIDIAN_SHARD;
        }
        if (nextInt.applyAsInt(3) > 0) {
            return GravelDrop.EMERALD_SHARD;
        }
        if (nextInt.applyAsInt(3) > 0) {
            return GravelDrop.GRAVEL;
        }
        if (nextInt.applyAsInt(3) > 0) {
            return GravelDrop.MITHRIL_NUGGET;
        }
        return GravelDrop.ADAMANTIUM_NUGGET;
    }
}
