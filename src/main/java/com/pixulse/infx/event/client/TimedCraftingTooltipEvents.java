package com.pixulse.infx.event.client;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.recipe.TimedCraftingMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Adds server-synchronized timed-crafting experience costs to result tooltips.
 *
 * <p>{@code AbstractContainerScreen#getTooltipFromContainerItem} ends in
 * {@code ItemStack#getTooltipLines}, which fires {@link ItemTooltipEvent} after all vanilla lines
 * are appended, so the timed-cost line is added at exactly the same position as the old mixin.
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID, value = Dist.CLIENT)
public final class TimedCraftingTooltipEvents {
    private TimedCraftingTooltipEvents() {}

    @SubscribeEvent
    public static void appendCraftingExperience(ItemTooltipEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        Slot hovered = screen.getSlotUnderMouse();
        if (hovered == null || !(hovered.container instanceof ResultContainer)) {
            return;
        }
        if (!(screen.getMenu() instanceof TimedCraftingMenu timedMenu)
                || hovered.container != timedMenu.infx$resultContainer()) {
            return;
        }
        int cost = timedMenu.infx$experienceCost();
        if (cost <= 0) {
            return;
        }
        event.getToolTip().add(Component.translatable("tooltip.infx.crafting_experience", cost)
                .withStyle(ChatFormatting.YELLOW));
    }
}
