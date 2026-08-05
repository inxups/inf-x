package com.pixulse.infx.network;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.server.ServerTestModePolicy;
import com.pixulse.infx.screen.menu.MetalAnvilMenu;
import com.pixulse.infx.world.RunegateTeleportation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
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
    public static final String TEST_MODE_MISMATCH_KEY = "disconnect.infx.test_mode_mismatch";
    private static final String PROTOCOL_VERSION = "3";

    private Network() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
                .configurationBidirectional(
                        TestModePayload.TYPE,
                        TestModePayload.STREAM_CODEC,
                        Network::handleServerTestMode,
                        Network::handleClientTestMode)
                .playToServer(EggThrowPayload.TYPE, EggThrowPayload.STREAM_CODEC, (payload, context) -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    InteractionHand hand = payload.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                    if (!player.getItemInHand(hand).is(Items.EGG)) return;
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
                .playToServer(
                        RenameMetalAnvilPayload.TYPE,
                        RenameMetalAnvilPayload.STREAM_CODEC,
                        (payload, context) -> {
                            if (context.player() instanceof ServerPlayer player) {
                                context.enqueueWork(() -> {
                                    if (player.containerMenu instanceof MetalAnvilMenu menu
                                            && menu.stillValid(player)) {
                                        menu.setItemName(payload.name());
                                    }
                                });
                            }
                        })
                .playToClient(RunegateStartPayload.TYPE, RunegateStartPayload.STREAM_CODEC)
                .playToClient(RunegateFinishedPayload.TYPE, RunegateFinishedPayload.STREAM_CODEC);
    }

    @SubscribeEvent
    public static void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
        event.register(new TestModeConfigurationTask(InfiniteXTestMode.isEnabled()));
    }

    static boolean testModesMatch(boolean serverTestMode, boolean clientTestMode) {
        return ServerTestModePolicy.modesMatch(serverTestMode, clientTestMode);
    }

    private static void handleServerTestMode(TestModePayload payload, IPayloadContext context) {
        handleServerTestMode(InfiniteXTestMode.isEnabled(), payload, context);
    }

    static void handleServerTestMode(boolean serverTestMode, TestModePayload payload, IPayloadContext context) {
        if (!testModesMatch(serverTestMode, payload.testMode())) {
            context.disconnect(Component.translatable(TEST_MODE_MISMATCH_KEY));
            return;
        }
        context.finishCurrentTask(TestModeConfigurationTask.TYPE);
    }

    private static void handleClientTestMode(TestModePayload payload, IPayloadContext context) {
        handleClientTestMode(InfiniteXTestMode.isEnabled(), context);
    }

    static void handleClientTestMode(boolean clientTestMode, IPayloadContext context) {
        context.reply(new TestModePayload(clientTestMode));
    }

    public record TestModePayload(boolean testMode) implements CustomPacketPayload {
        public static final Type<TestModePayload> TYPE = new Type<>(InfiniteX.id("test_mode"));
        public static final StreamCodec<FriendlyByteBuf, TestModePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, TestModePayload::testMode, TestModePayload::new);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
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

    public record RenameMetalAnvilPayload(String name) implements CustomPacketPayload {
        public static final Type<RenameMetalAnvilPayload> TYPE = new Type<>(InfiniteX.id("rename_metal_anvil"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RenameMetalAnvilPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.STRING_UTF8, RenameMetalAnvilPayload::name, RenameMetalAnvilPayload::new);

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
}
