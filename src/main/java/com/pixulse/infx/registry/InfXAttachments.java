package com.pixulse.infx.registry;

import com.mojang.serialization.Codec;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.agriculture.GrassTrampling;
import com.pixulse.infx.data.curse.CurseStatus;
import com.pixulse.infx.data.food.SurvivalData;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
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

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<String, Integer>> GRASS_TRAMPLING_STREAM =
            StreamCodec.of(
                    (buf, value) -> ByteBufCodecs.map(
                                    HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT)
                            .encode(buf, new HashMap<>(value)),
                    buf -> Map.copyOf(ByteBufCodecs.map(
                                    HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT)
                            .decode(buf)));

    /** Per-chunk grass trampling counts (MITE manure-brown grass). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Map<String, Integer>>> GRASS_TRAMPLING =
            ATTACHMENTS.register(
                    "grass_trampling",
                    () -> AttachmentType.<Map<String, Integer>>builder(Map::of)
                            .serialize(GrassTrampling.CODEC.fieldOf("tramplings"))
                            .sync(GRASS_TRAMPLING_STREAM)
                            .build());

    /** Persistent ordinary-spawner source stored on mobs so delayed deaths retain their origin. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<GlobalPos>>> SPAWNER_ORIGIN =
            ATTACHMENTS.register(
                    "spawner_origin",
                    () -> AttachmentType.<Optional<GlobalPos>>builder(Optional::empty)
                            .serialize(GlobalPos.CODEC.optionalFieldOf("origin"))
                            .build());

    /** Persistent MITE ordinary-spawner kill counter, synchronized so exhausted spawners also stop client animation. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> SPAWNER_KILLS =
            ATTACHMENTS.register(
                    "spawner_kills",
                    () -> AttachmentType.<Integer>builder(() -> 0)
                            .serialize(Codec.intRange(0, 15).fieldOf("kills"))
                            .sync(ByteBufCodecs.VAR_INT)
                            .build());

    /** Server-only expiration tick for MITE's recent player-or-tamed-wolf damage credit. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> SPAWNER_PLAYER_DAMAGE_UNTIL =
            ATTACHMENTS.register(
                    "spawner_player_damage_until",
                    () -> AttachmentType.<Long>builder(() -> Long.MIN_VALUE).build());

    private InfXAttachments() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
