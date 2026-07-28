package com.pixulse.infx.menu;

import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfinityXMenus;
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

public final class MetalAnvilMenu extends ItemCombinerMenu {
    private static final ItemCombinerMenuSlotDefinition SLOTS = ItemCombinerMenuSlotDefinition.create()
            .withSlot(0, 27, 47, stack -> RepairPlan.supportsType(stack))
            .withSlot(1, 76, 47, stack -> RepairPlan.isRepairMaterial(stack.getItem()))
            .withResultSlot(2, 134, 47)
            .build();

    private final MiteMaterial anvilMaterial;
    private final Block expectedBlock;

    private MetalAnvilMenu(
            int containerId,
            Inventory inventory,
            MiteMaterial anvilMaterial,
            ContainerLevelAccess access,
            Block expectedBlock) {
        super(InfinityXMenus.METAL_ANVIL.get(), containerId, inventory, access, SLOTS);
        this.anvilMaterial = anvilMaterial;
        this.expectedBlock = expectedBlock;
    }

    public static MetalAnvilMenu server(
            int containerId,
            Inventory inventory,
            MiteMaterial material,
            ContainerLevelAccess access,
            Block expectedBlock) {
        return new MetalAnvilMenu(containerId, inventory, material, access, expectedBlock);
    }

    public static MetalAnvilMenu client(
            int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int materialId = Math.clamp(buffer.readVarInt(), 0, MiteMaterial.values().length - 1);
        MiteMaterial material = MiteMaterial.values()[materialId];
        Block block = inventory.player.level().getBlockState(pos).getBlock();
        return new MetalAnvilMenu(
                containerId,
                inventory,
                material,
                ContainerLevelAccess.create(inventory.player.level(), pos),
                block);
    }

    public MiteMaterial anvilMaterial() {
        return anvilMaterial;
    }

    @Override
    public void createResult() {
        RepairPlan plan = currentPlan();
        resultSlots.setItem(0, plan.valid() ? plan.output() : ItemStack.EMPTY);
        broadcastChanges();
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasItem) {
        return hasItem && currentPlan().valid();
    }

    @Override
    protected void onTake(Player player, ItemStack carried) {
        RepairPlan plan = currentPlan();
        if (!plan.valid()) {
            return;
        }
        inputSlots.removeItem(0, 1);
        inputSlots.removeItem(1, plan.materialsUsed());
        access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel
                    && level.getBlockEntity(pos) instanceof MetalAnvilBlockEntity anvil) {
                anvil.addDamage(serverLevel, plan.anvilDamage());
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
        return RepairPlan.create(anvilMaterial, inputSlots.getItem(0), inputSlots.getItem(1));
    }
}
