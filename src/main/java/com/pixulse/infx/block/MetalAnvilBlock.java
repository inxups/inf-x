package com.pixulse.infx.block;

import com.mojang.serialization.MapCodec;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.menu.MetalAnvilMenu;
import java.util.List;
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
import org.jspecify.annotations.Nullable;

public final class MetalAnvilBlock extends FallingBlock implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty DAMAGE_STAGE = IntegerProperty.create("damage_stage", 0, 2);
    private static final VoxelShape SHAPE_X = Shapes.or(
            Block.box(2, 0, 2, 14, 4, 14),
            Block.box(4, 4, 3, 12, 5, 13),
            Block.box(6, 5, 4, 10, 10, 12),
            Block.box(3, 10, 0, 13, 16, 16));
    private static final VoxelShape SHAPE_Z = Shapes.rotateHorizontalAxis(SHAPE_X).get(Direction.Axis.Z);

    private final R196Material material;
    private final MapCodec<MetalAnvilBlock> codec;

    public MetalAnvilBlock(R196Material material, BlockBehaviour.Properties properties) {
        super(properties);
        this.material = material;
        this.codec = simpleCodec(p -> new MetalAnvilBlock(material, p));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DAMAGE_STAGE, 0));
    }

    public R196Material material() {
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
    protected MapCodec<? extends FallingBlock> codec() {
        return codec;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof MetalAnvilBlockEntity anvil) {
            anvil.setDamage(stack.getDamageValue());
            int stage = damageStage(stack.getDamageValue());
            if (stage < 3 && state.getValue(DAMAGE_STAGE) != stage) {
                level.setBlock(pos, state.setValue(DAMAGE_STAGE, stage), 3);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MetalAnvilBlockEntity(pos, state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
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
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MetalAnvilBlockEntity anvil) {
            stack.setDamageValue(Math.min(anvil.damage(), stack.getMaxDamage() - 1));
        }
        return List.of(stack);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DAMAGE_STAGE);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getMapColor(level, pos).col;
    }

    @Override
    public net.minecraft.world.damagesource.DamageSource getFallDamageSource(Entity entity) {
        return entity.damageSources().anvil(entity);
    }
}
