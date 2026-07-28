package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.material.MiteMaterial;
import com.pixulse.infx.registry.ModEntityTypes;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.Test;

class BucketRulesTest {
    @Test
    void sevenMaterialsExposeFiveStableVariantPaths() {
        var materials = java.util.List.of(
                MiteMaterial.COPPER,
                MiteMaterial.SILVER,
                MiteMaterial.GOLD,
                MiteMaterial.IRON,
                MiteMaterial.ANCIENT_METAL,
                MiteMaterial.MITHRIL,
                MiteMaterial.ADAMANTIUM);
        assertEquals(35, materials.size() * MiteBucketItem.Contents.values().length);
        assertEquals("copper_bucket", MiteBucketItem.Contents.EMPTY.path(MiteMaterial.COPPER));
        assertEquals("ancient_metal_lava_bucket", MiteBucketItem.Contents.LAVA.path(MiteMaterial.ANCIENT_METAL));
        assertEquals("adamantium_stone_bucket", MiteBucketItem.Contents.STONE.path(MiteMaterial.ADAMANTIUM));
    }

    @Test
    void sevenMaterialsExposeSixMobBucketPathsAndPowderSnow() {
        var materials = java.util.List.of(
                MiteMaterial.COPPER,
                MiteMaterial.SILVER,
                MiteMaterial.GOLD,
                MiteMaterial.IRON,
                MiteMaterial.ANCIENT_METAL,
                MiteMaterial.MITHRIL,
                MiteMaterial.ADAMANTIUM);
        assertEquals(42, materials.size() * MobBucketKind.values().length);
        assertEquals(6, MobBucketKind.values().length);
        assertEquals("cod_copper_bucket", MobBucketKind.COD.path(MiteMaterial.COPPER));
        assertEquals("salmon_iron_bucket", MobBucketKind.SALMON.path(MiteMaterial.IRON));
        assertEquals("pufferfish_gold_bucket", MobBucketKind.PUFFERFISH.path(MiteMaterial.GOLD));
        assertEquals("tropical_silver_bucket", MobBucketKind.TROPICAL.path(MiteMaterial.SILVER));
        assertEquals("axolotl_mithril_bucket", MobBucketKind.AXOLOTL.path(MiteMaterial.MITHRIL));
        assertEquals("tadpole_adamantium_bucket", MobBucketKind.TADPOLE.path(MiteMaterial.ADAMANTIUM));
        assertEquals("powder_snow_copper_bucket", "powder_snow_" + MiteMaterial.COPPER.path() + "_bucket");
    }

    @Test
    void fishBucketsReleaseReplacementEntitiesAndMigrateLegacyFish() {
        assertEquals(ModEntityTypes.R196_COD.get(), MobBucketKind.COD.entityType());
        assertEquals(ModEntityTypes.R196_SALMON.get(), MobBucketKind.SALMON.entityType());
        assertEquals(ModEntityTypes.R196_PUFFERFISH.get(), MobBucketKind.PUFFERFISH.entityType());
        assertEquals(ModEntityTypes.R196_TROPICAL_FISH.get(), MobBucketKind.TROPICAL.entityType());
        assertEquals(MobBucketKind.COD, MobBucketKind.of(EntityType.COD));
        assertEquals(MobBucketKind.SALMON, MobBucketKind.of(EntityType.SALMON));
        assertEquals(MobBucketKind.PUFFERFISH, MobBucketKind.of(EntityType.PUFFERFISH));
        assertEquals(MobBucketKind.TROPICAL, MobBucketKind.of(EntityType.TROPICAL_FISH));
    }

    @Test
    void lavaMeltingUsesTheR196MaterialTable() {
        Map<MiteMaterial, Float> expected = Map.of(
                MiteMaterial.COPPER, .16F,
                MiteMaterial.SILVER, .16F,
                MiteMaterial.GOLD, .20F,
                MiteMaterial.IRON, .08F,
                MiteMaterial.ANCIENT_METAL, .04F,
                MiteMaterial.MITHRIL, .01F,
                MiteMaterial.ADAMANTIUM, 0.0F);
        expected.forEach((material, chance) -> assertEquals(chance, MiteBucketItem.lavaMeltChance(material)));
    }

    /**
     * MITE derives the chance from material durability rather than a table, so a metal twice as
     * durable must melt half as often. Gold is exempt: MITE hardcodes it at 20%.
     */
    @Test
    void lavaMeltingScalesInverselyWithDurability() {
        assertEquals(
                MiteBucketItem.lavaMeltChance(MiteMaterial.COPPER) / 2.0F,
                MiteBucketItem.lavaMeltChance(MiteMaterial.IRON));
        assertEquals(
                MiteBucketItem.lavaMeltChance(MiteMaterial.IRON) / 2.0F,
                MiteBucketItem.lavaMeltChance(MiteMaterial.ANCIENT_METAL));
        assertEquals(
                MiteBucketItem.lavaMeltChance(MiteMaterial.ANCIENT_METAL) / 4.0F,
                MiteBucketItem.lavaMeltChance(MiteMaterial.MITHRIL));
    }

    @Test
    void sourcePlacementRequiresOneHundredTotalExperience() {
        assertEquals(100, MiteBucketItem.SOURCE_EXPERIENCE_COST);
        assertEquals(3200, MiteBucketItem.LAVA_BURN_TIME);
    }

    /** MITE scheduleBlockChange delays and the melt pickup grace, all in ticks. */
    @Test
    void pourDecayAndMeltDelaysMatchMite() {
        assertEquals(16, MiteBucketItem.WATER_DECAY_DELAY);
        assertEquals(48, MiteBucketItem.LAVA_DECAY_DELAY);
        assertEquals(30, MiteBucketItem.MELT_PICKUP_DELAY);
    }

    /** MITE ItemVessel water damage tiers for a bucket-sized vessel. */
    @Test
    void quenchDamageMatchesMiteVesselTiers() {
        assertEquals(20.0F, MiteBucketItem.FIRE_ELEMENTAL_QUENCH_DAMAGE);
        assertEquals(8.0F, MiteBucketItem.NETHERSPAWN_QUENCH_DAMAGE);
    }
}
