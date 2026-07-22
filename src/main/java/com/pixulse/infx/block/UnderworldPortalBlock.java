package com.pixulse.infx.block;

import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.progression.ProgressionEvents;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.world.UnderworldPortalEvents;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** The dedicated two-way surface between the Overworld and the Underworld. */
public final class UnderworldPortalBlock extends R196PortalBlock {
    public static final BooleanProperty RUNE_GATE = BooleanProperty.create("rune_gate");

    public UnderworldPortalBlock(BlockBehaviour.Properties properties) {
        super(PortalType.UNDERWORLD, properties);
        registerDefaultState(defaultBlockState().setValue(RUNE_GATE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RUNE_GATE);
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(
            ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        Optional<RuneGate> runeGate = findRuneGate(currentLevel, portalEntryPos);
        if (runeGate.isPresent()) {
            return runeTransition(currentLevel, entity, runeGate.get());
        }

        // Worlds saved before portal types were split still contain this one legacy block.
        PortalType legacyType = UnderworldPortalEvents.portalTypeFor(currentLevel, portalEntryPos);
        if (legacyType != null && legacyType != PortalType.UNDERWORLD) {
            UnderworldPortalEvents.replaceConnectedPortal(currentLevel, portalEntryPos, legacyType);
            return UnderworldPortalEvents.portalBlock(legacyType)
                    .getPortalDestination(currentLevel, entity, portalEntryPos);
        }
        return super.getPortalDestination(currentLevel, entity, portalEntryPos);
    }

    private static Optional<RuneGate> findRuneGate(ServerLevel level, BlockPos portal) {
        List<RuneEntry> runes = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(portal.offset(-4, -2, -4), portal.offset(4, 5, 4))) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof RuneStoneBlock)) continue;
            int adjacentFrame = 0;
            for (Direction direction : Direction.values()) {
                if (level.getBlockState(pos.relative(direction)).is(Blocks.OBSIDIAN)) adjacentFrame++;
            }
            if (adjacentFrame >= 2) runes.add(new RuneEntry(pos.immutable(), state));
        }
        if (runes.size() != 4) return Optional.empty();
        boolean adamantium = runes.stream().allMatch(entry -> entry.state().is(ModBlocks.ADAMANTIUM_RUNE_STONE.get()));
        boolean mithril = runes.stream().allMatch(entry -> entry.state().is(ModBlocks.MITHRIL_RUNE_STONE.get()));
        if (!adamantium && !mithril) return Optional.empty();
        runes.sort(Comparator.comparingInt((RuneEntry entry) -> entry.pos().getY())
                .thenComparingInt(entry -> entry.pos().getX())
                .thenComparingInt(entry -> entry.pos().getZ()));
        int signature = 0;
        for (RuneEntry entry : runes) signature = signature << 4 | entry.state().getValue(RuneStoneBlock.RUNE);
        return Optional.of(new RuneGate(
                adamantium ? R196Material.ADAMANTIUM : R196Material.MITHRIL,
                signature));
    }

    public static boolean hasRuneGate(ServerLevel level, BlockPos portal) {
        return findRuneGate(level, portal).isPresent();
    }

    private static TeleportTransition runeTransition(ServerLevel level, Entity entity, RuneGate gate) {
        int orientationGroup = switch (entity.getDirection()) {
            case EAST, NORTH -> 0;
            case WEST, SOUTH -> 1;
            default -> 0;
        };
        BlockPos destination = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            Vec3 offset = runeDestinationOffset(gate.material(), gate.signature(), orientationGroup, attempt);
            int x = Math.clamp((int) Math.round(offset.x), -29_999_000, 29_999_000);
            int z = Math.clamp((int) Math.round(offset.z), -29_999_000, 29_999_000);
            int y = level.dimension().equals(Level.OVERWORLD)
                    ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)
                    : entity.blockPosition().getY();
            BlockPos preferred = new BlockPos(x, y, z);
            if (attempt < 4 && level.getBiome(preferred).is(BiomeTags.IS_OCEAN)) continue;
            destination = findRuneArrival(level, preferred);
            break;
        }
        TeleportTransition.PostTeleportTransition post = TeleportTransition.PLAY_PORTAL_SOUND
                .then(TeleportTransition.PLACE_PORTAL_TICKET)
                .then(Entity::setPortalCooldown)
                .then(arrived -> {
                    if (arrived instanceof ServerPlayer player) {
                        ProgressionEvents.award(player, "runegate", "used_runegate");
                    }
                });
        return new TeleportTransition(
                level,
                Vec3.atBottomCenterOf(destination),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                post);
    }

    public static Vec3 runeDestinationOffset(
            R196Material material, int signature, int orientationGroup, int attempt) {
        long mixed = mix64(Integer.toUnsignedLong(signature)
                ^ (long) orientationGroup * 0x9E3779B97F4A7C15L
                ^ (long) attempt * 0xD1B54A32D192ED03L
                ^ (material == R196Material.ADAMANTIUM ? 0x94D049BB133111EBL : 0x369DEA0F31A53F85L));
        double minimum = material == R196Material.ADAMANTIUM ? 20_000.0 : 2_500.0;
        double span = material == R196Material.ADAMANTIUM ? 20_000.0 : 2_500.0;
        double unit = ((mixed >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
        double radius = minimum + span * unit;
        double angle = Math.floorMod(mixed >>> 32, 65_536L) / 65_536.0 * Math.PI * 2.0;
        return new Vec3(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
    }

    private static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private static BlockPos findRuneArrival(ServerLevel level, BlockPos preferred) {
        int minY = level.getMinY() + 2;
        int maxY = level.getMaxY() - 3;
        int start = Math.clamp(preferred.getY(), minY, maxY);
        for (int delta = 0; delta <= Math.min(128, maxY - minY); delta++) {
            for (int y : new int[]{start + delta, start - delta}) {
                if (y < minY || y > maxY) continue;
                BlockPos feet = new BlockPos(preferred.getX(), y, preferred.getZ());
                if (level.getBlockState(feet).isAir()
                        && level.getBlockState(feet.above()).isAir()
                        && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)) {
                    return feet;
                }
            }
        }
        BlockPos fallback = new BlockPos(preferred.getX(), start, preferred.getZ());
        level.setBlock(fallback.below(), Blocks.OBSIDIAN.defaultBlockState(), 3);
        level.setBlock(fallback, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(fallback.above(), Blocks.AIR.defaultBlockState(), 3);
        return fallback;
    }

    private record RuneEntry(BlockPos pos, BlockState state) {}

    private record RuneGate(R196Material material, int signature) {}
}
