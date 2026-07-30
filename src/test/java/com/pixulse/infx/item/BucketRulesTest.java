package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.Test;

class BucketRulesTest {
    @Test
    void sevenMaterialsExposeFiveStableVariantPaths() {
        var materials = java.util.List.of(
                InfxMaterial.COPPER,
                InfxMaterial.SILVER,
                InfxMaterial.GOLD,
                InfxMaterial.IRON,
                InfxMaterial.ANCIENT_METAL,
                InfxMaterial.MITHRIL,
                InfxMaterial.ADAMANTIUM);
        assertEquals(35, materials.size() * InfxBucketItem.Contents.values().length);
        assertEquals("copper_bucket", InfxBucketItem.Contents.EMPTY.path(InfxMaterial.COPPER));
        assertEquals("ancient_metal_lava_bucket", InfxBucketItem.Contents.LAVA.path(InfxMaterial.ANCIENT_METAL));
        assertEquals("adamantium_stone_bucket", InfxBucketItem.Contents.STONE.path(InfxMaterial.ADAMANTIUM));
    }

    @Test
    void sevenMaterialsExposeSixMobBucketPathsAndPowderSnow() {
        var materials = java.util.List.of(
                InfxMaterial.COPPER,
                InfxMaterial.SILVER,
                InfxMaterial.GOLD,
                InfxMaterial.IRON,
                InfxMaterial.ANCIENT_METAL,
                InfxMaterial.MITHRIL,
                InfxMaterial.ADAMANTIUM);
        assertEquals(42, materials.size() * MobBucketKind.values().length);
        assertEquals(6, MobBucketKind.values().length);
        assertEquals("cod_copper_bucket", MobBucketKind.COD.path(InfxMaterial.COPPER));
        assertEquals("salmon_iron_bucket", MobBucketKind.SALMON.path(InfxMaterial.IRON));
        assertEquals("pufferfish_gold_bucket", MobBucketKind.PUFFERFISH.path(InfxMaterial.GOLD));
        assertEquals("tropical_silver_bucket", MobBucketKind.TROPICAL.path(InfxMaterial.SILVER));
        assertEquals("axolotl_mithril_bucket", MobBucketKind.AXOLOTL.path(InfxMaterial.MITHRIL));
        assertEquals("tadpole_adamantium_bucket", MobBucketKind.TADPOLE.path(InfxMaterial.ADAMANTIUM));
        assertEquals("powder_snow_copper_bucket", "powder_snow_" + InfxMaterial.COPPER.path() + "_bucket");
    }

    @Test
    void fishBucketsReleaseReplacementEntitiesAndMigrateLegacyFish() {
        assertEquals(InfXEntityTypes.INFX_COD.get(), MobBucketKind.COD.entityType());
        assertEquals(InfXEntityTypes.INFX_SALMON.get(), MobBucketKind.SALMON.entityType());
        assertEquals(InfXEntityTypes.INFX_PUFFERFISH.get(), MobBucketKind.PUFFERFISH.entityType());
        assertEquals(InfXEntityTypes.INFX_TROPICAL_FISH.get(), MobBucketKind.TROPICAL.entityType());
        assertEquals(MobBucketKind.COD, MobBucketKind.of(EntityType.COD));
        assertEquals(MobBucketKind.SALMON, MobBucketKind.of(EntityType.SALMON));
        assertEquals(MobBucketKind.PUFFERFISH, MobBucketKind.of(EntityType.PUFFERFISH));
        assertEquals(MobBucketKind.TROPICAL, MobBucketKind.of(EntityType.TROPICAL_FISH));
    }

    @Test
    void lavaMeltingUsesTheR196MaterialTable() {
        Map<InfxMaterial, Float> expected = Map.of(
                InfxMaterial.COPPER, .16F,
                InfxMaterial.SILVER, .16F,
                InfxMaterial.GOLD, .20F,
                InfxMaterial.IRON, .08F,
                InfxMaterial.ANCIENT_METAL, .04F,
                InfxMaterial.MITHRIL, .01F,
                InfxMaterial.ADAMANTIUM, 0.0F);
        expected.forEach((material, chance) -> assertEquals(chance, InfxBucketItem.lavaMeltChance(material)));
    }

    /**
     * MITE derives the chance from material durability rather than a table, so a metal twice as
     * durable must melt half as often. Gold is exempt: MITE hardcodes it at 20%.
     */
    @Test
    void lavaMeltingScalesInverselyWithDurability() {
        assertEquals(
                InfxBucketItem.lavaMeltChance(InfxMaterial.COPPER) / 2.0F,
                InfxBucketItem.lavaMeltChance(InfxMaterial.IRON));
        assertEquals(
                InfxBucketItem.lavaMeltChance(InfxMaterial.IRON) / 2.0F,
                InfxBucketItem.lavaMeltChance(InfxMaterial.ANCIENT_METAL));
        assertEquals(
                InfxBucketItem.lavaMeltChance(InfxMaterial.ANCIENT_METAL) / 4.0F,
                InfxBucketItem.lavaMeltChance(InfxMaterial.MITHRIL));
    }

    @Test
    void sourcePlacementRequiresOneHundredTotalExperience() {
        assertEquals(100, InfxBucketItem.SOURCE_EXPERIENCE_COST);
        assertEquals(3200, InfxBucketItem.LAVA_BURN_TIME);
    }

    /** MITE scheduleBlockChange delays and the melt pickup grace, all in ticks. */
    @Test
    void pourDecayAndMeltDelaysMatchMite() {
        assertEquals(16, InfxBucketItem.WATER_DECAY_DELAY);
        assertEquals(48, InfxBucketItem.LAVA_DECAY_DELAY);
        assertEquals(30, InfxBucketItem.MELT_PICKUP_DELAY);
    }

    /** MITE ItemVessel water damage tiers for a bucket-sized vessel. */
    @Test
    void quenchDamageMatchesMiteVesselTiers() {
        assertEquals(20.0F, InfxBucketItem.FIRE_ELEMENTAL_QUENCH_DAMAGE);
        assertEquals(8.0F, InfxBucketItem.NETHERSPAWN_QUENCH_DAMAGE);
    }
}
