package com.pixulse.infx.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

/** MITE data overrides for vanilla gameplay loot tables. */
final class VanillaLootTableOverridesProvider implements DataProvider {
    private final Path carveDirectory;

    VanillaLootTableOverridesProvider(PackOutput output) {
        this.carveDirectory = output
                .getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("minecraft")
                .resolve("loot_table")
                .resolve("carve");
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        return DataProvider.saveStable(cache, carvePumpkin(), carveDirectory.resolve("pumpkin.json"));
    }

    /** MITE shears drop one pumpkin seed when carving, not the modern four. */
    private static JsonObject carvePumpkin() {
        JsonObject setCount = new JsonObject();
        setCount.addProperty("function", "minecraft:set_count");
        setCount.addProperty("count", 1.0);
        JsonArray functions = new JsonArray();
        functions.add(setCount);

        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.add("functions", functions);
        entry.addProperty("name", "minecraft:pumpkin_seeds");

        JsonArray entries = new JsonArray();
        entries.add(entry);

        JsonObject pool = new JsonObject();
        pool.add("entries", entries);
        pool.addProperty("rolls", 1.0);
        pool.addProperty("bonus_rolls", 0.0);

        JsonArray pools = new JsonArray();
        pools.add(pool);

        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block_interact");
        table.add("pools", pools);
        table.addProperty("random_sequence", "minecraft:carve/pumpkin");
        return table;
    }

    @Override
    public @NonNull String getName() {
        return "Vanilla loot table overrides";
    }
}
