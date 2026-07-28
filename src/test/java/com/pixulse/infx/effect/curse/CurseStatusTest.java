package com.pixulse.infx.effect.curse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.junit.jupiter.api.Test;

class CurseStatusTest {
    @Test
    void streamCodecRoundTripsTheSelfVisibleProjection() {
        var raw = Unpooled.buffer();
        try {
            var buffer = new RegistryFriendlyByteBuf(raw, RegistryAccess.EMPTY, ConnectionType.OTHER);
            var expected = new CurseStatus(CurseType.ENTANGLEMENT.id(), true);
            CurseStatus.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, CurseStatus.STREAM_CODEC.decode(buffer));
        } finally {
            raw.release();
        }
    }

    @Test
    void noneCannotBeKnownAndUnknownIdsAreRejected() {
        var none = new CurseStatus(0, true);
        assertFalse(none.active());
        assertFalse(none.known());
        assertTrue(new CurseStatus(16, false).active());
        assertThrows(IllegalArgumentException.class, () -> new CurseStatus(-1, false));
        assertThrows(IllegalArgumentException.class, () -> new CurseStatus(17, false));
    }
}
