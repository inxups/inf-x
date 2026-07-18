package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.registry.ModEntityTypes;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class R196MonsterRosterTest {
    @Test
    void overviewRosterContainsExactlyTwentyEightUniqueMonsters() {
        Set<String> paths = ModEntityTypes.NEW_MONSTERS.stream()
                .map(holder -> holder.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(28, ModEntityTypes.NEW_MONSTERS.size());
        assertEquals(28, paths.size());
        assertEquals(
                Set.of(
                        "invisible_stalker", "ghoul", "shadow", "wight", "revenant",
                        "longdead", "bone_lord", "ancient_bone_lord",
                        "black_widow_spider", "demon_spider", "wood_spider", "phase_spider",
                        "infernal_creeper", "fire_elemental", "earth_elemental",
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

        assertEquals(13, replacements.size());
        assertEquals(Set.of("magma_cube"), replacements.stream().filter(newMonsters::contains).collect(Collectors.toSet()));
        assertEquals(40, ModEntityTypes.ALL.size());
        assertEquals(40, ModEntityTypes.names().size());
    }

    @Test
    void onlyWorldDrivenSpawnReasonsAreReplacementEligible() {
        assertTrue(R196MonsterEvents.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.NATURAL));
        assertTrue(R196MonsterEvents.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.SPAWNER));
        assertTrue(!R196MonsterEvents.isWorldSpawn(net.minecraft.world.entity.EntitySpawnReason.COMMAND));
        assertTrue(!R196MonsterEvents.isWorldSpawn(null));
    }
}
