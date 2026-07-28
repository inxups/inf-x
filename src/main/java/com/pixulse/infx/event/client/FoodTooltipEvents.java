package com.pixulse.infx.event.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.food.FoodProfile;
import com.pixulse.infx.data.food.FoodProfiles;
import java.math.BigDecimal;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Adds the R196 food gains to F3+H item tooltips. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class FoodTooltipEvents {
    static final String SATIATION_TOOLTIP = "tooltip.infx.food.satiation";
    static final String NUTRITION_TOOLTIP = "tooltip.infx.food.nutrition";
    static final String PROTEIN_TOOLTIP = "tooltip.infx.food.protein";
    static final String ESSENTIAL_FATS_TOOLTIP = "tooltip.infx.food.essential_fats";
    static final String PHYTONUTRIENTS_TOOLTIP = "tooltip.infx.food.phytonutrients";
    static final String SUGAR_TOOLTIP = "tooltip.infx.food.sugar";
    static final String INSULIN_RESPONSE_TOOLTIP = "tooltip.infx.food.insulin_response";

    private FoodTooltipEvents() {}

    @SubscribeEvent
    public static void addFoodGains(ItemTooltipEvent event) {
        FoodProfile food = FoodProfiles.forStack(event.getItemStack());
        if (!shouldAddFoodGains(event.getFlags().isAdvanced(), food)) return;

        appendFoodGains(event.getToolTip(), food);
    }

    static boolean shouldAddFoodGains(boolean advancedTooltips, FoodProfile food) {
        return advancedTooltips && !food.isEmpty();
    }

    static void appendFoodGains(List<Component> tooltip, FoodProfile food) {
        tooltip.add(foodLine(SATIATION_TOOLTIP, food.satiation()));
        tooltip.add(foodLine(NUTRITION_TOOLTIP, food.nutrition()));
        tooltip.add(foodLine(PROTEIN_TOOLTIP, food.protein()));
        tooltip.add(foodLine(ESSENTIAL_FATS_TOOLTIP, food.essentialFats()));
        tooltip.add(foodLine(PHYTONUTRIENTS_TOOLTIP, food.phytonutrients()));
        tooltip.add(foodLine(SUGAR_TOOLTIP, food.sugarContent()));
        tooltip.add(foodLine(INSULIN_RESPONSE_TOOLTIP, food.insulinResponse()));
    }

    private static Component foodLine(String key, double amount) {
        return Component.translatable(key, formatAmount(amount)).withStyle(ChatFormatting.DARK_GRAY);
    }

    private static String formatAmount(double amount) {
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString();
    }
}
