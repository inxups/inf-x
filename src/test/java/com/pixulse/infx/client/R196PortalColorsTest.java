package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pixulse.infx.registry.ModBlocks;
import net.minecraft.client.color.block.BlockColors;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.junit.jupiter.api.Test;

class R196PortalColorsTest {
    @Test
    void netherPortalUsesItsDedicatedRedTintSource() {
        BlockColors colors = new BlockColors();
        R196PortalColors.registerBlockTintSources(new RegisterColorHandlersEvent.BlockTintSources(colors));

        var source = colors.getTintSource(ModBlocks.NETHER_PORTAL.get().defaultBlockState(), 0);
        assertNotNull(source);
        assertEquals(R196PortalColors.NETHER_PORTAL_TINT, source.color(ModBlocks.NETHER_PORTAL.get().defaultBlockState()));
    }
}
