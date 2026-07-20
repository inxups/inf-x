package com.pixulse.infx.mixin;

import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.crafting.CraftingEnvironment;
import com.pixulse.infx.crafting.TimedCraftingEngine;
import com.pixulse.infx.crafting.TimedCraftingMenu;
import com.pixulse.infx.crafting.TimedCraftingState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the same delayed crafting path to a vanilla 3x3 crafting menu. */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin extends AbstractCraftingMenu implements TimedCraftingMenu {
    @Shadow @Final private Player player;
    @Shadow private boolean placingRecipe;

    @Unique private TimedCraftingState infx$state;
    @Unique private SimpleContainerData infx$data;
    @Unique private long infx$lastTick;

    protected CraftingMenuMixin(MenuType<?> menuType, int containerId, int width, int height) {
        super(menuType, containerId, width, height);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void infx$initializeTimedCrafting(
            int containerId,
            Inventory inventory,
            net.minecraft.world.inventory.ContainerLevelAccess access,
            CallbackInfo callback) {
        infx$state = new TimedCraftingState();
        infx$data = new SimpleContainerData(DATA_COUNT);
        infx$lastTick = Long.MIN_VALUE;
        for (int index = 0; index < DATA_COUNT; index++) {
            addDataSlot(DataSlot.forContainer(infx$data, index));
        }
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
    private void infx$resolveRecipes(Container container, CallbackInfo callback) {
        if (container == craftSlots && !placingRecipe && player instanceof ServerPlayer serverPlayer) {
            TimedCraftingEngine.refreshResult(this, serverPlayer, true);
            callback.cancel();
        }
    }

    @Inject(method = "finishPlacingRecipe", at = @At("RETURN"))
    private void infx$finishPlacingRecipe(
            ServerLevel level,
            RecipeHolder<CraftingRecipe> recipe,
            CallbackInfo callback) {
        if (player instanceof ServerPlayer serverPlayer) {
            TimedCraftingEngine.refreshResult(this, serverPlayer, true);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void infx$resetWhenClosed(Player player, CallbackInfo callback) {
        infx$resetTimedCrafting();
        infx$setHasTimedResult(false);
    }

    @Override
    public BenchTier infx$benchTier() {
        // A vanilla crafting table is the generic R196 3x3 tool bench.
        return BenchTier.FLINT;
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
        return infx$state;
    }

    @Override
    public ContainerData infx$craftingData() {
        return infx$data;
    }

    @Override
    public boolean infx$isCraftingContextValid(Player player) {
        return ((CraftingMenu) (Object) this).stillValid(player)
                && CraftingEnvironment.canCraft(player);
    }

    @Override
    public boolean infx$hasTimedResult() {
        return infx$data.get(DATA_TIMED_RESULT) != 0;
    }

    @Override
    public void infx$setHasTimedResult(boolean hasTimedResult) {
        infx$data.set(DATA_TIMED_RESULT, hasTimedResult ? 1 : 0);
    }

    @Override
    public long infx$lastCraftingTick() {
        return infx$lastTick;
    }

    @Override
    public void infx$setLastCraftingTick(long gameTime) {
        infx$lastTick = gameTime;
    }
}
