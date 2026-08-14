package com.pixulse.infx.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/** Reloadable overworld spawn conditions in {@code data/<namespace>/mob_spawn_rules}. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class MobSpawnRules {
    private static final FileToIdConverter RULE_LISTER = FileToIdConverter.json("mob_spawn_rules");
    private static volatile Map<Identifier, Rule> rules = Map.of();

    private MobSpawnRules() {}

    @SubscribeEvent
    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(InfiniteX.id("mob_spawn_rules"), new ReloadListener());
    }

    /** Returns empty when a type has no datapack rule and callers should use their normal predicate. */
    public static Optional<Boolean> allows(
            EntityType<?> type, Level level, BlockPos pos, RandomSource random) {
        Rule rule = rules.get(BuiltInRegistries.ENTITY_TYPE.getKey(type));
        if (rule == null || level.dimension() != Level.OVERWORLD) {
            return Optional.empty();
        }
        if (rule.minOverworldY().isPresent() && pos.getY() < rule.minOverworldY().get()
                || rule.maxOverworldY().isPresent() && pos.getY() > rule.maxOverworldY().get()
                || random.nextFloat() >= rule.randomChance()) {
            return Optional.of(false);
        }
        if (rule.requiresCeiling() && level.canSeeSky(pos)) {
            return Optional.of(false);
        }
        if (!rule.spawnOn().isEmpty()) {
            BlockState ground = level.getBlockState(pos.below());
            if (rule.spawnOn().stream().noneMatch(target -> target.map(ground::is, ground::is))) {
                return Optional.of(false);
            }
        }
        return Optional.of(true);
    }

    private record Rule(
            Optional<Integer> minOverworldY,
            Optional<Integer> maxOverworldY,
            float randomChance,
            boolean requiresCeiling,
            List<Either<Block, TagKey<Block>>> spawnOn) {
        private static final Codec<Either<Block, TagKey<Block>>> BLOCK_OR_TAG = Codec.either(
                BuiltInRegistries.BLOCK.byNameCodec(), TagKey.hashedCodec(Registries.BLOCK));
        private static final Codec<Float> CHANCE = Codec.FLOAT.flatXmap(
                value -> Float.isFinite(value) && value >= 0.0F && value <= 1.0F
                        ? DataResult.success(value)
                        : DataResult.error(() -> "random_chance must be between 0 and 1"),
                value -> Float.isFinite(value) && value >= 0.0F && value <= 1.0F
                        ? DataResult.success(value)
                        : DataResult.error(() -> "random_chance must be between 0 and 1"));
        private static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("min_overworld_y").forGetter(Rule::minOverworldY),
                Codec.INT.optionalFieldOf("max_overworld_y").forGetter(Rule::maxOverworldY),
                CHANCE.optionalFieldOf("random_chance", 1.0F).forGetter(Rule::randomChance),
                Codec.BOOL.optionalFieldOf("requires_ceiling", false).forGetter(Rule::requiresCeiling),
                BLOCK_OR_TAG.listOf().optionalFieldOf("spawn_on", List.of()).forGetter(Rule::spawnOn)
        ).apply(instance, Rule::new));

        private Rule {
            spawnOn = List.copyOf(spawnOn);
            if (minOverworldY.isPresent() && maxOverworldY.isPresent()
                    && minOverworldY.get() > maxOverworldY.get()) {
                throw new IllegalArgumentException("min_overworld_y must not exceed max_overworld_y");
            }
        }
    }

    private static final class ReloadListener extends SimplePreparableReloadListener<Map<Identifier, Rule>> {
        @Override
        protected Map<Identifier, Rule> prepare(ResourceManager manager, ProfilerFiller profiler) {
            Map<Identifier, Rule> loaded = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(
                    manager, RULE_LISTER, JsonOps.INSTANCE, Rule.CODEC, loaded);
            return Map.copyOf(loaded);
        }

        @Override
        protected void apply(Map<Identifier, Rule> data, ResourceManager manager, ProfilerFiller profiler) {
            rules = data;
            InfiniteX.LOGGER.info("Loaded {} mob spawn rules", data.size());
        }
    }
}
