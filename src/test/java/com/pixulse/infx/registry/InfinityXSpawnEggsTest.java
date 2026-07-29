package com.pixulse.infx.registry;

import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfinityXSpawnEggsTest {
    @Test
    void everyMobEntityHasOneSpawnEggItem() {
        Set<String> entityPaths = InfXEntityTypes.names().stream()
                .map(InfXEntityTypes.EntityName::path)
                .collect(Collectors.toSet());
        Set<String> eggPaths = InfXItems.SPAWN_EGGS.stream()
                .map(item -> item.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(53, entityPaths.size());
        assertEquals(53, eggPaths.size());
        for (String path : entityPaths) {
            assertTrue(eggPaths.contains(path + "_spawn_egg"), path);
        }
    }

    @Test
    void spawnEggTexturesExistForEveryEgg() {
        java.nio.file.Path root = java.nio.file.Path.of("").toAbsolutePath();
        while (root != null && !java.nio.file.Files.isRegularFile(root.resolve("settings.gradle"))) {
            root = root.getParent();
        }
        assertNotNull(root, "project root");
        for (var egg : InfXItems.SPAWN_EGGS) {
            Identifier id = egg.getId();
            java.nio.file.Path texture = root.resolve(
                    "src/main/resources/assets/infx/textures/item/" + id.getPath() + ".png");
            assertTrue(java.nio.file.Files.isRegularFile(texture), texture.toString());
        }
    }
}
