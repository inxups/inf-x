package com.pixulse.infx.material;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

/**
 * The persistent non-average R196 quality states.
 *
 * <p>Average quality is represented by an absent data component so existing
 * stacks and third-party recipe outputs remain backward compatible.</p>
 */
public enum R196Quality implements StringRepresentable {
    POOR("poor", .75F, ChatFormatting.DARK_GRAY),
    FINE("fine", 1.5F, ChatFormatting.GREEN),
    EXCELLENT("excellent", 2.0F, ChatFormatting.AQUA),
    SUPERB("superb", 2.5F, ChatFormatting.BLUE),
    MASTERWORK("masterwork", 3.0F, ChatFormatting.LIGHT_PURPLE),
    LEGENDARY("legendary", 3.5F, ChatFormatting.GOLD);

    public static final Codec<R196Quality> CODEC = StringRepresentable.fromEnum(R196Quality::values);

    private final String serializedName;
    private final float durabilityMultiplier;
    private final ChatFormatting color;

    R196Quality(String serializedName, float durabilityMultiplier, ChatFormatting color) {
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

    public boolean isAtMost(R196Quality maximum) {
        return ordinal() <= maximum.ordinal();
    }
}
