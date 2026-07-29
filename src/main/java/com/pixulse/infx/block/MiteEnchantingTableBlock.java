package com.pixulse.infx.block;

import com.pixulse.infx.screen.menu.MiteEnchantmentMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MiteEnchantingTableBlock extends EnchantingTableBlock {
    private final MiteEnchantmentMenu.Kind kind;

    public MiteEnchantingTableBlock(MiteEnchantmentMenu.Kind kind, BlockBehaviour.Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public MiteEnchantmentMenu.Kind kind() {
        return kind;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(@NonNull BlockState state, Level level, @NonNull BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity table)) return null;
        Component title = table.getDisplayName();
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> new MiteEnchantmentMenu(
                        containerId, inventory, ContainerLevelAccess.create(level, pos), kind),
                title);
    }
}
