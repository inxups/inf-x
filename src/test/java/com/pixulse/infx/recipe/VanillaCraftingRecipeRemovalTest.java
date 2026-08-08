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
    void removesEveryVanillaCraftingRecipe() {
        Map<Identifier, JsonElement> recipes = new LinkedHashMap<>();
        recipes.put(minecraft("crafting_table"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("oak_planks"), recipe("minecraft:crafting_shapeless"));
        recipes.put(minecraft("torch"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("stick"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("iron_helmet"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("iron_ingot_from_nuggets"), recipe("minecraft:crafting_shapeless"));
        recipes.put(minecraft("saddle"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("leather_boots_dyed"), recipe("minecraft:crafting_dye"));
        recipes.put(minecraft("gray_bundle"), recipe("minecraft:crafting_transmute"));
        recipes.put(minecraft("banner_duplicate"), recipe("minecraft:crafting_special_bannerduplicate"));
        recipes.put(minecraft("repair_item"), recipe("minecraft:crafting_special_repairitem"));
        recipes.put(minecraft("tipped_arrow"), recipe("minecraft:crafting_imbue"));

        assertEquals(12, VanillaCraftingRecipeRemoval.removeVanillaCraftingRecipes(recipes));
        assertTrue(recipes.isEmpty());
    }

    @Test
    void retainsNonCraftingAndNonVanillaRecipes() {
        Map<Identifier, JsonElement> recipes = new LinkedHashMap<>();
        Identifier smelting = minecraft("copper_ingot_from_smelting_copper_ore");
        Identifier smithing = minecraft("netherite_helmet_smithing");
        Identifier stonecutting = minecraft("stone_bricks_from_stone_stonecutting");
        Identifier infxCrafting = Identifier.fromNamespaceAndPath("infx", "flint_knife");
        Identifier conditionalOverride = minecraft("disabled_override");
        recipes.put(smelting, recipe("minecraft:smelting"));
        recipes.put(smithing, recipe("minecraft:smithing_transform"));
        recipes.put(stonecutting, recipe("minecraft:stonecutting"));
        recipes.put(infxCrafting, recipe("minecraft:crafting_shaped"));
        recipes.put(conditionalOverride, JsonParser.parseString("{\"neoforge:conditions\":[]}"));

        assertEquals(0, VanillaCraftingRecipeRemoval.removeVanillaCraftingRecipes(recipes));
        assertTrue(recipes.containsKey(smelting));
        assertTrue(recipes.containsKey(smithing));
        assertTrue(recipes.containsKey(stonecutting));
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
