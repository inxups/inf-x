package com.pixulse.infx.world;

import com.pixulse.infx.registry.InfXBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * MITE-aligned mushroom growth mechanics shared by the {@code MushroomBlock} random-tick mixin,
 * the manure item handler and the game tests. MITE tracks mushroom growth in a four-tier counter
 * ({@code GROWTH} 0-3); tier 3 is mature and converts the small mushroom into a huge one.
 * Mirrors {@code BlockMushroom.canConvertBlockBelowToMycelium} and
 * {@code BlockMushroom.tryGrowGiantMushroom} from the MITE reference.
 */
public final class InfXMushroomGrowth {
    /** Growth tier shared by brown/red mushrooms; 3 is mature and converts to a huge mushroom. */
    public static final IntegerProperty GROWTH = IntegerProperty.create("growth", 0, 3);
    /** MITE {@code BlockMycelium.getLightValueTolerance()}, the brown-mushroom light ceiling. */
    private static final int MAX_MUSHROOM_LIGHT = 13;

    private InfXMushroomGrowth() {}

    /**
     * Whether a position is "indoor" (not under open sky). No-sky dimensions (nether/end/
     * underworld) are always indoor; otherwise the motion-blocking heightmap above the column
     * decides. For a non-solid mushroom the heightmap reporting the position's own level counts
     * as open sky, so the comparison is {@code > pos.getY()}.
     */
    public static boolean isIndoor(LevelReader level, BlockPos pos) {
        if (!level.dimensionType().hasSkyLight() || level.dimensionType().hasCeiling()) {
            return true;
        }
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY();
    }

    /**
     * MITE {@code BlockMushroom.canConvertBlockBelowToMycelium}: a brown mushroom on moist
     * fertilized farmland in a dark covered spot converts the farmland into mycelium. InfX
     * represents MITE's fertilized farmland bit as the distinct {@code InfXFertileFarmland} block.
     */
    public static boolean canConvertBlockBelowToMycelium(ServerLevel level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(InfXBlocks.FERTILE_FARMLAND)) {
            return false;
        }
        if (!below.hasProperty(FarmlandBlock.MOISTURE) || below.getValue(FarmlandBlock.MOISTURE) <= 0) {
            return false;
        }
        return isIndoor(level, pos) && level.getRawBrightness(pos, 0) <= MAX_MUSHROOM_LIGHT;
    }

    /**
     * MITE {@code BlockMushroom.tryGrowGiantMushroom} grow eligibility shared by the random-tick
     * mixin, the manure handler (which only consumes on a valid target) and the game tests: a
     * brown mushroom needs mycelium below in a covered, dim (≤ 13) spot; a red mushroom needs
     * grass below in the open. Works on any {@link Level}, client or server.
     */
    public static boolean isGrowableAt(Level level, BlockPos pos, BlockState state) {
        boolean brown = state.is(Blocks.BROWN_MUSHROOM);
        boolean red = state.is(Blocks.RED_MUSHROOM);
        if (!brown && !red) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        boolean indoor = isIndoor(level, pos);
        if (brown) {
            return below.is(Blocks.MYCELIUM) && indoor && level.getRawBrightness(pos, 0) <= MAX_MUSHROOM_LIGHT;
        }
        return below.is(Blocks.GRASS_BLOCK) && !indoor;
    }

    /**
     * MITE {@code BlockMushroom.tryGrowGiantMushroom}. Growth below tier 3 increments one tier; at
     * tier 3 the small mushroom is removed and the vanilla huge-mushroom feature is placed (which
     * restores the mature state when the space is insufficient).
     */
    public static boolean tryGrowGiantMushroom(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (!isGrowableAt(level, pos, state)) {
            return false;
        }
        int growth = state.getValue(GROWTH);
        if (growth < 3) {
            level.setBlock(pos, state.setValue(GROWTH, growth + 1), 6);
            return true;
        }
        return ((MushroomBlock) state.getBlock()).growMushroom(level, pos, state, random);
    }
}
