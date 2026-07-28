package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.registry.ModEntityTypes;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MonsterRosterTest {
    @Test
    void overviewRosterContainsExactlyTwentyNineUniqueMonsters() {
        Set<String> paths = ModEntityTypes.NEW_MONSTERS.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(29, ModEntityTypes.NEW_MONSTERS.size());
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
        Set<String> replacements = ModEntityTypes.REPLACEMENT_ENTITIES.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());
        Set<String> newMonsters = ModEntityTypes.NEW_MONSTERS.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(24, replacements.size());
        assertEquals(Set.of("magma_cube"), replacements.stream().filter(newMonsters::contains).collect(Collectors.toSet()));
        assertEquals(52, ModEntityTypes.ALL.size());
        assertEquals(52, ModEntityTypes.names().size());
        assertTrue(replacements.containsAll(Set.of(
                "r196_cow", "r196_chicken", "r196_sheep", "r196_pig", "r196_horse", "r196_ocelot", "r196_wolf",
                "r196_cod", "r196_salmon", "r196_pufferfish", "r196_tropical_fish")));
    }

    @Test
    void onlyWorldDrivenSpawnReasonsAreReplacementEligible() {
        assertTrue(MonsterEvents.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.NATURAL));
        assertTrue(MonsterEvents.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.SPAWNER));
        assertTrue(!MonsterEvents.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.COMMAND));
        assertTrue(!MonsterEvents.isWorldSpawn(null));
    }
}
