package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.survival.R196SurvivalData;
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

    private ModAttachments() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
