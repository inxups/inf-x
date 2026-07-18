package com.pixulse.infx.block;

import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.world.Underworld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class UnderworldPortalBlock extends NetherPortalBlock {
    private static final String RETURN_POS = "infx_underworld_return_pos";

    public UnderworldPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        return state;
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(
            ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        boolean returning = currentLevel.dimension() == Underworld.LEVEL;
        var targetDimension = returning ? Level.OVERWORLD : Underworld.LEVEL;
        ServerLevel targetLevel = currentLevel.getServer().getLevel(targetDimension);
        if (targetLevel == null) {
            return null;
        }

        BlockPos preferred;
        if (returning) {
            preferred = entity.getPersistentData()
                    .getLong(RETURN_POS)
                    .map(BlockPos::of)
                    .orElse(BlockPos.containing(entity.position()));
        } else {
            preferred = BlockPos.containing(entity.position());
            entity.getPersistentData().putLong(RETURN_POS, preferred.asLong());
        }

        BlockPos interior = createArrivalPortal(targetLevel, preferred);
        TeleportTransition.PostTeleportTransition post = TeleportTransition.PLAY_PORTAL_SOUND
                .then(TeleportTransition.PLACE_PORTAL_TICKET)
                .then(Entity::setPortalCooldown);
        return new TeleportTransition(
                targetLevel,
                Vec3.atBottomCenterOf(interior),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                post);
    }

    public BlockPos createArrivalPortal(ServerLevel level, BlockPos preferred) {
        return buildPortal(level, findSafePosition(level, preferred));
    }

    private static BlockPos findSafePosition(ServerLevel level, BlockPos preferred) {
        int minY = level.getMinY() + 2;
        int maxY = level.getMaxY() - 5;
        int preferredY = Math.clamp(preferred.getY(), minY, maxY);
        BlockPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos candidate = new BlockPos(preferred.getX() + dx, y, preferred.getZ() + dz);
                    if (isSafe(level, candidate)) {
                        int distance = Math.abs(y - preferredY) + Math.abs(dx) + Math.abs(dz);
                        if (distance < bestDistance) {
                            best = candidate;
                            bestDistance = distance;
                        }
                    }
                }
            }
        }
        return best != null ? best : new BlockPos(preferred.getX(), preferredY, preferred.getZ());
    }

    private static boolean isSafe(ServerLevel level, BlockPos feet) {
        if (!level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)) {
            return false;
        }
        for (int x = -1; x <= 2; x++) {
            for (int y = 0; y <= 3; y++) {
                if (!level.getBlockState(feet.offset(x, y, 0)).isAir()) {
                    return false;
                }
            }
        }
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                if (!level.getBlockState(feet.offset(x, y, -1)).isAir()
                        || !level.getBlockState(feet.offset(x, y, 1)).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockPos buildPortal(ServerLevel level, BlockPos feet) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState portal = ModBlocks.UNDERWORLD_PORTAL.get().defaultBlockState().setValue(AXIS, Direction.Axis.X);
        int z = feet.getZ();
        for (int x = -1; x <= 2; x++) {
            for (int platformZ = -1; platformZ <= 1; platformZ++) {
                level.setBlock(new BlockPos(feet.getX() + x, feet.getY() - 1, z + platformZ), obsidian, 3);
            }
            level.setBlock(new BlockPos(feet.getX() + x, feet.getY() + 3, z), obsidian, 3);
        }
        for (int y = 0; y < 3; y++) {
            level.setBlock(new BlockPos(feet.getX() - 1, feet.getY() + y, z), obsidian, 3);
            level.setBlock(new BlockPos(feet.getX() + 2, feet.getY() + y, z), obsidian, 3);
            for (int x = 0; x < 2; x++) {
                BlockPos portalPos = new BlockPos(feet.getX() + x, feet.getY() + y, z);
                level.setBlock(portalPos, portal, 18);
                level.setBlock(portalPos.relative(Direction.SOUTH), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(portalPos.relative(Direction.NORTH), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return feet.relative(Direction.SOUTH);
    }
}
