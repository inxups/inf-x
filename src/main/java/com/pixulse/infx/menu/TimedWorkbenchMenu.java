package com.pixulse.infx.menu;

import java.util.List;

import com.pixulse.infx.block.TieredWorkbenchBlock;
import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.TimedCraftingEngine;
import com.pixulse.infx.crafting.TimedCraftingMenu;
import com.pixulse.infx.crafting.TimedCraftingState;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModMenus;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class TimedWorkbenchMenu extends AbstractCraftingMenu implements TimedCraftingMenu {
    private static final int INVENTORY_START = 10;
    private static final int INVENTORY_END = 46;

    private final ContainerLevelAccess access;
    private final Player player;
    private final BenchTier benchTier;
    private final Block expectedBlock;
    private final TimedCraftingState craftingState = new TimedCraftingState();
    private final SimpleContainerData craftingData = new SimpleContainerData(DATA_COUNT);
    private long lastCraftingTick = Long.MIN_VALUE;

    private TimedWorkbenchMenu(
            int containerId,
            Inventory inventory,
            BenchTier benchTier,
            ContainerLevelAccess access,
            Block expectedBlock) {
        super(menuType(benchTier), containerId, 3, 3);
        this.access = access;
        this.player = inventory.player;
        this.benchTier = benchTier;
        this.expectedBlock = expectedBlock;
        this.addResultSlot(player, 124, 35);
        this.addCraftingGridSlots(30, 17);
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlots(craftingData);
    }

    public static TimedWorkbenchMenu server(
            int containerId,
            Inventory inventory,
            BenchTier benchTier,
            ContainerLevelAccess access,
            Block expectedBlock) {
        return new TimedWorkbenchMenu(containerId, inventory, benchTier, access, expectedBlock);
    }

    public static TimedWorkbenchMenu client(
            int containerId,
            Inventory inventory,
            BenchTier benchTier,
            RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        Block expected = benchTier == BenchTier.FLINT
                ? ModBlocks.FLINT_WORKBENCH.get()
                : ModBlocks.COPPER_WORKBENCH.get();
        return new TimedWorkbenchMenu(
                containerId,
                inventory,
                benchTier,
                ContainerLevelAccess.create(inventory.player.level(), pos),
                expected);
    }

    private static MenuType<TimedWorkbenchMenu> menuType(BenchTier tier) {
        return tier == BenchTier.FLINT ? ModMenus.FLINT_WORKBENCH.get() : ModMenus.COPPER_WORKBENCH.get();
    }

    @Override
    public void slotsChanged(Container container) {
        if (player instanceof ServerPlayer serverPlayer) {
            TimedCraftingEngine.refreshResult(this, serverPlayer, true);
        }
    }

    @Override
    public void removed(Player player) {
        infx$resetTimedCrafting();
        super.removed(player);
        resultSlots.clearContent();
        access.execute((level, pos) -> clearContainer(player, craftSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, expectedBlock)
                && access.evaluate((level, pos) -> !TieredWorkbenchBlock.isObstructed(level, pos), false);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return original;
        }

        ItemStack stack = slot.getItem();
        original = stack.copy();
        if (slotIndex == 0) {
            return ItemStack.EMPTY;
        }
        if (slotIndex >= INVENTORY_START && slotIndex < INVENTORY_END) {
            if (!moveItemStackTo(stack, 1, INVENTORY_START, false)) {
                if (slotIndex < 37) {
                    if (!moveItemStackTo(stack, 37, INVENTORY_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, INVENTORY_START, 37, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
        return target.container != resultSlots && super.canTakeItemForPickAll(carried, target);
    }

    @Override
    public Slot getResultSlot() {
        return slots.get(0);
    }

    @Override
    public List<Slot> getInputGridSlots() {
        return slots.subList(1, 10);
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected Player owner() {
        return player;
    }

    @Override
    public BenchTier infx$benchTier() {
        return benchTier;
    }

    @Override
    public CraftingContainer infx$craftingContainer() {
        return craftSlots;
    }

    @Override
    public ResultContainer infx$resultContainer() {
        return resultSlots;
    }

    @Override
    public TimedCraftingState infx$craftingState() {
        return craftingState;
    }

    @Override
    public ContainerData infx$craftingData() {
        return craftingData;
    }

    @Override
    public boolean infx$isCraftingContextValid(Player player) {
        return stillValid(player);
    }

    @Override
    public boolean infx$hasTimedResult() {
        return craftingData.get(DATA_TIMED_RESULT) != 0;
    }

    @Override
    public void infx$setHasTimedResult(boolean hasTimedResult) {
        craftingData.set(DATA_TIMED_RESULT, hasTimedResult ? 1 : 0);
    }

    @Override
    public long infx$lastCraftingTick() {
        return lastCraftingTick;
    }

    @Override
    public void infx$setLastCraftingTick(long gameTime) {
        lastCraftingTick = gameTime;
    }
}
