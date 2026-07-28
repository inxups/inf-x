package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.item.MiteBucketItem;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.material.MiteMaterial;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModLootModifiers;
import com.pixulse.infx.world.Underworld;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Adds R196's eight-roll ancient-metal pool only to Underworld monster rooms. */
public final class UnderworldDungeonLootModifier extends LootModifier {
    private static final Identifier SIMPLE_DUNGEON = Identifier.withDefaultNamespace("chests/simple_dungeon");
    private static final List<EquipmentType> EQUIPMENT = List.of(
            EquipmentType.PICKAXE,
            EquipmentType.SHOVEL,
            EquipmentType.AXE,
            EquipmentType.SWORD,
            EquipmentType.WAR_HAMMER,
            EquipmentType.BOW,
            EquipmentType.CHAINMAIL_HELMET,
            EquipmentType.CHAINMAIL_CHESTPLATE,
            EquipmentType.CHAINMAIL_LEGGINGS,
            EquipmentType.CHAINMAIL_BOOTS);

    public static final MapCodec<UnderworldDungeonLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, UnderworldDungeonLootModifier::new));

    public UnderworldDungeonLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (context.getLevel().dimension() != Underworld.LEVEL
                || !SIMPLE_DUNGEON.equals(context.getQueriedLootTableId())) {
            return loot;
        }
        for (int roll = 0; roll < 8; roll++) {
            ItemStack added = roll(context);
            if (!added.isEmpty()) {
                loot.add(added);
            }
        }
        return loot;
    }

    private static ItemStack roll(LootContext context) {
        int value = context.getRandom().nextInt(100);
        if (value < 10) {
            return ModItems.catalog().raw("ancient_metal_nugget").holder().toStack(1 + context.getRandom().nextInt(4));
        }
        if (value < 20) {
            return ModItems.ANCIENT_METAL_INGOT.toStack(1 + context.getRandom().nextInt(4));
        }
        if (value < 25) {
            return ModItems.catalog().raw("ancient_metal_coin").holder().toStack();
        }
        if (value < 27) {
            return ModItems.bucket(
                            MiteMaterial.ANCIENT_METAL,
                            MiteBucketItem.Contents.EMPTY)
                    .toStack();
        }
        if (value < 31) {
            return ModItems.R196_RECORDS.get(value - 27).toStack();
        }
        if (value < 36) {
            return equipment(EquipmentType.HORSE_ARMOR);
        }
        if (value < 46) {
            return equipment(EQUIPMENT.get(value - 36));
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack equipment(EquipmentType type) {
        return ModItems.catalog().equipment(MiteMaterial.ANCIENT_METAL, type).holder().toStack();
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.UNDERWORLD_DUNGEON.get();
    }
}
