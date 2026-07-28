package com.pixulse.infx.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.junit.jupiter.api.Test;

class ModEnchantmentsTest {
    @Test
    void r196ProfilesPreserveEveryMiteRarityAndDifficulty() {
        Map<ResourceKey<Enchantment>, ExpectedProfile> expected = Map.ofEntries(
                Map.entry(ModEnchantments.DURABILITY, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.DISARMING, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.QUICKNESS, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.PRECISION, expected(ModEnchantments.Rarity.COMMON, 10)),
                Map.entry(ModEnchantments.POISONING, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.BUTCHERING, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.STUNNING, expected(ModEnchantments.Rarity.UNCOMMON, 15)),
                Map.entry(ModEnchantments.VAMPIRISM, expected(ModEnchantments.Rarity.EPIC, 20)),
                Map.entry(ModEnchantments.RECOVERY, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.SLAUGHTER, expected(ModEnchantments.Rarity.COMMON, 10)),
                Map.entry(ModEnchantments.CLEAVING, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.HARVESTING, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.PENETRATION, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.BAITING, expected(ModEnchantments.Rarity.COMMON, 10)),
                Map.entry(ModEnchantments.FERTILITY, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.TREE_FELLING, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.FORTUNE, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.FREE_MOVEMENT, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.REGENERATION, expected(ModEnchantments.Rarity.RARE, 20)),
                Map.entry(ModEnchantments.SPEED, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.ENDURANCE, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.PROTECTION, expected(ModEnchantments.Rarity.COMMON, 10)));

        assertEquals(22, ModEnchantments.R196.size());
        assertEquals(22, expected.size());
        assertEquals(39, ModEnchantments.ALL.size());
        for (ResourceKey<Enchantment> key : ModEnchantments.R196) {
            ModEnchantments.EnchantmentProfile actual = ModEnchantments.profile(key);
            ExpectedProfile profile = expected.get(key);
            assertEquals(profile.rarity(), actual.rarity(), key.identifier().toString());
            assertEquals(profile.difficulty(), actual.difficulty(), key.identifier().toString());
            assertEquals(profile.rarity().weight(), actual.weight(), key.identifier().toString());
        }
        assertThrows(IllegalArgumentException.class, () -> ModEnchantments.profile(ModEnchantments.CLUMSINESS));
    }

    @Test
    void profileCostsUseTheMiteDifficultyWindow() {
        for (ResourceKey<Enchantment> key : ModEnchantments.R196) {
            ModEnchantments.EnchantmentProfile profile = ModEnchantments.profile(key);
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
                Map.entry(ModEnchantments.VANILLA_FIRE_PROTECTION, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_FEATHER_FALLING, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_BLAST_PROTECTION, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_PROJECTILE_PROTECTION, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_RESPIRATION, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.VANILLA_AQUA_AFFINITY, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.VANILLA_THORNS, expected(ModEnchantments.Rarity.RARE, 20)),
                Map.entry(ModEnchantments.VANILLA_SMITE, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_BANE_OF_ARTHROPODS, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_KNOCKBACK, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_FIRE_ASPECT, expected(ModEnchantments.Rarity.RARE, 20)),
                Map.entry(ModEnchantments.VANILLA_LOOTING, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_EFFICIENCY, expected(ModEnchantments.Rarity.COMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_SILK_TOUCH, expected(ModEnchantments.Rarity.RARE, 10)),
                Map.entry(ModEnchantments.VANILLA_POWER, expected(ModEnchantments.Rarity.COMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_PUNCH, expected(ModEnchantments.Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.VANILLA_FLAME, expected(ModEnchantments.Rarity.RARE, 20)));

        assertEquals(17, ModEnchantments.VANILLA_R196.size());
        assertEquals(17, expected.size());
        for (ResourceKey<Enchantment> key : ModEnchantments.VANILLA_R196) {
            assertEquals("minecraft", key.identifier().getNamespace(), key.identifier().toString());
            ModEnchantments.EnchantmentProfile actual = ModEnchantments.profile(key);
            ExpectedProfile profile = expected.get(key);
            assertEquals(profile.rarity(), actual.rarity(), key.identifier().toString());
            assertEquals(profile.difficulty(), actual.difficulty(), key.identifier().toString());
        }
    }

    /** R196 registers one rare piercing enchantment that only renames itself on axes. */
    @Test
    void cleavingSharesPenetrationsPiercingProfile() {
        assertEquals(
                ModEnchantments.profile(ModEnchantments.PENETRATION),
                ModEnchantments.profile(ModEnchantments.CLEAVING));
    }

    private static ExpectedProfile expected(ModEnchantments.Rarity rarity, int difficulty) {
        return new ExpectedProfile(rarity, difficulty);
    }

    private record ExpectedProfile(ModEnchantments.Rarity rarity, int difficulty) {}
}
