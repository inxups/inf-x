package com.pixulse.infx.data.food;

import com.pixulse.infx.registry.InfXAttachments;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** INFX's food-specific ingestion gate, separate from modern Player#canEat(boolean). */
public final class FoodIngestion {
    private FoodIngestion() {}

    public static boolean canIngest(Player player, ItemStack stack) {
        if (!canEatAndDrink(player)) return false;
        return canIngest(
                player.getData(InfXAttachments.SURVIVAL),
                SurvivalRules.foodCap(player.experienceLevel),
                FoodProfiles.forStack(stack));
    }

    public static boolean canIngest(Player player, FoodProfile food) {
        if (!canEatAndDrink(player)) return false;
        return canIngest(player.getData(InfXAttachments.SURVIVAL), SurvivalRules.foodCap(player.experienceLevel), food);
    }

    /**
     * Mirrors EntityPlayer#canIngest for an INFX food profile. Protein and phytonutrient deficits
     * take priority over energy fullness; essential fats intentionally do not, as in INFX. The
     * energy gate follows the displayed food bar: any energy food is edible while the Nutrition
     * layer (the bar) is not full, regardless of which energy layer it feeds.
     */
    public static boolean canIngest(SurvivalData data, double foodCap, FoodProfile food) {
        if (food.isEmpty()) return false;
        if (food.alwaysEdible()) return true;
        if (data.protein() < 1 && food.protein() > 0) return true;
        if (data.phytonutrients() < 1 && food.phytonutrients() > 0) return true;

        double satiation = food.satiationFor(data);
        if (satiation == 0.0D && food.nutrition() == 0.0D && food.sugarContent() == 0) return true;

        return (satiation > 0.0D || food.nutrition() > 0.0D) && data.nutrition() < foodCap;
    }

    private static boolean canEatAndDrink(Player player) {
        return !player.hasEffect(MobEffects.NAUSEA)
                && (!player.isEyeInFluid(FluidTags.WATER) || player.canBreatheUnderwater());
    }
}
