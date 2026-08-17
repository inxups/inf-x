package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.registry.InfXLootModifiers;
import com.pixulse.infx.world.SpawnGate;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jspecify.annotations.NonNull;

/**
 * InfX hostile piglins barter by the sword: since the alive barter paths are closed by
 * PiglinAiMixin, this modifier moves the vanilla {@code gameplay/piglin_bartering} payouts onto
 * player kills. The subtable is rolled manually (so NeoForge does not re-run loot modifiers on
 * it) and each stack is pushed through the shared progression filter, because the filter's own
 * path gate only covers chests and the alive barter table.
 */
public final class PiglinBarterDropLootModifier extends LootModifier {
    private static final ResourceKey<LootTable> BARTER_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("gameplay/piglin_bartering"));

    public static final MapCodec<PiglinBarterDropLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, PiglinBarterDropLootModifier::new));

    public PiglinBarterDropLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected @NonNull ObjectArrayList<ItemStack> doApply(@NonNull ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!SpawnGate.isPiglinHostilityEnabled()) {
            return loot;
        }
        ObjectArrayList<ItemStack> bartered = new ObjectArrayList<>();
        context.getResolver()
                .lookupOrThrow(Registries.LOOT_TABLE)
                .get(BARTER_TABLE)
                .ifPresent(table -> table.value()
                        .getRandomItemsRaw(
                                context,
                                LootTable.createStackSplitter(context.getLevel(), bartered::add)));
        filterProgression(bartered);
        loot.addAll(bartered);
        return loot;
    }

    /** Reuses the shared progression conversion and forbidden-item pass on the rolled stacks. */
    public static void filterProgression(ObjectArrayList<ItemStack> stacks) {
        for (int index = 0; index < stacks.size(); index++) {
            ItemStack converted = ModernProgressionLootFilter.convertEquipment(stacks.get(index));
            if (converted != stacks.get(index)) stacks.set(index, converted);
        }
        stacks.removeIf(ModernProgressionLootFilter::isForbidden);
    }

    @Override
    public @NonNull MapCodec<? extends IGlobalLootModifier> codec() {
        return InfXLootModifiers.PIGLIN_BARTER_DROPS.get();
    }
}
