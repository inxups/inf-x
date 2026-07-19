package com.pixulse.infx.survival;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Persistent and synchronized R196 player metabolism state. */
public record R196SurvivalData(
        double satiation,
        double nutrition,
        int protein,
        int phytonutrients,
        int essentialFats,
        int insulinResponse,
        double recoveryProgress) {
    public static final int NUTRIENT_CAP = 160_000;
    public static final int MILD_INSULIN_RESISTANCE = 48_000;
    public static final int MODERATE_INSULIN_RESISTANCE = 96_000;
    public static final int SEVERE_INSULIN_RESISTANCE = 144_000;

    public static final Codec<R196SurvivalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("satiation").forGetter(R196SurvivalData::satiation),
                    Codec.DOUBLE.fieldOf("nutrition").forGetter(R196SurvivalData::nutrition),
                    Codec.INT.fieldOf("protein").forGetter(R196SurvivalData::protein),
                    Codec.INT.fieldOf("phytonutrients").forGetter(R196SurvivalData::phytonutrients),
                    Codec.INT.fieldOf("essential_fats").forGetter(R196SurvivalData::essentialFats),
                    Codec.INT.fieldOf("insulin_response").forGetter(R196SurvivalData::insulinResponse),
                    Codec.DOUBLE.optionalFieldOf("recovery_progress", 0.0D)
                            .forGetter(R196SurvivalData::recoveryProgress))
            .apply(instance, R196SurvivalData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, R196SurvivalData> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public R196SurvivalData decode(RegistryFriendlyByteBuf buffer) {
                    return new R196SurvivalData(
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readDouble());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, R196SurvivalData value) {
                    buffer.writeDouble(value.satiation);
                    buffer.writeDouble(value.nutrition);
                    buffer.writeVarInt(value.protein);
                    buffer.writeVarInt(value.phytonutrients);
                    buffer.writeVarInt(value.essentialFats);
                    buffer.writeVarInt(value.insulinResponse);
                    buffer.writeDouble(value.recoveryProgress);
                }
            };

    public static R196SurvivalData initial() {
        return new R196SurvivalData(6.0D, 6.0D, NUTRIENT_CAP, NUTRIENT_CAP, NUTRIENT_CAP, 0, 0.0D);
    }

    public R196SurvivalData clamp(double foodCap) {
        return new R196SurvivalData(
                Math.clamp(satiation, 0.0D, foodCap),
                Math.clamp(nutrition, 0.0D, foodCap),
                Math.clamp(protein, 0, NUTRIENT_CAP),
                Math.clamp(phytonutrients, 0, NUTRIENT_CAP),
                Math.clamp(essentialFats, 0, NUTRIENT_CAP),
                Math.clamp(insulinResponse, 0, NUTRIENT_CAP),
                Math.max(0.0D, recoveryProgress));
    }

    public R196SurvivalData consume(double amount, int nutrientDecay, double foodCap) {
        double remaining = Math.max(0.0D, amount);
        double newSatiation = Math.max(0.0D, satiation - remaining);
        remaining = Math.max(0.0D, remaining - satiation);
        double newNutrition = Math.max(0.0D, nutrition - remaining);
        return new R196SurvivalData(
                        newSatiation,
                        newNutrition,
                        protein - nutrientDecay,
                        phytonutrients - nutrientDecay,
                        essentialFats - nutrientDecay,
                        Math.max(0, insulinResponse - Math.max(1, nutrientDecay / 2)),
                        recoveryProgress)
                .clamp(foodCap);
    }

    public R196SurvivalData eat(R196FoodProfile food, double foodCap) {
        int sugar = acceptsSugar() ? food.sugar() : 0;
        return new R196SurvivalData(
                        satiation + food.satiation(),
                        nutrition + food.nutrition(),
                        protein + food.protein(),
                        phytonutrients + food.phytonutrients(),
                        essentialFats + food.essentialFats(),
                        insulinResponse + sugar,
                        recoveryProgress)
                .clamp(foodCap);
    }

    public R196SurvivalData withRecoveryProgress(double progress) {
        return new R196SurvivalData(
                satiation, nutrition, protein, phytonutrients, essentialFats, insulinResponse, progress);
    }

    public boolean isMalnourished() {
        return protein <= 0 || phytonutrients <= 0;
    }

    public boolean isEnergyEmpty() {
        return satiation <= 0.0001D && nutrition <= 0.0001D;
    }

    public InsulinResistance insulinResistance() {
        if (insulinResponse >= SEVERE_INSULIN_RESISTANCE) return InsulinResistance.SEVERE;
        if (insulinResponse >= MODERATE_INSULIN_RESISTANCE) return InsulinResistance.MODERATE;
        if (insulinResponse >= MILD_INSULIN_RESISTANCE) return InsulinResistance.MILD;
        return InsulinResistance.NONE;
    }

    public boolean acceptsSugar() {
        return insulinResistance().ordinal() < InsulinResistance.MODERATE.ordinal();
    }

    public enum InsulinResistance {
        NONE,
        MILD,
        MODERATE,
        SEVERE
    }
}
