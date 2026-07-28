package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pixulse.infx.registry.InfXBlocks;
import net.minecraft.client.color.block.BlockColors;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.junit.jupiter.api.Test;

class PortalColorsTest {
    @Test
    void netherPortalUsesItsDedicatedRedTintSource() {
        BlockColors colors = new BlockColors();
        PortalColors.registerBlockTintSources(new RegisterColorHandlersEvent.BlockTintSources(colors));

        var source = colors.getTintSource(InfXBlocks.NETHER_PORTAL.get().defaultBlockState(), 0);
        assertNotNull(source);
        assertEquals(PortalColors.NETHER_PORTAL_TINT, source.color(InfXBlocks.NETHER_PORTAL.get().defaultBlockState()));
    }
}
