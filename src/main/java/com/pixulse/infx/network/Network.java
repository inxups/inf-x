package com.pixulse.infx.network;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.recipe.RecipeRule;
import com.pixulse.infx.recipe.RecipeRules;
import com.pixulse.infx.screen.menu.MetalAnvilMenu;
import com.pixulse.infx.world.RunegateTeleportation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class Network {
    public static final String FORCE_EGG_THROW = "infx_force_egg_throw";
    public static final String CTRL_USE = "infx_ctrl_use";
    private static final String PROTOCOL_VERSION = "4";

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
                .playToServer(EggThrowPayload.TYPE, EggThrowPayload.STREAM_CODEC, (payload, context) -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    InteractionHand hand = payload.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                    if (!player.getItemInHand(hand).is(ItemTags.EGGS)) return;
                    player.getPersistentData().putBoolean(FORCE_EGG_THROW, true);
                    try {
                        player.getItemInHand(hand).getItem().use(player.level(), player, hand);
                    } finally {
                        player.getPersistentData().remove(FORCE_EGG_THROW);
                    }
                })
                .playToServer(
                        PlaceFluidSourcePayload.TYPE,
                        PlaceFluidSourcePayload.STREAM_CODEC,
                        (payload, context) -> {
                            if (!(context.player() instanceof ServerPlayer player)) return;
                            InteractionHand hand =
                                    payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                            if (player.getItemInHand(hand).getItem() instanceof InfxBucketItem bucket) {
                                bucket.useWithCtrl(player, hand);
                            }
                        })
                .playToServer(
                        RunegateExecutePayload.TYPE,
                        RunegateExecutePayload.STREAM_CODEC,
                        (payload, context) -> {
                            if (context.player() instanceof ServerPlayer player) {
                                RunegateTeleportation.execute(player);
                            }
                        })
                .playToClient(RunegateStartPayload.TYPE, RunegateStartPayload.STREAM_CODEC)
                .playToClient(RunegateFinishedPayload.TYPE, RunegateFinishedPayload.STREAM_CODEC)
                .configurationToClient(
                        RecipeRulesPayload.TYPE,
                        RecipeRulesPayload.STREAM_CODEC,
                        Network::handleClientRecipeRulesConfiguration)
                .configurationToServer(
                        RecipeRulesAckPayload.TYPE,
                        RecipeRulesAckPayload.STREAM_CODEC,
                        Network::handleServerRecipeRulesAck)
                .playToClient(
                        RecipeRulesPayload.TYPE,
                        RecipeRulesPayload.STREAM_CODEC,
                        Network::handleClientRecipeRules)
                .playToServer(
                        MetalAnvilRenamePayload.TYPE,
                        MetalAnvilRenamePayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            if (!(context.player() instanceof ServerPlayer player)) return;
                            if (player.containerMenu instanceof MetalAnvilMenu menu && menu.stillValid(player)) {
                                menu.setItemName(payload.name());
                            }
                        }));
    }

    @SubscribeEvent
    public static void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
        event.register(new RecipeRulesConfigurationTask());
    }

    /** Sends the server-authoritative crafting rules during a datapack reload. */
    public static void sendRecipeRules(ServerPlayer player) {
        player.connection.send(new ClientboundCustomPayloadPacket(
                new RecipeRulesPayload(RecipeRules.serverResolvedRules())));
    }

    // Configuration tasks live on the server: the client applies the rules and
    // acknowledges; the server-side ack handler completes the task.
    static void handleClientRecipeRulesConfiguration(RecipeRulesPayload payload, IPayloadContext context) {
        RecipeRules.setClientRules(payload.resolvedRules());
        context.reply(RecipeRulesAckPayload.INSTANCE);
    }

    static void handleServerRecipeRulesAck(RecipeRulesAckPayload payload, IPayloadContext context) {
        context.finishCurrentTask(RecipeRulesConfigurationTask.TYPE);
    }

    /** Play-phase variant (datapack reload): no configuration task is running. */
    private static void handleClientRecipeRules(RecipeRulesPayload payload, IPayloadContext context) {
        RecipeRules.setClientRules(payload.resolvedRules());
    }

    public record EggThrowPayload(boolean offhand) implements CustomPacketPayload {
        public static final Type<EggThrowPayload> TYPE = new Type<>(InfiniteX.id("egg_throw"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EggThrowPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, EggThrowPayload::offhand, EggThrowPayload::new);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PlaceFluidSourcePayload(boolean offhand) implements CustomPacketPayload {
        public static final Type<PlaceFluidSourcePayload> TYPE =
                new Type<>(InfiniteX.id("place_fluid_source"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PlaceFluidSourcePayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.BOOL, PlaceFluidSourcePayload::offhand, PlaceFluidSourcePayload::new);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RunegateStartPayload() implements CustomPacketPayload {
        public static final RunegateStartPayload INSTANCE = new RunegateStartPayload();
        public static final Type<RunegateStartPayload> TYPE = new Type<>(InfiniteX.id("runegate_start"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RunegateStartPayload> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RunegateExecutePayload() implements CustomPacketPayload {
        public static final RunegateExecutePayload INSTANCE = new RunegateExecutePayload();
        public static final Type<RunegateExecutePayload> TYPE = new Type<>(InfiniteX.id("runegate_execute"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RunegateExecutePayload> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RunegateFinishedPayload() implements CustomPacketPayload {
        public static final RunegateFinishedPayload INSTANCE = new RunegateFinishedPayload();
        public static final Type<RunegateFinishedPayload> TYPE = new Type<>(InfiniteX.id("runegate_finished"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RunegateFinishedPayload> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client acknowledgment that completes the server's recipe-rules configuration task. */
    public record RecipeRulesAckPayload() implements CustomPacketPayload {
        public static final RecipeRulesAckPayload INSTANCE = new RecipeRulesAckPayload();
        public static final Type<RecipeRulesAckPayload> TYPE = new Type<>(InfiniteX.id("recipe_rules_ack"));
        public static final StreamCodec<FriendlyByteBuf, RecipeRulesAckPayload> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RecipeRulesPayload(java.util.List<RecipeRule.Resolved> resolvedRules)
            implements CustomPacketPayload {
        public static final Type<RecipeRulesPayload> TYPE = new Type<>(InfiniteX.id("recipe_rules"));
        public static final StreamCodec<ByteBuf, RecipeRulesPayload> STREAM_CODEC =
                StreamCodec.composite(
                        RecipeRule.Resolved.STREAM_CODEC.apply(ByteBufCodecs.list()),
                        RecipeRulesPayload::resolvedRules,
                        RecipeRulesPayload::new);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Carries the metal-anvil name box input from the client to the server. */
    public record MetalAnvilRenamePayload(String name) implements CustomPacketPayload {
        public static final Type<MetalAnvilRenamePayload> TYPE = new Type<>(InfiniteX.id("metal_anvil_rename"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MetalAnvilRenamePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.STRING_UTF8, MetalAnvilRenamePayload::name, MetalAnvilRenamePayload::new);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
