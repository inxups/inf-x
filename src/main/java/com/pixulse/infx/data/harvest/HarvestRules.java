package com.pixulse.infx.data.harvest;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/** Reloadable harvest-level overrides in {@code data/<namespace>/harvest_rules}. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class HarvestRules {
    private static final FileToIdConverter RULE_LISTER = FileToIdConverter.json("harvest_rules");
    private static volatile List<Candidate> rules = List.of();

    private HarvestRules() {}

    @SubscribeEvent
    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(InfiniteX.id("harvest_rules"), new ReloadListener());
    }

    public static OptionalInt requiredLevel(BlockState state) {
        for (Candidate candidate : rules) {
            if (candidate.matches(state)) {
                return OptionalInt.of(candidate.requiredLevel());
            }
        }
        return OptionalInt.empty();
    }

    private record Definition(List<Either<Block, TagKey<Block>>> blocks, int requiredLevel, int priority) {
        private static final Codec<Either<Block, TagKey<Block>>> BLOCK_OR_TAG = Codec.either(
                BuiltInRegistries.BLOCK.byNameCodec(), TagKey.hashedCodec(Registries.BLOCK));
        private static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BLOCK_OR_TAG.listOf().fieldOf("blocks").forGetter(Definition::blocks),
                Codec.intRange(0, HarvestRequirements.MAX_LEVEL)
                        .fieldOf("required_level").forGetter(Definition::requiredLevel),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Definition::priority)
        ).apply(instance, Definition::new));

        private Definition {
            blocks = List.copyOf(blocks);
            if (blocks.isEmpty()) {
                throw new IllegalArgumentException("harvest rules must target at least one block");
            }
        }
    }

    private record Candidate(Identifier source, int index, int priority, Block block, TagKey<Block> tag, int requiredLevel) {
        private boolean matches(BlockState state) {
            return block != null ? state.is(block) : state.is(tag);
        }

        private int specificity() {
            return block == null ? 0 : 1;
        }

        private String sortKey() {
            return source + "#" + index;
        }
    }

    private static List<Candidate> build(Map<Identifier, Definition> definitions) {
        List<Candidate> loaded = new ArrayList<>();
        definitions.forEach((source, definition) -> {
            for (int index = 0; index < definition.blocks().size(); index++) {
                int targetIndex = index;
                definition.blocks().get(index).ifLeft(block -> loaded.add(new Candidate(
                        source, targetIndex, definition.priority(), block, null, definition.requiredLevel())));
                definition.blocks().get(index).ifRight(tag -> loaded.add(new Candidate(
                        source, targetIndex, definition.priority(), null, tag, definition.requiredLevel())));
            }
        });
        loaded.sort(Comparator.comparingInt(Candidate::priority).reversed()
                .thenComparing(Comparator.comparingInt(Candidate::specificity).reversed())
                .thenComparing(Candidate::sortKey, Comparator.reverseOrder()));
        return List.copyOf(loaded);
    }

    private static final class ReloadListener extends SimplePreparableReloadListener<List<Candidate>> {
        @Override
        protected List<Candidate> prepare(ResourceManager manager, ProfilerFiller profiler) {
            Map<Identifier, Definition> definitions = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(
                    manager, RULE_LISTER, JsonOps.INSTANCE, Definition.CODEC, definitions);
            return build(definitions);
        }

        @Override
        protected void apply(List<Candidate> data, ResourceManager manager, ProfilerFiller profiler) {
            rules = data;
            InfiniteX.LOGGER.info("Loaded {} harvest rule targets", data.size());
        }
    }
}
