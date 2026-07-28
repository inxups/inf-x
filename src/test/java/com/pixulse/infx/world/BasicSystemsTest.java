package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.*;

import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfinityXEnchantments;
import java.util.HashSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class BasicSystemsTest {
    @Test
    void moonMultipliersAndWarningsFollowCalendar() {
        assertEquals(6, MoonPhase.NEW.outdoorHostileSpawnDenominator());
        assertEquals(3, MoonPhase.FULL.outdoorHostileSpawnDenominator());
        assertEquals(2, MoonPhase.BLOOD.outdoorHostileSpawnDenominator());
        assertEquals(54, MoonPhase.BLUE.outdoorHostileSpawnDenominator());
        assertFalse(MoonPhase.BLOOD.allowsSleep());
        assertEquals(MoonPhase.BLUE, MoonPhase.atDay(128));
        assertEquals(MoonPhase.PHANTOM, MoonPhase.atDay(120));
    }

    @Test
    void fallCushioningAndExplosionWearAreOrdered() {
        assertTrue(PhysicsRules.fallDamageMultiplier(Blocks.SPONGE.defaultBlockState())
                < PhysicsRules.fallDamageMultiplier(Blocks.SNOW_BLOCK.defaultBlockState()));
        assertTrue(PhysicsRules.fallDamageMultiplier(Blocks.SNOW_BLOCK.defaultBlockState())
                < PhysicsRules.fallDamageMultiplier(Blocks.DIRT.defaultBlockState()));
        assertTrue(PhysicsRules.explosionWear(0, 6) > PhysicsRules.explosionWear(5, 6));
        assertEquals(0, PhysicsRules.explosionWear(6, 6));
    }

    @Test
    void grassBlockIsNotLooseTerrain() {
        assertFalse(PhysicsRules.isLoose(Blocks.GRASS_BLOCK.defaultBlockState()));
        assertTrue(PhysicsRules.isLoose(Blocks.DIRT.defaultBlockState()));
        assertTrue(PhysicsRules.isLoose(Blocks.GRAVEL.defaultBlockState()));
    }

    @Test
    void safeOwnershipUsesMitePortabilityAndNumericHarvestLevels() {
        assertTrue(SafeEvents.mayBreak(MiteMaterial.COPPER, true, null));
        assertFalse(SafeEvents.mayBreak(MiteMaterial.COPPER, false, MiteMaterial.COPPER));
        assertFalse(SafeEvents.mayBreak(MiteMaterial.COPPER, false, MiteMaterial.SILVER));
        assertTrue(SafeEvents.mayBreak(MiteMaterial.COPPER, false, MiteMaterial.IRON));
        assertTrue(SafeEvents.mayBreak(MiteMaterial.MITHRIL, false, MiteMaterial.ADAMANTIUM));
        assertFalse(SafeEvents.mayBreak(MiteMaterial.ADAMANTIUM, false, MiteMaterial.ADAMANTIUM));
    }

    @Test
    void safeBlockItemDropsOnlyForOwnerOrCreative() {
        assertFalse(SafeEvents.mayDropSafeItem(null, null));
    }

    @Test
    void allTwentyTwoEnchantmentsAreDeclared() {
        assertEquals(22, InfinityXEnchantments.R196.size());
        assertEquals(22, InfinityXEnchantments.R196.stream().distinct().count());
    }

    @Test
    void creationBookIndexRequiresCorrectAuthorAndAllNineBits() {
        assertEquals(0, CreationBooks.index(CreationBooks.AUTHOR, "Boat"));
        assertEquals(-1, CreationBooks.index("Impostor", "Boat"));
        assertTrue(CreationBooks.complete(0x1FF));
        assertFalse(CreationBooks.complete(0x0FF));
    }

    @Test
    void everyCreationBookTitleCanBeClaimedExactlyOnce() {
        WorldData data = new WorldData();
        HashSet<Integer> claimed = new HashSet<>();
        RandomSource random = RandomSource.create(196L);
        for (int index = 0; index < CreationBooks.TITLES.size(); index++) {
            int title = data.claimCreationBook(random);
            assertTrue(title >= 0);
            assertTrue(claimed.add(title));
        }
        assertEquals(0x1FF, data.creationBookMask());
        assertEquals(-1, data.claimCreationBook(random));
    }
}
