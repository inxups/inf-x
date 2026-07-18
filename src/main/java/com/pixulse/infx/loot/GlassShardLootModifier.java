package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Restores R196's recoverable clear-glass fragments without altering Silk Touch. */
public final class GlassShardLootModifier extends LootModifier {
    private static final Identifier GLASS_TABLE = Identifier.withDefaultNamespace("blocks/glass");
    private static final Identifier GLASS_PANE_TABLE = Identifier.withDefaultNamespace("blocks/glass_pane");

    public static final MapCodec<GlassShardLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, GlassShardLootModifier::new));

    public GlassShardLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Identifier table = context.getQueriedLootTableId();
        BlockState state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
        int count;
        if (GLASS_TABLE.equals(table) && state != null && state.is(Blocks.GLASS)) {
            count = 6;
        } else if (GLASS_PANE_TABLE.equals(table) && state != null && state.is(Blocks.GLASS_PANE)) {
            count = 1;
        } else {
            return generatedLoot;
        }
        if (context.hasParameter(LootContextParams.EXPLOSION_RADIUS) || hasSilkTouch(context)) {
            return generatedLoot;
        }

        ItemStack shards = ModItems.catalog().raw("glass_shard").holder().toStack(count);
        generatedLoot.add(shards);
        return generatedLoot;
    }

    private static boolean hasSilkTouch(LootContext context) {
        ItemInstance tool = context.getOptionalParameter(LootContextParams.TOOL);
        if (tool == null) {
            return false;
        }
        var enchantments = context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return tool.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.SILK_TOUCH)) > 0;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.GLASS_SHARDS.get();
    }
}
