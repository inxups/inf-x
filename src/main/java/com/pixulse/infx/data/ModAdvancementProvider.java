package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.advancements.triggers.RecipeCraftedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

final class ModAdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        HolderLookup.RegistryLookup<Item> items = registries.lookupOrThrow(Registries.ITEM);

        AdvancementHolder openInventory = Advancement.Builder.recipeAdvancement()
                .display(
                        Items.STICK,
                        title("open_inventory"),
                        description("open_inventory"),
                        net.minecraft.resources.Identifier.withDefaultNamespace(
                                "gui/advancements/backgrounds/stone"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .addCriterion("inventory_changed", inventoryChanged())
                .build(InfiniteX.id("progression/open_inventory"));
        output.accept(openInventory);

        AdvancementHolder stickPicker = child("stick_picker", openInventory, Items.STICK)
                .addCriterion(
                        "has_stick", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STICK))
                .build(InfiniteX.id("progression/stick_picker"));
        output.accept(stickPicker);

        AdvancementHolder cuttingEdge = child("cutting_edge", stickPicker, ModItems.FLINT_HATCHET)
                .addCriterion(
                        "crafted_hatchet",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("flint_hatchet")))
                .addCriterion(
                        "crafted_flint_knife",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("flint_knife")))
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/cutting_edge"));
        output.accept(cuttingEdge);

        AdvancementHolder mineWood = child("mine_wood", cuttingEdge, Items.OAK_LOG)
                .addCriterion(
                        "has_log",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item().of(items, ItemTags.LOGS)))
                .build(InfiniteX.id("progression/mine_wood"));
        output.accept(mineWood);

        AdvancementHolder buildWorkbench = child("build_work_bench", mineWood, ModBlocks.FLINT_WORKBENCH)
                .addCriterion(
                        "crafted_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("flint_workbench")))
                .build(InfiniteX.id("progression/build_work_bench"));
        output.accept(buildWorkbench);

        AdvancementHolder buildClub = child(
                        "build_club",
                        buildWorkbench,
                        ModItems.catalog()
                                .equipment(R196Material.WOOD, R196EquipmentType.CLUB)
                                .holder())
                .addCriterion(
                        "crafted_wood_club",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("wood_club")))
                .build(InfiniteX.id("progression/build_club"));
        output.accept(buildClub);

        Advancement.Builder buildAxe = child("build_axe", buildWorkbench, ModItems.FLINT_AXE)
                .addCriterion(
                        "crafted_flint_axe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("flint_axe")))
                .addCriterion(
                        "crafted_copper_axe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("copper_axe")))
                .addCriterion(
                        "crafted_iron_axe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("iron_axe")))
                .addCriterion(
                        "crafted_silver_axe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("silver_axe")))
                .addCriterion(
                        "crafted_gold_axe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("gold_axe")))
                .addCriterion(
                        "crafted_ancient_metal_axe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("ancient_metal_axe")))
                .addCriterion(
                        "crafted_mithril_axe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("mithril_axe")))
                .addCriterion(
                        "crafted_adamantium_axe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("adamantium_axe")))
                .requirements(AdvancementRequirements.Strategy.OR);
        output.accept(addCraftedRecipeCriteria(
                        buildAxe,
                        "copper_battle_axe",
                        "silver_battle_axe",
                        "gold_battle_axe",
                        "iron_battle_axe",
                        "ancient_metal_battle_axe",
                        "mithril_battle_axe",
                        "adamantium_battle_axe")
                .build(InfiniteX.id("progression/build_axe")));

        AdvancementHolder buildShovel = addCraftedRecipeCriteria(
                        child("build_shovel", buildWorkbench, ModItems.FLINT_SHOVEL)
                                .addCriterion(
                                        "crafted_shovel",
                                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("flint_shovel"))),
                        "obsidian_shovel",
                        "copper_shovel",
                        "silver_shovel",
                        "gold_shovel",
                        "iron_shovel",
                        "ancient_metal_shovel",
                        "mithril_shovel",
                        "adamantium_shovel")
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/build_shovel"));
        output.accept(buildShovel);

        AdvancementHolder nuggets = child("nuggets", buildShovel, Items.COPPER_NUGGET)
                .addCriterion(
                        "has_copper_nugget",
                        InventoryChangeTrigger.TriggerInstance.hasItems(Items.COPPER_NUGGET))
                .build(InfiniteX.id("progression/nuggets"));
        output.accept(nuggets);

        AdvancementHolder betterTools = child("better_tools", nuggets, ModBlocks.COPPER_WORKBENCH)
                .addCriterion(
                        "crafted_copper_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("copper_workbench")))
                .build(InfiniteX.id("progression/better_tools"));
        output.accept(betterTools);

        Advancement.Builder wearLeather = child(
                "wear_leather",
                buildWorkbench,
                equipment(R196Material.LEATHER, R196EquipmentType.CHESTPLATE));
        for (R196EquipmentType piece : R196EquipmentType.platePieces()) {
            wearLeather.addCriterion(
                    "wearing_leather_" + piece.path(),
                    wearingPiece(items, R196Material.LEATHER, piece));
        }
        output.accept(wearLeather
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/wear_leather")));

        List<R196Material> chainMaterials = List.of(
                R196Material.COPPER,
                R196Material.SILVER,
                R196Material.GOLD,
                R196Material.RUSTED_IRON,
                R196Material.IRON,
                R196Material.ANCIENT_METAL,
                R196Material.MITHRIL,
                R196Material.ADAMANTIUM);
        Advancement.Builder buildChainMail = child(
                "build_chain_mail",
                betterTools,
                equipment(R196Material.IRON, R196EquipmentType.CHAINMAIL_CHESTPLATE));
        for (R196Material material : chainMaterials) {
            for (R196EquipmentType piece : R196EquipmentType.chainPieces()) {
                String recipe = material.path() + "_" + piece.path();
                buildChainMail.addCriterion(
                        "crafted_" + recipe,
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey(recipe)));
            }
        }
        AdvancementHolder buildChainMailHolder = buildChainMail
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/build_chain_mail"));
        output.accept(buildChainMailHolder);

        List<R196Material> plateMaterials = List.of(
                R196Material.COPPER,
                R196Material.SILVER,
                R196Material.GOLD,
                R196Material.IRON,
                R196Material.ANCIENT_METAL,
                R196Material.MITHRIL,
                R196Material.ADAMANTIUM);
        AdvancementHolder wearAllPlateArmor = child(
                        "wear_all_plate_armor",
                        buildChainMailHolder,
                        equipment(R196Material.IRON, R196EquipmentType.CHESTPLATE))
                .addCriterion("wearing_full_plate", wearingPlateSet(items, plateMaterials))
                .build(InfiniteX.id("progression/wear_all_plate_armor"));
        output.accept(wearAllPlateArmor);

        AdvancementHolder wearAllAdamantiumPlateArmor = Advancement.Builder.recipeAdvancement()
                .parent(wearAllPlateArmor)
                .display(
                        equipment(R196Material.ADAMANTIUM, R196EquipmentType.CHESTPLATE),
                        title("wear_all_adamantium_plate_armor"),
                        description("wear_all_adamantium_plate_armor"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false)
                .addCriterion(
                        "wearing_full_adamantium_plate",
                        wearingPlateSet(items, List.of(R196Material.ADAMANTIUM)))
                .build(InfiniteX.id("progression/wear_all_adamantium_plate_armor"));
        output.accept(wearAllAdamantiumPlateArmor);

        Advancement.Builder buildHoeBuilder = child("build_hoe", betterTools, ModItems.COPPER_HOE)
                .addCriterion(
                        "crafted_copper_hoe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("copper_hoe")))
                .addCriterion(
                        "crafted_iron_hoe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("iron_hoe")))
                .addCriterion(
                        "crafted_silver_hoe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("silver_hoe")))
                .addCriterion(
                        "crafted_gold_hoe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("gold_hoe")))
                .addCriterion(
                        "crafted_ancient_metal_hoe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("ancient_metal_hoe")))
                .addCriterion(
                        "crafted_mithril_hoe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("mithril_hoe")))
                .addCriterion(
                        "crafted_adamantium_hoe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("adamantium_hoe")))
                .requirements(AdvancementRequirements.Strategy.OR);
        AdvancementHolder buildHoe = addCraftedRecipeCriteria(
                        buildHoeBuilder,
                        "copper_mattock",
                        "silver_mattock",
                        "gold_mattock",
                        "iron_mattock",
                        "ancient_metal_mattock",
                        "mithril_mattock",
                        "adamantium_mattock")
                .build(InfiniteX.id("progression/build_hoe"));
        output.accept(buildHoe);

        AdvancementHolder buildScythe = addCraftedRecipeCriteria(
                        child(
                                "build_scythe",
                                buildHoe,
                                ModItems.catalog()
                                        .equipment(R196Material.COPPER, R196EquipmentType.SCYTHE)
                                        .holder()),
                        "copper_scythe",
                        "silver_scythe",
                        "gold_scythe",
                        "iron_scythe",
                        "ancient_metal_scythe",
                        "mithril_scythe",
                        "adamantium_scythe")
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/build_scythe"));
        output.accept(buildScythe);

        AdvancementHolder buildPickaxe = child("build_pickaxe", betterTools, ModItems.COPPER_PICKAXE)
                .addCriterion(
                        "crafted_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("copper_pickaxe")))
                .addCriterion(
                        "crafted_silver_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("silver_pickaxe")))
                .addCriterion(
                        "crafted_gold_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("gold_pickaxe")))
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/build_pickaxe"));
        output.accept(buildPickaxe);

        AdvancementHolder buildFurnace = child("build_furnace", buildPickaxe, Items.FURNACE)
                .addCriterion(
                        "crafted_furnace",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("cobblestone_furnace")))
                .build(InfiniteX.id("progression/build_furnace"));
        output.accept(buildFurnace);

        AdvancementHolder acquireIron = child("acquire_iron", buildFurnace, Items.IRON_INGOT)
                .addCriterion(
                        "smelted_iron",
                        CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                .build(InfiniteX.id("progression/acquire_iron"));
        output.accept(acquireIron);

        Advancement.Builder buildBetterPickaxe = child("build_better_pickaxe", acquireIron, ModItems.IRON_PICKAXE)
                .addCriterion(
                        "crafted_iron_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("iron_pickaxe")))
                .addCriterion(
                        "crafted_ancient_metal_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("ancient_metal_pickaxe")))
                .addCriterion(
                        "crafted_mithril_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("mithril_pickaxe")))
                .addCriterion(
                        "crafted_adamantium_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("adamantium_pickaxe")))
                .requirements(AdvancementRequirements.Strategy.OR);
        output.accept(addCraftedRecipeCriteria(
                        buildBetterPickaxe,
                        "iron_war_hammer",
                        "ancient_metal_war_hammer",
                        "mithril_war_hammer",
                        "adamantium_war_hammer")
                .build(InfiniteX.id("progression/build_better_pickaxe")));
    }

    private static Advancement.Builder child(String name, AdvancementHolder parent, ItemLike icon) {
        return Advancement.Builder.recipeAdvancement()
                .parent(parent)
                .display(
                        icon,
                        title(name),
                        description(name),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false);
    }

    private static Advancement.Builder addCraftedRecipeCriteria(
            Advancement.Builder builder, String... recipePaths) {
        for (String recipePath : recipePaths) {
            builder.addCriterion(
                    "crafted_" + recipePath,
                    RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey(recipePath)));
        }
        return builder;
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
            R196Material material,
            R196EquipmentType piece) {
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
            List<R196Material> materials) {
        EntityEquipmentPredicate.Builder equipment = EntityEquipmentPredicate.Builder.equipment()
                .head(platePredicate(items, materials, R196EquipmentType.HELMET))
                .chest(platePredicate(items, materials, R196EquipmentType.CHESTPLATE))
                .legs(platePredicate(items, materials, R196EquipmentType.LEGGINGS))
                .feet(platePredicate(items, materials, R196EquipmentType.BOOTS));
        return PlayerTrigger.TriggerInstance.located(
                EntityPredicate.Builder.entity().equipment(equipment));
    }

    private static ItemPredicate.Builder platePredicate(
            HolderLookup.RegistryLookup<Item> items,
            List<R196Material> materials,
            R196EquipmentType piece) {
        ItemLike[] allowed = materials.stream()
                .map(material -> equipment(material, piece))
                .toArray(ItemLike[]::new);
        return ItemPredicate.Builder.item().of(items, allowed);
    }

    private static ItemLike equipment(R196Material material, R196EquipmentType type) {
        return ModItems.catalog().equipment(material, type).holder();
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
