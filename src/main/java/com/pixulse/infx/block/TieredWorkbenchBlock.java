package com.pixulse.infx.block;

import com.pixulse.infx.crafting.BenchTier;
import com.pixulse.infx.menu.TimedWorkbenchMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class TieredWorkbenchBlock extends Block {
    private final BenchTier tier;
    private final Component title;

    protected TieredWorkbenchBlock(BenchTier tier, String titleKey, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        this.title = Component.translatable(titleKey);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult) {
        if (isObstructed(level, pos)) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.infx.workbench_obstructed"));
            }
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            player.openMenu(state.getMenuProvider(level, pos), pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> TimedWorkbenchMenu.server(
                        containerId,
                        inventory,
                        tier,
                        ContainerLevelAccess.create(level, pos),
                        this),
                title);
    }

    public static boolean isObstructed(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isCollisionShapeFullBlock(level, above);
    }
}
