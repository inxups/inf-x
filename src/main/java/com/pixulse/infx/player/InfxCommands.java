package com.pixulse.infx.player;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.StructureGenerationGates;
import com.pixulse.infx.world.StructureGenerationGates.ConditionReport;
import com.pixulse.infx.world.StructureGenerationGates.StructureGate;
import com.pixulse.infx.world.StructureGenerationGates.WorldProgressSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** INFX game commands under the single root `/infx`. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class InfxCommands {
    public static final String ROOT = "infx";
    public static final List<String> NAMES = List.of("infx day", "infx structure", "infx xp");

    private InfxCommands() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(ROOT)
                .then(Commands.literal("day")
                        .executes(context -> reply(
                                context, "Survival day: " + InfxMonsterDay.day(player(context))))
                        .then(Commands.argument("day", IntegerArgumentType.integer(1))
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(context -> {
                                    int target = IntegerArgumentType.getInteger(context, "day");
                                    InfxMonsterDay.setDay(player(context), target);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Survival day set to " + target), true);
                                    return target;
                                })))
                .then(Commands.literal("structure")
                        .executes(context -> reply(context, structureListMessage(
                                StructureGenerationGates.progress(player(context).level()))))
                        .then(Commands.argument("structure", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (StructureGate gate : StructureGenerationGates.rules()) {
                                        builder.suggest(gate.id().getPath());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayer player = player(context);
                                    String name = StringArgumentType.getString(context, "structure");
                                    Optional<StructureGate> gate = StructureGenerationGates.rule(
                                            InfiniteX.id(name));
                                    if (gate.isEmpty()) {
                                        context.getSource()
                                                .sendFailure(Component.literal("Unknown structure gate: " + name));
                                        return 0;
                                    }
                                    return reply(context, structureGateMessage(
                                            gate.get(), StructureGenerationGates.progress(player.level())));
                                })))
                .then(Commands.literal("xp").executes(context -> {
                    ServerPlayer player = player(context);
                    return reply(context, experienceMessage(
                            player.totalExperience, player.experienceLevel, player.experienceProgress));
                })));
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getSource().getPlayerOrException();
    }

    private static int reply(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    /** Lists every INFX-gated structure with its current unlock state. */
    static String structureListMessage(WorldProgressSnapshot progress) {
        return StructureGenerationGates.rules().stream()
                .map(gate -> gate.id().getPath() + ": " + state(gate.condition().test(progress)))
                .collect(Collectors.joining("\n"));
    }

    /** Reports one gated structure's unlock state and the current status of each condition. */
    static String structureGateMessage(StructureGate gate, WorldProgressSnapshot progress) {
        StringBuilder message = new StringBuilder();
        message.append(gate.id().getPath()).append(" (").append(gate.id()).append("): ")
                .append(state(gate.condition().test(progress)));
        for (ConditionReport line : gate.condition().report(progress)) {
            message.append('\n').append("  [").append(line.satisfied() ? '\u2713' : '\u2717')
                    .append("] ").append(line.description());
        }
        return message.toString();
    }

    private static String state(boolean unlocked) {
        return unlocked ? "unlocked" : "locked";
    }

    static String experienceMessage(int totalExperience, int level, float progress) {
        return "Experience: total " + totalExperience + "; level " + level
                + "; progress " + Math.round(progress * 100.0F) + "%";
    }

    static final class InfxMonsterDay {
        static final long TICKS_PER_DAY = 24_000L;

        static long day(ServerPlayer player) {
            return dayFromTicks(player.level().getOverworldClockTime());
        }

        static long dayFromTicks(long ticks) {
            return Math.max(1L, ticks / TICKS_PER_DAY + 1L);
        }

        static void setDay(ServerPlayer player, long targetDay) {
            MinecraftServer server = player.level().getServer();
            Holder<WorldClock> overworld = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
            server.clockManager().setTotalTicks(overworld, ticksForDay(targetDay));
        }

        static long ticksForDay(long day) {
            return Math.max(0L, day - 1L) * TICKS_PER_DAY;
        }
    }
}
