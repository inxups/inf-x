package com.pixulse.infx.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

/** InfX data overrides for vanilla gameplay loot tables. */
final class VanillaLootTableOverridesProvider implements DataProvider {
    private final Path carveDirectory;
    private final Path entityDirectory;

    VanillaLootTableOverridesProvider(PackOutput output) {
        Path lootRoot = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("minecraft").resolve("loot_table");
        this.carveDirectory = lootRoot.resolve("carve");
        this.entityDirectory = lootRoot.resolve("entities");
        this.blockDirectory = lootRoot.resolve("blocks");
    }

    private final Path blockDirectory;

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        return CompletableFuture.allOf(
                DataProvider.saveStable(cache, carvePumpkin(), carveDirectory.resolve("pumpkin.json")),
                DataProvider.saveStable(cache, wolfDrops(), entityDirectory.resolve("wolf.json")),
                DataProvider.saveStable(cache, copperOre("copper_ore"), blockDirectory.resolve("copper_ore.json")),
                DataProvider.saveStable(
                        cache,
                        copperOre("deepslate_copper_ore"),
                        blockDirectory.resolve("deepslate_copper_ore.json")));
    }

    /** InfX wolves leave one piece of leather even when spawned outside world generation. */
    private static JsonObject wolfDrops() {
        JsonObject setCount = new JsonObject();
        setCount.addProperty("function", "minecraft:set_count");
        setCount.addProperty("count", 1.0);
        JsonArray functions = new JsonArray();
        functions.add(setCount);

        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.add("functions", functions);
        entry.addProperty("name", "minecraft:leather");

        JsonArray entries = new JsonArray();
        entries.add(entry);

        JsonObject pool = new JsonObject();
        pool.add("entries", entries);
        pool.addProperty("rolls", 1.0);
        pool.addProperty("bonus_rolls", 0.0);

        JsonArray pools = new JsonArray();
        pools.add(pool);

        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:entity");
        table.add("pools", pools);
        table.addProperty("random_sequence", "minecraft:entities/wolf");
        return table;
    }

    /**
     * InfX copper ores drop one raw copper (fortune scales via the ore_drops formula),
     * replacing the modern uniform 2-5 drop; silk touch keeps the ore block.
     */
    private static JsonObject copperOre(String orePath) {
        JsonObject silkCondition = new JsonObject();
        silkCondition.addProperty("condition", "minecraft:match_tool");
        JsonObject enchantmentPredicate = new JsonObject();
        JsonObject levels = new JsonObject();
        levels.addProperty("min", 1.0);
        enchantmentPredicate.addProperty("enchantments", "minecraft:silk_touch");
        enchantmentPredicate.add("levels", levels);
        JsonObject predicates = new JsonObject();
        predicates.add("minecraft:enchantments", enchantmentPredicate);
        JsonObject predicate = new JsonObject();
        predicate.add("predicates", predicates);
        silkCondition.add("predicate", predicate);
        JsonArray silkConditions = new JsonArray();
        silkConditions.add(silkCondition);

        JsonObject silkEntry = new JsonObject();
        silkEntry.addProperty("type", "minecraft:item");
        silkEntry.add("conditions", silkConditions);
        silkEntry.addProperty("name", "minecraft:" + orePath);

        JsonObject fortune = new JsonObject();
        fortune.addProperty("enchantment", "minecraft:fortune");
        fortune.addProperty("formula", "minecraft:ore_drops");
        fortune.addProperty("function", "minecraft:apply_bonus");
        JsonObject explosionDecay = new JsonObject();
        explosionDecay.addProperty("function", "minecraft:explosion_decay");
        JsonArray functions = new JsonArray();
        functions.add(fortune);
        functions.add(explosionDecay);

        JsonObject rawEntry = new JsonObject();
        rawEntry.addProperty("type", "minecraft:item");
        rawEntry.add("functions", functions);
        rawEntry.addProperty("name", "minecraft:raw_copper");

        JsonArray children = new JsonArray();
        children.add(silkEntry);
        children.add(rawEntry);

        JsonObject alternatives = new JsonObject();
        alternatives.addProperty("type", "minecraft:alternatives");
        alternatives.add("children", children);

        JsonArray entries = new JsonArray();
        entries.add(alternatives);

        JsonObject pool = new JsonObject();
        pool.add("entries", entries);
        pool.addProperty("rolls", 1.0);
        pool.addProperty("bonus_rolls", 0.0);

        JsonArray pools = new JsonArray();
        pools.add(pool);

        JsonObject table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools);
        table.addProperty("random_sequence", "minecraft:blocks/" + orePath);
        return table;
    }

    /** InfX shears drop one pumpkin seed when carving, not the modern four. */
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
