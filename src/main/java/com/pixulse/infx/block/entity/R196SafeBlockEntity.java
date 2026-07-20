package com.pixulse.infx.block.entity;

import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.registry.ModBlockEntityTypes;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public @Nullable UUID owner() { return owner; }
    public String ownerName() { return ownerName; }
    public boolean isOwner(Player player) { return owner != null && owner.equals(player.getUUID()); }
    public boolean isUnowned() { return owner == null; }
    public boolean isPortableTo(Player player) { return isUnowned() || isOwner(player); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (owner != null) tag.putString("Owner", owner.toString());
        tag.putString("OwnerName", ownerName);
        return tag;
    }

    @Override
    public boolean canOpen(Player player) {
        return (player.hasInfiniteMaterials() || isPortableTo(player)) && super.canOpen(player);
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
