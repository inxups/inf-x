package com.pixulse.infx.curse;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

/** Client-visible projection of a realized curse. Pending curses are deliberately not synchronized. */
public record R196CurseStatus(int typeId, boolean known) {
    public static final R196CurseStatus NONE = new R196CurseStatus(0, false);
    public static final StreamCodec<RegistryFriendlyByteBuf, R196CurseStatus> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    R196CurseStatus::typeId,
                    ByteBufCodecs.BOOL,
                    R196CurseStatus::known,
                    R196CurseStatus::new);

    public R196CurseStatus {
        if (typeId < 0 || typeId > R196CurseType.values().length) {
            throw new IllegalArgumentException("Unknown R196 curse id: " + typeId);
        }
        if (typeId == 0) known = false;
    }

    public boolean active() {
        return typeId != 0;
    }

    public @Nullable R196CurseType type() {
        return R196CurseType.byId(typeId);
    }

    public boolean is(R196CurseType type) {
        return typeId == type.id();
    }
}
