package com.pixulse.infx.world;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * InfX's two fire rates and the arithmetic used by its fire update.
 *
 * <p>The first value is the chance that a block encourages fire in a neighbouring air cell
 * ({@code chanceToEncourageFire}); the second is the chance that a fire directly consumes the
 * block ({@code abilityToCatchFire}). NeoForge exposes the same two concepts as fire spread speed
 * and flammability, respectively.
 */
public final class InfXFireSpreadRules {
    private static final int MAX_FIRE_AGE = 15;
    private static final int AIR_SPREAD_BASE_DENOMINATOR = 100;
    private static final int AIR_SPREAD_HEIGHT_STEP = 100;
    private static final int AIR_SPREAD_BONUS = 40;
    private static final int DIFFICULTY_BONUS = 7;
    private static final int HUMIDITY_PENALTY = 50;

    private static final Map<Block, FireRate> RATES = createRates();

    private InfXFireSpreadRules() {}

    /** The immutable InfX rate table, keyed by block identity. */
    public static Map<Block, FireRate> rates() {
        return RATES;
    }

    /** Returns InfX's chance-to-encourage-fire value, or zero for an unlisted block. */
    public static int chanceToEncourageFire(Block block) {
        FireRate rate = rate(block);
        return rate == null ? 0 : rate.encouragement();
    }

    /** Returns InfX's ability-to-catch-fire value, or zero for an unlisted block. */
    public static int abilityToCatchFire(Block block) {
        FireRate rate = rate(block);
        return rate == null ? 0 : rate.flammability();
    }

