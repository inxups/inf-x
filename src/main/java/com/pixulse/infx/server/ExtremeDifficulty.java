package com.pixulse.infx.server;

import com.mojang.brigadier.CommandDispatcher;
import com.pixulse.infx.InfiniteX;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * InfiniteX's Extreme world difficulty profile.
 *
 * <p>Minecraft's public difficulty API has {@link Difficulty#HARD} as its highest engine difficulty.
 * Extreme deliberately uses that maximum setting and persists the vanilla difficulty lock so the
 * world-selection UI cannot lower it.</p>
 */
public final class ExtremeDifficulty {
    public static final String NAME = "extreme";
    public static final Difficulty VANILLA_DIFFICULTY = Difficulty.HARD;

    private ExtremeDifficulty() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(ExtremeDifficulty::onServerStarted);
        gameBus.addListener(ExtremeDifficulty::onRegisterCommands);
    }

    /** Applies the Extreme profile, including to worlds that were previously difficulty-locked. */
    public static void apply(MinecraftServer server) {
        server.setDifficulty(VANILLA_DIFFICULTY, true);
        server.setDifficultyLocked(true);
    }

    public static boolean isActive(Difficulty difficulty, boolean locked) {
        return difficulty == VANILLA_DIFFICULTY && locked;
    }

    private static void onServerStarted(ServerStartedEvent event) {
        apply(event.getServer());
        InfiniteX.LOGGER.info("InfiniteX Extreme difficulty is active and locked");
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        var difficulty = dispatcher.getRoot().getChild("difficulty");
        if (difficulty == null) return;
        difficulty.addChild(Commands.literal(NAME).executes(context -> {
            apply(context.getSource().getServer());
            context.getSource().sendSuccess(() -> Component.translatable("commands.infx.difficulty.extreme"), true);
            return 1;
        }).build());
    }
}
