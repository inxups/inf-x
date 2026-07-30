package com.pixulse.infx.data.curse;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

/** Client-visible projection of a realized curse. Pending curses are deliberately not synchronized. */
public record CurseStatus(int typeId, boolean known) {
    public static final CurseStatus NONE = new CurseStatus(0, false);
    public static final StreamCodec<RegistryFriendlyByteBuf, CurseStatus> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    CurseStatus::typeId,
                    ByteBufCodecs.BOOL,
                    CurseStatus::known,
                    CurseStatus::new);

    public CurseStatus {
        if (typeId < 0 || typeId > CurseType.values().length) {
            throw new IllegalArgumentException("Unknown INFX curse id: " + typeId);
        }
        if (typeId == 0) known = false;
    }

    public boolean active() {
        return typeId != 0;
    }

    public @Nullable CurseType type() {
        return CurseType.byId(typeId);
    }

    public boolean is(CurseType type) {
        return typeId == type.id();
    }
}
