package com.pixulse.infx.curse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.junit.jupiter.api.Test;

class R196CurseStatusTest {
    @Test
    void streamCodecRoundTripsTheSelfVisibleProjection() {
        var raw = Unpooled.buffer();
        try {
            var buffer = new RegistryFriendlyByteBuf(raw, RegistryAccess.EMPTY, ConnectionType.OTHER);
            var expected = new R196CurseStatus(R196CurseType.ENTANGLEMENT.id(), true);
            R196CurseStatus.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, R196CurseStatus.STREAM_CODEC.decode(buffer));
        } finally {
            raw.release();
        }
    }

    @Test
    void noneCannotBeKnownAndUnknownIdsAreRejected() {
        var none = new R196CurseStatus(0, true);
        assertFalse(none.active());
        assertFalse(none.known());
        assertTrue(new R196CurseStatus(16, false).active());
        assertThrows(IllegalArgumentException.class, () -> new R196CurseStatus(-1, false));
        assertThrows(IllegalArgumentException.class, () -> new R196CurseStatus(17, false));
    }
}
