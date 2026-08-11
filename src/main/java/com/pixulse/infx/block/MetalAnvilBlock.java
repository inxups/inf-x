package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.screen.menu.MetalAnvilMenu;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MetalAnvilBlock extends FallingBlock implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty DAMAGE_STAGE = IntegerProperty.create("damage_stage", 0, 2);
    private static final VoxelShape SHAPE_Z = Shapes.or(
            Block.box(2, 0, 2, 14, 4, 14),
            Block.box(4, 4, 3, 12, 5, 13),
            Block.box(6, 5, 4, 10, 10, 12),
            Block.box(3, 10, 0, 13, 16, 16));
    private static final Map<Direction.Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(SHAPE_Z);

    private final InfxMaterial material;
    private final MapCodec<MetalAnvilBlock> codec;

    public MetalAnvilBlock(InfxMaterial material, BlockBehaviour.Properties properties) {
        super(properties);
        this.material = material;
        this.codec = simpleCodec(p -> new MetalAnvilBlock(material, p));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DAMAGE_STAGE, 0));
    }

    public InfxMaterial material() {
        return material;
    }

    public int maximumDamage() {
        return Math.round(1_600 * 31 * material.durabilityMultiplier());
    }

    public int damageStage(int damage) {
        float factor = damage / (float) maximumDamage();
        return factor >= 1.0F ? 3 : factor >= 0.8F ? 2 : factor >= 0.5F ? 1 : 0;
    }

    @Override
    protected @NonNull MapCodec<? extends FallingBlock> codec() {
        return codec;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    public void setPlacedBy(Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity by, @NonNull ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof MetalAnvilBlockEntity anvil) {
            anvil.setDamage(stack.getDamageValue());
            int stage = damageStage(stack.getDamageValue());
            if (stage < 3 && state.getValue(DAMAGE_STAGE) != stage) {
                level.setBlock(pos, state.setValue(DAMAGE_STAGE, stage), 3);
            }
        }
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inventory, ignored) -> MetalAnvilMenu.server(
                            id, inventory, material, net.minecraft.world.inventory.ContainerLevelAccess.create(level, pos), this),
                    Component.translatable("container.infx.metal_anvil"));
            serverPlayer.openMenu(provider, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeVarInt(material.ordinal());
            });
            player.awardStat(Stats.INTERACT_WITH_ANVIL);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new MetalAnvilBlockEntity(pos, state);
    }

    @Override
    protected void tick(@NonNull BlockState state, ServerLevel level, BlockPos pos, @NonNull RandomSource random) {
        if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinY()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            var data = blockEntity instanceof MetalAnvilBlockEntity anvil
                    ? anvil.saveWithoutMetadata(level.registryAccess())
                    : null;
            FallingBlockEntity entity = FallingBlockEntity.fall(level, pos, state);
            entity.blockData = data;
            falling(entity);
        }
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(2.0F, 40);
    }

    @Override
    protected @NonNull List<ItemStack> getDrops(@NonNull BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MetalAnvilBlockEntity anvil) {
            stack.setDamageValue(Math.min(anvil.damage(), stack.getMaxDamage() - 1));
        }
        return List.of(stack);
    }

    @Override
    protected @NonNull VoxelShape getShape(BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPES.get(state.getValue(FACING).getAxis());
    }

    @Override
    protected @NonNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NonNull BlockState mirror(@NonNull BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DAMAGE_STAGE);
    }

    @Override
    protected boolean isPathfindable(@NonNull BlockState state, @NonNull PathComputationType type) {
        return false;
    }

    @Override
    public int getDustColor(BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        return state.getMapColor(level, pos).col;
    }

    @Override
    public net.minecraft.world.damagesource.@NonNull DamageSource getFallDamageSource(Entity entity) {
        return entity.damageSources().anvil(entity);
    }
}
