package com.pixulse.infx.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.Test;

class SpawnGateCompatTest {
    private static final List<EntityType<?>> REPLACEABLE_TYPES = List.of(
            EntityType.BAT,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.CREEPER,
            EntityType.SLIME,
            EntityType.ENDERMAN,
            EntityType.SQUID,
            EntityType.COD,
            EntityType.SALMON,
            EntityType.PUFFERFISH,
            EntityType.TROPICAL_FISH,
            EntityType.WITCH,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.BLAZE,
            EntityType.GHAST,
            EntityType.MAGMA_CUBE,
            EntityType.COW,
            EntityType.CHICKEN,
            EntityType.SHEEP,
            EntityType.PIG,
            EntityType.HORSE,
            EntityType.OCELOT,
            EntityType.WOLF);

    @Test
    void emptyKeepTagLeavesReplacementIntact() {
        // Unit tests never load datapack tags: an unbound holder must read as "not kept",
        // preserving the vanilla replacement behavior.
        assertEquals(InfXEntityTypes.INFX_COD.get(), SpawnGate.replacementFor(EntityType.COD));
        assertEquals(InfXEntityTypes.INFX_SPIDER.get(), SpawnGate.replacementFor(EntityType.SPIDER));
    }

    @Test
    void masterSwitchDisablesEveryReplacement() {
        InfXConfig.INSTANCE.mobs.replaceVanillaMobs.setValue(false);
        try {
            for (EntityType<?> type : REPLACEABLE_TYPES) {
                assertNull(SpawnGate.replacementFor(type), "replaceVanillaMobs=false must keep " + type);
            }
        } finally {
            InfXConfig.INSTANCE.mobs.replaceVanillaMobs.setValue(true);
        }
        assertEquals(InfXEntityTypes.INFX_COD.get(), SpawnGate.replacementFor(EntityType.COD));
    }
}
