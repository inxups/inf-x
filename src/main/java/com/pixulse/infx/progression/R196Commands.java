package com.pixulse.infx.progression;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.pixulse.infx.world.R196VillageProgression;
import com.pixulse.infx.world.R196WorldData;
import net.minecraft.server.permissions.Permissions;

/** The fourteen read-only diagnostic and survival commands shipped by R196. */
public final class R196Commands {
    public static final List<String> NAMES = List.of(
            "day", "ground", "hunger", "load", "mem", "xp", "rendering", "skills",
            "stats", "version", "versions", "syncpos", "villages", "chunks");

    private R196Commands() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196Commands::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("day").executes(context -> reply(
                context, "Survival day: " + R196MonsterDay.day(player(context)))));
        dispatcher.register(Commands.literal("ground").executes(context -> {
            ServerPlayer player = player(context);
            BlockPos pos = player.blockPosition();
            int ground = player.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
            return reply(context, "Position: " + pos.toShortString() + "; ground Y: " + ground);
        }));
        dispatcher.register(Commands.literal("hunger").executes(context -> {
            var food = player(context).getFoodData();
            return reply(context, "Hunger: " + food.getFoodLevel() + "; saturation: " + format(food.getSaturationLevel()));
        }));
        dispatcher.register(Commands.literal("load").executes(context -> {
            ServerPlayer player = player(context);
            return reply(context, "Loaded chunks: " + player.level().getChunkSource().getLoadedChunksCount()
                    + "; players: " + player.level().getServer().getPlayerCount());
        }));
        dispatcher.register(Commands.literal("mem").executes(context -> {
            Runtime runtime = Runtime.getRuntime();
            long used = (runtime.totalMemory() - runtime.freeMemory()) / 1_048_576L;
            long maximum = runtime.maxMemory() / 1_048_576L;
            return reply(context, "Memory: " + used + " / " + maximum + " MiB");
        }));
        dispatcher.register(Commands.literal("xp").executes(context -> {
            ServerPlayer player = player(context);
            return reply(context, "Raw XP: " + player.totalExperience + "; MITE level: " + player.experienceLevel);
        }));
        dispatcher.register(Commands.literal("rendering").executes(context -> reply(
                context,
                "Server view distance: " + player(context).level().getServer().getPlayerList().getViewDistance() + " chunks")));
        dispatcher.register(Commands.literal("skills").executes(context -> {
            int level = player(context).experienceLevel;
            return reply(context, "Mining +" + level * 2 + "%; crafting +" + level * 2
                    + "%; melee +" + format(level * .5F) + "%");
        }));
        dispatcher.register(Commands.literal("stats").executes(context -> {
            ServerPlayer player = player(context);
            return reply(context, "Health: " + format(player.getHealth()) + "/" + format(player.getMaxHealth())
                    + "; armor: " + format((float) player.getAttributeValue(Attributes.ARMOR)));
        }));
        dispatcher.register(Commands.literal("version").executes(context -> reply(
                context,
                "InfiniteX " + ModList.get().getModContainerById(InfiniteX.MOD_ID)
                        .map(container -> container.getModInfo().getVersion().toString())
                        .orElse("unknown"))));
        dispatcher.register(Commands.literal("versions").executes(context -> reply(
                context, "Minecraft " + SharedConstants.getCurrentVersion().name() + "; InfiniteX R196 rules")));
        dispatcher.register(Commands.literal("syncpos").executes(context -> {
            ServerPlayer player = player(context);
            Component message = Component.literal(player.getScoreboardName() + " @ " + player.blockPosition().toShortString());
            player.level().getServer().getPlayerList().broadcastSystemMessage(message, false);
            return 1;
        }));
        dispatcher.register(Commands.literal("villages").executes(context -> {
            ServerPlayer player = player(context);
            long day = R196VillageProgression.day(player.level());
            boolean ironTool = R196WorldData.get(player.level()).ironToolCrafted();
            return reply(context, "Village generation: day " + day + "/60; world iron-tier milestone: "
                    + (ironTool ? "yes" : "no") + "; unlocked: "
                    + R196VillageProgression.generationUnlocked(player.level()));
        }));
        dispatcher.register(Commands.literal("infxrecords")
                .then(Commands.literal("personal").executes(R196Commands::personalRecords))
                .then(Commands.literal("world").executes(R196Commands::worldRecords))
                .then(Commands.literal("dev")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(R196Commands::developmentRecords)));
        dispatcher.register(Commands.literal("chunks").executes(context -> {
            ServerPlayer player = player(context);
            return reply(context, "Chunk: " + player.chunkPosition() + "; loaded: "
                    + player.level().getChunkSource().getLoadedChunksCount());
        }));
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return context.getSource().getPlayerOrException();
    }

    private static int reply(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    private static String format(float value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static int personalRecords(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = player(context);
        long completed = player.level().getServer().getAdvancements().getAllAdvancements().stream()
                .filter(advancement -> advancement.id().getNamespace().equals(InfiniteX.MOD_ID))
                .filter(advancement -> player.getAdvancements().getOrStartProgress(advancement).isDone())
                .count();
        return reply(context, "Personal R196 advancements: " + completed + "/62");
    }

    private static int worldRecords(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = player(context);
        var records = R196WorldData.get(player.level()).firstCompletions();
        context.getSource().sendSuccess(() -> Component.literal("World-first R196 records: " + records.size() + "/62"), false);
        records.entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(12).forEach(entry ->
                context.getSource().sendSuccess(() -> Component.literal(
                        entry.getKey() + ": " + entry.getValue().player() + " (day " + entry.getValue().day() + ")"), false));
        return records.size();
    }

    private static int developmentRecords(CommandContext<CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = player(context);
        R196WorldData data = R196WorldData.get(player.level());
        return reply(context, "R196 dev records: firsts=" + data.firstCompletions().size()
                + ", creationBooks=0x" + Integer.toHexString(data.creationBookMask())
                + ", villages=" + R196VillageProgression.generationUnlocked(player.level())
                + ", endConquered=" + data.endConquered());
    }

    private static final class R196MonsterDay {
        static long day(ServerPlayer player) {
            return Math.max(1L, player.level().getOverworldClockTime() / 24_000L + 1L);
        }
    }
}
