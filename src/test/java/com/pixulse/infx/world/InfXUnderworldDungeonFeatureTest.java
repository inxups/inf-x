package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.junit.jupiter.api.Test;

class InfXUnderworldDungeonFeatureTest {
    private static final BlockPos ORIGIN = new BlockPos(0, 150, 0);
    private static final InfXUnderworldDungeonFeature FEATURE =
            new InfXUnderworldDungeonFeature(NoneFeatureConfiguration.CODEC);

    @Test
    void acceptsOneThroughFiveEntrancesOnly() {
        assertAll(
                () -> assertFalse(InfXUnderworldDungeonFeature.hasValidEntranceCount(0)),
                () -> assertTrue(InfXUnderworldDungeonFeature.hasValidEntranceCount(1)),
                () -> assertTrue(InfXUnderworldDungeonFeature.hasValidEntranceCount(5)),
                () -> assertFalse(InfXUnderworldDungeonFeature.hasValidEntranceCount(6)));
    }

    @Test
    void waterDoesNotCountAsAnEntrance() {
        DungeonLevel level = DungeonLevel.roomWithEntrance(Blocks.WATER.defaultBlockState());

        assertFalse(place(level, new DungeonRandom()));
    }

    @Test
    void requiresSolidFloorAndRoof() {
        DungeonLevel missingFloor = DungeonLevel.roomWithEntrance(Blocks.CAVE_AIR.defaultBlockState());
        missingFloor.setRaw(ORIGIN.below(), Blocks.CAVE_AIR.defaultBlockState());
        DungeonLevel missingRoof = DungeonLevel.roomWithEntrance(Blocks.CAVE_AIR.defaultBlockState());
        missingRoof.setRaw(ORIGIN.above(4), Blocks.CAVE_AIR.defaultBlockState());

        assertAll(
                () -> assertFalse(place(missingFloor, new DungeonRandom())),
                () -> assertFalse(place(missingRoof, new DungeonRandom())));
    }

    @Test
    void buildsTheMonsterRoomAndBindsTheDedicatedLootTable() {
        DungeonLevel level = DungeonLevel.roomWithEntrance(Blocks.CAVE_AIR.defaultBlockState());

        assertTrue(place(level, new DungeonRandom()));
        assertAll(
                () -> assertTrue(level.blockAt(ORIGIN).is(Blocks.SPAWNER)),
                () -> assertTrue(level.blockAt(ORIGIN.above(2)).isAir()),
                () -> assertTrue(level.blockAt(ORIGIN.offset(-3, -1, -3)).is(Blocks.COBBLESTONE)
                        || level.blockAt(ORIGIN.offset(-3, -1, -3)).is(Blocks.MOSSY_COBBLESTONE)),
                () -> assertTrue(level.hasChestWithLootTable(Underworld.DUNGEON_LOOT)));
    }

