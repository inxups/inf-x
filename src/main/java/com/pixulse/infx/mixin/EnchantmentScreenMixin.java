package com.pixulse.infx.mixin;

import com.pixulse.infx.menu.R196EnchantmentMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces vanilla's level-based affordability and tooltip for R196 enchanting menus. */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin {
    @Redirect(
            method = "extractBackground",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I"))
    private int infx$compareRawExperience(LocalPlayer player) {
        EnchantmentMenu menu = ((EnchantmentScreen) (Object) this).getMenu();
        return menu instanceof R196EnchantmentMenu ? player.totalExperience : player.experienceLevel;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void infx$replaceExperienceTooltip(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        EnchantmentScreen screen = (EnchantmentScreen) (Object) this;
        if (!(screen.getMenu() instanceof R196EnchantmentMenu menu)) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        boolean infiniteMaterials = minecraft.player.hasInfiniteMaterials();
        int currencyCount = menu.getGoldCount();

        for (int index = 0; index < menu.costs.length; index++) {
            int experienceCost = menu.costs[index];
            int relativeX = mouseX - (screen.getLeftPos() + 60);
            int relativeY = mouseY - (screen.getTopPos() + 14 + 19 * index);
            if (relativeX < 0 || relativeY < 0 || relativeX >= 108 || relativeY >= 17 || experienceCost <= 0) {
                continue;
            }

            Optional<Holder.Reference<Enchantment>> enchantment = minecraft.level
                    .registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(menu.enchantClue[index]);
            int enchantmentLevel = menu.levelClue[index];
            if (enchantment.isEmpty() || enchantmentLevel < 1) continue;
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(
                            "container.enchant.clue",
                            Enchantment.getFullname(enchantment.get(), enchantmentLevel))
                    .withStyle(ChatFormatting.WHITE));
            if (!infiniteMaterials) {
                tooltip.add(CommonComponents.EMPTY);
                if (minecraft.player.totalExperience < experienceCost) {
                    tooltip.add(Component.translatable(
                                    "container.infx.enchant.experience.requirement", experienceCost)
                            .withStyle(ChatFormatting.RED));
                } else {
                    int currencyCost = index + 1;
                    tooltip.add(Component.translatable(
                                    "container.infx.enchant.currency_cost",
                                    currencyCost,
                                    menu.currency().getDefaultInstance().getHoverName())
                            .withStyle(currencyCount >= currencyCost ? ChatFormatting.GRAY : ChatFormatting.RED));
                    tooltip.add(Component.translatable(
                                    "container.infx.enchant.experience.cost", experienceCost)
                            .withStyle(ChatFormatting.GRAY));
                }
            }

            List<FormattedCharSequence> lines = tooltip.stream()
                    .map(Component::getVisualOrderText)
                    .toList();
            graphics.setTooltipForNextFrame(
                    minecraft.font,
                    lines,
                    DefaultTooltipPositioner.INSTANCE,
                    mouseX,
                    mouseY,
                    true);
            break;
        }
    }
}
