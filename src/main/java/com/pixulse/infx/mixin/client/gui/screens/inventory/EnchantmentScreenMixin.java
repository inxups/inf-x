package com.pixulse.infx.mixin.client.gui.screens.inventory;

import com.pixulse.infx.screen.menu.InfxEnchantmentMenu;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Uses raw experience for InfX enchanting menus and hides their vanilla option clue tooltip. */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin {
    @Redirect(
            method = "extractBackground",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I"))
    private int infx$compareRawExperience(LocalPlayer player) {
        EnchantmentMenu menu = ((EnchantmentScreen) (Object) this).getMenu();
        return menu instanceof InfxEnchantmentMenu ? player.totalExperience : player.experienceLevel;
    }

    @Redirect(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setComponentTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
    private void infx$hideEnchantmentTooltip(
            GuiGraphicsExtractor graphics, Font font, List<Component> tooltip, int mouseX, int mouseY) {
        if (!(((EnchantmentScreen) (Object) this).getMenu() instanceof InfxEnchantmentMenu)) {
            graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }
    }

    /**
     * INFX conversion inputs (golden apples, water bottles) have no enchantment choice, so the
     * server keeps their clue at -1; without this the client's disabled-slot test would never
     * light up their level options even though the conversion itself is purchasable.
     */
    @Redirect(
            method = "extractBackground",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/inventory/EnchantmentMenu;enchantClue:[I"))
    private int[] infx$conversionEnchantClue(EnchantmentMenu menu) {
        if (!(menu instanceof InfxEnchantmentMenu)) {
            return menu.enchantClue;
        }
        int[] clues = menu.enchantClue.clone();
        for (int index = 0; index < clues.length; index++) {
            if (menu.costs[index] > 0 && clues[index] == -1) {
                clues[index] = 0;
            }
        }
        return clues;
    }
}