    @Test
    void guardianUsesOneOfSixSpawnerRolls() {
        assertAll(
                () -> assertTrue(InfXUnderworldDungeonFeature.isGuardianRoll(0)),
                () -> assertFalse(InfXUnderworldDungeonFeature.isGuardianRoll(1)),
                () -> assertFalse(InfXUnderworldDungeonFeature.isGuardianRoll(2)),
                () -> assertFalse(InfXUnderworldDungeonFeature.isGuardianRoll(3)),
                () -> assertFalse(InfXUnderworldDungeonFeature.isGuardianRoll(4)),
                () -> assertFalse(InfXUnderworldDungeonFeature.isGuardianRoll(5)),
                () -> assertThrows(IllegalArgumentException.class, () -> InfXUnderworldDungeonFeature.isGuardianRoll(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> InfXUnderworldDungeonFeature.isGuardianRoll(6)));
    }

    private static boolean place(DungeonLevel level, RandomSource random) {
        return FEATURE.place(new FeaturePlaceContext<>(
                Optional.empty(), level.world(), null, random, ORIGIN, NoneFeatureConfiguration.INSTANCE));
    }

    private static final class DungeonLevel {
        private static final BlockState DEFAULT_STATE = Blocks.STONE.defaultBlockState();
        private final Map<BlockPos, BlockState> blocks = new HashMap<>();
        private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
        private final WorldGenLevel world = (WorldGenLevel) Proxy.newProxyInstance(
                WorldGenLevel.class.getClassLoader(), new Class<?>[] {WorldGenLevel.class}, this::invoke);

        static DungeonLevel roomWithEntrance(BlockState entranceState) {
            DungeonLevel level = new DungeonLevel();
            int radius = 2;
            for (int x = -radius; x <= radius; x++) {
                for (int y = 0; y <= 3; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        level.setRaw(ORIGIN.offset(x, y, z), Blocks.CAVE_AIR.defaultBlockState());
                    }
                }
            }
            level.setRaw(ORIGIN.offset(-radius - 1, 0, 0), entranceState);
            level.setRaw(ORIGIN.offset(-radius - 1, 1, 0), entranceState);
            return level;
        }

        WorldGenLevel world() {
            return this.world;
        }

        BlockState blockAt(BlockPos pos) {
            return this.blocks.getOrDefault(pos.immutable(), DEFAULT_STATE);
        }

        void setRaw(BlockPos pos, BlockState state) {
            BlockPos immutablePos = pos.immutable();
            this.blocks.put(immutablePos, state);
            if (state.is(Blocks.CHEST)) {
                this.blockEntities.put(immutablePos, new ChestBlockEntity(immutablePos, state));
            } else if (state.is(Blocks.SPAWNER)) {
                this.blockEntities.put(immutablePos, new SpawnerBlockEntity(immutablePos, state));
            } else {
                this.blockEntities.remove(immutablePos);
            }
        }

        boolean hasChestWithLootTable(net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTable) {
            return this.blockEntities.values().stream()
                    .filter(ChestBlockEntity.class::isInstance)
                    .map(ChestBlockEntity.class::cast)
                    .anyMatch(chest -> lootTable.equals(chest.getLootTable()));
        }

        private Object invoke(Object proxy, java.lang.reflect.Method method, Object[] arguments) {
            return switch (method.getName()) {
                case "getBlockState" -> this.blockAt((BlockPos) arguments[0]);
                case "getBlockEntity" -> this.blockEntities.get(((BlockPos) arguments[0]).immutable());
                case "isEmptyBlock" -> this.blockAt((BlockPos) arguments[0]).isAir();
                case "setBlock" -> {
                    this.setRaw((BlockPos) arguments[0], (BlockState) arguments[1]);
                    yield true;
                }
                case "getMinY" -> Underworld.MIN_Y;
                case "getSeed" -> 0L;
                case "ensureCanWrite" -> true;
                case "toString" -> "DungeonLevel";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == arguments[0];
                default -> defaultValue(method.getReturnType());
            };
        }

        private static Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) {
                return false;
            }
            if (type == byte.class) {
                return (byte) 0;
            }
            if (type == short.class) {
                return (short) 0;
            }
            if (type == int.class) {
                return 0;
            }
            if (type == long.class) {
                return 0L;
            }
            if (type == float.class) {
                return 0.0F;
            }
            if (type == double.class) {
                return 0.0D;
            }
            if (type == char.class) {
                return '\0';
            }
            return null;
        }
    }

    private static final class DungeonRandom implements RandomSource {
        private final RandomSource delegate = RandomSource.create(0xD06E0L);
        private int chestCoordinateCalls;

        @Override
        public RandomSource fork() {
            return this.delegate.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            return this.delegate.forkPositional();
        }

        @Override
        public void setSeed(long seed) {
            this.delegate.setSeed(seed);
        }

        @Override
        public int nextInt() {
            return this.delegate.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            if (bound == 2) {
                return 0;
            }
            if (bound == 5) {
                return this.chestCoordinateCalls++ == 0 ? 2 : 0;
            }
            return this.delegate.nextInt(bound);
        }

        @Override
        public long nextLong() {
            return this.delegate.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            return this.delegate.nextBoolean();
        }

        @Override
        public float nextFloat() {
            return this.delegate.nextFloat();
        }

        @Override
        public double nextDouble() {
            return this.delegate.nextDouble();
        }

        @Override
        public double nextGaussian() {
            return this.delegate.nextGaussian();
        }
    }
}
