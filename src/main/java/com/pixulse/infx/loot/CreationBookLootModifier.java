package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.registry.ModLootModifiers;
import com.pixulse.infx.world.R196CreationBooks;
import com.pixulse.infx.world.R196WorldData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Adds at most one world-unique creation book per eligible structure component. */
public final class CreationBookLootModifier extends LootModifier {
    private static final long FIRST_DAY = 40L;
    private static final Map<String, Float> CHANCES = Map.of(
            "chests/desert_pyramid", 0.10F,
            "chests/jungle_temple", 0.25F,
            "chests/stronghold_library", 0.50F);
    public static final MapCodec<CreationBookLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, CreationBookLootModifier::new));

    public CreationBookLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        Identifier table = context.getQueriedLootTableId();
        if (table == null || !table.getNamespace().equals("minecraft")) return loot;
        Float chance = CHANCES.get(table.getPath());
        if (chance == null) return loot;
        long day = Math.max(1L, context.getLevel().getOverworldClockTime() / 24_000L + 1L);
        R196WorldData data = R196WorldData.get(context.getLevel());
        if (day < FIRST_DAY || !data.hasWorldAdvancement("bookcase")) return loot;
        Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
        String component = table + ":" + (origin == null
                ? "unknown"
                : net.minecraft.world.level.ChunkPos.pack(
                        (int) Math.floor(origin.x) >> 4, (int) Math.floor(origin.z) >> 4));
        if (context.getRandom().nextFloat() >= chance || !data.beginCreationBookComponent(component)) return loot;
        int title = data.claimCreationBook(context.getRandom());
        if (title >= 0) loot.add(R196CreationBooks.create(title));
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.CREATION_BOOK.get();
    }
}
