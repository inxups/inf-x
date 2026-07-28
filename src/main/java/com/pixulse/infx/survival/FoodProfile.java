package com.pixulse.infx.survival;

/** R196's two food-energy layers plus long-term nutrient contribution. */
public record FoodProfile(
        double satiation,
        double nutrition,
        int protein,
        int phytonutrients,
        int essentialFats,
        int sugar) {
    public static final FoodProfile EMPTY = new FoodProfile(0, 0, 0, 0, 0, 0);

    public FoodProfile {
        if (satiation < 0 || nutrition < 0 || protein < 0 || phytonutrients < 0
                || essentialFats < 0 || sugar < 0) {
            throw new IllegalArgumentException("Food values cannot be negative");
        }
    }
}
