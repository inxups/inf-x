package com.pixulse.infx.crafting;

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
    void removesEveryVanillaCraftingSerializerFamily() {
        Map<Identifier, JsonElement> recipes = new LinkedHashMap<>();
        recipes.put(minecraft("shaped"), recipe("minecraft:crafting_shaped"));
        recipes.put(minecraft("shapeless"), recipe("minecraft:crafting_shapeless"));
        recipes.put(minecraft("special"), recipe("minecraft:crafting_special_repairitem"));
        recipes.put(minecraft("transmute"), recipe("minecraft:crafting_transmute"));

        assertEquals(4, VanillaCraftingRecipeRemoval.removeVanillaCraftingRecipes(recipes));
        assertTrue(recipes.isEmpty());
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
