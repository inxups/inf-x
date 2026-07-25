package com.pixulse.infx.network;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.world.RunegateTeleportation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class R196Network {
    public static final String FORCE_EGG_THROW = "infx_force_egg_throw";
    public static final String CTRL_USE = "infx_ctrl_use";

    private R196Network() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(R196Network::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("2")
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
                            ItemStack held = player.getItemInHand(hand);
                            if (!(held.getItem() instanceof com.pixulse.infx.item.R196BucketItem bucket)) {
                                return;
                            }
                            // Ctrl-fill (empty bucket takes the liquid cell) is free; Ctrl-place of a
                            // permanent source still costs experience.
                            boolean filling =
                                    bucket.contents() == com.pixulse.infx.item.R196BucketItem.Contents.EMPTY;
                            boolean pouring =
                                    bucket.contents() == com.pixulse.infx.item.R196BucketItem.Contents.WATER
                                            || bucket.contents()
                                                    == com.pixulse.infx.item.R196BucketItem.Contents.LAVA;
                            if (!filling && !pouring) {
                                return;
                            }
                            if (pouring && !com.pixulse.infx.item.R196BucketItem.canPlaceAsSource(player, true)) {
                                return;
                            }
                            player.getPersistentData().putBoolean(CTRL_USE, true);
                            try {
                                held.getItem().use(player.level(), player, hand);
                            } finally {
                                player.getPersistentData().remove(CTRL_USE);
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
                .playToClient(RunegateFinishedPayload.TYPE, RunegateFinishedPayload.STREAM_CODEC);
    }

    public record EggThrowPayload(boolean offhand) implements CustomPacketPayload {
        public static final Type<EggThrowPayload> TYPE = new Type<>(InfiniteX.id("egg_throw"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EggThrowPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, EggThrowPayload::offhand, EggThrowPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
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
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RunegateStartPayload() implements CustomPacketPayload {
        public static final RunegateStartPayload INSTANCE = new RunegateStartPayload();
        public static final Type<RunegateStartPayload> TYPE = new Type<>(InfiniteX.id("runegate_start"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RunegateStartPayload> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RunegateExecutePayload() implements CustomPacketPayload {
        public static final RunegateExecutePayload INSTANCE = new RunegateExecutePayload();
        public static final Type<RunegateExecutePayload> TYPE = new Type<>(InfiniteX.id("runegate_execute"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RunegateExecutePayload> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RunegateFinishedPayload() implements CustomPacketPayload {
        public static final RunegateFinishedPayload INSTANCE = new RunegateFinishedPayload();
        public static final Type<RunegateFinishedPayload> TYPE = new Type<>(InfiniteX.id("runegate_finished"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RunegateFinishedPayload> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
