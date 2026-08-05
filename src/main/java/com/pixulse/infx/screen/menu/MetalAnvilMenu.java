package com.pixulse.infx.screen.menu;

import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXMenus;
import com.pixulse.infx.item.repair.RepairPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public final class MetalAnvilMenu extends ItemCombinerMenu {
    private static final ItemCombinerMenuSlotDefinition SLOTS = ItemCombinerMenuSlotDefinition.create()
            .withSlot(0, 27, 47, stack -> !stack.isEmpty())
            .withSlot(1, 76, 47, RepairPlan::isAdditionalItem)
            .withResultSlot(2, 134, 47)
            .build();

    private final InfxMaterial anvilMaterial;
    private final Block expectedBlock;
    private String itemName;

    private MetalAnvilMenu(
            int containerId,
            Inventory inventory,
            InfxMaterial anvilMaterial,
            ContainerLevelAccess access,
            Block expectedBlock) {
        super(InfXMenus.METAL_ANVIL.get(), containerId, inventory, access, SLOTS);
        this.anvilMaterial = anvilMaterial;
        this.expectedBlock = expectedBlock;
    }

    public static MetalAnvilMenu server(
            int containerId,
            Inventory inventory,
            InfxMaterial material,
            ContainerLevelAccess access,
            Block expectedBlock) {
        return new MetalAnvilMenu(containerId, inventory, material, access, expectedBlock);
    }

    public static MetalAnvilMenu client(
            int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int materialId = Math.clamp(buffer.readVarInt(), 0, InfxMaterial.values().length - 1);
        InfxMaterial material = InfxMaterial.values()[materialId];
        Block block = inventory.player.level().getBlockState(pos).getBlock();
        return new MetalAnvilMenu(
                containerId,
                inventory,
                material,
                ContainerLevelAccess.create(inventory.player.level(), pos),
                block);
    }

    public InfxMaterial anvilMaterial() {
        return anvilMaterial;
    }

    public String itemName() {
        return itemName;
    }

    public boolean setItemName(String name) {
        String filteredName = RepairPlan.normalizeName(name);
        if (filteredName == null || filteredName.equals(itemName)) {
            return false;
        }
        itemName = filteredName;
        createResult();
        return true;
    }

    @Override
    public void createResult() {
        RepairPlan plan = currentPlan();
        resultSlots.setItem(0, plan.valid() ? plan.output() : ItemStack.EMPTY);
        broadcastChanges();
    }

    @Override
    protected boolean mayPickup(@NonNull Player player, boolean hasItem) {
        return hasItem && currentPlan().valid();
    }

    @Override
    protected void onTake(@NonNull Player player, @NonNull ItemStack carried) {
        RepairPlan plan = currentPlan(player);
        if (!plan.valid()) {
            return;
        }
        inputSlots.removeItem(0, 1);
        if (plan.materialsUsed() > 0) {
            inputSlots.removeItem(1, plan.materialsUsed());
        } else if (plan.consumesAdditional()) {
            inputSlots.removeItem(1, 1);
        }
        access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel) {
                if (plan.anvilDamage() > 0
                        && level.getBlockEntity(pos) instanceof MetalAnvilBlockEntity anvil) {
                    anvil.addDamage(serverLevel, plan.anvilDamage());
                }
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        });
        createResult();
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(expectedBlock) && state.getBlock() instanceof MetalAnvilBlock;
    }

    private RepairPlan currentPlan() {
        return currentPlan(player);
    }

    private RepairPlan currentPlan(Player player) {
        return RepairPlan.create(
                anvilMaterial,
                player,
                inputSlots.getItem(0),
                inputSlots.getItem(1),
                itemName);
    }
}
