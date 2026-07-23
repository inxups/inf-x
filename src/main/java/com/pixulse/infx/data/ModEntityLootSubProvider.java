package com.pixulse.infx.data;

import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Supplies a valid loot table for every custom entity without copying generated vanilla JSON. */
final class ModEntityLootSubProvider extends EntityLootSubProvider {
    ModEntityLootSubProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        zombieDrops(ModEntityTypes.R196_ZOMBIE.get());
        drops(ModEntityTypes.R196_SKELETON.get(), Items.BONE, 0.0F, 2.0F);
        drops(ModEntityTypes.R196_SPIDER.get(), Items.STRING, 0.0F, 2.0F);
        drops(ModEntityTypes.R196_CAVE_SPIDER.get(), Items.STRING, 0.0F, 2.0F);
        drops(ModEntityTypes.R196_CREEPER.get(), Items.GUNPOWDER, 0.0F, 2.0F);
        emptyDrops(ModEntityTypes.R196_SLIME.get());
        drops(ModEntityTypes.R196_ENDERMAN.get(), Items.ENDER_PEARL, 0.0F, 1.0F);
        drops(ModEntityTypes.R196_SQUID.get(), Items.INK_SAC, 1.0F, 3.0F);
        drops(ModEntityTypes.R196_WITCH.get(), Items.REDSTONE, 0.0F, 2.0F);
        drops(ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), Items.GOLD_NUGGET, 0.0F, 1.0F);
        drops(ModEntityTypes.R196_BLAZE.get(), Items.BLAZE_ROD, 0.0F, 1.0F);
        drops(ModEntityTypes.R196_GHAST.get(), Items.GUNPOWDER, 0.0F, 2.0F);

        for (var type : java.util.List.of(
                ModEntityTypes.INVISIBLE_STALKER,
                ModEntityTypes.GHOUL,
                ModEntityTypes.SHADOW,
                ModEntityTypes.WIGHT,
                ModEntityTypes.REVENANT)) {
            zombieDrops(type.get());
        }
        for (var type : java.util.List.of(
                ModEntityTypes.LONGDEAD,
                ModEntityTypes.BONE_LORD,
                ModEntityTypes.ANCIENT_BONE_LORD)) {
            drops(type.get(), Items.BONE, 0.0F, 2.0F);
        }
        for (var type : java.util.List.of(
                ModEntityTypes.BLACK_WIDOW_SPIDER,
                ModEntityTypes.DEMON_SPIDER,
                ModEntityTypes.WOOD_SPIDER,
                ModEntityTypes.PHASE_SPIDER)) {
            drops(type.get(), Items.STRING, 0.0F, 2.0F);
        }
        drops(ModEntityTypes.INFERNAL_CREEPER.get(), Items.GUNPOWDER, 1.0F, 3.0F);
        drops(ModEntityTypes.FIRE_ELEMENTAL.get(), Items.BLAZE_POWDER, 0.0F, 2.0F);
        add(
                ModEntityTypes.EARTH_ELEMENTAL.get(),
                LootTable.lootTable().withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.COBBLESTONE))));
        for (var type : java.util.List.of(
                ModEntityTypes.JELLY,
                ModEntityTypes.BLOB,
                ModEntityTypes.OOZE,
                ModEntityTypes.PUDDING)) {
            emptyDrops(type.get());
        }
        drops(ModEntityTypes.MAGMA_CUBE.get(), Items.MAGMA_CREAM, 0.0F, 1.0F);
        for (var type : java.util.List.of(
                ModEntityTypes.NETHERSPAWN,
                ModEntityTypes.COPPERSPINE,
                ModEntityTypes.HOARY_SILVERFISH)) {
            add(type.get(), LootTable.lootTable());
        }
        for (var type : java.util.List.of(
                ModEntityTypes.VAMPIRE_BAT,
                ModEntityTypes.NIGHTWING,
                ModEntityTypes.GIANT_VAMPIRE_BAT)) {
            add(type.get(), LootTable.lootTable());
        }
        for (var type : java.util.List.of(ModEntityTypes.HELLHOUND, ModEntityTypes.DIRE_WOLF)) {
            add(type.get(), LootTable.lootTable());
        }
    }

    private void drops(EntityType<?> custom, Item item, float minimum, float maximum) {
        add(
                custom,
                LootTable.lootTable().withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(item).apply(
                                        SetItemCountFunction.setCount(UniformGenerator.between(minimum, maximum))))));
    }

    private void emptyDrops(EntityType<?> custom) {
        add(custom, LootTable.lootTable());
    }

    private void zombieDrops(EntityType<?> custom) {
        add(
                custom,
                LootTable.lootTable()
                        .withPool(itemPool(Items.ROTTEN_FLESH, 0.0F, 2.0F))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(LootItemRandomChanceCondition.randomChance(0.10F))
                                .add(LootItem.lootTableItem(Items.COPPER_NUGGET))
                                .add(LootItem.lootTableItem(ModItems.SILVER_NUGGET.get()))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET))
                                .add(LootItem.lootTableItem(Items.IRON_NUGGET))));
    }

    private static LootPool.Builder itemPool(Item item, float minimum, float maximum) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item).apply(
                        SetItemCountFunction.setCount(UniformGenerator.between(minimum, maximum))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return ModEntityTypes.ALL.stream().map(holder -> holder.get());
    }
}
