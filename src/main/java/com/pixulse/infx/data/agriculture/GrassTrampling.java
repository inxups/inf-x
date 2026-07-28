package com.pixulse.infx.data.agriculture;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.mojang.serialization.Codec;
import com.pixulse.infx.entity.Livestock;
import com.pixulse.infx.registry.InfXAttachments;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * MITE grass trampling: adult livestock darken grass toward brown manure color.
 *
 * <p>Trampling count is stored per grass block on the chunk attachment (0-15), matching
 * MITE {@code BlockGrass} metadata bits. Color blend uses the same brown target (134, 96, 67).
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class GrassTrampling {
    public static final int MAX_TRAMPLINGS = 15;
    /** MITE brown manure grass target RGB. */
    public static final int MANURE_RED = 134;
    public static final int MANURE_GREEN = 96;
    public static final int MANURE_BLUE = 67;

    public static final Codec<Map<String, Integer>> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.INT);

    private static final String LAST_X = "infx_trample_x";
    private static final String LAST_Y = "infx_trample_y";
    private static final String LAST_Z = "infx_trample_z";
    private static final String LAST_RESET = "infx_trample_reset";

    private GrassTrampling() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Animal animal)
                || !(entity.level() instanceof ServerLevel level)
                || !Livestock.isLivestock(entity)
                || animal.isBaby()
                || !entity.onGround()) {
            return;
        }

        var data = entity.getPersistentData();
        long now = level.getGameTime();
        if (entity.tickCount % 1000 == 0 || data.getLong(LAST_RESET).orElse(0L) == 0L) {
            data.putLong(LAST_RESET, now);
            data.putInt(LAST_X, Integer.MIN_VALUE);
            data.putInt(LAST_Y, Integer.MIN_VALUE);
            data.putInt(LAST_Z, Integer.MIN_VALUE);
        }

        BlockPos under = entity.blockPosition().below();
        int x = under.getX();
        int y = under.getY();
        int z = under.getZ();
        if (data.getIntOr(LAST_X, Integer.MIN_VALUE) == x
                && data.getIntOr(LAST_Y, Integer.MIN_VALUE) == y
                && data.getIntOr(LAST_Z, Integer.MIN_VALUE) == z) {
            return;
        }
        data.putInt(LAST_X, x);
        data.putInt(LAST_Y, y);
        data.putInt(LAST_Z, z);

        BlockState state = level.getBlockState(under);
        if (!state.is(Blocks.GRASS_BLOCK)) {
            return;
        }

        int tramplings = getTramplings(level, under);
        if (tramplings < MAX_TRAMPLINGS) {
            tramplings++;
            setTramplings(level, under, tramplings);
        }

        float effect = tramplingEffect(tramplings);
        if (entity instanceof AbstractCow) {
            effect *= 2.0F;
        }
        if (effect >= 0.2F && entity.getRandom().nextFloat() < effect * 2.0F) {
            BlockPos above = under.above();
            BlockState plant = level.getBlockState(above);
            // MITE tramples plants/crops on the grass surface.
            if (plant.getBlock() instanceof CropBlock
                    || plant.is(Blocks.SHORT_GRASS)
                    || plant.is(Blocks.TALL_GRASS)
                    || plant.is(Blocks.FERN)
                    || plant.is(Blocks.LARGE_FERN)
                    || plant.is(Blocks.DEAD_BUSH)) {
                level.destroyBlock(above, true, entity);
            }
        }
    }

    /** Decrease trampling by 1 when a grass block is selected for MITE-style recovery. */
    public static boolean recoverOne(ServerLevel level, BlockPos pos) {
        int tramplings = getTramplings(level, pos);
        if (tramplings <= 0) {
            return false;
        }
        setTramplings(level, pos, tramplings - 1);
        return true;
    }

    public static int getTramplings(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return 0;
        }
        LevelChunk chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        Map<String, Integer> map = chunk.getExistingDataOrNull(InfXAttachments.GRASS_TRAMPLING.get());
        if (map == null || map.isEmpty()) {
            return 0;
        }
        return Mth.clamp(map.getOrDefault(key(pos), 0), 0, MAX_TRAMPLINGS);
    }

    public static void setTramplings(ServerLevel level, BlockPos pos, int tramplings) {
        LevelChunk chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        Map<String, Integer> current = chunk.getData(InfXAttachments.GRASS_TRAMPLING);
        Map<String, Integer> next = new HashMap<>(current);
        int clamped = Mth.clamp(tramplings, 0, MAX_TRAMPLINGS);
        String k = key(pos);
        if (clamped <= 0) {
            next.remove(k);
        } else {
            next.put(k, clamped);
        }
        chunk.setData(InfXAttachments.GRASS_TRAMPLING, Map.copyOf(next));
        // Force client block color refresh without changing state.
        BlockState grass = level.getBlockState(pos);
        level.sendBlockUpdated(pos, grass, grass, 2);
    }

    public static float tramplingEffect(int tramplings) {
        return Mth.clamp((tramplings - 3) * 0.05F, 0.0F, 0.5F);
    }

    /** Blend biome grass color toward MITE manure brown by trampling effect. */
    public static int blendColor(int biomeColor, int tramplings) {
        float effect = tramplingEffect(tramplings);
        if (effect <= 0.0F) {
            return biomeColor;
        }
        float keep = 1.0F - effect;
        int r = (int) (((biomeColor >> 16) & 0xFF) * keep + MANURE_RED * effect);
        int g = (int) (((biomeColor >> 8) & 0xFF) * keep + MANURE_GREEN * effect);
        int b = (int) ((biomeColor & 0xFF) * keep + MANURE_BLUE * effect);
        return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    static String key(BlockPos pos) {
        return Long.toString(pos.asLong());
    }

    /** Compact in-memory view for tests. */
    public static Long2ByteMap snapshot(Map<String, Integer> map) {
        Long2ByteMap result = new Long2ByteOpenHashMap();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            result.put(Long.parseLong(entry.getKey()), entry.getValue().byteValue());
        }
        return result;
    }

}
