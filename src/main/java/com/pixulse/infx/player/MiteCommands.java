package com.pixulse.infx.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixulse.infx.world.VillageProgression;
import com.pixulse.infx.world.WorldData;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** R196 game commands under the single root `/infx`. */
public final class MiteCommands {
    public static final String ROOT = "infx";
    public static final List<String> NAMES = List.of("infx day", "infx villages");

    private MiteCommands() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(MiteCommands::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(ROOT)
                .then(Commands.literal("day").executes(context -> reply(
                        context, "Survival day: " + R196MonsterDay.day(player(context)))))
                .then(Commands.literal("villages").executes(context -> {
                    ServerPlayer player = player(context);
                    long day = VillageProgression.day(player.level());
                    boolean ironTool = WorldData.get(player.level()).ironToolCrafted();
                    return reply(context, "Village generation: day " + day + "/60; world iron-tier milestone: "
                            + (ironTool ? "yes" : "no") + "; unlocked: "
                            + VillageProgression.generationUnlocked(player.level()));
                })));
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getSource().getPlayerOrException();
    }

    private static int reply(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    private static final class R196MonsterDay {
        static long day(ServerPlayer player) {
            return Math.max(1L, player.level().getOverworldClockTime() / 24_000L + 1L);
        }
    }
}
