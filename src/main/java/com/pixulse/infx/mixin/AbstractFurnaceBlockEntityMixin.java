package com.pixulse.infx.mixin;

import com.pixulse.infx.furnace.FurnaceHeatAccess;
import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import com.pixulse.infx.furnace.FurnaceItemPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements FurnaceHeatAccess {
    @Shadow
    private int litTimeRemaining;

    @Shadow
    private int cookingTimer;

    @Shadow
    private Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed;

    @Unique
    private int infx$currentHeat;

    @Override
    public int infx$currentHeat() {
        return infx$currentHeat;
    }

    @Override
    public void infx$setCurrentHeat(int heat) {
        infx$currentHeat = heat;
    }

    @Override
    public int infx$litTimeRemaining() {
        return litTimeRemaining;
    }

    @Override
    public void infx$setLitTimeRemaining(int ticks) {
        litTimeRemaining = ticks;
    }

    @Override
    public void infx$setCookingTimer(int ticks) {
        cookingTimer = ticks;
    }

    @Override
    public void infx$popAutomationExperience(ServerLevel level) {
        AbstractFurnaceBlockEntity entity = (AbstractFurnaceBlockEntity) (Object) this;
        if (recipesUsed.isEmpty()) {
            return;
        }
        BlockState state = entity.getBlockState();
        Direction facing = state.hasProperty(AbstractFurnaceBlock.FACING)
                ? state.getValue(AbstractFurnaceBlock.FACING)
                : Direction.NORTH;
        entity.getRecipesToAwardAndPopExperience(
                level, Vec3.atCenterOf(entity.getBlockPos().relative(facing)));
        recipesUsed.clear();
        entity.setChanged();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void infx$loadCurrentHeat(ValueInput input, CallbackInfo callback) {
        infx$currentHeat = input.getIntOr("infx_current_heat", 0);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void infx$saveCurrentHeat(ValueOutput output, CallbackInfo callback) {
        output.putInt("infx_current_heat", infx$currentHeat);
    }

    @Inject(method = "getBurnDuration", at = @At("RETURN"), cancellable = true)
    private void infx$enforceFuelHeat(
            FuelValues fuelValues,
            ItemStack fuel,
            CallbackInfoReturnable<Integer> callback) {
        AbstractFurnaceBlockEntity entity = (AbstractFurnaceBlockEntity) (Object) this;
        int maximumHeat = FurnaceHeatPolicy.maximumHeat(entity.getBlockState());
        if (maximumHeat == 0) {
            return;
        }

        int burnTime = callback.getReturnValue();
        int fuelHeat = FurnaceHeatPolicy.fuelHeat(fuel, burnTime);
        int requiredHeat = FurnaceHeatPolicy.requiredHeat(entity.getItem(0));
        if (fuelHeat == 0 || fuelHeat > maximumHeat || fuelHeat < requiredHeat) {
            infx$currentHeat = 0;
            callback.setReturnValue(0);
        } else {
            infx$currentHeat = fuelHeat;
        }
    }

    @Inject(method = "canPlaceItem", at = @At("RETURN"), cancellable = true)
    private void infx$rejectFuelAboveFurnaceCapacity(
            int slot,
            ItemStack stack,
            CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue()) {
            return;
        }
        AbstractFurnaceBlockEntity entity = (AbstractFurnaceBlockEntity) (Object) this;
        if (slot < 2 && !FurnaceItemPolicy.canPlaceItem(entity.getBlockState(), stack)) {
            callback.setReturnValue(false);
            return;
        }
        if (slot != 1 || stack.is(Items.BUCKET)) {
            return;
        }
        int maximumHeat = FurnaceHeatPolicy.maximumHeat(entity.getBlockState());
        if (maximumHeat == 0 || entity.getLevel() == null) {
            return;
        }
        int burnTime = stack.getBurnTime(RecipeType.SMELTING, entity.getLevel().fuelValues());
        if (FurnaceHeatPolicy.fuelHeat(stack, burnTime) > maximumHeat) {
            callback.setReturnValue(false);
        }
    }

    @Redirect(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;assemble(Lnet/minecraft/world/item/crafting/SingleRecipeInput;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack infx$assembleSandBatch(
            AbstractCookingRecipe recipe,
            SingleRecipeInput input,
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity entity) {
        ItemStack ingredient = input.item();
        if (!ingredient.is(Items.SAND) || FurnaceHeatPolicy.maximumHeat(state) == 0) {
            return recipe.assemble(input);
        }
        if (ingredient.getCount() < 4) {
            return ItemStack.EMPTY;
        }

        FurnaceHeatAccess heat = (FurnaceHeatAccess) entity;
        int effectiveHeat = heat.infx$currentHeat();
        if (heat.infx$litTimeRemaining() <= 0) {
            ItemStack fuel = entity.getItem(1);
            int burnTime = fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues());
            effectiveHeat = FurnaceHeatPolicy.fuelHeat(fuel, burnTime);
        }
        if (effectiveHeat == FurnaceHeatPolicy.HEAT_WOOD) {
            return new ItemStack(Blocks.SANDSTONE);
        }
        return effectiveHeat >= FurnaceHeatPolicy.HEAT_COAL
                ? new ItemStack(Blocks.GLASS)
                : ItemStack.EMPTY;
    }

    @Redirect(
            method = "burn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void infx$consumeSandBatch(ItemStack input, int amount) {
        input.shrink(input.is(Items.SAND) ? 4 : amount);
    }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void infx$enforceBurningHeatAndOpenMouth(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity entity,
            CallbackInfo callback) {
        int maximumHeat = FurnaceHeatPolicy.maximumHeat(state);
        if (maximumHeat == 0) {
            return;
        }

        FurnaceHeatAccess heat = (FurnaceHeatAccess) entity;
        if (FurnaceHeatPolicy.isMouthBlocked(level, pos, state)) {
            boolean changed = heat.infx$litTimeRemaining() > 0 || heat.infx$currentHeat() > 0;
            heat.infx$setLitTimeRemaining(0);
            heat.infx$setCookingTimer(0);
            heat.infx$setCurrentHeat(0);
            if (state.getValue(AbstractFurnaceBlock.LIT)) {
                level.setBlock(pos, state.setValue(AbstractFurnaceBlock.LIT, false), 3);
                changed = true;
            }
            if (changed) {
                entity.setChanged();
            }
            callback.cancel();
            return;
        }

        int requiredHeat = FurnaceHeatPolicy.requiredHeat(entity.getItem(0));
        if (heat.infx$litTimeRemaining() > 0 && heat.infx$currentHeat() < requiredHeat) {
            int remaining = heat.infx$litTimeRemaining() - 1;
            heat.infx$setLitTimeRemaining(remaining);
            heat.infx$setCookingTimer(0);
            if (remaining <= 0) {
                heat.infx$setCurrentHeat(0);
                if (state.getValue(AbstractFurnaceBlock.LIT)) {
                    level.setBlock(pos, state.setValue(AbstractFurnaceBlock.LIT, false), 3);
                }
            }
            entity.setChanged();
            callback.cancel();
        }
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void infx$clearExhaustedHeat(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity entity,
            CallbackInfo callback) {
        if (FurnaceHeatPolicy.maximumHeat(state) == 0) {
            return;
        }
        FurnaceHeatAccess heat = (FurnaceHeatAccess) entity;
        if (heat.infx$litTimeRemaining() <= 0 && heat.infx$currentHeat() != 0) {
            heat.infx$setCurrentHeat(0);
            entity.setChanged();
        }
    }
}
