package com.pixulse.infx.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.recipe.RecipeRule;
import com.pixulse.infx.recipe.RecipeRules;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.junit.jupiter.api.Test;

class NetworkTest {
    @Test
    void testModePayloadRoundTrips() {
        var raw = Unpooled.buffer();
        try {
            var buffer = new FriendlyByteBuf(raw);
            var expected = new Network.TestModePayload(true);
            Network.TestModePayload.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, Network.TestModePayload.STREAM_CODEC.decode(buffer));
        } finally {
            raw.release();
        }
    }

    @Test
    void configurationTaskSendsTheServerMode() {
        var payloads = new ArrayList<CustomPacketPayload>();
        var task = new TestModeConfigurationTask(true);

        task.run(payloads::add);

        assertEquals("infx:test_mode", task.type().id());
        assertEquals(new Network.TestModePayload(true), payloads.getFirst());
    }

    @Test
    void configurationRequiresEqualModes() {
        assertTrue(Network.testModesMatch(false, false));
        assertTrue(Network.testModesMatch(true, true));
        assertFalse(Network.testModesMatch(false, true));
        assertFalse(Network.testModesMatch(true, false));
    }

    @Test
    void configurationFinishesOnlyForMatchingModes() {
        for (boolean serverTestMode : new boolean[] {false, true}) {
            for (boolean clientTestMode : new boolean[] {false, true}) {
                var context = new RecordingContext();

                Network.handleServerTestMode(serverTestMode, new Network.TestModePayload(clientTestMode), context);

                if (serverTestMode == clientTestMode) {
                    assertEquals(TestModeConfigurationTask.TYPE, context.finishedTask);
                    assertNull(context.disconnectReason);
                } else {
                    assertNull(context.finishedTask);
                    var contents = (TranslatableContents) context.disconnectReason.getContents();
                    assertEquals(Network.TEST_MODE_MISMATCH_KEY, contents.getKey());
                }
            }
        }
    }

    @Test
    void clientConfigurationRepliesWithItsOwnMode() {
        for (boolean clientTestMode : new boolean[] {false, true}) {
            var context = new RecordingContext();

            Network.handleClientTestMode(clientTestMode, context);

            assertEquals(new Network.TestModePayload(clientTestMode), context.repliedPayload);
        }
    }

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
        private Component disconnectReason;
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
        public void disconnect(Component reason) {
            this.disconnectReason = reason;
        }

        @Override
        public void reply(CustomPacketPayload payload) {
            this.repliedPayload = payload;
        }
    }
}
