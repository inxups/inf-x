package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.material.R196Material;
import java.util.Map;
import org.junit.jupiter.api.Test;

class R196BucketRulesTest {
    @Test
    void sevenMaterialsExposeFiveStableVariantPaths() {
        var materials = java.util.List.of(
                R196Material.COPPER,
                R196Material.SILVER,
                R196Material.GOLD,
                R196Material.IRON,
                R196Material.ANCIENT_METAL,
                R196Material.MITHRIL,
                R196Material.ADAMANTIUM);
        assertEquals(35, materials.size() * R196BucketItem.Contents.values().length);
        assertEquals("copper_bucket", R196BucketItem.Contents.EMPTY.path(R196Material.COPPER));
        assertEquals("ancient_metal_lava_bucket", R196BucketItem.Contents.LAVA.path(R196Material.ANCIENT_METAL));
        assertEquals("adamantium_stone_bucket", R196BucketItem.Contents.STONE.path(R196Material.ADAMANTIUM));
    }

    @Test
    void sevenMaterialsExposeSixMobBucketPathsAndPowderSnow() {
        var materials = java.util.List.of(
                R196Material.COPPER,
                R196Material.SILVER,
                R196Material.GOLD,
                R196Material.IRON,
                R196Material.ANCIENT_METAL,
                R196Material.MITHRIL,
                R196Material.ADAMANTIUM);
        assertEquals(42, materials.size() * R196MobBucketKind.values().length);
        assertEquals(6, R196MobBucketKind.values().length);
        assertEquals("cod_copper_bucket", R196MobBucketKind.COD.path(R196Material.COPPER));
        assertEquals("salmon_iron_bucket", R196MobBucketKind.SALMON.path(R196Material.IRON));
        assertEquals("pufferfish_gold_bucket", R196MobBucketKind.PUFFERFISH.path(R196Material.GOLD));
        assertEquals("tropical_silver_bucket", R196MobBucketKind.TROPICAL.path(R196Material.SILVER));
        assertEquals("axolotl_mithril_bucket", R196MobBucketKind.AXOLOTL.path(R196Material.MITHRIL));
        assertEquals("tadpole_adamantium_bucket", R196MobBucketKind.TADPOLE.path(R196Material.ADAMANTIUM));
        assertEquals("powder_snow_copper_bucket", "powder_snow_" + R196Material.COPPER.path() + "_bucket");
    }

    @Test
    void lavaMeltingUsesTheR196MaterialTable() {
        Map<R196Material, Float> expected = Map.of(
                R196Material.COPPER, .16F,
                R196Material.SILVER, .16F,
                R196Material.GOLD, .20F,
                R196Material.IRON, .08F,
                R196Material.ANCIENT_METAL, .04F,
                R196Material.MITHRIL, .01F,
                R196Material.ADAMANTIUM, 0.0F);
        expected.forEach((material, chance) -> assertEquals(chance, R196BucketItem.lavaMeltChance(material)));
    }

    @Test
    void sourcePlacementRequiresOneHundredTotalExperience() {
        assertEquals(100, R196BucketItem.SOURCE_EXPERIENCE_COST);
        assertEquals(3200, R196BucketItem.LAVA_BURN_TIME);
    }
}