    /** Resolves a vanilla or InfX block to its InfX rate without loading deferred registers early. */
    public static FireRate rate(Block block) {
        FireRate rate = RATES.get(block);
        if (rate != null) return rate;

        var id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null || !"infx".equals(id.getNamespace())) return null;
        return switch (id.getPath()) {
            case "witherwood", "blueberry_bush" -> new FireRate(30, 50);
            case "infx_wheat", "infx_carrots", "infx_potatoes", "infx_beetroots" -> new FireRate(60, 100);
            default -> null;
        };
    }

    /** Returns the InfX/InfX spread speed for a listed state, preserving waterlogged blocks as inert. */
    public static int fireSpreadSpeed(BlockState state) {
        if (isWaterlogged(state)) return 0;
        return chanceToEncourageFire(state.getBlock());
    }

    /** Returns the InfX/InfX flammability for a listed state, preserving waterlogged blocks as inert. */
    public static int flammability(BlockState state) {
        if (isWaterlogged(state)) return 0;
        return abilityToCatchFire(state.getBlock());
    }

    /** InfX's direct six-direction denominator, including the high-humidity penalty. */
    public static int directCatchDenominator(Direction direction, boolean humid) {
        int base = direction.getAxis() == Direction.Axis.Y ? 250 : 300;
        return Math.max(1, base - (humid ? HUMIDITY_PENALTY : 0));
    }

    /** InfX's height-scaled denominator for an air cell relative to the source fire. */
    public static int airSpreadDenominator(int relativeY) {
        return AIR_SPREAD_BASE_DENOMINATOR
                + Math.max(0, relativeY - 1) * AIR_SPREAD_HEIGHT_STEP;
    }

    /**
     * InfX's air-spread numerator. Integer division is deliberate: this is the original game's
     * integer probability calculation, not a floating-point approximation.
     */
    public static int airSpreadOdds(int maximumEncouragement, int age, Difficulty difficulty, boolean humid) {
        return airSpreadOdds(maximumEncouragement, age, difficulty.getId(), humid);
    }

    public static int airSpreadOdds(int maximumEncouragement, int age, int difficultyId, boolean humid) {
        int odds = (maximumEncouragement + AIR_SPREAD_BONUS + difficultyId * DIFFICULTY_BONUS)
                / (Math.max(0, age) + 30);
        return humid ? odds / 2 : odds;
    }

    /** InfX advances a flame by one age with one of the three random rolls. */
    public static int nextAge(int age, int randomRoll) {
        int increment = Math.floorDiv(Math.max(0, randomRoll), 2);
        return Math.min(MAX_FIRE_AGE, Math.max(0, age) + increment);
    }

    /** InfX's inherited age for a newly spread flame (four rolls keep the source age unchanged). */
    public static int inheritedAge(int age, int randomRoll) {
        return Math.min(MAX_FIRE_AGE, Math.max(0, age) + Math.floorDiv(Math.max(0, randomRoll), 4));
    }

    /** Finds the maximum InfX encouragement around an empty air cell. */
    public static int maximumNeighbourEncouragement(LevelReader level, BlockPos pos) {
        if (!level.isEmptyBlock(pos)) return 0;

        int maximum = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            BlockState state = level.getBlockState(neighbour);
            int value = rateForSpread(state, level, neighbour, direction.getOpposite());
            maximum = Math.max(maximum, value);
        }
        return maximum;
    }

    /** InfX's exposed-rain check for the source and all four horizontal neighbours. */
    public static boolean isNearRain(Level level, BlockPos pos) {
        return level.isRainingAt(pos)
                || level.isRainingAt(pos.west())
                || level.isRainingAt(pos.east())
                || level.isRainingAt(pos.north())
                || level.isRainingAt(pos.south());
    }

    /** Runs the InfX fire update; called from the smallest possible FireBlock mixin. */
    public static void tick(FireBlock fire, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, fire, 30 + level.getRandom().nextInt(10));
        if (!level.canSpreadFireAround(pos)) return;

        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && !hasFlammableNeighbour(level, pos)) {
            level.removeBlock(pos, false);
            return;
        }

        boolean permanent = isPermanentFireBase(level, below);
        int age = state.getValue(FireBlock.AGE);

        if (!permanent && level.isRaining() && isNearRain(level, pos)) {
            level.removeBlock(pos, false);
            return;
        }

        int newAge = nextAge(age, random.nextInt(3));
        if (newAge != age) {
            state = state.setValue(FireBlock.AGE, newAge);
            level.setBlock(pos, state, 260);
        }

        if (!permanent) {
            if (!hasFlammableNeighbour(level, pos)) {
                if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) || age > 3) {
                    level.removeBlock(pos, false);
                }
                return;
            }

            if (age == MAX_FIRE_AGE
                    && random.nextInt(4) == 0
                    && !canEncourageFire(level, below, Direction.UP)) {
                level.removeBlock(pos, false);
                return;
            }
        }

        boolean humid = level.environmentAttributes().getValue(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, pos);
        catchNeighbour(fire, level, pos.east(), Direction.WEST, directCatchDenominator(Direction.EAST, humid), random, age);
        catchNeighbour(fire, level, pos.west(), Direction.EAST, directCatchDenominator(Direction.WEST, humid), random, age);
        catchNeighbour(fire, level, below, Direction.UP, directCatchDenominator(Direction.DOWN, humid), random, age);
        catchNeighbour(fire, level, pos.above(), Direction.DOWN, directCatchDenominator(Direction.UP, humid), random, age);
        catchNeighbour(fire, level, pos.north(), Direction.SOUTH, directCatchDenominator(Direction.NORTH, humid), random, age);
        catchNeighbour(fire, level, pos.south(), Direction.NORTH, directCatchDenominator(Direction.SOUTH, humid), random, age);

        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 4; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    testPos.setWithOffset(pos, dx, dy, dz);
                    int encouragement = maximumNeighbourEncouragement(level, testPos);
                    if (encouragement <= 0) continue;

                    int odds = airSpreadOdds(encouragement, age, level.getDifficulty(), humid);
                    if (odds <= 0
                            || random.nextInt(airSpreadDenominator(dy)) > odds
                            || (level.isRaining() && isNearRain(level, testPos))) {
                        continue;
                    }

                    int spreadAge = inheritedAge(age, random.nextInt(5));
                    level.setBlock(testPos, stateWithAge(fire, level, testPos, spreadAge), 3);
                }
            }
        }
    }

    /** InfX's direct ignition helper, including TNT priming and the small chance to leave fire. */
    public static boolean tryToCatchBlockOnFire(
            FireBlock fire,
            ServerLevel level,
            BlockPos pos,
            int denominator,
            RandomSource random,
            int age,
            Direction face) {
        BlockState oldState = level.getBlockState(pos);
        int ability = flammabilityFor(oldState, level, pos, face);
        if (random.nextInt(Math.max(1, denominator)) >= ability) return false;

        oldState.onCaughtFire(level, pos, face, null);
        if (random.nextInt(Math.max(1, age + 10)) < 5 && !level.isRainingAt(pos)) {
            level.setBlock(pos, stateWithAge(fire, level, pos, inheritedAge(age, random.nextInt(5))), 3);
        } else {
            level.removeBlock(pos, false);
        }
        return true;
    }

    private static boolean hasFlammableNeighbour(BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (canEncourageFire(level, pos.relative(direction), direction.getOpposite())) return true;
        }
        return false;
    }

    private static boolean canEncourageFire(BlockGetter level, BlockPos pos, Direction face) {
        BlockState state = level.getBlockState(pos);
        // InfX treats netherrack as a valid neighbour even though it has no spread-speed entry.
        return state.is(Blocks.NETHERRACK) || rateForSpread(state, level, pos, face) > 0;
    }

    private static void catchNeighbour(
            FireBlock fire,
            ServerLevel level,
            BlockPos pos,
            Direction face,
            int denominator,
            RandomSource random,
            int age) {
        tryToCatchBlockOnFire(fire, level, pos, denominator, random, age, face);
    }

    private static int rateForSpread(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        if (isWaterlogged(state)) return 0;
        int value = chanceToEncourageFire(state.getBlock());
        return value > 0 ? value : state.getFireSpreadSpeed(level, pos, face);
    }

    private static int flammabilityFor(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        if (isWaterlogged(state)) return 0;
        int value = abilityToCatchFire(state.getBlock());
        return value > 0 ? value : state.getFlammability(level, pos, face);
    }

    private static BlockState stateWithAge(FireBlock fire, LevelReader level, BlockPos pos, int age) {
        BlockState fireState = BaseFireBlock.getState(level, pos);
        return fireState.getBlock() == fire ? fireState.setValue(FireBlock.AGE, age) : fireState;
    }

    private static boolean isPermanentFireBase(ServerLevel level, BlockPos below) {
        return level.getBlockState(below).is(Blocks.NETHERRACK)
                || (level.dimension() == Level.END && level.getBlockState(below).is(Blocks.BEDROCK));
    }

    private static boolean isWaterlogged(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    private static Map<Block, FireRate> createRates() {
        Map<Block, FireRate> rates = new IdentityHashMap<>();
        register(rates, 5, 20,
                Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS,
                Blocks.ACACIA_PLANKS, Blocks.CHERRY_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.PALE_OAK_PLANKS,
                Blocks.MANGROVE_PLANKS, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_MOSAIC,
                Blocks.OAK_SLAB, Blocks.SPRUCE_SLAB, Blocks.BIRCH_SLAB, Blocks.JUNGLE_SLAB,
                Blocks.ACACIA_SLAB, Blocks.CHERRY_SLAB, Blocks.DARK_OAK_SLAB, Blocks.PALE_OAK_SLAB,
                Blocks.MANGROVE_SLAB, Blocks.BAMBOO_SLAB, Blocks.BAMBOO_MOSAIC_SLAB,
                Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE,
                Blocks.ACACIA_FENCE_GATE, Blocks.CHERRY_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE,
                Blocks.PALE_OAK_FENCE_GATE, Blocks.MANGROVE_FENCE_GATE, Blocks.BAMBOO_FENCE_GATE,
                Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE,
                Blocks.ACACIA_FENCE, Blocks.CHERRY_FENCE, Blocks.DARK_OAK_FENCE, Blocks.PALE_OAK_FENCE,
                Blocks.MANGROVE_FENCE, Blocks.BAMBOO_FENCE,
                Blocks.OAK_STAIRS, Blocks.BIRCH_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.JUNGLE_STAIRS,
                Blocks.ACACIA_STAIRS, Blocks.CHERRY_STAIRS, Blocks.DARK_OAK_STAIRS, Blocks.PALE_OAK_STAIRS,
                Blocks.MANGROVE_STAIRS, Blocks.BAMBOO_STAIRS, Blocks.BAMBOO_MOSAIC_STAIRS);
        register(rates, 5, 5,
                Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG,
                Blocks.CHERRY_LOG, Blocks.PALE_OAK_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG,
                Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_SPRUCE_LOG,
                Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_ACACIA_LOG,
                Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG,
                Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.STRIPPED_OAK_WOOD,
                Blocks.STRIPPED_SPRUCE_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_JUNGLE_WOOD,
                Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_CHERRY_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD,
                Blocks.STRIPPED_PALE_OAK_WOOD, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.OAK_WOOD,
                Blocks.SPRUCE_WOOD, Blocks.BIRCH_WOOD, Blocks.JUNGLE_WOOD, Blocks.ACACIA_WOOD,
                Blocks.CHERRY_WOOD, Blocks.PALE_OAK_WOOD, Blocks.DARK_OAK_WOOD, Blocks.MANGROVE_WOOD,
                Blocks.COAL_BLOCK);
        register(rates, 30, 60,
                Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
                Blocks.ACACIA_LEAVES, Blocks.CHERRY_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.PALE_OAK_LEAVES,
                Blocks.MANGROVE_LEAVES,
                Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL,
                Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL,
                Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL,
                Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL);
        register(rates, 30, 20, Blocks.BOOKSHELF);
        register(rates, 15, 100, Blocks.TNT, Blocks.VINE);
        register(rates, 60, 100,
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.LARGE_FERN, Blocks.DEAD_BUSH,
                Blocks.COBWEB, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
                Blocks.HAY_BLOCK);
        register(rates, 30, 50, Blocks.SUGAR_CANE, Blocks.BUSH);
        return Collections.unmodifiableMap(rates);
    }

    private static void register(Map<Block, FireRate> rates, int encouragement, int flammability, Block... blocks) {
        FireRate rate = new FireRate(encouragement, flammability);
        for (Block block : blocks) rates.put(block, rate);
    }

    public record FireRate(int encouragement, int flammability) {
        public FireRate {
            if (encouragement < 0 || flammability < 0) {
                throw new IllegalArgumentException("Fire rates cannot be negative");
            }
        }
    }
}
