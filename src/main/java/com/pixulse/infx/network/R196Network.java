package com.pixulse.infx.network;

import com.pixulse.infx.InfiniteX;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class R196Network {
    public static final String FORCE_EGG_THROW = "infx_force_egg_throw";

    private R196Network() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(R196Network::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(EggThrowPayload.TYPE, EggThrowPayload.STREAM_CODEC, (payload, context) -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            InteractionHand hand = payload.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            if (!player.getItemInHand(hand).is(Items.EGG)) return;
            player.getPersistentData().putBoolean(FORCE_EGG_THROW, true);
            try {
                player.getItemInHand(hand).getItem().use(player.level(), player, hand);
            } finally {
                player.getPersistentData().remove(FORCE_EGG_THROW);
            }
        });
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
}
