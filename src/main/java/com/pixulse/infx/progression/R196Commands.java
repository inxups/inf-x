package com.pixulse.infx.progression;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixulse.infx.entity.R196Livestock;
import com.pixulse.infx.world.R196VillageProgression;
import com.pixulse.infx.world.R196WorldData;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** R196 game commands under the single root `/infx`. */
public final class R196Commands {
    public static final String ROOT = "infx";
    public static final List<String> NAMES = List.of(
            "infx day", "infx villages", "infx livestock");

    private R196Commands() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196Commands::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(ROOT)
                .then(Commands.literal("day").executes(context -> reply(
                        context, "Survival day: " + R196MonsterDay.day(player(context)))))
                .then(Commands.literal("villages").executes(context -> {
                    ServerPlayer player = player(context);
                    long day = R196VillageProgression.day(player.level());
                    boolean ironTool = R196WorldData.get(player.level()).ironToolCrafted();
                    return reply(context, "Village generation: day " + day + "/60; world iron-tier milestone: "
                            + (ironTool ? "yes" : "no") + "; unlocked: "
                            + R196VillageProgression.generationUnlocked(player.level()));
                }))
                .then(Commands.literal("livestock")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("sick")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> forceLivestockDisease(context, true))))
                        .then(Commands.literal("cure")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> forceLivestockDisease(context, false))))));
    }

    private static int forceLivestockDisease(CommandContext<CommandSourceStack> context, boolean diseased)
            throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
        int count = 0;
        int skippedVanilla = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof Animal animal)) {
                continue;
            }
            if (R196Livestock.setDiseased(animal, diseased)) {
                count++;
            } else if (R196Livestock.isLivestock(animal) && !R196Livestock.hasSickSkin(animal)) {
                skippedVanilla++;
            }
        }
        String action = diseased ? "diseased" : "cured";
        int finalCount = count;
        int finalSkipped = skippedVanilla;
        if (finalCount == 0 && finalSkipped > 0) {
            context.getSource().sendFailure(Component.literal(
                    "No R196 livestock matched; skipped " + finalSkipped
                            + " vanilla animals (use type=infx:r196_cow|chicken|sheep|pig)"));
            return 0;
        }
        context.getSource().sendSuccess(
                () -> Component.literal("Forced " + action + " on " + finalCount + " livestock"
                        + (finalSkipped > 0 ? " (skipped " + finalSkipped + " vanilla)" : "")),
                true);
        return count;
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
