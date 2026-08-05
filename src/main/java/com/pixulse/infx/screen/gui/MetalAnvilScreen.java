package com.pixulse.infx.screen.gui;

import com.pixulse.infx.network.Network;
import com.pixulse.infx.item.repair.RepairPlan;
import com.pixulse.infx.screen.menu.MetalAnvilMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class MetalAnvilScreen extends ItemCombinerScreen<MetalAnvilMenu> {
    private static final Identifier TEXT_FIELD_SPRITE = Identifier.withDefaultNamespace("container/anvil/text_field");
    private static final Identifier TEXT_FIELD_DISABLED_SPRITE =
            Identifier.withDefaultNamespace("container/anvil/text_field_disabled");
    private static final Identifier ERROR_SPRITE = Identifier.withDefaultNamespace("container/anvil/error");
    private static final Identifier BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/anvil.png");
    private EditBox name;
    private final Player player;

    public MetalAnvilScreen(MetalAnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BACKGROUND);
        this.player = inventory.player;
        this.titleLabelX = 60;
    }

    @Override
    protected void subInit() {
        int xo = (width - imageWidth) / 2;
        int yo = (height - imageHeight) / 2;
        name = new EditBox(font, xo + 62, yo + 24, 103, 12, Component.translatable("container.repair"));
        name.setCanLoseFocus(false);
        name.setTextColor(-1);
        name.setTextColorUneditable(-1);
        name.setInvertHighlightedTextColor(false);
        name.setBordered(false);
        name.setMaxLength(RepairPlan.MAX_NAME_LENGTH);
        name.setResponder(this::onNameChanged);
        name.setValue("");
        name.setEditable(menu.getSlot(0).hasItem());
        addRenderableWidget(name);
    }

    @Override
    protected void setInitialFocus() {
        setInitialFocus(name);
    }

    @Override
    public void resize(int width, int height) {
        String oldEdit = name.getValue();
        init(width, height);
        name.setValue(oldEdit);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            minecraft.player.closeContainer();
            return true;
        }
        return !name.keyPressed(event) && !name.canConsumeInput() ? super.keyPressed(event) : true;
    }

    private void onNameChanged(String value) {
        Slot slot = menu.getSlot(0);
        if (!slot.hasItem()) {
            return;
        }
        String newName = value;
        if (!slot.getItem().has(DataComponents.CUSTOM_NAME)
                && value.equals(slot.getItem().getHoverName().getString())) {
            newName = "";
        }
        if (menu.setItemName(newName)) {
            ClientPacketDistributor.sendToServer(new Network.RenameMetalAnvilPayload(newName));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                menu.getSlot(0).hasItem() ? TEXT_FIELD_SPRITE : TEXT_FIELD_DISABLED_SPRITE,
                leftPos + 59,
                topPos + 20,
                110,
                16);
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor graphics, int xo, int yo) {
        if ((menu.getSlot(0).hasItem() || menu.getSlot(1).hasItem())
                && !menu.getSlot(menu.getResultSlot()).hasItem()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, xo + 99, yo + 45, 28, 21);
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack itemStack) {
        if (slotIndex == 0) {
            name.setValue(itemStack.isEmpty() ? "" : itemStack.getHoverName().getString());
            name.setEditable(!itemStack.isEmpty());
            setFocused(name);
        }
    }
}
