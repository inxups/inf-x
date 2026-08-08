package com.pixulse.infx.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class VanillaCraftingRecipeRemovalTest {
    @Test
    void removesVanillaWeaponAndToolRecipes() {
        Map<Identifier, JsonElement> recipes = new LinkedHashMap<>();
        recipes.put(minecraft("wooden_sword"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("iron_pickaxe"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("diamond_spear"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("bow"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("arrow"), recipe("minecraft:crafting_shapeless"));
        recipes.put(minecraft("shield"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("shears"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("tipped_arrow"), recipe("minecraft:crafting_imbue"));
        recipes.put(minecraft("shield_decoration"), recipe("minecraft:crafting_special_shielddecoration"));
        recipes.put(minecraft("repair_item"), recipe("minecraft:crafting_special_repairitem"));

        assertEquals(10, VanillaCraftingRecipeRemoval.removeVanillaCraftingRecipes(recipes));
        assertTrue(recipes.isEmpty());
    }

    @Test
    void restoresEveryOtherVanillaRecipe() {
        Map<Identifier, JsonElement> recipes = new LinkedHashMap<>();
        Identifier planks = minecraft("oak_planks");
        Identifier torch = minecraft("torch");
        Identifier helmet = minecraft("iron_helmet");
        Identifier dye = minecraft("leather_boots_dyed");
        Identifier transmute = minecraft("gray_bundle");
        Identifier banner = minecraft("banner_duplicate");
        recipes.put(planks, recipe("minecraft:crafting_shapeless"));
        recipes.put(torch, recipe("minecraft:crafting_shaped"));
        recipes.put(helmet, recipe("minecraft:crafting_shaped"));
        recipes.put(dye, recipe("minecraft:crafting_dye"));
        recipes.put(transmute, recipe("minecraft:crafting_transmute"));
        recipes.put(banner, recipe("minecraft:crafting_special_bannerduplicate"));
        recipes.put(minecraft("copper_ingot_from_smelting_copper_ore"), recipe("minecraft:smelting"));
        recipes.put(minecraft("netherite_helmet_smithing"), recipe("minecraft:smithing_transform"));

        assertEquals(0, VanillaCraftingRecipeRemoval.removeVanillaCraftingRecipes(recipes));
        assertTrue(recipes.containsKey(planks));
        assertTrue(recipes.containsKey(torch));
        assertTrue(recipes.containsKey(helmet));
        assertTrue(recipes.containsKey(dye));
        assertTrue(recipes.containsKey(transmute));
        assertTrue(recipes.containsKey(banner));
    }

    @Test
    void retainsNonCraftingAndNonVanillaRecipes() {
        Map<Identifier, JsonElement> recipes = new LinkedHashMap<>();
        Identifier smelting = minecraft("smelting");
        Identifier infxCrafting = Identifier.fromNamespaceAndPath("infx", "custom");
        Identifier conditionalOverride = minecraft("disabled_override");
        recipes.put(smelting, recipe("minecraft:smelting"));
        recipes.put(infxCrafting, recipe("minecraft:crafting_shaped"));
        recipes.put(conditionalOverride, JsonParser.parseString("{\"neoforge:conditions\":[]}"));

        assertEquals(0, VanillaCraftingRecipeRemoval.removeVanillaCraftingRecipes(recipes));
        assertTrue(recipes.containsKey(smelting));
        assertTrue(recipes.containsKey(infxCrafting));
        assertTrue(recipes.containsKey(conditionalOverride));
        assertFalse(VanillaCraftingRecipeRemoval.isVanillaCraftingRecipe(
                minecraft("malformed"), JsonParser.parseString("[]")));
    }

    private static Identifier minecraft(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    private static JsonElement recipe(String type) {
        return JsonParser.parseString("{\"type\":\"" + type + "\"}");
    }
}
