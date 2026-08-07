package com.pixulse.infx.event.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.data.food.FoodProfile;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

class FoodTooltipEventsTest {
    @Test
    void advancedFoodTooltipsListEveryR196Gain() {
        List<Component> tooltip = new ArrayList<>();
        FoodProfile apple = FoodProfile.of(2, 1, 1_000, false, false, true);

        FoodTooltipEvents.appendFoodGains(tooltip, apple);

        assertEquals(
                List.of(
                        FoodTooltipEvents.SATIATION_TOOLTIP,
                        FoodTooltipEvents.NUTRITION_TOOLTIP,
                        FoodTooltipEvents.PROTEIN_TOOLTIP,
                        FoodTooltipEvents.ESSENTIAL_FATS_TOOLTIP,
                        FoodTooltipEvents.PHYTONUTRIENTS_TOOLTIP,
                        FoodTooltipEvents.SUGAR_TOOLTIP,
                        FoodTooltipEvents.INSULIN_RESPONSE_TOOLTIP),
                tooltip.stream().map(FoodTooltipEventsTest::translationKey).toList());
        assertEquals(
                List.of("2", "1", "0", "0", "8000", "1000", "4800"),
                tooltip.stream().map(FoodTooltipEventsTest::firstArgument).toList());
        assertTrue(tooltip.stream().allMatch(component -> component.getStyle().getColor().equals(
                TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY))));
    }

    @Test
    void onlyAdvancedTooltipsForKnownFoodReceiveGains() {
        FoodProfile apple = FoodProfile.of(2, 1, 1_000, false, false, true);

        assertTrue(FoodTooltipEvents.shouldAddFoodGains(true, true, apple));
        assertFalse(FoodTooltipEvents.shouldAddFoodGains(false, true, apple));
        assertFalse(FoodTooltipEvents.shouldAddFoodGains(true, false, apple));
        assertFalse(FoodTooltipEvents.shouldAddFoodGains(true, true, FoodProfile.EMPTY));
    }

    private static String translationKey(Component component) {
        return ((TranslatableContents) component.getContents()).getKey();
    }

    private static String firstArgument(Component component) {
        return (String) ((TranslatableContents) component.getContents()).getArgs()[0];
    }
}
