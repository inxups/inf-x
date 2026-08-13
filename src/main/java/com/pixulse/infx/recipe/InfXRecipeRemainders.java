package com.pixulse.infx.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.crafting.CraftingInput;

/**
 * Crafting remainders shared by the InfX returning-container recipes. A
 * liquid bowl (milk/water bowl, or any ingredient with a use remainder)
 * consumed by such a recipe leaves its empty container behind, matching
 * how filled InfX buckets already behave through their craft remainder.
 */
final class InfXRecipeRemainders {
    private InfXRecipeRemainders() {}

    static NonNullList<ItemStack> forInput(CraftingInput input) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < remainders.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            UseRemainder useRemainder = stack.get(DataComponents.USE_REMAINDER);
            ItemStackTemplate template = useRemainder != null
                    ? useRemainder.convertInto()
                    : stack.getItem().getCraftingRemainder();
            if (template != null) {
                remainders.set(slot, template.create());
            }
        }
        return remainders;
    }
}
