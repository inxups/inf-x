package com.pixulse.infx.world;

/** Continuous, seed-stable density field for the Underworld's large deep-dark caverns. */
final class InfXUnderworldCaveNoise {
    private static final long WARP_X_SALT = 0x243F6A8885A308D3L;
    private static final long WARP_Y_SALT = 0x13198A2E03707344L;
    private static final long WARP_Z_SALT = 0xA4093822299F31D0L;
    private static final long CHEESE_SALT = 0x082EFA98EC4E6C89L;
    private static final long DETAIL_SALT = 0x452821E638D01377L;
    private static final long LAYER_SALT = 0xBE5466CF34E90C6CL;
    private static final long PILLAR_A_SALT = 0xC0AC29B7C97C50DDL;
    private static final long PILLAR_B_SALT = 0x3F84D5B5B5470917L;
    private static final long SPAGHETTI_A_SALT = 0x9216D5D98979FB1BL;
    private static final long SPAGHETTI_B_SALT = 0xD1310BA698DFB5ACL;
    private static final long NOODLE_A_SALT = 0x2FFD72DBD01ADFB7L;
    private static final long NOODLE_B_SALT = 0xB8E1AFED6A267E96L;
    private static final long SECOND_OCTAVE_SALT = 0x9E3779B97F4A7C15L;
    private static final long THIRD_OCTAVE_SALT = 0xD1B54A32D192ED03L;
    private static final long X_HASH = 0x9E3779B97F4A7C15L;
    private static final long Y_HASH = 0xC2B2AE3D27D4EB4FL;
    private static final long Z_HASH = 0x165667B19E3779F9L;

    private InfXUnderworldCaveNoise() {
    }

    static double sample(long seed, double relativeX, double worldY, double relativeZ) {
        if (worldY <= Underworld.LARGE_CAVE_MIN_Y || worldY >= Underworld.LARGE_CAVE_MAX_Y) {
            return -1.0;
        }
        double horizontalDistanceSquared = relativeX * relativeX + relativeZ * relativeZ;
        if (horizontalDistanceSquared
                > Underworld.LARGE_CAVE_OUTER_RADIUS * (double) Underworld.LARGE_CAVE_OUTER_RADIUS) {
            return -1.0;
        }

        double relativeY = worldY - Underworld.LARGE_CAVE_CENTER_Y;
        double warpX = fractalNoise(
                        seed ^ WARP_X_SALT, relativeX / 84.0, relativeY / 64.0, relativeZ / 84.0)
                * 32.0;
        double warpY = fractalNoise(
                        seed ^ WARP_Y_SALT, relativeX / 96.0, relativeY / 72.0, relativeZ / 96.0)
                * 10.0;
        double warpZ = fractalNoise(
                        seed ^ WARP_Z_SALT, relativeX / 84.0, relativeY / 64.0, relativeZ / 84.0)
                * 32.0;
        double warpedX = relativeX + warpX;
        double warpedY = relativeY + warpY;
        double warpedZ = relativeZ + warpZ;

        double horizontalEnvelope = (warpedX * warpedX + warpedZ * warpedZ)
                / (Underworld.LARGE_CAVE_MAIN_RADIUS * (double) Underworld.LARGE_CAVE_MAIN_RADIUS);
        double verticalEnvelope = warpedY * warpedY
                / (Underworld.LARGE_CAVE_MAIN_VERTICAL_RADIUS
                        * (double) Underworld.LARGE_CAVE_MAIN_VERTICAL_RADIUS);
        double envelope = 1.0 - horizontalEnvelope - verticalEnvelope;
        double cheese = fractalNoise(seed ^ CHEESE_SALT, warpedX / 44.0, warpedY / 32.0, warpedZ / 44.0);
        double detail = valueNoise(seed ^ DETAIL_SALT, warpedX / 20.0, warpedY / 16.0, warpedZ / 20.0);
        double chamber = envelope + cheese * 0.72 + detail * 0.15 - 0.18;

        double layerDistance = Math.abs(valueNoise(
                seed ^ LAYER_SALT, warpedX / 72.0, warpedY / 11.0, warpedZ / 72.0));
        double shelf = clamp01((0.12 - layerDistance) / 0.12) * 0.75;
        double pillarA = Math.abs(valueNoise(
                seed ^ PILLAR_A_SALT, warpedX / 30.0, warpedY / 100.0, warpedZ / 30.0));
        double pillarB = Math.abs(valueNoise(
                seed ^ PILLAR_B_SALT, warpedX / 30.0, warpedY / 100.0, warpedZ / 30.0));
        double pillar = clamp01((0.17 - Math.max(pillarA, pillarB)) / 0.17) * 1.40;
        double mainChamber = chamber - shelf - pillar;

        double horizontalDistance = Math.sqrt(horizontalDistanceSquared);
        double tunnelEnvelope = Math.min(
                (Underworld.LARGE_CAVE_OUTER_RADIUS - horizontalDistance) / 18.0,
                Math.min(
                        (worldY - Underworld.LARGE_CAVE_MIN_Y) / 12.0,
                        (Underworld.LARGE_CAVE_MAX_Y - worldY) / 12.0));
        double spaghettiRidge = Math.max(
                Math.abs(fractalNoise(
                        seed ^ SPAGHETTI_A_SALT, relativeX / 38.0, relativeY / 30.0, relativeZ / 38.0)),
                Math.abs(fractalNoise(
                        seed ^ SPAGHETTI_B_SALT, relativeX / 38.0, relativeY / 30.0, relativeZ / 38.0)));
        double spaghetti = Math.min(tunnelEnvelope, (0.105 - spaghettiRidge) * 5.0);
        double noodleRidge = Math.max(
                Math.abs(valueNoise(
                        seed ^ NOODLE_A_SALT, relativeX / 22.0, relativeY / 18.0, relativeZ / 22.0)),
                Math.abs(valueNoise(
                        seed ^ NOODLE_B_SALT, relativeX / 22.0, relativeY / 18.0, relativeZ / 22.0)));
        double noodle = Math.min(tunnelEnvelope, (0.06 - noodleRidge) * 7.0);
        double verticalBoundaryDistance = Math.min(
                worldY - Underworld.LARGE_CAVE_MIN_Y, Underworld.LARGE_CAVE_MAX_Y - worldY);
        double outerBoundaryDistance = Underworld.LARGE_CAVE_OUTER_RADIUS - horizontalDistance;
        double boundaryPenalty = clamp01((6.0 - verticalBoundaryDistance) / 6.0) * 0.35
                + clamp01((12.0 - outerBoundaryDistance) / 12.0) * 0.25;
        return Math.max(mainChamber, Math.max(spaghetti, noodle)) - boundaryPenalty;
    }

