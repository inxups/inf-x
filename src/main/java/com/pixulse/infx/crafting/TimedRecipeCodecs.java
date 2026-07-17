package com.pixulse.infx.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

final class TimedRecipeCodecs {
    static final Codec<BenchTier> BENCH_TIER = Codec.STRING.comapFlatMap(
            name -> BenchTier.fromSerializedName(name)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown required_bench: " + name)),
            BenchTier::serializedName);

    static final StreamCodec<ByteBuf, BenchTier> BENCH_TIER_STREAM = ByteBufCodecs.idMapper(
            ByIdMap.continuous(BenchTier::ordinal, BenchTier.values(), ByIdMap.OutOfBoundsStrategy.ZERO),
            BenchTier::ordinal);

    private TimedRecipeCodecs() {}
}
