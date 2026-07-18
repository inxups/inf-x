package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
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

        AdvancementHolder buildShovel = child("build_shovel", buildWorkbench, ModItems.FLINT_SHOVEL)
                .addCriterion(
                        "crafted_shovel",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("flint_shovel")))
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

        AdvancementHolder buildPickaxe = child("build_pickaxe", betterTools, ModItems.COPPER_PICKAXE)
                .addCriterion(
                        "crafted_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("copper_pickaxe")))
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

        output.accept(child("build_better_pickaxe", acquireIron, ModItems.IRON_PICKAXE)
                .addCriterion(
                        "crafted_iron_pickaxe",
                        RecipeCraftedTrigger.TriggerInstance.craftedItem(recipeKey("iron_pickaxe")))
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

    private static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryChanged() {
        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
                new InventoryChangeTrigger.TriggerInstance(
                        Optional.empty(),
                        InventoryChangeTrigger.TriggerInstance.Slots.ANY,
                        List.of()));
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
