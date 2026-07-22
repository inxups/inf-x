package com.pixulse.infx.integration.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class InfiniteXJeiPluginTest {
    @Test
    void runeStoneSubtypesUseTheirBlockStateComponents() {
        RecordingSubtypeRegistration registration = new RecordingSubtypeRegistration();

        InfiniteXJeiPlugin.registerRuneStoneSubtypes(registration, Items.STONE, Items.DIRT);

        assertEquals(
                Map.of(
                        Items.STONE, List.of(DataComponents.BLOCK_STATE),
                        Items.DIRT, List.of(DataComponents.BLOCK_STATE)),
                registration.componentsByItem);
    }

    private static final class RecordingSubtypeRegistration implements ISubtypeRegistration {
        private final Map<Item, List<DataComponentType<?>>> componentsByItem = new LinkedHashMap<>();

        @Override
        public <B, I> void registerSubtypeInterpreter(
                IIngredientTypeWithSubtypes<B, I> type, B base, ISubtypeInterpreter<I> interpreter) {
            fail("Rune stones should use JEI's data-component subtype registration");
        }

        @Override
        public void registerFromDataComponentTypes(Item item, DataComponentType<?>... components) {
            componentsByItem.put(item, Arrays.asList(components));
        }
    }
}
