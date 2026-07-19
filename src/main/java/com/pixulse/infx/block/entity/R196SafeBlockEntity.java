package com.pixulse.infx.block.entity;

import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.registry.ModBlockEntityTypes;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class R196SafeBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private @Nullable UUID owner;
    private String ownerName = "?";

    public R196SafeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.SAFE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items);
        if (owner != null) output.putString("Owner", owner.toString());
        output.putString("OwnerName", ownerName);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(input)) ContainerHelper.loadAllItems(input, items);
        owner = input.getString("Owner").flatMap(value -> {
            try {
                return java.util.Optional.of(UUID.fromString(value));
            } catch (IllegalArgumentException ignored) {
                return java.util.Optional.empty();
            }
        }).orElse(null);
        ownerName = input.getString("OwnerName").orElse("?");
    }

    public void setOwner(Player player) {
        owner = player.getUUID();
        ownerName = player.getScoreboardName();
        setChanged();
    }

    public @Nullable UUID owner() { return owner; }
    public String ownerName() { return ownerName; }
    public boolean isOwner(Player player) { return owner != null && owner.equals(player.getUUID()); }
    public boolean isUnowned() { return owner == null; }

    @Override
    public boolean canOpen(Player player) {
        return (player.hasInfiniteMaterials() || isUnowned() || isOwner(player)) && super.canOpen(player);
    }

    @Override
    public int getContainerSize() { return 27; }

    @Override
    protected NonNullList<ItemStack> getItems() { return items; }

    @Override
    protected void setItems(NonNullList<ItemStack> items) { this.items = items; }

    @Override
    protected Component getDefaultName() {
        String path = getBlockState().getBlock() instanceof R196SafeBlock safe
                ? safe.material().path()
                : "metal";
        return Component.translatable("container.infx." + path + "_safe");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }
}
