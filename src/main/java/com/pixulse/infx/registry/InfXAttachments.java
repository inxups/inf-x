package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.agriculture.GrassTrampling;
import com.pixulse.infx.data.curse.CurseStatus;
import com.pixulse.infx.data.food.SurvivalData;
import com.pixulse.infx.data.nightwing.NightwingDimming;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class InfXAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, InfiniteX.MOD_ID);

    /**
     * Player metabolism state. Death cloning is handled by {@code SurvivalEvents} so transient
     * food state is reset while long-term nutrition remains.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SurvivalData>> SURVIVAL =
            ATTACHMENTS.register("survival", () -> AttachmentType.builder(SurvivalData::initial)
                    .serialize(SurvivalData.CODEC.fieldOf("survival"))
                    .sync((holder, player) -> holder == player, SurvivalData.STREAM_CODEC)
                    .build());

    /** Realized curse state is projected from world SavedData and synchronized only to its player. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CurseStatus>> CURSE_STATUS =
            ATTACHMENTS.register("curse_status", () -> AttachmentType.builder(() -> CurseStatus.NONE)
                    .sync((holder, player) -> holder == player, CurseStatus.STREAM_CODEC)
                    .build());

    /** Transient player-only screen dimming sent by a Nightwing hit. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<NightwingDimming>> NIGHTWING_DIMMING =
            ATTACHMENTS.register("nightwing_dimming", () -> AttachmentType.builder(() -> NightwingDimming.NONE)
                    .sync((holder, player) -> holder == player, NightwingDimming.STREAM_CODEC)
                    .build());

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<String, Integer>> GRASS_TRAMPLING_STREAM =
            StreamCodec.of(
                    (buf, value) -> ByteBufCodecs.map(
                                    HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT)
                            .encode(buf, new HashMap<>(value)),
                    buf -> Map.copyOf(ByteBufCodecs.map(
                                    HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT)
                            .decode(buf)));

    /** Per-chunk grass trampling counts (InfX manure-brown grass). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Map<String, Integer>>> GRASS_TRAMPLING =
            ATTACHMENTS.register(
                    "grass_trampling",
                    () -> AttachmentType.<Map<String, Integer>>builder(Map::of)
                            .serialize(GrassTrampling.CODEC.fieldOf("tramplings"))
                            .sync(GRASS_TRAMPLING_STREAM)
                            .build());

    private InfXAttachments() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
