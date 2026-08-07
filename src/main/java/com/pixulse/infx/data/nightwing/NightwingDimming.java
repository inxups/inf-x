package com.pixulse.infx.data.nightwing;

import com.pixulse.infx.registry.InfXAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * One server-tick's Nightwing vision-dimming value, synchronized only to the struck player.
 *
 * <p>InfX sends the maximum dimming accumulated during a server tick and lets the client fade
 * it. The revision preserves that packet-like behavior when a player is struck again later.
 */
public record NightwingDimming(float amount, long updatedAt, long revision) {
    public static final float MAX_DIMMING = 2.0F;
    public static final NightwingDimming NONE = new NightwingDimming(0.0F, -1L, 0L);
    public static final StreamCodec<RegistryFriendlyByteBuf, NightwingDimming> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    NightwingDimming::amount,
                    ByteBufCodecs.VAR_LONG,
                    NightwingDimming::updatedAt,
                    ByteBufCodecs.VAR_LONG,
                    NightwingDimming::revision,
                    NightwingDimming::new);

    public static void apply(ServerPlayer player, float amount) {
        @Nullable NightwingDimming current = player.getExistingDataOrNull(InfXAttachments.NIGHTWING_DIMMING.get());
        if (current == null) {
            current = NONE;
        }
        long now = player.level().getGameTime();
        float accumulated = current.updatedAt == now ? current.amount + amount : amount;
        long revision = current.revision == Long.MAX_VALUE ? 1L : current.revision + 1L;
        player.setData(
                InfXAttachments.NIGHTWING_DIMMING.get(),
                new NightwingDimming(Math.clamp(accumulated, 0.0F, MAX_DIMMING), now, revision));
    }
}
