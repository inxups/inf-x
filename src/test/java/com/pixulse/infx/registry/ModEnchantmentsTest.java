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
                Map.entry(ModEnchantments.DURABILITY, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.DISARMING, expected(ModEnchantments.R196Rarity.RARE, 10)),
                Map.entry(ModEnchantments.QUICKNESS, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.PRECISION, expected(ModEnchantments.R196Rarity.COMMON, 10)),
                Map.entry(ModEnchantments.POISONING, expected(ModEnchantments.R196Rarity.RARE, 10)),
                Map.entry(ModEnchantments.BUTCHERING, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.STUNNING, expected(ModEnchantments.R196Rarity.UNCOMMON, 15)),
                Map.entry(ModEnchantments.VAMPIRISM, expected(ModEnchantments.R196Rarity.EPIC, 20)),
                Map.entry(ModEnchantments.RECOVERY, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.SLAUGHTER, expected(ModEnchantments.R196Rarity.COMMON, 10)),
                Map.entry(ModEnchantments.CLEAVING, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.HARVESTING, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.PENETRATION, expected(ModEnchantments.R196Rarity.RARE, 10)),
                Map.entry(ModEnchantments.BAITING, expected(ModEnchantments.R196Rarity.COMMON, 10)),
                Map.entry(ModEnchantments.FERTILITY, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.TREE_FELLING, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.FORTUNE, expected(ModEnchantments.R196Rarity.RARE, 10)),
                Map.entry(ModEnchantments.FREE_MOVEMENT, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.REGENERATION, expected(ModEnchantments.R196Rarity.RARE, 20)),
                Map.entry(ModEnchantments.SPEED, expected(ModEnchantments.R196Rarity.RARE, 10)),
                Map.entry(ModEnchantments.ENDURANCE, expected(ModEnchantments.R196Rarity.UNCOMMON, 10)),
                Map.entry(ModEnchantments.PROTECTION, expected(ModEnchantments.R196Rarity.COMMON, 10)));

        assertEquals(22, ModEnchantments.R196.size());
        assertEquals(22, expected.size());
        for (ResourceKey<Enchantment> key : ModEnchantments.R196) {
            ModEnchantments.R196EnchantmentProfile actual = ModEnchantments.profile(key);
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
            ModEnchantments.R196EnchantmentProfile profile = ModEnchantments.profile(key);
            for (int level = 1; level <= 5; level++) {
                int expectedMinimum = Math.max(profile.difficulty() - 10, 0)
                        + profile.difficulty() * (level - 1) + 1;
                assertEquals(expectedMinimum, profile.minimumCost(level), key.identifier() + " level " + level);
                assertEquals(profile.difficulty(), profile.maximumCost(level) - profile.minimumCost(level) + 1,
                        key.identifier() + " level " + level);
            }
        }
    }

    private static ExpectedProfile expected(ModEnchantments.R196Rarity rarity, int difficulty) {
        return new ExpectedProfile(rarity, difficulty);
    }

    private record ExpectedProfile(ModEnchantments.R196Rarity rarity, int difficulty) {}
}
