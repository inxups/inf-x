package com.pixulse.infx.data.food;

/**
 * INFX food metadata. Long-term nutrient quantities are derived from the source item's Nutrition
 * value and nutrient flags, while sugar content is kept separate from its insulin response.
 */
public record FoodProfile(
        double satiation,
        double nutrition,
        int protein,
        int phytonutrients,
        int essentialFats,
        int sugarContent,
        boolean alwaysEdible) {
    public static final int NUTRIENTS_PER_NUTRITION = 8_000;
    public static final float INSULIN_RESPONSE_PER_SUGAR = 4.8F;
    public static final FoodProfile EMPTY = new FoodProfile(0, 0, 0, 0, 0, 0, false);

    public FoodProfile {
        if (satiation < 0 || nutrition < 0 || protein < 0 || phytonutrients < 0
                || essentialFats < 0 || sugarContent < 0) {
            throw new IllegalArgumentException("Food values cannot be negative");
        }
    }

    /** Creates a profile from the INFX Item#setFoodValue inputs. */
    public static FoodProfile of(
            double satiation,
            double nutrition,
            int sugarContent,
            boolean hasProtein,
            boolean hasEssentialFats,
            boolean hasPhytonutrients) {
        return of(
                satiation,
                nutrition,
                sugarContent,
                hasProtein,
                hasEssentialFats,
                hasPhytonutrients,
                0,
                false);
    }

    /** Creates an INFX profile with an item-specific nutrient bonus, such as wheat seeds. */
    public static FoodProfile of(
            double satiation,
            double nutrition,
            int sugarContent,
            boolean hasProtein,
            boolean hasEssentialFats,
            boolean hasPhytonutrients,
            int extraEssentialFats,
            boolean alwaysEdible) {
        int nutrients = (int) (nutrition * NUTRIENTS_PER_NUTRITION);
        return new FoodProfile(
                satiation,
                nutrition,
                hasProtein ? nutrients : 0,
                hasPhytonutrients ? nutrients : 0,
                (hasEssentialFats ? nutrients : 0) + Math.max(0, extraEssentialFats),
                sugarContent,
                alwaysEdible);
    }

    /** Mirrors Item#getInsulinResponse: sugar content multiplied by 4.8, truncated to an int. */
    public int insulinResponse() {
        return (int) (sugarContent * INSULIN_RESPONSE_PER_SUGAR);
    }

    /**
     * Mirrors Item#getSatiation(EntityPlayer): moderate and severe insulin resistance reduce the
     * satiation contributed by a sugary food, but do not stop that food from raising resistance.
     */
    public double satiationFor(SurvivalData data) {
        if (sugarContent <= 0 || data.canMetabolizeFoodSugars()) return satiation;
        return Math.max(0.0D, satiation - (sugarContent < 1_000 ? 1 : sugarContent / 1_000));
    }

    public boolean isEmpty() {
        return equals(EMPTY);
    }
}
