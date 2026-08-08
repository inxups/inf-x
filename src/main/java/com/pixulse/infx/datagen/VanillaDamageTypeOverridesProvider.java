package com.pixulse.infx.datagen;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

/**
 * InfX data overrides for vanilla gameplay damage types.
 *
 * <p>{@code minecraft:magic} is pointed at InfX's {@code infx:poison} death message type so the
 * poison death message comes from the {@link net.neoforged.neoforge.common.damagesource.IDeathMessageProvider}
 * registered by {@code META-INF/enumextender.json} instead of a mixin.
 */
final class VanillaDamageTypeOverridesProvider implements DataProvider {
    private final Path damageTypeDirectory;

    VanillaDamageTypeOverridesProvider(PackOutput output) {
        this.damageTypeDirectory = output
                .getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("minecraft")
                .resolve("damage_type");
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        return DataProvider.saveStable(cache, magic(), damageTypeDirectory.resolve("magic.json"));
    }

    @Override
    public @NonNull String getName() {
        return "Vanilla damage type overrides";
    }

    /** The vanilla magic damage type with only the death message type replaced. */
    private static JsonObject magic() {
        JsonObject magic = new JsonObject();
        magic.addProperty("message_id", "magic");
        magic.addProperty("scaling", "when_caused_by_living_non_player");
        magic.addProperty("exhaustion", 0.0);
        magic.addProperty("death_message_type", "infx:poison");
        return magic;
    }
}
