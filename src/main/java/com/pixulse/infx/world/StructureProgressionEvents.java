package com.pixulse.infx.world;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Records the player actions that unlock world-wide structure generation gates. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class StructureProgressionEvents {
    private StructureProgressionEvents() {}

    /** Called by the custom XP pipeline after it applies a player's actual point delta. */
    public static void recordExperienceGain(ServerPlayer player, long amount) {
        if (WorldData.get(player.level()).recordMansionExperienceGain(player.getUUID(), amount)) {
            StructureGenerationGates.refresh(player.level());
        }
    }

    /** Preserves progress for players who already held XP before this gate was introduced. */
    public static void observeExperience(ServerPlayer player) {
        if (WorldData.get(player.level()).observeMansionExperience(player.getUUID(), player.totalExperience)) {
            StructureGenerationGates.refresh(player.level());
        }
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getTo().equals(Level.NETHER)) return;
        if (WorldData.get(player.level()).markNetherEntered()) {
            StructureGenerationGates.refresh(player.level());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().getType() != EntityType.GUARDIAN
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (WorldData.get(player.level()).markMonumentGuardianKilled()) {
            StructureGenerationGates.refresh(player.level());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !insideNetherFortress(player)) return;
        if (WorldData.get(player.level()).markNetherFortressEntered()) {
            StructureGenerationGates.refresh(player.level());
        }
    }

    private static boolean insideNetherFortress(ServerPlayer player) {
        if (!player.level().dimension().equals(Level.NETHER)) return false;
        if (WorldData.get(player.level()).netherFortressEntered()) return false;
        var fortress = player.level().registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(BuiltinStructures.FORTRESS)
                .value();
        return player.level().structureManager().getStructureWithPieceAt(player.blockPosition(), fortress)
                != StructureStart.INVALID_START;
    }
}
