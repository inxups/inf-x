package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.world.entity.EntitySpawnReason;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonsterRosterTest {
    @Test
    void overviewRosterContainsExactlyTwentyNineUniqueMonsters() {
        Set<String> paths = InfXEntityTypes.NEW_MONSTERS.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(29, InfXEntityTypes.NEW_MONSTERS.size());
        assertEquals(29, paths.size());
        assertEquals(
                Set.of(
                        "invisible_stalker", "ghoul", "shadow", "wight", "revenant",
                        "longdead", "bone_lord", "ancient_bone_lord",
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
        assertEquals(53, InfXEntityTypes.ALL.size());
        assertEquals(53, InfXEntityTypes.names().size());
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
}
