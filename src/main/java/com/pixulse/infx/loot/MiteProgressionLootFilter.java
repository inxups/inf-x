package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jspecify.annotations.NonNull;

/** Applies the MITE day and height gates after structure supplements have been rolled. */
public final class MiteProgressionLootFilter extends LootModifier {
    public static final long AXE_UNLOCK_DAY = 10L;
    public static final long INGOT_UNLOCK_DAY = 20L;
    public static final int LOW_HEIGHT = 48;

    public static final MapCodec<MiteProgressionLootFilter> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, MiteProgressionLootFilter::new));

    public MiteProgressionLootFilter(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected @NonNull ObjectArrayList<ItemStack> doApply(
            @NonNull ObjectArrayList<ItemStack> loot, LootContext context) {
        Identifier table = context.getQueriedLootTableId();
        if (table == null || !table.getPath().startsWith("chests/")) return loot;

        long day = Math.max(1L, context.getLevel().getOverworldClockTime() / 24_000L + 1L);
        Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
        int y = origin == null ? LOW_HEIGHT : (int) Math.floor(origin.y);
        boolean overworld = context.getLevel().dimension() == Level.OVERWORLD;
        loot.removeIf(stack -> isLocked(stack, day, y, overworld));
        return loot;
    }

    public static boolean isLocked(ItemStack stack, long day, int y) {
        return isLocked(BuiltInRegistries.ITEM.getKey(stack.getItem()), day, y, true);
    }

    public static boolean isLocked(Identifier id, long day, int y) {
        return isLocked(id, day, y, true);
    }

    static boolean isLocked(ItemStack stack, long day, int y, boolean overworld) {
        return isLocked(BuiltInRegistries.ITEM.getKey(stack.getItem()), day, y, overworld);
    }

    static boolean isLocked(Identifier id, long day, int y, boolean overworld) {
        if (id == null || (!id.getNamespace().equals(InfiniteX.MOD_ID)
                && !id.getNamespace().equals("minecraft"))) {
            return false;
        }
        String path = id.getPath();
        if (y < LOW_HEIGHT && (path.endsWith("_hoe") || path.endsWith("_fishing_rod"))) return true;
        if (!overworld) return false;
        if (day < AXE_UNLOCK_DAY && (path.endsWith("_axe")
                || path.endsWith("_hatchet")
                || path.endsWith("_hoe")
                || path.endsWith("_mattock"))) {
            return true;
        }
        return day < INGOT_UNLOCK_DAY && (path.endsWith("_pickaxe")
                || path.endsWith("_ingot")
                || path.endsWith("_coin"));
    }

    @Override
    public @NonNull MapCodec<? extends IGlobalLootModifier> codec() {
        return InfXLootModifiers.MITE_PROGRESSION_FILTER.get();
    }
}
