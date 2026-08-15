package com.pixulse.infx.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * JUnit cannot resolve INFX's deferred item holders, so the recipe logic is
 * exercised with vanilla stand-in items; the real leather + sinew target is
 * covered by the generated {@code infx:leather_repair} recipe and game tests.
 */
class InfXRepairRecipeTest {
    private static final int VANILLA_LEATHER_HELMET_MAX_DAMAGE = 55;
    private static final int LEATHER_HELMET_COMPONENTS = 5;

    private static final List<Item> VANILLA_STANDS_INS =
            List.of(Items.LEATHER_HELMET, Items.STRING, Items.STICK);

    @BeforeAll
    static void bindItemComponents() {
        // JUnit does not bootstrap the component registry, so the vanilla
        // built-in holders used by new ItemStack(...) must be bound first;
        // matching an Ingredient compares holder identity, so the stacks must
        // use the item's built-in registry holder (Holder.direct would not).
        for (Item item : VANILLA_STANDS_INS) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(
                        net.minecraft.core.component.DataComponentMap.EMPTY);
            }
        }
    }

    private static InfXRepairRecipe recipe() {
        return new InfXRepairRecipe(
                Ingredient.of(Items.LEATHER_HELMET),
                Ingredient.of(Items.STRING));
    }

    private static ItemStack damagedLeatherHelmet(int damage) {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET, 1);
        stack.set(DataComponents.MAX_DAMAGE, VANILLA_LEATHER_HELMET_MAX_DAMAGE);
        stack.set(DataComponents.DAMAGE, damage);
        return stack;
    }

    private static ItemStack string() {
        return new ItemStack(Items.STRING, 1);
    }

    private static CraftingInput grid(ItemStack... stacks) {
        List<ItemStack> slots = new java.util.ArrayList<>(stacks.length + 1);
        slots.addAll(List.of(stacks));
        while (slots.size() < 4) {
            slots.add(ItemStack.EMPTY);
        }
        return CraftingInput.of(2, 2, slots);
    }

    @Test
    void isSpecialSoItIsHiddenFromTheRecipeBook() {
        assertTrue(recipe().isSpecial());
    }

    @Test
    void repairsDamagedArmorAndPreservesComponents() {
        InfXRepairRecipe recipe = recipe();
        ItemStack helmet = damagedLeatherHelmet(20);
        helmet.set(DataComponents.CUSTOM_NAME, Component.literal("My Hat"));
        CraftingInput input = grid(helmet, string());

        assertTrue(recipe.matches(input, null));

        ItemStack repaired = recipe.assemble(input);
        assertEquals(Items.LEATHER_HELMET, repaired.getItem());
        // repairPerMaterial = max(1, 55 / 5); the vanilla helmet's registry
        // path matches the InfX leather-helmet entry, so its component count
        // applies even though JUnit cannot resolve the mod's item holders.
        assertEquals(20 - VANILLA_LEATHER_HELMET_MAX_DAMAGE / LEATHER_HELMET_COMPONENTS, repaired.getDamageValue());
        assertEquals(Component.literal("My Hat"), repaired.get(DataComponents.CUSTOM_NAME));
        assertTrue(repaired.isDamaged());
    }

    @Test
    void fullyRepairsWhenDamageIsBelowOneMaterialRepair() {
        InfXRepairRecipe recipe = recipe();
        int repairPer = VANILLA_LEATHER_HELMET_MAX_DAMAGE / LEATHER_HELMET_COMPONENTS;
        CraftingInput input = grid(damagedLeatherHelmet(repairPer - 1), string());

        assertTrue(recipe.matches(input, null));
        assertEquals(0, recipe.assemble(input).getDamageValue());
    }

    @Test
    void rejectsUndamagedArmor() {
        InfXRepairRecipe recipe = recipe();
        ItemStack helmet = damagedLeatherHelmet(0);
        assertFalse(recipe.matches(grid(helmet, string()), null));
    }

    @Test
    void rejectsGridsWithMoreThanTwoOccupiedSlots() {
        InfXRepairRecipe recipe = recipe();
        ItemStack stick = new ItemStack(Items.STICK, 1);
        CraftingInput input = grid(damagedLeatherHelmet(10), string(), stick);
        assertFalse(recipe.matches(input, null));
    }

    @Test
    void rejectsMultipleArmorPieces() {
        InfXRepairRecipe recipe = recipe();
        CraftingInput input = grid(damagedLeatherHelmet(10), damagedLeatherHelmet(5));
        assertFalse(recipe.matches(input, null));
    }

    @Test
    void rejectsUnrelatedIngredients() {
        InfXRepairRecipe recipe = recipe();
        ItemStack stick = new ItemStack(Items.STICK, 1);
        CraftingInput input = grid(damagedLeatherHelmet(10), stick);
        assertFalse(recipe.matches(input, null));
        assertEquals(ItemStack.EMPTY, recipe.assemble(input));
    }

    @Test
    void codecRoundTrips() {
        RegistryOps<JsonElement> registryOps =
                RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        InfXRepairRecipe recipe = recipe();
        var codec = InfXRepairRecipe.MAP_CODEC.codec();
        var encoded = codec.encodeStart(registryOps, recipe).getOrThrow();
        InfXRepairRecipe decoded = codec.parse(registryOps, encoded).getOrThrow();
        assertInstanceOf(InfXRepairRecipe.class, decoded);
        CraftingInput input = grid(damagedLeatherHelmet(10), string());
        assertTrue(decoded.matches(input, null));
        assertEquals(recipe.assemble(input).getDamageValue(), decoded.assemble(input).getDamageValue());
        assertEquals(Items.LEATHER_HELMET, decoded.assemble(input).getItem());
    }
}
