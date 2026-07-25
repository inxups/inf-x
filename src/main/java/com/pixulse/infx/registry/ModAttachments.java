package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.agriculture.R196GrassTrampling;
import com.pixulse.infx.survival.R196SurvivalData;
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

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, InfiniteX.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<R196SurvivalData>> SURVIVAL =
            ATTACHMENTS.register("survival", () -> AttachmentType.builder(R196SurvivalData::initial)
                    .serialize(R196SurvivalData.CODEC.fieldOf("survival"))
                    .copyOnDeath()
                    .sync((holder, player) -> holder == player, R196SurvivalData.STREAM_CODEC)
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
                            .serialize(R196GrassTrampling.CODEC.fieldOf("tramplings"))
                            .sync(GRASS_TRAMPLING_STREAM)
                            .build());

    private ModAttachments() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
