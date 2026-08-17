package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.world.SpawnGate;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonsterRosterTest {
    @Test
    void overviewRosterContainsExactlyThirtyOneUniqueMonsters() {
        Set<String> paths = InfXEntityTypes.NEW_MONSTERS.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(
                Set.of(
                        "invisible_stalker", "ghoul", "shadow", "wight", "revenant",
                        "infx_wither_skeleton",
                        "longdead", "longdead_guardian", "bone_lord", "ancient_bone_lord",
                        "black_widow_spider", "demon_spider", "wood_spider", "phase_spider",
                        "infernal_creeper", "fire_elemental", "earth_elemental", "clay_golem",
                        "jelly", "blob", "ooze", "pudding", "magma_cube",
                        "netherspawn", "copperspine", "hoary_silverfish",
                        "vampire_bat", "nightwing", "giant_vampire_bat",
                        "hellhound", "dire_wolf"),
                paths);
    }

    @Test
    void replacementAndNewEntityRegistriesStayDisjointExceptForMagmaCube() {
        Set<String> replacements = InfXEntityTypes.REPLACEMENT_ENTITIES.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());
        Set<String> newMonsters = InfXEntityTypes.NEW_MONSTERS.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(Set.of("magma_cube"), replacements.stream().filter(newMonsters::contains).collect(Collectors.toSet()));
        assertTrue(replacements.containsAll(Set.of(
                "infx_bat", "infx_cow", "infx_chicken", "infx_sheep", "infx_pig", "infx_horse", "infx_ocelot", "infx_wolf",
                "infx_cod", "infx_salmon", "infx_pufferfish", "infx_tropical_fish")));
    }

    @Test
    void onlyWorldDrivenSpawnReasonsAreReplacementEligible() {
        assertTrue(SpawnGate.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.NATURAL));
        assertTrue(SpawnGate.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.SPAWNER));
        assertFalse(SpawnGate.isWorldSpawn(EntitySpawnReason.COMMAND));
        assertFalse(SpawnGate.isWorldSpawn(null));
    }

    @Test
    void longdeadGuardianReplacementUsesOnlyNaturalUnderworldRollZero() {
        for (int roll = 0; roll < 6; roll++) {
            assertFalse(SpawnGate.shouldReplaceLongdeadWithGuardian(
                    InfXEntityTypes.LONGDEAD.get(), Level.OVERWORLD, EntitySpawnReason.NATURAL, roll));
            assertEquals(
                    roll == 0,
                    SpawnGate.shouldReplaceLongdeadWithGuardian(
                            InfXEntityTypes.LONGDEAD.get(),
                            com.pixulse.infx.world.Underworld.LEVEL,
                            EntitySpawnReason.NATURAL,
                            roll));
            assertFalse(SpawnGate.shouldReplaceLongdeadWithGuardian(
                    InfXEntityTypes.LONGDEAD.get(),
                    com.pixulse.infx.world.Underworld.LEVEL,
                    EntitySpawnReason.SPAWNER,
                    roll));
            assertFalse(SpawnGate.shouldReplaceLongdeadWithGuardian(
                    InfXEntityTypes.LONGDEAD_GUARDIAN.get(),
                    com.pixulse.infx.world.Underworld.LEVEL,
                    EntitySpawnReason.NATURAL,
                    roll));
        }
        assertThrows(IllegalArgumentException.class, () -> SpawnGate.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(),
                com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.NATURAL,
                -1));
        assertThrows(IllegalArgumentException.class, () -> SpawnGate.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(),
                com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.NATURAL,
                6));
    }
}
