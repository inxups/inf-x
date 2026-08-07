package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.item.equipment.CorrosionType;
import org.junit.jupiter.api.Test;

class GelatinousSphereItemTest {
    @Test
    void sphereColorsCarryDamageAndCorrosionPayloads() {
        assertSphere(GelatinousSphereItem.Color.GREEN, "green", CorrosionType.PEPSIN, 1);
        assertSphere(GelatinousSphereItem.Color.OCHRE, "ochre", CorrosionType.PEPSIN, 2);
        assertSphere(GelatinousSphereItem.Color.CRIMSON, "crimson", CorrosionType.PEPSIN, 3);
        assertSphere(GelatinousSphereItem.Color.GRAY, "gray", CorrosionType.ACID, 3);
        assertSphere(GelatinousSphereItem.Color.BLACK, "black", CorrosionType.ACID, 4);
    }

    private static void assertSphere(
            GelatinousSphereItem.Color color, String path, CorrosionType type, int attackDamage) {
        assertEquals(path, color.path());
        assertEquals(type, color.corrosionType());
        assertEquals(attackDamage, color.attackDamage());
    }
}
