package com.pixulse.infx.block;

import com.pixulse.infx.menu.R196EnchantmentMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class R196EnchantingTableBlock extends EnchantingTableBlock {
    private final R196EnchantmentMenu.Kind kind;

    public R196EnchantingTableBlock(R196EnchantmentMenu.Kind kind, BlockBehaviour.Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public R196EnchantmentMenu.Kind kind() {
        return kind;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (TieredWorkbenchBlock.isObstructed(level, pos)) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.infx.enchanting_table_obstructed"));
            }
            return InteractionResult.FAIL;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity table)) return null;
        Component title = table.getDisplayName();
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> new R196EnchantmentMenu(
                        containerId, inventory, ContainerLevelAccess.create(level, pos), kind),
                title);
    }
}
