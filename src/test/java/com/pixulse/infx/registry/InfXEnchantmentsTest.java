package com.pixulse.infx.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.junit.jupiter.api.Test;

class InfXEnchantmentsTest {
    @Test
    void r196ProfilesPreserveEveryMiteRarityAndDifficulty() {
        Map<ResourceKey<Enchantment>, ExpectedProfile> expected = Map.ofEntries(
                Map.entry(InfXEnchantments.DURABILITY, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.DISARMING, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.QUICKNESS, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.PRECISION, expected(InfXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfXEnchantments.POISONING, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.BUTCHERING, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.STUNNING, expected(InfXEnchantments.Rarity.UNCOMMON, 15)),
                Map.entry(InfXEnchantments.VAMPIRISM, expected(InfXEnchantments.Rarity.EPIC, 20)),
                Map.entry(InfXEnchantments.RECOVERY, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.SLAUGHTER, expected(InfXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfXEnchantments.CLEAVING, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.HARVESTING, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.PENETRATION, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.BAITING, expected(InfXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfXEnchantments.FERTILITY, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.TREE_FELLING, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.FORTUNE, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.FREE_MOVEMENT, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.REGENERATION, expected(InfXEnchantments.Rarity.RARE, 20)),
                Map.entry(InfXEnchantments.SPEED, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.ENDURANCE, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.PROTECTION, expected(InfXEnchantments.Rarity.COMMON, 10)));

        assertEquals(22, InfXEnchantments.INFX.size());
        assertEquals(22, expected.size());
        assertEquals(42, InfXEnchantments.ALL.size());
        for (ResourceKey<Enchantment> key : InfXEnchantments.INFX) {
            InfXEnchantments.EnchantmentProfile actual = InfXEnchantments.profile(key);
            ExpectedProfile profile = expected.get(key);
            assertEquals(profile.rarity(), actual.rarity(), key.identifier().toString());
            assertEquals(profile.difficulty(), actual.difficulty(), key.identifier().toString());
            assertEquals(profile.rarity().weight(), actual.weight(), key.identifier().toString());
        }
        assertThrows(IllegalArgumentException.class, () -> InfXEnchantments.profile(InfXEnchantments.CLUMSINESS));
    }

    @Test
    void profileCostsUseTheMiteDifficultyWindow() {
        for (ResourceKey<Enchantment> key : InfXEnchantments.INFX) {
            InfXEnchantments.EnchantmentProfile profile = InfXEnchantments.profile(key);
            for (int level = 1; level <= 5; level++) {
                int expectedMinimum = Math.max(profile.difficulty() - 10, 0)
                        + profile.difficulty() * (level - 1) + 1;
                assertEquals(expectedMinimum, profile.minimumCost(level), key.identifier() + " level " + level);
                assertEquals(profile.difficulty(), profile.maximumCost(level) - profile.minimumCost(level) + 1,
                        key.identifier() + " level " + level);
            }
        }
    }

    /** The 17 vanilla-derived enchantments keep MITE's rarity and difficulty exactly. */
    @Test
    void vanillaProfilesPreserveEveryMiteRarityAndDifficulty() {
        Map<ResourceKey<Enchantment>, ExpectedProfile> expected = Map.ofEntries(
                Map.entry(InfXEnchantments.VANILLA_FIRE_PROTECTION, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_FEATHER_FALLING, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_BLAST_PROTECTION, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_PROJECTILE_PROTECTION, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_RESPIRATION, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.VANILLA_AQUA_AFFINITY, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.VANILLA_THORNS, expected(InfXEnchantments.Rarity.RARE, 20)),
                Map.entry(InfXEnchantments.VANILLA_SMITE, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_BANE_OF_ARTHROPODS, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_KNOCKBACK, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_FIRE_ASPECT, expected(InfXEnchantments.Rarity.RARE, 20)),
                Map.entry(InfXEnchantments.VANILLA_LOOTING, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_EFFICIENCY, expected(InfXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_SILK_TOUCH, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.VANILLA_POWER, expected(InfXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_PUNCH, expected(InfXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_FLAME, expected(InfXEnchantments.Rarity.RARE, 20)),
                Map.entry(InfXEnchantments.VANILLA_SHARPNESS, expected(InfXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfXEnchantments.VANILLA_SWEEPING_EDGE, expected(InfXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfXEnchantments.VANILLA_SWIFT_SNEAK, expected(InfXEnchantments.Rarity.RARE, 10)));

        assertEquals(20, InfXEnchantments.VANILLA_R196.size());
        assertEquals(20, expected.size());
        for (ResourceKey<Enchantment> key : InfXEnchantments.VANILLA_R196) {
            assertEquals("minecraft", key.identifier().getNamespace(), key.identifier().toString());
            InfXEnchantments.EnchantmentProfile actual = InfXEnchantments.profile(key);
            ExpectedProfile profile = expected.get(key);
            assertEquals(profile.rarity(), actual.rarity(), key.identifier().toString());
            assertEquals(profile.difficulty(), actual.difficulty(), key.identifier().toString());
        }
    }

    /** INFX registers one rare piercing enchantment that only renames itself on axes. */
    @Test
    void cleavingSharesPenetrationsPiercingProfile() {
        assertEquals(
                InfXEnchantments.profile(InfXEnchantments.PENETRATION),
                InfXEnchantments.profile(InfXEnchantments.CLEAVING));
    }

    private static ExpectedProfile expected(InfXEnchantments.Rarity rarity, int difficulty) {
        return new ExpectedProfile(rarity, difficulty);
    }

    private record ExpectedProfile(InfXEnchantments.Rarity rarity, int difficulty) {}
}
