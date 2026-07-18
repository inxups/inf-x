package com.pixulse.infx.block.entity;

import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class MetalAnvilBlockEntity extends BlockEntity {
    private int damage;

    public MetalAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.METAL_ANVIL.get(), pos, state);
    }

    public int damage() {
        return damage;
    }

    public void setDamage(int damage) {
        MetalAnvilBlock block = (MetalAnvilBlock) getBlockState().getBlock();
        this.damage = Math.clamp(damage, 0, block.maximumDamage());
        setChanged();
    }

    public void addDamage(ServerLevel level, int amount) {
        if (amount <= 0 || !(getBlockState().getBlock() instanceof MetalAnvilBlock block)) {
            return;
        }
        damage = (int) Math.min(block.maximumDamage(), (long) damage + amount);
        int stage = block.damageStage(damage);
        if (stage >= 3) {
            level.removeBlock(worldPosition, false);
            return;
        }
        BlockState state = getBlockState();
        if (state.getValue(MetalAnvilBlock.DAMAGE_STAGE) != stage) {
            level.setBlock(worldPosition, state.setValue(MetalAnvilBlock.DAMAGE_STAGE, stage), 3);
        }
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        damage = input.getIntOr("Damage", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Damage", damage);
    }
}
