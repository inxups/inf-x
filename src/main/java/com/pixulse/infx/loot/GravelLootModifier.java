package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import com.pixulse.infx.progression.ProgressionEvents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.tags.StructureTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class GravelLootModifier extends LootModifier {
    private static final Identifier GRAVEL_LOOT_TABLE = Identifier.withDefaultNamespace("blocks/gravel");

    public static final MapCodec<GravelLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, GravelLootModifier::new));

    public GravelLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!GRAVEL_LOOT_TABLE.equals(context.getQueriedLootTableId())) {
            return generatedLoot;
        }

        BlockState state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
        if (state == null || !state.is(Blocks.GRAVEL)) {
            return generatedLoot;
        }
        if (!(context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player)) {
            return generatedLoot;
        }
        if (context.hasParameter(LootContextParams.EXPLOSION_RADIUS)) {
            return generatedLoot;
        }
        if (isVillageRoad(context)) {
            generatedLoot.clear();
            generatedLoot.add(new ItemStack(Items.GRAVEL));
            return generatedLoot;
        }

        int fortune = fortuneLevel(context);
        GravelDrop selected = GravelDropSelector.select(fortune, context.getRandom()::nextInt);
        if (selected == GravelDrop.DIAMOND_SHARD && context.getLevel().dimension() == Level.NETHER) {
            selected = GravelDrop.NETHER_QUARTZ_SHARD;
        }
        if ((selected == GravelDrop.FLINT_CHIP || selected == GravelDrop.FLINT)
                && player instanceof ServerPlayer serverPlayer) {
            ProgressionEvents.award(serverPlayer, "flint_finder", "mined_flint_from_gravel");
        }
        ItemStack replacement = createStack(selected);
        FirstLootUnitReplacer.replace(
                generatedLoot,
                stack -> stack.is(Items.GRAVEL) || stack.is(Items.FLINT),
                ItemStack::getCount,
                ItemStack::setCount,
                replacement);
        return generatedLoot;
    }

    static boolean isVillageRoad(LootContext context) {
        Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin == null) {
            return false;
        }
        StructureStart village = context.getLevel().structureManager()
                .getStructureWithPieceAt(BlockPos.containing(origin), StructureTags.VILLAGE);
        return village != StructureStart.INVALID_START;
    }

    private static int fortuneLevel(LootContext context) {
        ItemInstance tool = context.getOptionalParameter(LootContextParams.TOOL);
        if (tool == null) {
            return 0;
        }
        var enchantments = context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return tool.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FORTUNE));
    }

    private static ItemStack createStack(GravelDrop drop) {
        return switch (drop) {
            case GRAVEL -> new ItemStack(Items.GRAVEL);
            case FLINT_CHIP -> ModItems.FLINT_CHIP.toStack();
            case FLINT -> new ItemStack(Items.FLINT);
            case COPPER_NUGGET -> new ItemStack(Items.COPPER_NUGGET);
            case SILVER_NUGGET -> ModItems.SILVER_NUGGET.toStack();
            case GOLD_NUGGET -> new ItemStack(Items.GOLD_NUGGET);
            case OBSIDIAN_SHARD -> ModItems.OBSIDIAN_SHARD.toStack();
            case EMERALD_SHARD -> ModItems.EMERALD_SHARD.toStack();
            case DIAMOND_SHARD -> ModItems.catalog().raw("diamond_shard").holder().toStack();
            case NETHER_QUARTZ_SHARD -> ModItems.catalog().raw("nether_quartz_shard").holder().toStack();
            case MITHRIL_NUGGET -> ModItems.MITHRIL_NUGGET.toStack();
            case ADAMANTIUM_NUGGET -> ModItems.ADAMANTIUM_NUGGET.toStack();
        };
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.GRAVEL.get();
    }
}
