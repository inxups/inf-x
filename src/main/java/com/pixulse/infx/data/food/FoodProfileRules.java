package com.pixulse.infx.data.food;

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
import java.util.Optional;
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

/** Reloadable nutrition overrides in {@code data/<namespace>/food_profiles}. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class FoodProfileRules {
    private static final FileToIdConverter RULE_LISTER = FileToIdConverter.json("food_profiles");
    private static volatile List<Candidate> rules = List.of();

    private FoodProfileRules() {}

    @SubscribeEvent
    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(InfiniteX.id("food_profiles"), new ReloadListener());
    }

    /** Returns the highest-precedence datapack profile for the supplied stack. */
    public static Optional<FoodProfile> profile(ItemStack stack) {
        for (Candidate candidate : rules) {
            if (candidate.matches(stack)) {
                return Optional.of(candidate.profile());
            }
        }
        return Optional.empty();
    }

    public static int loadedRuleCount() {
        return rules.size();
    }

    private record Definition(List<Either<Item, TagKey<Item>>> items, FoodProfile profile, int priority) {
        private static final Codec<Either<Item, TagKey<Item>>> ITEM_OR_TAG = Codec.either(
                BuiltInRegistries.ITEM.byNameCodec(), TagKey.hashedCodec(Registries.ITEM));
        private static final Codec<FoodProfile> PROFILE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("satiation").forGetter(FoodProfile::satiation),
                Codec.DOUBLE.fieldOf("nutrition").forGetter(FoodProfile::nutrition),
                Codec.INT.fieldOf("protein").forGetter(FoodProfile::protein),
                Codec.INT.fieldOf("phytonutrients").forGetter(FoodProfile::phytonutrients),
                Codec.INT.fieldOf("essential_fats").forGetter(FoodProfile::essentialFats),
                Codec.INT.fieldOf("sugar_content").forGetter(FoodProfile::sugarContent),
                Codec.BOOL.optionalFieldOf("always_edible", false).forGetter(FoodProfile::alwaysEdible)
        ).apply(instance, FoodProfile::new));
        private static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ITEM_OR_TAG.listOf().fieldOf("items").forGetter(Definition::items),
                PROFILE_CODEC.fieldOf("profile").forGetter(Definition::profile),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Definition::priority)
        ).apply(instance, Definition::new));

        private Definition {
            items = List.copyOf(items);
            if (items.isEmpty()) {
                throw new IllegalArgumentException("food profile rules must target at least one item");
            }
        }
    }

    private record Candidate(Identifier source, int index, int priority, Item item, TagKey<Item> tag, FoodProfile profile) {
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
                        new Candidate(source, targetIndex, definition.priority(), item, null, definition.profile())));
                definition.items().get(index).ifRight(tag -> loaded.add(
                        new Candidate(source, targetIndex, definition.priority(), null, tag, definition.profile())));
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
            InfiniteX.LOGGER.info("Loaded {} food profile rule targets", data.size());
        }
    }
}