    private static double fractalNoise(long seed, double x, double y, double z) {
        return valueNoise(seed, x, y, z) * 0.625
                + valueNoise(seed ^ SECOND_OCTAVE_SALT, x * 2.0, y * 2.0, z * 2.0) * 0.25
                + valueNoise(seed ^ THIRD_OCTAVE_SALT, x * 4.0, y * 4.0, z * 4.0) * 0.125;
    }

    private static double valueNoise(long seed, double x, double y, double z) {
        int x0 = floor(x);
        int y0 = floor(y);
        int z0 = floor(z);
        double factorX = fade(x - x0);
        double factorY = fade(y - y0);
        double factorZ = fade(z - z0);
        double x00 = lerp(factorX, lattice(seed, x0, y0, z0), lattice(seed, x0 + 1, y0, z0));
        double x10 = lerp(factorX, lattice(seed, x0, y0 + 1, z0), lattice(seed, x0 + 1, y0 + 1, z0));
        double x01 = lerp(factorX, lattice(seed, x0, y0, z0 + 1), lattice(seed, x0 + 1, y0, z0 + 1));
        double x11 = lerp(
                factorX,
                lattice(seed, x0, y0 + 1, z0 + 1),
                lattice(seed, x0 + 1, y0 + 1, z0 + 1));
        return lerp(factorZ, lerp(factorY, x00, x10), lerp(factorY, x01, x11));
    }

    private static double lattice(long seed, int x, int y, int z) {
        long hash = mix(seed ^ x * X_HASH ^ y * Y_HASH ^ z * Z_HASH);
        return (hash >>> 11) * 0x1.0p-53 * 2.0 - 1.0;
    }

    private static long mix(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
    }

    private static double lerp(double factor, double first, double second) {
        return first + factor * (second - first);
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
