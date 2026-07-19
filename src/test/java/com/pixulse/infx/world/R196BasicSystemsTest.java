package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.*;

import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModEnchantments;
import java.util.HashSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class R196BasicSystemsTest {
    @Test
    void moonMultipliersAndWarningsFollowCalendar() {
        assertEquals(0.5D, R196MoonPhase.NEW.hostileSpawnRate());
        assertEquals(1.5D, R196MoonPhase.FULL.hostileSpawnRate());
        assertEquals(2.0D, R196MoonPhase.BLOOD.hostileSpawnRate());
        assertEquals(0.5D, R196MoonPhase.BLUE.hostileSpawnRate());
        assertFalse(R196MoonPhase.BLOOD.allowsSleep());
        assertEquals(R196MoonPhase.BLUE, R196MoonPhase.atDay(128));
        assertEquals(R196MoonPhase.PHANTOM, R196MoonPhase.atDay(120));
    }

    @Test
    void fallCushioningAndExplosionWearAreOrdered() {
        assertTrue(R196PhysicsRules.fallDamageMultiplier(Blocks.SPONGE.defaultBlockState())
                < R196PhysicsRules.fallDamageMultiplier(Blocks.SNOW_BLOCK.defaultBlockState()));
        assertTrue(R196PhysicsRules.fallDamageMultiplier(Blocks.SNOW_BLOCK.defaultBlockState())
                < R196PhysicsRules.fallDamageMultiplier(Blocks.DIRT.defaultBlockState()));
        assertTrue(R196PhysicsRules.explosionWear(0, 6) > R196PhysicsRules.explosionWear(5, 6));
        assertEquals(0, R196PhysicsRules.explosionWear(6, 6));
    }

    @Test
    void safeOwnershipUsesOneHigherMetalTier() {
        assertTrue(R196SafeEvents.mayBreak(R196Material.COPPER, true, R196Material.COPPER));
        assertFalse(R196SafeEvents.mayBreak(R196Material.COPPER, false, R196Material.COPPER));
        assertTrue(R196SafeEvents.mayBreak(R196Material.COPPER, false, R196Material.SILVER));
        assertTrue(R196SafeEvents.mayBreak(R196Material.MITHRIL, false, R196Material.ADAMANTIUM));
        assertFalse(R196SafeEvents.mayBreak(R196Material.ADAMANTIUM, false, R196Material.ADAMANTIUM));
    }

    @Test
    void allTwentyTwoEnchantmentsAreDeclared() {
        assertEquals(22, ModEnchantments.R196.size());
        assertEquals(22, ModEnchantments.R196.stream().distinct().count());
    }

    @Test
    void creationBookIndexRequiresCorrectAuthorAndAllNineBits() {
        assertEquals(0, R196CreationBooks.index(R196CreationBooks.AUTHOR, "Boat"));
        assertEquals(-1, R196CreationBooks.index("Impostor", "Boat"));
        assertTrue(R196CreationBooks.complete(0x1FF));
        assertFalse(R196CreationBooks.complete(0x0FF));
    }

    @Test
    void everyCreationBookTitleCanBeClaimedExactlyOnce() {
        R196WorldData data = new R196WorldData();
        HashSet<Integer> claimed = new HashSet<>();
        RandomSource random = RandomSource.create(196L);
        for (int index = 0; index < R196CreationBooks.TITLES.size(); index++) {
            int title = data.claimCreationBook(random);
            assertTrue(title >= 0);
            assertTrue(claimed.add(title));
        }
        assertEquals(0x1FF, data.creationBookMask());
        assertEquals(-1, data.claimCreationBook(random));
    }
}
