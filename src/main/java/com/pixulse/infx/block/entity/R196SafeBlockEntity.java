package com.pixulse.infx.block.entity;

import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.registry.ModBlockEntityTypes;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** MITE strongbox: player-only inventory; hoppers and other automation get no sided slots. */
public final class R196SafeBlockEntity extends RandomizableContainerBlockEntity
        implements LidBlockEntity, WorldlyContainer {
    private static final int[] NO_SLOTS = new int[0];
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private @Nullable UUID owner;
    private String ownerName = "?";
    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            R196SafeBlockEntity.this.playSound(SoundEvents.CHEST_OPEN);
            R196SafeBlockEntity.this.updateBlockState(state, true);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            R196SafeBlockEntity.this.playSound(SoundEvents.CHEST_CLOSE);
            R196SafeBlockEntity.this.updateBlockState(state, false);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
            level.blockEvent(pos, state.getBlock(), 1, current);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof ChestMenu chestMenu)) {
                return false;
            }
            Container container = chestMenu.getContainer();
            return container == R196SafeBlockEntity.this;
        }
    };

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

    public R196SafeBlock materialBlock() {
        return (R196SafeBlock) getBlockState().getBlock();
    }

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
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            chestLidController.shouldBeOpen(type > 0);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public void startOpen(ContainerUser containerUser) {
        if (!remove && !containerUser.getLivingEntity().isSpectator()) {
            openersCounter.incrementOpeners(
                    containerUser.getLivingEntity(),
                    getLevel(),
                    getBlockPos(),
                    getBlockState(),
                    containerUser.getContainerInteractionRange());
        }
    }

    @Override
    public void stopOpen(ContainerUser containerUser) {
        if (!remove && !containerUser.getLivingEntity().isSpectator()) {
            openersCounter.decrementOpeners(
                    containerUser.getLivingEntity(), getLevel(), getBlockPos(), getBlockState());
        }
    }

    @Override
    public List<ContainerUser> getEntitiesWithContainerOpen() {
        return openersCounter.getEntitiesWithContainerOpen(getLevel(), getBlockPos());
    }

    public void recheckOpen() {
        if (!remove) {
            openersCounter.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
        }
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, R196SafeBlockEntity entity) {
        entity.chestLidController.tickLid();
    }

    @Override
    public float getOpenNess(float partialTick) {
        return chestLidController.getOpenness(partialTick);
    }

    @Override
    public int getContainerSize() { return 27; }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

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

    private void updateBlockState(BlockState state, boolean open) {
        if (level != null) {
            level.setBlock(worldPosition, state.setValue(BarrelBlock.OPEN, open), 3);
        }
    }

    private void playSound(SoundEvent sound) {
        if (level == null) {
            return;
        }
        var direction = getBlockState().getValue(BarrelBlock.FACING).getUnitVec3i();
        double x = worldPosition.getX() + 0.5 + direction.getX() / 2.0;
        double y = worldPosition.getY() + 0.5 + direction.getY() / 2.0;
        double z = worldPosition.getZ() + 0.5 + direction.getZ() / 2.0;
        level.playSound(
                null,
                x,
                y,
                z,
                sound,
                SoundSource.BLOCKS,
                0.5F,
                level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
