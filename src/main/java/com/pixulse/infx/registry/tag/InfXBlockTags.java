package com.pixulse.infx.registry.tag;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.MiningFamily;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class InfXBlockTags {
    public static final TagKey<Block> PORTABLE_HAND_HARVEST = create("portable_hand_harvest");
    public static final TagKey<Block> NO_EFFECTIVE_TOOL = create("no_effective_tool");
    public static final TagKey<Block> METAL_SHOVEL_EFFECTIVE = create("effective_tool/metal_shovel");
    public static final TagKey<Block> WAR_HAMMER_EFFECTIVE = create("effective_tool/war_hammer");
    public static final TagKey<Block> AXE_HALF_SPEED = create("effective_tool/axe_half_speed");
    public static final TagKey<Block> UNDERWORLD_CARVER_REPLACEABLES = create("underworld_carver_replaceables");
    public static final TagKey<Block> PEPSIN_DISSOLVABLE = create("dissolves/pepsin");
    public static final TagKey<Block> ACID_DISSOLVES_INSTANTLY = create("dissolves/acid_instantly");
    public static final TagKey<Block> ACID_DISSOLVES_GRADUALLY = create("dissolves/acid_gradually");
    public static final TagKey<Block> CURSE_VINES = create("curse/vines");
    public static final TagKey<Block> CURSE_PLANTS = create("curse/plants");

    private InfXBlockTags() {
    }

    public static TagKey<Block> requiredLevel(int level) {
        if (level < 0 || level > 6) {
            throw new IllegalArgumentException("Harvest level must be between 0 and 6: " + level);
        }
        return create("requires_harvest_level/" + level);
    }

    public static TagKey<Block> effectiveWith(MiningFamily family) {
        if (family == MiningFamily.NONE) {
            throw new IllegalArgumentException("NONE has no effective block tag");
        }
        return create("effective_tool/" + family.path());
    }

    private static TagKey<Block> create(String path) {
        return TagKey.create(Registries.BLOCK, InfiniteX.id(path));
    }
}
