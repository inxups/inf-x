package com.pixulse.infx.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;

/**
 * A datapack rule that overrides the inferred INFX crafting difficulty and
 * workbench tier of the recipes it targets.
 *
 * <p>Rules live at {@code data/<namespace>/recipe_rules/<name>.json} and use
 * the file name only as the rule name; the {@code target} field declares the
 * affected recipes explicitly. {@code target} accepts a single recipe ID, an
 * array of recipe IDs, or {@code #recipe} tag references. Every field except
 * {@code target} is optional, but at least one of {@code difficulty} and
 * {@code workbench_tier} must be present.</p>
 *
 * <pre>{@code
 * {
 *   "target": ["infx:copper_pickaxe", "#infx:metal_tools"],
 *   "difficulty": 1250.0,
 *   "workbench_tier": "copper"
 * }
 * }</pre>
 */
public record RecipeRule(
        Identifier ruleId,
        List<Target> targets,
        Optional<Float> difficulty,
        Optional<BenchTier> workbenchTier) {

    public static final Codec<RecipeRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    // Single ID/tag or an array of them.
                    Codec.either(Target.CODEC, Target.CODEC.listOf())
                            .xmap(
                                    either -> either.map(List::of, list -> list),
                                    list -> list.size() == 1
                                            ? com.mojang.datafixers.util.Either.left(list.getFirst())
                                            : com.mojang.datafixers.util.Either.right(list))
                            .fieldOf("target")
                            .forGetter(RecipeRule::targets),
                    strictOptionalField("difficulty", Codec.FLOAT).forGetter(RecipeRule::difficulty),
                    strictOptionalField("workbench_tier", BenchTier.CODEC)
                            .forGetter(RecipeRule::workbenchTier))
            // The rule ID is the file name and is filled in by the loader/datagen.
            .apply(instance, (targets, difficulty, tier) -> new RecipeRule(null, targets, difficulty, tier)));

    public RecipeRule {
        if (targets == null || targets.isEmpty()) {
            throw new IllegalArgumentException("recipe rule requires at least one target");
        }
        if (difficulty.isPresent()
                && (!Float.isFinite(difficulty.get()) || difficulty.get() <= 0.0F)) {
            throw new IllegalArgumentException("difficulty must be a positive finite number");
        }
        if (difficulty.isEmpty() && workbenchTier.isEmpty()) {
            throw new IllegalArgumentException("recipe rule must set difficulty and/or workbench_tier");
        }
    }

    /** Datagen helper for the common single-recipe rule. */
    public static RecipeRule of(ResourceKey<Recipe<?>> ruleId, Identifier recipeId, float difficulty, BenchTier tier) {
        return new RecipeRule(ruleId.identifier(), List.of(new IdTarget(recipeId)), Optional.of(difficulty), Optional.of(tier));
    }

    /** One {@code target} entry: either a concrete recipe ID or a recipe tag. */
    public sealed interface Target permits IdTarget, TagTarget {
        Codec<Target> CODEC = Codec.STRING.flatXmap(
                text -> {
                    if (text.startsWith("#")) {
                        Identifier tagId = Identifier.tryParse(text.substring(1));
                        return tagId == null
                                ? DataResult.error(() -> "Invalid recipe tag target: " + text)
                                : DataResult.success(new TagTarget(TagKey.create(Registries.RECIPE, tagId)));
                    }
                    Identifier recipeId = Identifier.tryParse(text);
                    return recipeId == null
                            ? DataResult.error(() -> "Invalid recipe target: " + text)
                            : DataResult.success(new IdTarget(recipeId));
                },
                target -> DataResult.success(target.serialized()));

        /** The serialized form as written in the rule file. */
        String serialized();
    }

    public record IdTarget(Identifier recipeId) implements Target {
        @Override
        public String serialized() {
            return recipeId.toString();
        }
    }

    public record TagTarget(TagKey<Recipe<?>> tag) implements Target {
        @Override
        public String serialized() {
            return "#" + tag.location();
        }
    }

    /**
     * The rule in the form sent to clients: tag targets are already resolved
     * against the server's loaded recipe tags, so the client never needs the
     * tag files themselves.
     */
    public record Resolved(
            Identifier ruleId,
            List<Identifier> exactTargets,
            List<Identifier> tagResolvedTargets,
            Optional<Float> difficulty,
            Optional<BenchTier> workbenchTier) {
        public static final StreamCodec<ByteBuf, Resolved> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC,
                Resolved::ruleId,
                Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()),
                Resolved::exactTargets,
                Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()),
                Resolved::tagResolvedTargets,
                optionalFloat(),
                Resolved::difficulty,
                optionalTier(),
                Resolved::workbenchTier,
                Resolved::new);
    }

    private static <A> MapCodec<Optional<A>> strictOptionalField(String name, Codec<A> elementCodec) {
        // optionalFieldOf silently turns decode errors into empty optionals;
        // datapack authors need the full error instead of a silent fallback.
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString(name));
            }

            @Override
            public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
                var value = input.get(name);
                return value == null
                        ? DataResult.success(Optional.empty())
                        : elementCodec.parse(ops, value).map(Optional::of);
            }

            @Override
            public <T> RecordBuilder<T> encode(
                    Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return input.isPresent()
                        ? prefix.add(name, elementCodec.encodeStart(ops, input.get()))
                        : prefix;
            }
        };
    }

    private static StreamCodec<ByteBuf, Optional<Float>> optionalFloat() {
        return optionalCodec(ByteBufCodecs.FLOAT);
    }

    private static StreamCodec<ByteBuf, Optional<BenchTier>> optionalTier() {
        return optionalCodec(BenchTier.STREAM_CODEC);
    }

    private static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optionalCodec(StreamCodec<B, V> inner) {
        return new StreamCodec<>() {
            @Override
            public Optional<V> decode(B buffer) {
                return buffer.readBoolean() ? Optional.of(inner.decode(buffer)) : Optional.empty();
            }

            @Override
            public void encode(B buffer, Optional<V> value) {
                buffer.writeBoolean(value.isPresent());
                value.ifPresent(v -> inner.encode(buffer, v));
            }
        };
    }
}
