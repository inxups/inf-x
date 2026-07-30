package com.pixulse.infx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;

public record InfXShiftedYDensityFunction(DensityFunction input, int offset) implements DensityFunction {
    private static final MapCodec<InfXShiftedYDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                            DensityFunction.HOLDER_HELPER_CODEC
                                    .fieldOf("input")
                                    .forGetter(InfXShiftedYDensityFunction::input),
                            Codec.INT.fieldOf("offset").forGetter(InfXShiftedYDensityFunction::offset))
                    .apply(instance, InfXShiftedYDensityFunction::new));
    public static final KeyDispatchDataCodec<InfXShiftedYDensityFunction> CODEC =
            KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        return input.compute(new ShiftedContext(context, offset));
    }

    @Override
    public void fillArray(double[] output, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new InfXShiftedYDensityFunction(input.mapAll(visitor), offset));
    }

    @Override
    public double minValue() {
        return input.minValue();
    }

    @Override
    public double maxValue() {
        return input.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    private record ShiftedContext(FunctionContext source, int offset) implements FunctionContext {
        @Override
        public int blockX() {
            return source.blockX();
        }

        @Override
        public int blockY() {
            return source.blockY() - offset;
        }

        @Override
        public int blockZ() {
            return source.blockZ();
        }

        @Override
        public Blender getBlender() {
            return source.getBlender();
        }
    }
}
