package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXItems;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.advancements.criterion.RecipeCraftedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

final class ModAdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        HolderLookup.RegistryLookup<Item> items = registries.lookupOrThrow(Registries.ITEM);

        AdvancementHolder firstSteps = Advancement.Builder.recipeAdvancement()
                .display(
                        Items.STICK,
                        title("first_steps"),
                        description("first_steps"),
                        net.minecraft.resources.Identifier.withDefaultNamespace(
                                "gui/advancements/backgrounds/stone"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .addCriterion("inventory_changed", inventoryChanged())
                .build(InfiniteX.id("progression/first_steps"));
        output.accept(firstSteps);

        AdvancementHolder flintKit = recipeNode(
                output, "flint_kit", firstSteps, InfXItems.FLINT_HATCHET,
                "flint_hatchet", "flint_knife", "flint_shovel", "flint_axe");

        Advancement.Builder flintWorkbenchBuilder = child(
                "flint_workbench", flintKit, InfXBlocks.STRIPPED_LOG_WORKBENCHES.getFirst().flint());
        for (var workbench : InfXBlocks.STRIPPED_LOG_WORKBENCHES) {
            String prefix = "stripped_" + workbench.wood();
            flintWorkbenchBuilder
                    .addCriterion(
                            "crafted_" + prefix + "_flint_bench",
                            RecipeCraftedTrigger.TriggerInstance.craftedItem(
                                    recipeKey(prefix + "_flint_workbench")))
                    .addCriterion(
                            "crafted_" + prefix + "_obsidian_bench",
                            RecipeCraftedTrigger.TriggerInstance.craftedItem(
                                    recipeKey(prefix + "_obsidian_workbench")));
        }
        AdvancementHolder flintWorkbench = flintWorkbenchBuilder
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/flint_workbench"));
        output.accept(flintWorkbench);

        AdvancementHolder firstFurnace = recipeNode(
                output, "first_furnace", firstSteps, Items.FURNACE,
                "clay_furnace", "sandstone_furnace", "hardened_clay_furnace", "cobblestone_furnace");

        AdvancementHolder copperWorkbench = recipeNode(
                output, "copper_workbench", flintWorkbench, InfXBlocks.COPPER_WORKBENCH,
                "copper_workbench", "silver_workbench", "gold_workbench");

        Advancement.Builder ironAgeBuilder = child("iron_age", copperWorkbench, Items.IRON_INGOT)
                .addCriterion("smelted_iron", manualCriterion())
                .addCriterion(
                        "crafted_iron_workbench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("iron_workbench")))
                .addCriterion(
                        "crafted_iron_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("iron_pickaxe")))
                .requirements(AdvancementRequirements.Strategy.OR);
        AdvancementHolder ironAge = ironAgeBuilder.build(InfiniteX.id("progression/iron_age"));
        output.accept(ironAge);

        AdvancementHolder obsidianFurnace = child("obsidian_furnace", firstFurnace, InfXBlocks.OBSIDIAN_FURNACE)
                .addCriterion(
                        "crafted_obsidian_furnace",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("obsidian_furnace")))
                .build(InfiniteX.id("progression/obsidian_furnace"));
        output.accept(obsidianFurnace);

        AdvancementHolder ancientMetalAge = child(
                        "ancient_metal_age", ironAge, InfXBlocks.ANCIENT_METAL_WORKBENCH)
                .addCriterion(
                        "crafted_ancient_metal_workbench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("ancient_metal_workbench")))
                .build(InfiniteX.id("progression/ancient_metal_age"));
        output.accept(ancientMetalAge);

        Advancement.Builder mithrilAgeBuilder = child("mithril_age", ancientMetalAge, InfXItems.MITHRIL_INGOT)
                .addCriterion("smelted_mithril", manualCriterion())
                .addCriterion(
                        "crafted_mithril_workbench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("mithril_workbench")))
                .requirements(AdvancementRequirements.Strategy.OR);
        AdvancementHolder mithrilAge = mithrilAgeBuilder.build(InfiniteX.id("progression/mithril_age"));
        output.accept(mithrilAge);

        Advancement.Builder adamantiumAgeBuilder = child(
                        "adamantium_age", mithrilAge, InfXItems.ADAMANTIUM_INGOT)
                .addCriterion("smelted_adamantium", manualCriterion())
                .addCriterion(
                        "crafted_adamantium_workbench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("adamantium_workbench")))
                .requirements(AdvancementRequirements.Strategy.OR);
        AdvancementHolder adamantiumAge = adamantiumAgeBuilder.build(InfiniteX.id("progression/adamantium_age"));
        output.accept(adamantiumAge);

        AdvancementHolder masterwork = recipeNode(
                output, "masterwork", adamantiumAge,
                InfXItems.catalog().equipment(InfxMaterial.ADAMANTIUM, EquipmentType.PICKAXE).holder(),
                "adamantium_pickaxe", "adamantium_war_hammer");

        Advancement.Builder leatherArmor = child(
                        "leather_armor", flintWorkbench,
                        equipment(InfxMaterial.LEATHER, EquipmentType.CHESTPLATE));
        for (EquipmentType piece : EquipmentType.platePieces()) {
            leatherArmor.addCriterion(
                    "wearing_leather_" + piece.path(), wearingPiece(items, InfxMaterial.LEATHER, piece));
        }
        AdvancementHolder leatherArmorHolder = leatherArmor.build(InfiniteX.id("progression/leather_armor"));
        output.accept(leatherArmorHolder);

        List<InfxMaterial> metalArmorMaterials = List.of(
                InfxMaterial.COPPER,
                InfxMaterial.SILVER,
                InfxMaterial.GOLD,
                InfxMaterial.IRON,
                InfxMaterial.ANCIENT_METAL,
                InfxMaterial.MITHRIL);
        AdvancementHolder metalArmor = child(
                        "metal_armor", copperWorkbench,
                        equipment(InfxMaterial.IRON, EquipmentType.CHESTPLATE))
                .addCriterion("wearing_full_metal_armor", wearingPlateSet(items, metalArmorMaterials))
                .build(InfiniteX.id("progression/metal_armor"));
        output.accept(metalArmor);

        AdvancementHolder adamantiumArmor = Advancement.Builder.recipeAdvancement()
                .parent(adamantiumAge)
                .display(
                        equipment(InfxMaterial.ADAMANTIUM, EquipmentType.CHESTPLATE),
                        title("adamantium_armor"),
                        description("adamantium_armor"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false)
                .addCriterion(
                        "wearing_full_adamantium_armor",
                        wearingPlateSet(items, List.of(InfxMaterial.ADAMANTIUM)))
                .build(InfiniteX.id("progression/adamantium_armor"));
        output.accept(adamantiumArmor);

        AdvancementHolder farming = recipeNode(
                output, "farming", copperWorkbench, InfXItems.COPPER_HOE,
                "copper_hoe", "silver_hoe", "gold_hoe", "iron_hoe");

        AdvancementHolder food = recipeNode(
                output, "food", farming, InfXItems.FLOUR,
                "flour", "dough", "bread_from_dough", "mushroom_stew", "beef_stew",
                "chicken_soup", "cream_of_mushroom_soup", "cream_of_vegetable_soup",
                "pumpkin_soup", "vegetable_soup");

        AdvancementHolder enchanting = recipeNode(
                output, "enchanting", ironAge, InfXBlocks.EMERALD_ENCHANTING_TABLE,
                "emerald_enchanting_table", "diamond_enchanting_table");

        AdvancementHolder bookcase = child("bookcase", enchanting, Items.BOOKSHELF)
                .addCriterion(
                        "crafted_bookcase",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(ResourceKey.create(
                                Registries.RECIPE,
                                net.minecraft.resources.Identifier.withDefaultNamespace("bookshelf"))))
                .build(InfiniteX.id("progression/bookcase"));
        output.accept(bookcase);

        AdvancementHolder enlightenment = manual(
                output, "enlightenment", bookcase, Items.WRITTEN_BOOK, true, "read_nine_books");

        AdvancementHolder underworld = manual(
                output, "underworld", obsidianFurnace, Items.OBSIDIAN, false, "entered_underworld");

        AdvancementHolder nether = manual(
                output, "nether", underworld, Items.NETHERRACK, false, "entered_nether");

        AdvancementHolder netherForge = child("nether_forge", nether, InfXBlocks.NETHERRACK_FURNACE)
                .addCriterion(
                        "crafted_netherrack_furnace",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("netherrack_furnace")))
                .build(InfiniteX.id("progression/nether_forge"));
        output.accept(netherForge);

        AdvancementHolder runeGate = manual(
                output, "rune_gate", underworld, InfXBlocks.MITHRIL_RUNE_STONE, true, "used_runegate");

        AdvancementHolder theEnd = manual(
                output, "the_end", netherForge, Items.END_CRYSTAL, true, "entered_end");

        manual(output, "the_end2", theEnd, Items.DRAGON_EGG, true, "returned_from_end");
    }

    private static AdvancementHolder recipeNode(
            Consumer<AdvancementHolder> output,
            String name,
            AdvancementHolder parent,
            ItemLike icon,
            String... recipePaths) {
        Advancement.Builder builder = child(name, parent, icon);
        for (String recipePath : recipePaths) {
            builder.addCriterion(
                    "crafted_" + recipePath,
                    RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey(recipePath)));
        }
        AdvancementHolder holder = builder
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/" + name));
        output.accept(holder);
        return holder;
    }

    private static Advancement.Builder child(String name, AdvancementHolder parent, ItemLike icon) {
        return node(name, parent, icon, AdvancementType.TASK);
    }

    private static Advancement.Builder node(
            String name,
            AdvancementHolder parent,
            ItemLike icon,
            AdvancementType type) {
        return Advancement.Builder.recipeAdvancement()
                .parent(parent)
                .display(
                        icon,
                        title(name),
                        description(name),
                        null,
                        type,
                        true,
                        true,
                        false);
    }

    private static AdvancementHolder manual(
            Consumer<AdvancementHolder> output,
            String name,
            AdvancementHolder parent,
            ItemLike icon,
            boolean challenge,
            String criterion) {
        AdvancementHolder holder = node(
                        name,
                        parent,
                        icon,
                        challenge ? AdvancementType.CHALLENGE : AdvancementType.TASK)
                .addCriterion(criterion, manualCriterion())
                .build(InfiniteX.id("progression/" + name));
        output.accept(holder);
        return holder;
    }

    private static Criterion<ImpossibleTrigger.TriggerInstance> manualCriterion() {
        return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryChanged() {
        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
                new InventoryChangeTrigger.TriggerInstance(
                        Optional.empty(),
                        InventoryChangeTrigger.TriggerInstance.Slots.ANY,
                        List.of()));
    }

    private static Criterion<PlayerTrigger.TriggerInstance> wearingPiece(
            HolderLookup.RegistryLookup<Item> items,
            InfxMaterial material,
            EquipmentType piece) {
        ItemPredicate.Builder predicate = ItemPredicate.Builder.item().of(items, equipment(material, piece));
        EntityEquipmentPredicate.Builder equipment = EntityEquipmentPredicate.Builder.equipment();
        switch (piece) {
            case HELMET -> equipment.head(predicate);
            case CHESTPLATE -> equipment.chest(predicate);
            case LEGGINGS -> equipment.legs(predicate);
            case BOOTS -> equipment.feet(predicate);
            default -> throw new IllegalArgumentException("Not a plate armor piece: " + piece);
        }
        return PlayerTrigger.TriggerInstance.located(
                EntityPredicate.Builder.entity().equipment(equipment));
    }

    private static Criterion<PlayerTrigger.TriggerInstance> wearingPlateSet(
            HolderLookup.RegistryLookup<Item> items,
            List<InfxMaterial> materials) {
        EntityEquipmentPredicate.Builder equipment = EntityEquipmentPredicate.Builder.equipment()
                .head(armorMaterialPredicate(items, materials, EquipmentType.HELMET))
                .chest(armorMaterialPredicate(items, materials, EquipmentType.CHESTPLATE))
                .legs(armorMaterialPredicate(items, materials, EquipmentType.LEGGINGS))
                .feet(armorMaterialPredicate(items, materials, EquipmentType.BOOTS));
        return PlayerTrigger.TriggerInstance.located(
                EntityPredicate.Builder.entity().equipment(equipment));
    }

    private static ItemPredicate.Builder armorMaterialPredicate(
            HolderLookup.RegistryLookup<Item> items,
            List<InfxMaterial> materials,
            EquipmentType piece) {
        ItemLike[] allowed = materials.stream()
                .flatMap(material -> java.util.stream.Stream.of(
                        equipment(material, piece),
                        equipment(material, chainPiece(piece))))
                .toArray(ItemLike[]::new);
        return ItemPredicate.Builder.item().of(items, allowed);
    }

    private static EquipmentType chainPiece(EquipmentType platePiece) {
        return switch (platePiece) {
            case HELMET -> EquipmentType.CHAINMAIL_HELMET;
            case CHESTPLATE -> EquipmentType.CHAINMAIL_CHESTPLATE;
            case LEGGINGS -> EquipmentType.CHAINMAIL_LEGGINGS;
            case BOOTS -> EquipmentType.CHAINMAIL_BOOTS;
            default -> throw new IllegalArgumentException("Not a plate armor piece: " + platePiece);
        };
    }

    private static ItemLike equipment(InfxMaterial material, EquipmentType type) {
        return InfXItems.catalog().equipment(material, type).holder();
    }

    private static Component title(String name) {
        return Component.translatable("advancements.infx." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements.infx." + name + ".description");
    }

    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, InfiniteX.id(path));
    }
}
