package com.pixulse.infx.item.material;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

/**
 * The persistent non-average R196 quality states.
 *
 * <p>Average quality is represented by an absent data component so existing
 * stacks and third-party recipe outputs remain backward compatible.</p>
 */
public enum Quality implements StringRepresentable {
    WRETCHED("wretched", .50F, ChatFormatting.DARK_RED),
    POOR("poor", .75F, ChatFormatting.DARK_GRAY),
    FINE("fine", 1.5F, ChatFormatting.GREEN),
    EXCELLENT("excellent", 2.0F, ChatFormatting.AQUA),
    SUPERB("superb", 2.5F, ChatFormatting.BLUE),
    MASTERWORK("masterwork", 3.0F, ChatFormatting.LIGHT_PURPLE),
    LEGENDARY("legendary", 3.5F, ChatFormatting.GOLD);

    public static final Codec<Quality> CODEC = StringRepresentable.fromEnum(Quality::values);

    private final String serializedName;
    private final float durabilityMultiplier;
    private final ChatFormatting color;

    Quality(String serializedName, float durabilityMultiplier, ChatFormatting color) {
        this.serializedName = serializedName;
        this.durabilityMultiplier = durabilityMultiplier;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public float durabilityMultiplier() {
        return durabilityMultiplier;
    }

    public ChatFormatting color() {
        return color;
    }

    public boolean isAtMost(Quality maximum) {
        return ordinal() <= maximum.ordinal();
    }

    public int craftingDifficultyMultiplier() {
        return switch (this) {
            case WRETCHED, POOR -> 1;
            case FINE -> 2;
            case EXCELLENT -> 4;
            case SUPERB -> 8;
            case MASTERWORK -> 16;
            case LEGENDARY -> 32;
        };
    }
}
