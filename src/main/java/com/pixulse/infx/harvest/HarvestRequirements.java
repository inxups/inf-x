package com.pixulse.infx.harvest;

import com.pixulse.infx.tag.ModTags;
import java.util.OptionalInt;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

/** Resolves MITE's numeric block harvest level without replacing vanilla tags. */
public final class HarvestRequirements {
    public static final int MAX_LEVEL = 6;

    private HarvestRequirements() {}

    public static int requiredLevel(BlockState state) {
        OptionalInt explicit = explicitLevel(state);
        return inferLevel(explicit, state.is(BlockTags.LOGS), state.is(BlockTags.MINEABLE_WITH_PICKAXE));
    }

    public static OptionalInt explicitLevel(BlockState state) {
        if (state.is(ModTags.Blocks.requiredLevel(0))) {
            return OptionalInt.of(0);
        }
        for (int level = MAX_LEVEL; level >= 1; level--) {
            if (state.is(ModTags.Blocks.requiredLevel(level))) {
                return OptionalInt.of(level);
            }
        }
        return OptionalInt.empty();
    }

    public static int explicitLevelCount(BlockState state) {
        int matches = 0;
        for (int level = 0; level <= MAX_LEVEL; level++) {
            if (state.is(ModTags.Blocks.requiredLevel(level))) {
                matches++;
            }
        }
        return matches;
    }

    static int inferLevel(OptionalInt explicitLevel, boolean log, boolean mineableWithPickaxe) {
        if (explicitLevel.isPresent()) {
            return explicitLevel.getAsInt();
        }
        if (log) {
            return 1;
        }
        return mineableWithPickaxe ? 2 : 0;
    }
}
