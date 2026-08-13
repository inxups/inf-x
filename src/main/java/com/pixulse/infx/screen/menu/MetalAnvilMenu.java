package com.pixulse.infx.screen.menu;

import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXMenus;
import com.pixulse.infx.item.repair.EnchantPlan;
import com.pixulse.infx.item.repair.RepairPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MetalAnvilMenu extends ItemCombinerMenu {
    public static final int MAX_NAME_LENGTH = 50;
    private static final ItemCombinerMenuSlotDefinition SLOTS = ItemCombinerMenuSlotDefinition.create()
            .withSlot(0, 27, 47, MetalAnvilMenu::acceptsInput)
            .withSlot(1, 76, 47, MetalAnvilMenu::acceptsAddition)
            .withResultSlot(2, 134, 47)
            .build();

    private static boolean acceptsInput(ItemStack stack) {
        return RepairPlan.supportsType(stack) || stack.is(Items.ENCHANTED_BOOK);
    }

    private static boolean acceptsAddition(ItemStack stack) {
        return RepairPlan.isRepairMaterial(stack.getItem()) || stack.is(Items.ENCHANTED_BOOK);
    }

    private final InfxMaterial anvilMaterial;
    private final Block expectedBlock;
    private @Nullable String itemName;

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

    @Override
    public void createResult() {
        resultSlots.setItem(0, computeResult());
        broadcastChanges();
    }

    @Override
    protected boolean mayPickup(@NonNull Player player, boolean hasItem) {
        return hasItem && !computeResult().isEmpty();
    }

    @Override
    protected void onTake(@NonNull Player player, @NonNull ItemStack carried) {
        RepairPlan plan = currentPlan();
        EnchantPlan enchant = EnchantPlan.create(inputSlots.getItem(0), inputSlots.getItem(1));
        inputSlots.removeItem(0, 1);
        ItemStack addition = inputSlots.getItem(1);
        if (addition.is(Items.ENCHANTED_BOOK)) {
            inputSlots.setItem(1, ItemStack.EMPTY);
        } else {
            inputSlots.removeItem(1, plan.materialsUsed());
        }
        long wear = (long) plan.anvilDamage() + enchant.wear();
        access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel
                    && level.getBlockEntity(pos) instanceof MetalAnvilBlockEntity anvil) {
                anvil.addDamage(serverLevel, (int) Math.min(Integer.MAX_VALUE, wear));
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        });
        createResult();
    }

    /** Free naming: applies, changes or clears the custom name, optionally combined with repair. */
    public boolean setItemName(String name) {
        String validated = validateName(name);
        if (validated == null || validated.equals(itemName)) {
            return false;
        }
        itemName = validated;
        createResult();
        return true;
    }

    private static @Nullable String validateName(String name) {
        String filtered = StringUtil.filterText(name);
        return filtered.length() <= MAX_NAME_LENGTH ? filtered : null;
    }

    private ItemStack computeResult() {
        ItemStack input = inputSlots.getItem(0);
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        RepairPlan plan = currentPlan();
        ItemStack result = plan.valid() ? plan.output() : input.copy();
        boolean changed = plan.valid();
        EnchantPlan enchant = EnchantPlan.create(result, inputSlots.getItem(1));
        if (enchant.valid()) {
            result = enchant.output();
            changed = true;
        }
        boolean renamed = false;
        if (itemName != null) {
            if (!StringUtil.isBlank(itemName)) {
                if (!itemName.equals(input.getHoverName().getString())) {
                    result.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
                    renamed = true;
                }
            } else if (input.has(DataComponents.CUSTOM_NAME)) {
                result.remove(DataComponents.CUSTOM_NAME);
                renamed = true;
            }
        }
        return changed || renamed ? result : ItemStack.EMPTY;
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(expectedBlock) && state.getBlock() instanceof MetalAnvilBlock;
    }

    private RepairPlan currentPlan() {
        return RepairPlan.create(anvilMaterial, inputSlots.getItem(0), inputSlots.getItem(1));
    }
}
