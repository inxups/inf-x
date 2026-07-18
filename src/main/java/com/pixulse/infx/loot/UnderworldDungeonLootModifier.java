package com.pixulse.infx.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
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
    private static final List<R196EquipmentType> EQUIPMENT = List.of(
            R196EquipmentType.PICKAXE,
            R196EquipmentType.SHOVEL,
            R196EquipmentType.AXE,
            R196EquipmentType.SWORD,
            R196EquipmentType.WAR_HAMMER,
            R196EquipmentType.BOW,
            R196EquipmentType.CHAINMAIL_HELMET,
            R196EquipmentType.CHAINMAIL_CHESTPLATE,
            R196EquipmentType.CHAINMAIL_LEGGINGS,
            R196EquipmentType.CHAINMAIL_BOOTS);

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
        if (value < 30) {
            return equipment(R196EquipmentType.HORSE_ARMOR);
        }
        if (value < 40) {
            return equipment(EQUIPMENT.get(value - 30));
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack equipment(R196EquipmentType type) {
        return ModItems.catalog().equipment(R196Material.ANCIENT_METAL, type).holder().toStack();
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.UNDERWORLD_DUNGEON.get();
    }
}
