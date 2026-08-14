package com.pixulse.infx.data.furnace;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/** Reloadable fuel heat overrides in {@code data/<namespace>/fuel_heat}. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class FuelHeatRules {
    private static final FileToIdConverter RULE_LISTER = FileToIdConverter.json("fuel_heat");
    private static volatile List<Candidate> rules = List.of();

    private FuelHeatRules() {}

    @SubscribeEvent
    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(InfiniteX.id("fuel_heat"), new ReloadListener());
    }

    public static OptionalInt heat(ItemStack stack) {
        for (Candidate candidate : rules) {
            if (candidate.matches(stack)) {
                return OptionalInt.of(candidate.heat());
            }
        }
        return OptionalInt.empty();
    }

    private record Definition(List<Either<Item, TagKey<Item>>> items, int heat, int priority) {
        private static final Codec<Either<Item, TagKey<Item>>> ITEM_OR_TAG = Codec.either(
                BuiltInRegistries.ITEM.byNameCodec(), TagKey.hashedCodec(Registries.ITEM));
        private static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ITEM_OR_TAG.listOf().fieldOf("items").forGetter(Definition::items),
                Codec.intRange(0, FurnaceHeatPolicy.HEAT_BLAZE).fieldOf("heat").forGetter(Definition::heat),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Definition::priority)
        ).apply(instance, Definition::new));

        private Definition {
            items = List.copyOf(items);
            if (items.isEmpty()) {
                throw new IllegalArgumentException("fuel heat rules must target at least one item");
            }
        }
    }

    private record Candidate(Identifier source, int index, int priority, Item item, TagKey<Item> tag, int heat) {
        private boolean matches(ItemStack stack) {
            return item != null ? stack.is(item) : stack.is(tag);
        }

        private int specificity() {
            return item == null ? 0 : 1;
        }

        private String sortKey() {
            return source + "#" + index;
        }
    }

    private static List<Candidate> build(Map<Identifier, Definition> definitions) {
        List<Candidate> loaded = new ArrayList<>();
        definitions.forEach((source, definition) -> {
            for (int index = 0; index < definition.items().size(); index++) {
                int targetIndex = index;
                definition.items().get(index).ifLeft(item -> loaded.add(
                        new Candidate(source, targetIndex, definition.priority(), item, null, definition.heat())));
                definition.items().get(index).ifRight(tag -> loaded.add(
                        new Candidate(source, targetIndex, definition.priority(), null, tag, definition.heat())));
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
            InfiniteX.LOGGER.info("Loaded {} fuel heat rule targets", data.size());
        }
    }
}
