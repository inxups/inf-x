package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonsterRosterTest {
    @Test
    void overviewRosterContainsExactlyThirtyUniqueMonsters() {
        Set<String> paths = InfXEntityTypes.NEW_MONSTERS.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(30, InfXEntityTypes.NEW_MONSTERS.size());
        assertEquals(30, paths.size());
        assertEquals(
                Set.of(
                        "invisible_stalker", "ghoul", "shadow", "wight", "revenant",
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

        assertEquals(25, replacements.size());
        assertEquals(Set.of("magma_cube"), replacements.stream().filter(newMonsters::contains).collect(Collectors.toSet()));
        assertEquals(54, InfXEntityTypes.ALL.size());
        assertEquals(54, InfXEntityTypes.names().size());
        assertTrue(replacements.containsAll(Set.of(
                "infx_bat", "infx_cow", "infx_chicken", "infx_sheep", "infx_pig", "infx_horse", "infx_ocelot", "infx_wolf",
                "infx_cod", "infx_salmon", "infx_pufferfish", "infx_tropical_fish")));
    }

    @Test
    void onlyWorldDrivenSpawnReasonsAreReplacementEligible() {
        assertTrue(MonsterEvents.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.NATURAL));
        assertTrue(MonsterEvents.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.SPAWNER));
        assertFalse(MonsterEvents.isWorldSpawn(EntitySpawnReason.COMMAND));
        assertFalse(MonsterEvents.isWorldSpawn(null));
    }

    @Test
    void longdeadGuardianReplacementUsesOnlyNaturalUnderworldRollZero() {
        for (int roll = 0; roll < 6; roll++) {
            assertFalse(MonsterEvents.shouldReplaceLongdeadWithGuardian(
                    InfXEntityTypes.LONGDEAD.get(), Level.OVERWORLD, EntitySpawnReason.NATURAL, roll));
            assertEquals(
                    roll == 0,
                    MonsterEvents.shouldReplaceLongdeadWithGuardian(
                            InfXEntityTypes.LONGDEAD.get(),
                            com.pixulse.infx.world.Underworld.LEVEL,
                            EntitySpawnReason.NATURAL,
                            roll));
            assertFalse(MonsterEvents.shouldReplaceLongdeadWithGuardian(
                    InfXEntityTypes.LONGDEAD.get(),
                    com.pixulse.infx.world.Underworld.LEVEL,
                    EntitySpawnReason.SPAWNER,
                    roll));
            assertFalse(MonsterEvents.shouldReplaceLongdeadWithGuardian(
                    InfXEntityTypes.LONGDEAD_GUARDIAN.get(),
                    com.pixulse.infx.world.Underworld.LEVEL,
                    EntitySpawnReason.NATURAL,
                    roll));
        }
        assertThrows(IllegalArgumentException.class, () -> MonsterEvents.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(),
                com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.NATURAL,
                -1));
        assertThrows(IllegalArgumentException.class, () -> MonsterEvents.shouldReplaceLongdeadWithGuardian(
                InfXEntityTypes.LONGDEAD.get(),
                com.pixulse.infx.world.Underworld.LEVEL,
                EntitySpawnReason.NATURAL,
                6));
    }
}
