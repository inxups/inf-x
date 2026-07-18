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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

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

        AdvancementHolder stickPicker = manual(
                output, "stick_picker", openInventory, Items.STICK, false, "picked_up_stick");

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

        AdvancementHolder mineWood = manual(
                output, "mine_wood", cuttingEdge, Items.OAK_LOG, false, "picked_up_log");

        AdvancementHolder buildWorkbench = child("build_work_bench", mineWood, ModBlocks.FLINT_WORKBENCH)
                .addCriterion(
                        "crafted_flint_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("flint_workbench")))
                .addCriterion(
                        "crafted_obsidian_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("obsidian_workbench")))
                .requirements(AdvancementRequirements.Strategy.OR)
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

        AdvancementHolder killCow = manual(
                output, "kill_cow", buildClub, Items.LEATHER, false, "picked_up_leather");

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

        AdvancementHolder nuggets = manual(
                output, "nuggets", buildShovel, Items.COPPER_NUGGET, false, "picked_up_metal_nugget");

        AdvancementHolder betterTools = child("better_tools", nuggets, ModBlocks.COPPER_WORKBENCH)
                .addCriterion(
                        "crafted_copper_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("copper_workbench")))
                .addCriterion(
                        "crafted_silver_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("silver_workbench")))
                .addCriterion(
                        "crafted_gold_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("gold_workbench")))
                .addCriterion(
                        "crafted_iron_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("iron_workbench")))
                .addCriterion(
                        "crafted_ancient_metal_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("ancient_metal_workbench")))
                .addCriterion(
                        "crafted_mithril_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("mithril_workbench")))
                .addCriterion(
                        "crafted_adamantium_bench",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("adamantium_workbench")))
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/better_tools"));
        output.accept(betterTools);

        Advancement.Builder wearLeather = child(
                "wear_leather",
                killCow,
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
        AdvancementHolder buildBetterPickaxeHolder = addCraftedRecipeCriteria(
                        buildBetterPickaxe,
                        "iron_war_hammer",
                        "ancient_metal_war_hammer",
                        "mithril_war_hammer",
                        "adamantium_war_hammer")
                .build(InfiniteX.id("progression/build_better_pickaxe"));
        output.accept(buildBetterPickaxeHolder);

        AdvancementHolder killEnemy = manual(
                output, "kill_enemy", buildClub, Items.ROTTEN_FLESH, false, "killed_enemy");
        manual(output, "snipe_skeleton", killEnemy, Items.BOW, true, "long_range_skeleton_kill");
        manual(output, "fly_pig", killCow, Items.SADDLE, true, "pig_fall");

        AdvancementHolder flour = manual(
                output, "flour", buildHoe, Items.WHEAT, false, "crafted_flour");
        manual(output, "make_bread", flour, Items.BREAD, false, "smelted_bread");
        AdvancementHolder bakeCake = child("bake_cake", flour, Items.CAKE)
                .addCriterion(
                        "crafted_cake",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(
                                ResourceKey.create(Registries.RECIPE, net.minecraft.resources.Identifier.withDefaultNamespace("cake"))))
                .build(InfiniteX.id("progression/bake_cake"));
        output.accept(bakeCake);
        manual(output, "on_a_rail", acquireIron, Items.MINECART, true, "travelled_rail_1000");

        AdvancementHolder obsidianFurnace = child(
                        "obsidian_furnace", buildBetterPickaxeHolder, ModBlocks.OBSIDIAN_FURNACE)
                .addCriterion(
                        "crafted_obsidian_furnace",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("obsidian_furnace")))
                .build(InfiniteX.id("progression/obsidian_furnace"));
        output.accept(obsidianFurnace);
        AdvancementHolder mithrilIngot = manual(
                output, "mithril_ingot", obsidianFurnace, ModItems.MITHRIL_INGOT, false, "smelted_mithril");
        AdvancementHolder diamonds = manual(
                output, "diamonds", mithrilIngot, Items.DIAMOND, false, "picked_up_diamond");
        AdvancementHolder emeralds = manual(
                output, "emeralds", buildBetterPickaxeHolder, Items.EMERALD, false, "picked_up_emerald");
        AdvancementHolder enchantments = node("enchantments", diamonds, Items.ENCHANTING_TABLE, AdvancementType.TASK)
                .addCriterion("diamond_path", manualCriterion())
                .addCriterion("emerald_path", manualCriterion())
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/enchantments"));
        output.accept(enchantments);
        manual(output, "overkill", enchantments, ModItems.IRON_SWORD, true, "melee_damage_18");
        AdvancementHolder bookcase = child("bookcase", enchantments, Items.BOOKSHELF)
                .addCriterion(
                        "crafted_bookcase",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(ResourceKey.create(
                                Registries.RECIPE,
                                net.minecraft.resources.Identifier.withDefaultNamespace("bookshelf"))))
                .build(InfiniteX.id("progression/bookcase"));
        output.accept(bookcase);
        manual(output, "enlightenment", bookcase, Items.WRITTEN_BOOK, true, "read_nine_books");

        AdvancementHolder portal = manual(
                output, "portal", buildBetterPickaxeHolder, Items.OBSIDIAN, false, "changed_r196_dimension");
        AdvancementHolder portalToNether = manual(
                output, "portal_to_nether", portal, ModBlocks.MANTLE, false, "found_mantle");
        manual(output, "ghast", portalToNether, Items.GHAST_TEAR, true, "reflected_fireball_kill");
        AdvancementHolder blazeRod = manual(
                output, "blaze_rod", portalToNether, Items.BLAZE_ROD, false, "picked_up_blaze_rod");
        manual(output, "potion", blazeRod, Items.POTION, false, "brewed_potion");
        AdvancementHolder theEnd = manual(
                output, "the_end", blazeRod, Items.ENDER_EYE, true, "entered_end");
        manual(output, "the_end2", theEnd, Items.DRAGON_EGG, true, "returned_from_end");
        AdvancementHolder netherrackFurnace = child(
                        "netherrack_furnace", blazeRod, ModBlocks.NETHERRACK_FURNACE)
                .addCriterion(
                        "crafted_netherrack_furnace",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("netherrack_furnace")))
                .build(InfiniteX.id("progression/netherrack_furnace"));
        output.accept(netherrackFurnace);
        AdvancementHolder adamantiumIngot = manual(
                output,
                "adamantium_ingot",
                netherrackFurnace,
                ModItems.ADAMANTIUM_INGOT,
                false,
                "smelted_adamantium");
        manual(output, "crystal_breaker", adamantiumIngot, ModItems.catalog()
                .equipment(R196Material.ADAMANTIUM, R196EquipmentType.PICKAXE).holder(), true, "crafted_crystal_tool");
        manual(output, "runegate", portal, ModBlocks.MITHRIL_RUNE_STONE, true, "used_runegate");

        AdvancementHolder seeds = manual(
                output, "seeds", openInventory, Items.WHEAT_SEEDS, false, "picked_up_seed");
        manual(output, "eggs", seeds, Items.EGG, false, "ate_raw_egg");
        AdvancementHolder buildOven = addCraftedRecipeCriteria(
                        child("build_oven", openInventory, ModBlocks.CLAY_FURNACE),
                        "clay_furnace",
                        "large_clay_oven",
                        "sandstone_furnace",
                        "hardened_clay_furnace",
                        "cobblestone_furnace",
                        "obsidian_furnace",
                        "netherrack_furnace")
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/build_oven"));
        output.accept(buildOven);
        manual(output, "flint_finder", openInventory, Items.FLINT, false, "mined_flint_from_gravel");
        AdvancementHolder buildTorches = child("build_torches", buildWorkbench, Items.TORCH)
                .addCriterion(
                        "crafted_torches",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(ResourceKey.create(
                                Registries.RECIPE,
                                net.minecraft.resources.Identifier.withDefaultNamespace("torch"))))
                .build(InfiniteX.id("progression/build_torches"));
        output.accept(buildTorches);
        AdvancementHolder soilEnrichment = manual(
                output,
                "soil_enrichment",
                buildHoe,
                ModItems.catalog().raw("manure").holder(),
                false,
                "fertilized_soil");
        AdvancementHolder makeMycelium = manual(
                output, "make_mycelium", soilEnrichment, Blocks.MYCELIUM, false, "made_mycelium");
        manual(output, "supersize_me", makeMycelium, Blocks.BROWN_MUSHROOM_BLOCK, false, "grew_giant_mushroom");
        manual(output, "plant_doctor", buildHoe, Items.BONE_MEAL, false, "cured_crop");
        manual(output, "well_rested", buildWorkbench, Blocks.BED.pick(DyeColor.RED), false, "slept_6000_ticks");
        manual(output, "seaworthy", buildWorkbench, Items.OAK_BOAT, false, "sailed_deep_water");
        manual(output, "explorer", openInventory, Items.COMPASS, true, "reached_10000_blocks");
        AdvancementHolder fishingRod = addCraftedRecipeCriteria(
                        child(
                                "fishing_rod",
                                betterTools,
                                ModItems.catalog().equipment(R196Material.FLINT, R196EquipmentType.FISHING_ROD).holder()),
                        "flint_fishing_rod",
                        "obsidian_fishing_rod",
                        "copper_fishing_rod",
                        "silver_fishing_rod",
                        "gold_fishing_rod",
                        "rusted_iron_fishing_rod",
                        "iron_fishing_rod",
                        "ancient_metal_fishing_rod",
                        "mithril_fishing_rod",
                        "adamantium_fishing_rod")
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(InfiniteX.id("progression/fishing_rod"));
        output.accept(fishingRod);
        manual(output, "cook_fish", fishingRod, Items.COOKED_COD, false, "smelted_fish");
        manual(output, "fine_dining", buildWorkbench, Items.MUSHROOM_STEW, false, "crafted_fine_food");
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
                .head(armorMaterialPredicate(items, materials, R196EquipmentType.HELMET))
                .chest(armorMaterialPredicate(items, materials, R196EquipmentType.CHESTPLATE))
                .legs(armorMaterialPredicate(items, materials, R196EquipmentType.LEGGINGS))
                .feet(armorMaterialPredicate(items, materials, R196EquipmentType.BOOTS));
        return PlayerTrigger.TriggerInstance.located(
                EntityPredicate.Builder.entity().equipment(equipment));
    }

    private static ItemPredicate.Builder armorMaterialPredicate(
            HolderLookup.RegistryLookup<Item> items,
            List<R196Material> materials,
            R196EquipmentType piece) {
        ItemLike[] allowed = materials.stream()
                .flatMap(material -> java.util.stream.Stream.of(
                        equipment(material, piece),
                        equipment(material, chainPiece(piece))))
                .toArray(ItemLike[]::new);
        return ItemPredicate.Builder.item().of(items, allowed);
    }

    private static R196EquipmentType chainPiece(R196EquipmentType platePiece) {
        return switch (platePiece) {
            case HELMET -> R196EquipmentType.CHAINMAIL_HELMET;
            case CHESTPLATE -> R196EquipmentType.CHAINMAIL_CHESTPLATE;
            case LEGGINGS -> R196EquipmentType.CHAINMAIL_LEGGINGS;
            case BOOTS -> R196EquipmentType.CHAINMAIL_BOOTS;
            default -> throw new IllegalArgumentException("Not a plate armor piece: " + platePiece);
        };
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
