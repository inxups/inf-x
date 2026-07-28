package com.pixulse.infx.world;

import com.pixulse.infx.entity.MonsterTactics;
import com.pixulse.infx.player.ProgressionEvents;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.food.SurvivalEvents;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** R196 bed entry checks, sustained rest, cooperative fast-forward and sleep ambushes. */
public final class BedEvents {
    private static final double BED_HORIZONTAL_HOSTILE_RANGE = 8.0D;
    private static final double BED_VERTICAL_HOSTILE_RANGE = 5.0D;
    private static final int MAX_AMBUSH_RANGE = 64;
    private static final int AMBUSH_PATH_TAIL = 8;
    private static final int AMBUSH_ROLL_DENOMINATOR = 1_000;
    private static final Player.BedSleepingProblem NOT_SHELTERED = problem("message.infx.bed.not_sheltered");
    private static final Player.BedSleepingProblem TOO_HUNGRY = problem("message.infx.bed.too_hungry");
    private static final Player.BedSleepingProblem POISONED = problem("message.infx.bed.poisoned");
    private static final Player.BedSleepingProblem MOBS_DIGGING = problem("message.infx.bed.mobs_digging");
    private static final Map<ServerPlayer, BlockPos> BED_ATTEMPTS = new WeakHashMap<>();
    private static final Set<ServerPlayer> DEFERRED_RESPAWNS =
            Collections.newSetFromMap(new WeakHashMap<ServerPlayer, Boolean>());
    private static final Set<ServerPlayer> APPLYING_RESPAWN =
            Collections.newSetFromMap(new WeakHashMap<ServerPlayer, Boolean>());

