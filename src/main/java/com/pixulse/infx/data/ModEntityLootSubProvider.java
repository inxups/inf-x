package com.pixulse.infx.data;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.material.MiteMaterial;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SlimePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SheepPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Supplies a valid loot table for every custom entity without copying generated vanilla JSON. */
final class ModEntityLootSubProvider extends EntityLootSubProvider {
    private final HolderLookup.Provider lookup;

    ModEntityLootSubProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
        this.lookup = registries;
    }

    @Override
    public void generate() {
        // MITE zombie flesh: one piece at 50% for player kills, 25% otherwise. Rare drops
        // are procedural because a villager zombie uses a different MITE item pool.
        zombieDrops(ModEntityTypes.R196_ZOMBIE.get());
        zombieDrops(ModEntityTypes.WIGHT.get());
        zombieDrops(ModEntityTypes.REVENANT.get());
        // MITE ghouls, stalkers and shadows drop nothing at all.
        emptyDrops(ModEntityTypes.INVISIBLE_STALKER.get());
        emptyDrops(ModEntityTypes.GHOUL.get());
        emptyDrops(ModEntityTypes.SHADOW.get());

        for (var type : java.util.List.of(
                ModEntityTypes.R196_SKELETON,
                ModEntityTypes.LONGDEAD,
                ModEntityTypes.BONE_LORD,
                ModEntityTypes.ANCIENT_BONE_LORD)) {
            drops(type.get(), Items.BONE, 0.0F, 2.0F);
        }

        // MiteSpider emits its actual remaining web stock on player death. The data table
        // retains the independent one-in-three spider eye roll; phase spiders have no webs.
        spiderDrops(ModEntityTypes.R196_SPIDER.get());
        spiderDrops(ModEntityTypes.R196_CAVE_SPIDER.get());
        spiderDrops(ModEntityTypes.BLACK_WIDOW_SPIDER.get());
        spiderDrops(ModEntityTypes.DEMON_SPIDER.get());
        spiderDrops(ModEntityTypes.WOOD_SPIDER.get());
        add(
                ModEntityTypes.PHASE_SPIDER.get(),
                LootTable.lootTable().withPool(spiderEyePool()));

        // MITE creepers keep their powder for player kills and yield a disc to skeleton kills.
        add(
                ModEntityTypes.R196_CREEPER.get(),
                LootTable.lootTable()
                        .withPool(killedByPlayerPool(Items.GUNPOWDER, 0.0F, 2.0F))
                        .withPool(creeperMusicDiscPool()));
        // EntityInfernalCreeper's nested count, Looting and non-player reduction rolls are
        // procedural, so MiteCreeper performs the powder rolls at death. Its inherited skeleton
        // disc drop remains data-driven here.
        add(
                ModEntityTypes.INFERNAL_CREEPER.get(),
                LootTable.lootTable().withPool(creeperMusicDiscPool()));

        emptyDrops(ModEntityTypes.R196_SLIME.get());
        for (var type : java.util.List.of(
                ModEntityTypes.JELLY,
                ModEntityTypes.BLOB,
                ModEntityTypes.OOZE,
                ModEntityTypes.PUDDING)) {
            emptyDrops(type.get());
        }
        // MITE magma cubes only leak cream from size two and up: nextInt(4 + looting) - 2.
        add(
                ModEntityTypes.MAGMA_CUBE.get(),
                LootTable.lootTable().withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.MAGMA_CREAM)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(-2.0F, 1.0F)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                                lookup, UniformGenerator.between(0.0F, 1.0F)))
                                        .when(LootItemEntityPropertyCondition.hasProperties(
                                                LootContext.EntityTarget.THIS,
                                                EntityPredicate.Builder.entity()
                                                        .subPredicate(SlimePredicate.sized(MinMaxBounds.Ints.atLeast(2))))))));

        // MITE endermen roll nextInt(2 + looting), so the normal 0-1 pearl roll
        // receives the same per-level random count increase as blaze rods.
        add(
                ModEntityTypes.R196_ENDERMAN.get(),
                LootTable.lootTable().withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.ENDER_PEARL)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                                lookup, UniformGenerator.between(0.0F, 1.0F))))));
        // MITE squid surrender exactly one ink sac, and only to player kills.
        add(
                ModEntityTypes.R196_SQUID.get(),
                LootTable.lootTable().withPool(killedByPlayerPool(Items.INK_SAC, 1.0F, 1.0F)));
        drops(ModEntityTypes.R196_COD.get(), Items.COD, 1.0F, 1.0F);
        drops(ModEntityTypes.R196_SALMON.get(), Items.SALMON, 1.0F, 1.0F);
        drops(ModEntityTypes.R196_PUFFERFISH.get(), Items.PUFFERFISH, 1.0F, 1.0F);
        drops(ModEntityTypes.R196_TROPICAL_FISH.get(), Items.TROPICAL_FISH, 1.0F, 1.0F);

        witchDrops();

        // MITE pig zombies: flesh like zombies, loose nuggets, and a rare gold ingot.
        add(
                ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get(),
                LootTable.lootTable()
                        .withPool(fleshPool())
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                                lookup, UniformGenerator.between(0.0F, 1.0F)))))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                                        lookup, 0.025F, 0.01F))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT))));

        // MITE blaze rods: nextInt(2 + looting), player kills only.
        add(
                ModEntityTypes.R196_BLAZE.get(),
                LootTable.lootTable().withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                                .add(LootItem.lootTableItem(Items.BLAZE_ROD)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                                lookup, UniformGenerator.between(0.0F, 1.0F))))));
        // MITE fire elementals leave nothing behind.
        emptyDrops(ModEntityTypes.FIRE_ELEMENTAL.get());
        // MITE ghasts carry both powder and the tear the brewing chain depends on.
        add(
                ModEntityTypes.R196_GHAST.get(),
                LootTable.lootTable()
                        .withPool(itemPool(Items.GHAST_TEAR, 0.0F, 1.0F))
                        .withPool(itemPool(Items.GUNPOWDER, 0.0F, 2.0F)));
        // Earth-elemental drops follow its synced material body, so the entities emit their one
        // block directly rather than a static JSON table.
        emptyDrops(ModEntityTypes.EARTH_ELEMENTAL.get());
        emptyDrops(ModEntityTypes.CLAY_GOLEM.get());

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
        // MITE: hellhounds drop nothing; wolves and dire wolves leave one piece of leather.
        emptyDrops(ModEntityTypes.HELLHOUND.get());
        drops(ModEntityTypes.DIRE_WOLF.get(), Items.LEATHER, 1.0F, 1.0F);

        // R196 livestock replacements: simplified 26.2-style drops (models reuse vanilla assets).
        add(
                ModEntityTypes.R196_COW.get(),
                LootTable.lootTable()
                        .withPool(itemPool(Items.LEATHER, 0.0F, 2.0F))
                        .withPool(itemPool(Items.BEEF, 1.0F, 3.0F)));
        add(
                ModEntityTypes.R196_CHICKEN.get(),
                LootTable.lootTable()
                        .withPool(itemPool(Items.FEATHER, 0.0F, 2.0F))
                        .withPool(itemPool(Items.CHICKEN, 1.0F, 1.0F)));
        // Mutton + one matching wool block when not sheared (inline; no nested vanilla tables).
        add(
                ModEntityTypes.R196_SHEEP.get(),
                LootTable.lootTable()
                        .withPool(itemPool(Items.MUTTON, 1.0F, 2.0F))
                        .withPool(sheepWoolPool()));
        drops(ModEntityTypes.R196_PIG.get(), Items.PORKCHOP, 1.0F, 3.0F);
        drops(ModEntityTypes.R196_HORSE.get(), Items.LEATHER, 0.0F, 2.0F);
        emptyDrops(ModEntityTypes.R196_OCELOT.get());
        drops(ModEntityTypes.R196_WOLF.get(), Items.LEATHER, 1.0F, 1.0F);
    }

    /** MITE witches roll 1-5 draws from an 18-slot table (stick twice, one slot of six potions). */
    private void witchDrops() {
        LootPool.Builder pool = LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 5.0F));
        Item[] singles = {
            Items.GLOWSTONE_DUST,
            Items.SUGAR,
            Items.REDSTONE,
            Items.SPIDER_EYE,
            Items.GLASS_BOTTLE,
            Items.GUNPOWDER,
            Items.IRON_NUGGET,
            Items.WHEAT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.CARROT,
            Items.POTATO,
            Items.DANDELION,
            Items.POPPY,
        };
        for (Item item : singles) {
            pool.add(LootItem.lootTableItem(item).setWeight(6));
        }
        pool.add(LootItem.lootTableItem(ModItems.ONION.get()).setWeight(6));
        pool.add(LootItem.lootTableItem(
                        ModItems.catalog().equipment(MiteMaterial.FLINT, EquipmentType.KNIFE).holder())
                .setWeight(6));
        pool.add(LootItem.lootTableItem(Items.STICK).setWeight(12));
        pool.add(potionEntry(Items.POTION, Potions.FIRE_RESISTANCE));
        pool.add(potionEntry(Items.POTION, Potions.STRENGTH));
        pool.add(potionEntry(Items.SPLASH_POTION, Potions.POISON));
        pool.add(potionEntry(Items.SPLASH_POTION, Potions.WEAKNESS));
        pool.add(potionEntry(Items.SPLASH_POTION, Potions.SLOWNESS));
        pool.add(potionEntry(Items.SPLASH_POTION, Potions.HARMING));
        add(ModEntityTypes.R196_WITCH.get(), LootTable.lootTable().withPool(pool));
    }

    private static LootPoolEntryContainer.Builder<?> potionEntry(
            Item container, net.minecraft.core.Holder<Potion> potion) {
        return LootItem.lootTableItem(container)
                .setWeight(1)
                .apply(SetPotionFunction.setPotion(potion));
    }

    private static LootPool.Builder sheepWoolPool() {
        AlternativesEntry.Builder variants = AlternativesEntry.alternatives();
        for (DyeColor color : DyeColor.VALUES) {
            variants = variants.otherwise(
                    LootItem.lootTableItem(woolBlock(color))
                            .when(LootItemEntityPropertyCondition.hasProperties(
                                    LootContext.EntityTarget.THIS,
                                    EntityPredicate.Builder.entity()
                                            .components(DataComponentMatchers.Builder.components()
                                                    .exact(DataComponentExactPredicate.expect(
                                                            DataComponents.SHEEP_COLOR, color))
                                                    .build())
                                            .subPredicate(SheepPredicate.hasWool()))));
        }
        return LootPool.lootPool().add(variants);
    }

    private static net.minecraft.world.level.block.Block woolBlock(DyeColor color) {
        return switch (color) {
            case WHITE -> Blocks.WHITE_WOOL;
            case ORANGE -> Blocks.ORANGE_WOOL;
            case MAGENTA -> Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
            case YELLOW -> Blocks.YELLOW_WOOL;
            case LIME -> Blocks.LIME_WOOL;
            case PINK -> Blocks.PINK_WOOL;
            case GRAY -> Blocks.GRAY_WOOL;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
            case CYAN -> Blocks.CYAN_WOOL;
            case PURPLE -> Blocks.PURPLE_WOOL;
            case BLUE -> Blocks.BLUE_WOOL;
            case BROWN -> Blocks.BROWN_WOOL;
            case GREEN -> Blocks.GREEN_WOOL;
            case RED -> Blocks.RED_WOOL;
            case BLACK -> Blocks.BLACK_WOOL;
        };
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
        add(custom, LootTable.lootTable().withPool(fleshPool()));
    }

    /** One rotten flesh at 50% for player kills, 25% for everything else. */
    private static LootPool.Builder fleshPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(AlternativesEntry.alternatives(
                        LootItem.lootTableItem(Items.ROTTEN_FLESH)
                                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                                .when(LootItemRandomChanceCondition.randomChance(0.5F)),
                        LootItem.lootTableItem(Items.ROTTEN_FLESH)
                                .when(InvertedLootItemCondition.invert(
                                        LootItemKilledByPlayerCondition.killedByPlayer()))
                                .when(LootItemRandomChanceCondition.randomChance(0.25F))));
    }

    private void spiderDrops(EntityType<?> custom) {
        add(
                custom,
                LootTable.lootTable()
                        .withPool(spiderEyePool()));
    }

    private static LootPool.Builder spiderEyePool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemRandomChanceCondition.randomChance(1.0F / 3.0F))
                .add(LootItem.lootTableItem(Items.SPIDER_EYE));
    }

    private static LootPool.Builder killedByPlayerPool(ItemLike item, float minimum, float maximum) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .add(LootItem.lootTableItem(item).apply(
                        SetItemCountFunction.setCount(UniformGenerator.between(minimum, maximum))));
    }

    private LootPool.Builder creeperMusicDiscPool() {
        return LootPool.lootPool()
                .add(TagEntry.expandTag(ItemTags.CREEPER_DROP_MUSIC_DISCS))
                .when(LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.ATTACKER,
                        EntityPredicate.Builder.entity()
                                .of(lookup.lookupOrThrow(Registries.ENTITY_TYPE), EntityTypeTags.SKELETONS)));
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
