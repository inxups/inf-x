package com.pixulse.infx.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pixulse.infx.equipment.R196CorrosionType;
import org.junit.jupiter.api.Test;

class R196GelatinousSphereItemTest {
    @Test
    void sphereColorsCarryMiteDamageAndCorrosionPayloads() {
        assertSphere(R196GelatinousSphereItem.Color.GREEN, "green", R196CorrosionType.PEPSIN, 1);
        assertSphere(R196GelatinousSphereItem.Color.OCHRE, "ochre", R196CorrosionType.PEPSIN, 2);
        assertSphere(R196GelatinousSphereItem.Color.CRIMSON, "crimson", R196CorrosionType.PEPSIN, 3);
        assertSphere(R196GelatinousSphereItem.Color.GRAY, "gray", R196CorrosionType.ACID, 3);
        assertSphere(R196GelatinousSphereItem.Color.BLACK, "black", R196CorrosionType.ACID, 4);
    }

    private static void assertSphere(
            R196GelatinousSphereItem.Color color, String path, R196CorrosionType type, int attackDamage) {
        assertEquals(path, color.path());
        assertEquals(type, color.corrosionType());
        assertEquals(attackDamage, color.attackDamage());
    }
}