    private BedEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(BedEvents::onBedUse);
        gameBus.addListener(BedEvents::deferBedRespawn);
        gameBus.addListener(BedEvents::canStartSleeping);
        gameBus.addListener(BedEvents::canContinueSleeping);
        gameBus.addListener(BedEvents::tickLevel);
    }

    private static void onBedUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockState state = level.getBlockState(event.getPos());
        if (!state.is(BlockTags.BEDS)) return;

        if (!level.dimension().equals(Level.OVERWORLD)) {
            event.getEntity().sendOverlayMessage(Component.translatable("message.infx.bed.unsafe_dimension"));
            event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
            event.setCanceled(true);
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            BED_ATTEMPTS.put(player, bedHead(event.getPos(), state));
        }
    }

    /**
     * Modern ServerPlayer writes a bed respawn before CanPlayerSleepEvent has decided whether
     * R196's additional checks pass. Delay that one write and apply it only after a successful
     * bed entry, so a poisoned or exposed player cannot accidentally claim the bed.
     */
    private static void deferBedRespawn(PlayerSetSpawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || APPLYING_RESPAWN.contains(player)) return;
        BlockPos attemptedBed = BED_ATTEMPTS.get(player);
        if (attemptedBed == null
                || event.getNewSpawn() == null
                || !event.getSpawnLevel().equals(Level.OVERWORLD)
                || !event.getNewSpawn().equals(attemptedBed)) {
            return;
        }
        event.setCanceled(true);
        DEFERRED_RESPAWNS.add(player);
    }

    private static void canStartSleeping(CanPlayerSleepEvent event) {
        ServerPlayer player = event.getEntity();
        boolean deferredRespawn = DEFERRED_RESPAWNS.remove(player);
        BED_ATTEMPTS.remove(player);
        if (!(player.level() instanceof ServerLevel level)
                || !level.dimension().equals(Level.OVERWORLD)
                || !event.getState().is(BlockTags.BEDS)) {
            return;
        }

        Player.BedSleepingProblem vanillaProblem = event.getVanillaProblem();
        if (!Objects.equals(event.getProblem(), vanillaProblem)) return;
        if (mustKeepVanillaFailure(vanillaProblem)) return;

        Player.BedSleepingProblem problem = entryProblem(level, player, event.getPos());
        event.setProblem(problem);
        if (problem == null && deferredRespawn) setBedRespawn(player, event.getPos());
    }

    private static void canContinueSleeping(CanContinueSleepingEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || !level.dimension().equals(Level.OVERWORLD)
                || !isSleepingInBed(player)) {
            return;
        }
        BlockPos bed = player.getSleepingPos().orElseThrow();
        if (player.getData(com.pixulse.infx.registry.ModAttachments.SURVIVAL).isStarving()) {
            event.setContinueSleeping(false);
            player.sendOverlayMessage(Component.translatable("message.infx.bed.wake_hungry"));
            return;
        }
        if (hasReachableHostile(level, bed) || hasDiggingZombie(level, bed)) {
            event.setContinueSleeping(false);
            player.sendOverlayMessage(Component.translatable("message.infx.bed.wake_mobs"));
            return;
        }
        // Override the modern daytime-only BedRule; R196 permits resting at any hour.
        event.setContinueSleeping(true);
    }

    private static void tickLevel(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD)) return;
        List<ServerPlayer> players = participatingPlayers(level);
        if (players.isEmpty() || !allDeeplySleepingInBeds(players)) return;
        if (!BedRules.isFastForwardWindow(level.getOverworldClockTime())) return;
        // Blood moons block the skip itself, but players remain in bed and retain the normal rest benefits.
        if (!MoonPhase.at(level).allowsSleep()) return;
        if (!level.getGameRules().get(GameRules.ADVANCE_TIME)) {
            wakeSleepingPlayers(players);
            return;
        }

        runSleepTicks(level, players, BedRules.ticksUntilSunrise(level.getOverworldClockTime()));
    }

    private static void runSleepTicks(ServerLevel level, List<ServerPlayer> players, int requestedTicks) {
        int ticksSlept = 0;
        boolean interrupted = false;
        while (ticksSlept < requestedTicks && !interrupted) {
            ticksSlept++;
            for (ServerPlayer player : players) {
                if (!player.isAlive() || !player.isSleeping() || !SurvivalEvents.tickSleepingMetabolism(player)) {
                    if (player.isSleeping()) {
                        player.stopSleepInBed(false, true);
                        player.sendOverlayMessage(Component.translatable("message.infx.bed.wake_hungry"));
                    }
                    interrupted = true;
                    break;
                }
            }
            if (!interrupted
                    && level.getRandom().nextInt(AMBUSH_ROLL_DENOMINATOR) == 0
                    && trySleepingAmbush(level, players)) {
                interrupted = true;
            }
        }

        if (ticksSlept >= BedRules.EFFECT_CLEAR_TICKS) {
            for (ServerPlayer player : players) {
                if (player.isSleeping()) player.removeAllEffects();
            }
        }
        if (!interrupted && ticksSlept == requestedTicks && ticksSlept >= BedRules.WELL_RESTED_TICKS) {
            for (ServerPlayer player : players) {
                if (player.isSleeping()) ProgressionEvents.award(player, "well_rested", "slept_6000_ticks");
            }
        }
        if (ticksSlept > 0) {
            int elapsedTicks = ticksSlept;
            level.registryAccess()
                    .get(WorldClocks.OVERWORLD)
                    .ifPresent(clock -> level.clockManager().addTicks(clock, elapsedTicks));
        }
        wakeSleepingPlayers(players);
    }

    private static boolean trySleepingAmbush(ServerLevel level, List<ServerPlayer> players) {
        List<ServerPlayer> sleepers = players.stream().filter(ServerPlayer::isSleeping).toList();
        if (sleepers.isEmpty()) return false;
        ServerPlayer player = sleepers.get(level.getRandom().nextInt(sleepers.size()));
        List<Mob> mobs = nearbyAmbushers(level, player);
        Set<Integer> tried = new HashSet<>();
        for (int attempt = 0; attempt < Math.min(mobs.size(), 16); attempt++) {
            int index = level.getRandom().nextInt(mobs.size());
            if (!tried.add(index)) continue;
            if (wakeForAmbush(mobs.get(index), player, MAX_AMBUSH_RANGE)) return true;
        }

        Mob spawned = spawnSleepingAmbusher(level, player);
        return spawned != null && wakeForAmbush(spawned, player, 32);
    }

    private static List<Mob> nearbyAmbushers(ServerLevel level, ServerPlayer player) {
        List<Mob> mobs = List.of();
        for (int distance = 16; distance <= MAX_AMBUSH_RANGE; distance += 16) {
            mobs = level.getEntitiesOfClass(
                    Mob.class,
                    player.getBoundingBox().inflate(distance, distance / 4.0D, distance),
                    mob -> mob.isAlive() && mob instanceof Enemy && !(mob instanceof EnderMan));
            if (mobs.size() >= 16) break;
        }
        return mobs;
    }

    private static Mob spawnSleepingAmbusher(ServerLevel level, ServerPlayer player) {
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
            int distance = 8 + level.getRandom().nextInt(41);
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
            int minimumY = Math.max(level.getMinY() + 1, player.blockPosition().getY() - 8);
            int maximumY = Math.min(level.getMaxY() - 2, player.blockPosition().getY() + 8);
            for (int y = minimumY; y <= maximumY; y++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!isAmbushSpawnPosition(level, pos)) continue;
                var zombie = ModEntityTypes.R196_ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
                if (zombie == null) return null;
                zombie.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
                zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
                if (!level.noCollision(zombie) || pathToSleepingPlayer(zombie, player, 32) == null) continue;
                if (level.addFreshEntity(zombie)) return zombie;
            }
        }
        return null;
    }

    private static boolean isAmbushSpawnPosition(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)
                || !level.getBlockState(pos).isAir()
                || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private static boolean wakeForAmbush(Mob mob, ServerPlayer player, int maxPathLength) {
        Path path = pathToSleepingPlayer(mob, player, maxPathLength);
        if (path == null) return false;
        int nodeIndex = Math.max(0, path.getNodeCount() - AMBUSH_PATH_TAIL);
        BlockPos approach = path.getNodePos(nodeIndex);
        mob.teleportTo(approach.getX() + 0.5D, approach.getY(), approach.getZ() + 0.5D);
        mob.setTarget(player);
        player.stopSleepInBed(false, true);
        player.sendOverlayMessage(Component.translatable("message.infx.bed.wake_mobs"));
        return true;
    }

    private static Path pathToSleepingPlayer(Mob mob, ServerPlayer player, int maxPathLength) {
        BlockPos bed = player.getSleepingPos().orElse(null);
        if (bed == null) return null;
        // Beds are not pathfindable blocks in 26.2. Permit a two-block target radius, then retain
        // MITE's tighter final-node check below so only a genuine bedside route counts.
        Path path = mob.getNavigation().createPath(bed, 2, maxPathLength);
        if (path == null || !path.canReach()) return null;
        Node end = path.getEndNode();
        return end != null && end.distanceToSqr(bed) <= 2.0F ? path : null;
    }

    private static List<ServerPlayer> participatingPlayers(ServerLevel level) {
        return level.players().stream().filter(player -> !player.isSpectator() && player.isAlive()).toList();
    }

    private static boolean allDeeplySleepingInBeds(List<ServerPlayer> players) {
        return players.stream().allMatch(player -> player.isSleepingLongEnough() && isSleepingInBed(player));
    }

    private static void wakeSleepingPlayers(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            if (player.isSleeping()) player.stopSleepInBed(false, true);
        }
    }

    private static Player.BedSleepingProblem entryProblem(ServerLevel level, ServerPlayer player, BlockPos bed) {
        if (!player.onGround()) return Player.BedSleepingProblem.OTHER_PROBLEM;
        if (hasReachableHostile(level, bed)) return Player.BedSleepingProblem.NOT_SAFE;
        if (level.canSeeSky(bed.above())) return NOT_SHELTERED;
        if (player.getData(com.pixulse.infx.registry.ModAttachments.SURVIVAL).isStarving()) return TOO_HUNGRY;
        if (player.hasEffect(MobEffects.POISON)) return POISONED;
        return hasDiggingZombie(level, bed) ? MOBS_DIGGING : null;
    }

    private static boolean hasReachableHostile(ServerLevel level, BlockPos bed) {
        AABB area = new AABB(bed).inflate(BED_HORIZONTAL_HOSTILE_RANGE, BED_VERTICAL_HOSTILE_RANGE, BED_HORIZONTAL_HOSTILE_RANGE);
        for (Mob mob : level.getEntitiesOfClass(
                Mob.class, area, mob -> mob.isAlive() && mob instanceof Enemy)) {
            if (pathToBed(mob, bed, 24)) return true;
        }
        return false;
    }

    private static boolean hasDiggingZombie(ServerLevel level, BlockPos bed) {
        AABB area = new AABB(bed).inflate(BED_HORIZONTAL_HOSTILE_RANGE, BED_VERTICAL_HOSTILE_RANGE, BED_HORIZONTAL_HOSTILE_RANGE);
        return level.getEntitiesOfClass(Mob.class, area, MonsterTactics::isDigging).size() > 0;
    }

    private static boolean pathToBed(Mob mob, BlockPos bed, int maxPathLength) {
        Path path = mob.getNavigation().createPath(bed, 2, maxPathLength);
        if (path == null || !path.canReach()) return false;
        Node end = path.getEndNode();
        return end != null && end.distanceToSqr(bed) <= 2.0F;
    }

    private static boolean isSleepingInBed(ServerPlayer player) {
        return player.isSleeping()
                && player.getSleepingPos().map(pos -> player.level().getBlockState(pos).is(BlockTags.BEDS)).orElse(false);
    }

    private static boolean mustKeepVanillaFailure(Player.BedSleepingProblem problem) {
        return problem == Player.BedSleepingProblem.TOO_FAR_AWAY
                || problem == Player.BedSleepingProblem.OBSTRUCTED
                || problem == Player.BedSleepingProblem.OTHER_PROBLEM;
    }

    private static void setBedRespawn(ServerPlayer player, BlockPos bed) {
        APPLYING_RESPAWN.add(player);
        try {
            player.setRespawnPosition(
                    new ServerPlayer.RespawnConfig(
                            LevelData.RespawnData.of(player.level().dimension(), bed, player.getYRot(), player.getXRot()),
                            false),
                    true);
        } finally {
            APPLYING_RESPAWN.remove(player);
        }
    }

    private static BlockPos bedHead(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) != net.minecraft.world.level.block.state.properties.BedPart.HEAD) {
            return pos.relative(state.getValue(BedBlock.FACING));
        }
        return pos;
    }

    private static Player.BedSleepingProblem problem(String translationKey) {
        return new Player.BedSleepingProblem(Component.translatable(translationKey));
    }
}
