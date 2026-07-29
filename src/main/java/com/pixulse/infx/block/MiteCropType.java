package com.pixulse.infx.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Crop-specific MITE growth, rendering, and harvest settings. */
public enum MiteCropType {
    WHEAT("wheat", "wheat", Blocks.WHEAT, Items.WHEAT_SEEDS, Items.WHEAT, 7, 8, 7, 0.0005F, 1, 0.0F),
    CARROTS("carrots", "carrots", Blocks.CARROTS, Items.CARROT, Items.CARROT, 7, 4, 3, 0.0005F, 2, 0.25F),
    POTATOES("potatoes", "potatoes", Blocks.POTATOES, Items.POTATO, Items.POTATO, 7, 4, 3, 0.0010F, 2, 0.25F),
    BEETROOTS("beetroots", "beetroot", Blocks.BEETROOTS, Items.BEETROOT_SEEDS, Items.BEETROOT, 7, 4, 4, 0.0005F, 2, 0.5F);

    private final String registryName;
    private final String textureName;
    private final Block vanillaBlock;
    private final Item seed;
    private final Item product;
    private final int maxAge;
    private final int textureStages;
    private final int deadTextureStages;
    private final float blightChance;
    private final int matureYield;
    private final float bonusYieldChance;

    MiteCropType(
            String registryName,
            String textureName,
            Block vanillaBlock,
            Item seed,
            Item product,
            int maxAge,
            int textureStages,
            int deadTextureStages,
            float blightChance,
            int matureYield,
            float bonusYieldChance) {
        this.registryName = registryName;
        this.textureName = textureName;
        this.vanillaBlock = vanillaBlock;
        this.seed = seed;
        this.product = product;
        this.maxAge = maxAge;
        this.textureStages = textureStages;
        this.deadTextureStages = deadTextureStages;
        this.blightChance = blightChance;
        this.matureYield = matureYield;
        this.bonusYieldChance = bonusYieldChance;
    }

    public String textureName() {
        return textureName;
    }

    public String registryName() {
        return registryName;
    }

    public Block vanillaBlock() {
        return vanillaBlock;
    }

    public Item seed() {
        return seed;
    }

    public Item product() {
        return product;
    }

    public int maxAge() {
        return maxAge;
    }

    public int textureStages() {
        return textureStages;
    }

    public int deadTextureStages() {
        return deadTextureStages;
    }

    public float blightChance() {
        return blightChance;
    }

    public int matureYield() {
        return matureYield;
    }

    /** Chance for a mature crop to yield one additional item. */
    public float bonusYieldChance() {
        return bonusYieldChance;
    }

    /**
     * Converts the modern four-step beetroot state to ITF Reborn's eight stored MITE ages while
     * preserving its visible growth frame. Other crop ages are already storage-compatible.
     */
    public int ageFromVanilla(int age) {
        int clamped = Math.clamp(age, 0, maxAge);
        if (this != BEETROOTS) {
            return clamped;
        }
        return switch (clamped) {
            case 0 -> 0;
            case 1 -> 2;
            case 2 -> 4;
            default -> 7;
        };
    }

    /** MITE carrots, potatoes, and ITF Reborn beetroots map eight stored ages to four frames. */
    public int textureStage(int age) {
        int clamped = Math.clamp(age, 0, maxAge);
        if (this != WHEAT) {
            return (clamped == 6 ? 5 : clamped) / 2;
        }
        return clamped;
    }

    public int deadTextureStage(int age) {
        return Math.min(textureStage(age), deadTextureStages - 1);
    }
}
