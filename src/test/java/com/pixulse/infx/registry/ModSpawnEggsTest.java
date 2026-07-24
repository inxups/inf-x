package com.pixulse.infx.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class ModSpawnEggsTest {
    @Test
    void everyMobEntityHasOneSpawnEggItem() {
        Set<String> entityPaths = ModEntityTypes.names().stream()
                .map(ModEntityTypes.EntityName::path)
                .collect(Collectors.toSet());
        Set<String> eggPaths = ModItems.SPAWN_EGGS.stream()
                .map(item -> item.getId().getPath())
                .collect(Collectors.toSet());

        assertEquals(40, entityPaths.size());
        assertEquals(40, eggPaths.size());
        for (String path : entityPaths) {
            assertTrue(eggPaths.contains(path + "_spawn_egg"), path);
        }
    }

    @Test
    void spawnEggTexturesExistForEveryEgg() throws Exception {
        java.nio.file.Path root = java.nio.file.Path.of("").toAbsolutePath();
        while (root != null && !java.nio.file.Files.isRegularFile(root.resolve("settings.gradle"))) {
            root = root.getParent();
        }
        assertTrue(root != null, "project root");
        for (var egg : ModItems.SPAWN_EGGS) {
            Identifier id = egg.getId();
            java.nio.file.Path texture = root.resolve(
                    "src/main/resources/assets/infx/textures/item/" + id.getPath() + ".png");
            assertTrue(java.nio.file.Files.isRegularFile(texture), texture.toString());
        }
    }
}
