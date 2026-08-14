package com.pixulse.infx.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.pixulse.infx.recipe.RecipeRule;
import com.pixulse.infx.recipe.RecipeRules;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.junit.jupiter.api.Test;

class NetworkTest {
    @Test
    void recipeRulesConfigurationAppliesRulesAndRepliesWithAck() {
        var rule = new RecipeRule.Resolved(
                Identifier.fromNamespaceAndPath("infx", "test_rule"),
                List.of(Identifier.withDefaultNamespace("crafting_table")),
                List.of(),
                Optional.of(2.5f),
                Optional.empty());
        var context = new RecordingContext();

        Network.handleClientRecipeRulesConfiguration(new Network.RecipeRulesPayload(List.of(rule)), context);

        assertEquals(Network.RecipeRulesAckPayload.INSTANCE, context.repliedPayload);
        assertNull(context.finishedTask);
        RecipeRules.clearClientRules();
    }

    @Test
    void serverFinishesRecipeRulesTaskOnAck() {
        var context = new RecordingContext();

        Network.handleServerRecipeRulesAck(Network.RecipeRulesAckPayload.INSTANCE, context);

        assertEquals(RecipeRulesConfigurationTask.TYPE, context.finishedTask);
    }

    private static final class RecordingContext implements IPayloadContext {
        private ConfigurationTask.Type finishedTask;
        private CustomPacketPayload repliedPayload;

        @Override
        public ICommonPacketListener listener() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Player player() {
            return null;
        }

        @Override
        public CompletableFuture<Void> enqueueWork(Runnable work) {
            work.run();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <T> CompletableFuture<T> enqueueWork(Supplier<T> work) {
            return CompletableFuture.completedFuture(work.get());
        }

        @Override
        public PacketFlow flow() {
            return PacketFlow.SERVERBOUND;
        }

        @Override
        public void handle(CustomPacketPayload payload) {}

        @Override
        public void finishCurrentTask(ConfigurationTask.Type type) {
            this.finishedTask = type;
        }

        @Override
        public void disconnect(Component reason) {}

        @Override
        public void reply(CustomPacketPayload payload) {
            this.repliedPayload = payload;
        }
    }
}
