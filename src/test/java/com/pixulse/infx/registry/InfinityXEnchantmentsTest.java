package com.pixulse.infx.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.junit.jupiter.api.Test;

class InfinityXEnchantmentsTest {
    @Test
    void r196ProfilesPreserveEveryMiteRarityAndDifficulty() {
        Map<ResourceKey<Enchantment>, ExpectedProfile> expected = Map.ofEntries(
                Map.entry(InfinityXEnchantments.DURABILITY, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.DISARMING, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.QUICKNESS, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.PRECISION, expected(InfinityXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfinityXEnchantments.POISONING, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.BUTCHERING, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.STUNNING, expected(InfinityXEnchantments.Rarity.UNCOMMON, 15)),
                Map.entry(InfinityXEnchantments.VAMPIRISM, expected(InfinityXEnchantments.Rarity.EPIC, 20)),
                Map.entry(InfinityXEnchantments.RECOVERY, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.SLAUGHTER, expected(InfinityXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfinityXEnchantments.CLEAVING, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.HARVESTING, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.PENETRATION, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.BAITING, expected(InfinityXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfinityXEnchantments.FERTILITY, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.TREE_FELLING, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.FORTUNE, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.FREE_MOVEMENT, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.REGENERATION, expected(InfinityXEnchantments.Rarity.RARE, 20)),
                Map.entry(InfinityXEnchantments.SPEED, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.ENDURANCE, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.PROTECTION, expected(InfinityXEnchantments.Rarity.COMMON, 10)));

        assertEquals(22, InfinityXEnchantments.R196.size());
        assertEquals(22, expected.size());
        assertEquals(39, InfinityXEnchantments.ALL.size());
        for (ResourceKey<Enchantment> key : InfinityXEnchantments.R196) {
            InfinityXEnchantments.EnchantmentProfile actual = InfinityXEnchantments.profile(key);
            ExpectedProfile profile = expected.get(key);
            assertEquals(profile.rarity(), actual.rarity(), key.identifier().toString());
            assertEquals(profile.difficulty(), actual.difficulty(), key.identifier().toString());
            assertEquals(profile.rarity().weight(), actual.weight(), key.identifier().toString());
        }
        assertThrows(IllegalArgumentException.class, () -> InfinityXEnchantments.profile(InfinityXEnchantments.CLUMSINESS));
    }

    @Test
    void profileCostsUseTheMiteDifficultyWindow() {
        for (ResourceKey<Enchantment> key : InfinityXEnchantments.R196) {
            InfinityXEnchantments.EnchantmentProfile profile = InfinityXEnchantments.profile(key);
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
                Map.entry(InfinityXEnchantments.VANILLA_FIRE_PROTECTION, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_FEATHER_FALLING, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_BLAST_PROTECTION, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_PROJECTILE_PROTECTION, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_RESPIRATION, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_AQUA_AFFINITY, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_THORNS, expected(InfinityXEnchantments.Rarity.RARE, 20)),
                Map.entry(InfinityXEnchantments.VANILLA_SMITE, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_BANE_OF_ARTHROPODS, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_KNOCKBACK, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_FIRE_ASPECT, expected(InfinityXEnchantments.Rarity.RARE, 20)),
                Map.entry(InfinityXEnchantments.VANILLA_LOOTING, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_EFFICIENCY, expected(InfinityXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_SILK_TOUCH, expected(InfinityXEnchantments.Rarity.RARE, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_POWER, expected(InfinityXEnchantments.Rarity.COMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_PUNCH, expected(InfinityXEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(InfinityXEnchantments.VANILLA_FLAME, expected(InfinityXEnchantments.Rarity.RARE, 20)));

        assertEquals(17, InfinityXEnchantments.VANILLA_R196.size());
        assertEquals(17, expected.size());
        for (ResourceKey<Enchantment> key : InfinityXEnchantments.VANILLA_R196) {
            assertEquals("minecraft", key.identifier().getNamespace(), key.identifier().toString());
            InfinityXEnchantments.EnchantmentProfile actual = InfinityXEnchantments.profile(key);
            ExpectedProfile profile = expected.get(key);
            assertEquals(profile.rarity(), actual.rarity(), key.identifier().toString());
            assertEquals(profile.difficulty(), actual.difficulty(), key.identifier().toString());
        }
    }

    /** R196 registers one rare piercing enchantment that only renames itself on axes. */
    @Test
    void cleavingSharesPenetrationsPiercingProfile() {
        assertEquals(
                InfinityXEnchantments.profile(InfinityXEnchantments.PENETRATION),
                InfinityXEnchantments.profile(InfinityXEnchantments.CLEAVING));
    }

    private static ExpectedProfile expected(InfinityXEnchantments.Rarity rarity, int difficulty) {
        return new ExpectedProfile(rarity, difficulty);
    }

    private record ExpectedProfile(InfinityXEnchantments.Rarity rarity, int difficulty) {}
}
