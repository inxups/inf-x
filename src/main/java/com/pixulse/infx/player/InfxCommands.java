package com.pixulse.infx.player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** INFX game commands under the single root `/infx`. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class InfxCommands {
    public static final String ROOT = "infx";
    public static final List<String> NAMES = List.of("infx day", "infx xp");

    private InfxCommands() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(ROOT)
                .then(Commands.literal("day").executes(context -> reply(
                        context, "Survival day: " + InfxMonsterDay.day(player(context)))))
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

    static String experienceMessage(int totalExperience, int level, float progress) {
        return "Experience: total " + totalExperience + "; level " + level
                + "; progress " + Math.round(progress * 100.0F) + "%";
    }

    private static final class InfxMonsterDay {
        static long day(ServerPlayer player) {
            return Math.max(1L, player.level().getOverworldClockTime() / 24_000L + 1L);
        }
    }
}
