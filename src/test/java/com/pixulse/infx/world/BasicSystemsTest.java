package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.*;

import com.pixulse.infx.event.SafeEvents;
import com.pixulse.infx.item.material.InfxMaterial;
import java.util.HashSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
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
    void visualMoonTimelineAndNightWindowMatchCalendar() {
        assertEquals(net.minecraft.world.level.MoonPhase.WANING_GIBBOUS, MoonPhase.visualPhaseAtTime(0));
        assertEquals(net.minecraft.world.level.MoonPhase.NEW_MOON, MoonPhase.visualPhaseAtTime(72_000));
        assertEquals(net.minecraft.world.level.MoonPhase.FULL_MOON, MoonPhase.visualPhaseAtTime(168_000));
        assertTrue(MoonPhase.isOverworld(Level.OVERWORLD));
        assertFalse(MoonPhase.isOverworld(Level.NETHER));
        assertFalse(MoonPhase.isNightTime(12_999));
        assertTrue(MoonPhase.isNightTime(13_000));
        assertTrue(MoonPhase.isNightTime(22_999));
        assertFalse(MoonPhase.isNightTime(23_000));
        assertFalse(MoonPhase.isNightTime(23_001));
    }

    @Test
    void miteMoonBrightnessAndStormWindowMatchMite() {
        assertEquals(0.6F, MoonPhase.miteMoonBrightness(744_000L));       // blood moon day 32
        assertEquals(1.1F, MoonPhase.miteMoonBrightness(3_048_000L));     // blue moon day 128
        assertEquals(1.0F, MoonPhase.miteMoonBrightness(552_000L));       // harvest moon day 24
        assertEquals(1.25F, MoonPhase.miteMoonBrightness(168_000L), 1.0E-6F); // full moon day 8
        assertEquals(0.75F, MoonPhase.miteMoonBrightness(264_000L), 1.0E-6F); // new moon day 12

        assertFalse(MoonPhase.isBloodMoonThunderWindow(744_000L + 5_999L));
        assertTrue(MoonPhase.isBloodMoonThunderWindow(744_000L + 6_000L));
        assertTrue(MoonPhase.isBloodMoonThunderWindow(744_000L + 23_000L));
        assertFalse(MoonPhase.isBloodMoonThunderWindow(720_000L)); // day 31, ordinary
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
    void safeOwnershipUsesPortabilityAndNumericHarvestLevels() {
        assertTrue(SafeEvents.mayBreak(InfxMaterial.COPPER, true, null));
        assertFalse(SafeEvents.mayBreak(InfxMaterial.COPPER, false, InfxMaterial.COPPER));
        assertFalse(SafeEvents.mayBreak(InfxMaterial.COPPER, false, InfxMaterial.SILVER));
        assertTrue(SafeEvents.mayBreak(InfxMaterial.COPPER, false, InfxMaterial.IRON));
        assertTrue(SafeEvents.mayBreak(InfxMaterial.MITHRIL, false, InfxMaterial.ADAMANTIUM));
        assertFalse(SafeEvents.mayBreak(InfxMaterial.ADAMANTIUM, false, InfxMaterial.ADAMANTIUM));
    }

    @Test
    void safeBlockItemDropsOnlyForOwnerOrCreative() {
        assertFalse(SafeEvents.mayDropSafeItem(null, null));
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
