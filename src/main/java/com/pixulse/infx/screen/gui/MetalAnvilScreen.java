package com.pixulse.infx.screen.gui;

import com.pixulse.infx.network.Network;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class MetalAnvilScreen extends ItemCombinerScreen<MetalAnvilMenu> {
    private static final Identifier BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/anvil.png");
    private static final Identifier TEXT_FIELD_SPRITE =
            Identifier.withDefaultNamespace("container/anvil/text_field");
    private static final Identifier TEXT_FIELD_DISABLED_SPRITE =
            Identifier.withDefaultNamespace("container/anvil/text_field_disabled");
    private EditBox name;

    public MetalAnvilScreen(MetalAnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BACKGROUND);
        this.titleLabelX = 60;
    }

    @Override
    protected void subInit() {
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        this.name = new EditBox(this.font, xo + 62, yo + 24, 103, 12, Component.translatable("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setInvertHighlightedTextColor(false);
        this.name.setBordered(false);
        this.name.setMaxLength(MetalAnvilMenu.MAX_NAME_LENGTH);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addRenderableWidget(this.name);
        this.name.setEditable(this.menu.getSlot(0).hasItem());
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.name);
    }

    @Override
    public void resize(int width, int height) {
        String oldEdit = this.name.getValue();
        this.init(width, height);
        this.name.setValue(oldEdit);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (event.isEscape()) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return !this.name.keyPressed(event) && !this.name.canConsumeInput() ? super.keyPressed(event) : true;
    }

    private void onNameChanged(String newName) {
        if (!this.menu.getSlot(0).hasItem()) {
            return;
        }
        ItemStack input = this.menu.getSlot(0).getItem();
        String filtered = newName;
        if (!input.has(DataComponents.CUSTOM_NAME) && newName.equals(input.getHoverName().getString())) {
            filtered = "";
        }
        if (this.menu.setItemName(filtered)) {
            this.minecraft.player.connection.send(new Network.MetalAnvilRenamePayload(filtered));
        }
    }

    @Override
    public void extractBackground(
            @NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                this.menu.getSlot(0).hasItem() ? TEXT_FIELD_SPRITE : TEXT_FIELD_DISABLED_SPRITE,
                this.leftPos + 59,
                this.topPos + 20,
                110,
                16);
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor graphics, int xo, int yo) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack itemStack) {
        if (slotIndex == 0) {
            this.name.setValue(itemStack.isEmpty() ? "" : itemStack.getHoverName().getString());
            this.name.setEditable(!itemStack.isEmpty());
            this.setFocused(this.name);
        }
    }
}
