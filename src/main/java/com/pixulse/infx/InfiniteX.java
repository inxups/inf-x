package com.pixulse.infx;

import com.mojang.logging.LogUtils;
import com.pixulse.infx.gametest.ModEquipmentGameTests;
import com.pixulse.infx.gametest.ModGameTests;
import com.pixulse.infx.gametest.ModMonsterGameTests;
import com.pixulse.infx.gametest.ModCompletionGameTests;
import com.pixulse.infx.registry.InfXBlockEntityTypes;
import com.pixulse.infx.registry.InfXAttachments;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXBiomeModifiers;
import com.pixulse.infx.registry.InfXPoiTypes;
import com.pixulse.infx.registry.InfXCreativeTabs;
import com.pixulse.infx.registry.InfXChunkGeneratorTypes;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXDensityFunctionTypes;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.InfXLootModifiers;
import com.pixulse.infx.registry.InfXRecipes;
import com.pixulse.infx.registry.InfXSounds;
import com.pixulse.infx.registry.InfXMenus;
import com.pixulse.infx.registry.InfXMobEffects;
import com.pixulse.infx.registry.InfXParticles;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(InfiniteX.MOD_ID)
public final class InfiniteX {
    public static final String MOD_ID = "infx";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InfiniteX(IEventBus modBus) {
        InfXSounds.register(modBus);
        InfXChunkGeneratorTypes.register(modBus);
        InfXDensityFunctionTypes.register(modBus);
        InfXBiomeModifiers.register(modBus);
        InfXAttachments.register(modBus);
        InfXParticles.register(modBus);
        InfXBlocks.register(modBus);
        InfXPoiTypes.register(modBus);
        InfXBlockEntityTypes.register(modBus);
        InfXDataComponents.register(modBus);
        // Entity types must register before spawn eggs bind via Item.Properties#spawnEgg.
        InfXEntityTypes.register(modBus);
        InfXItems.register(modBus);
        InfXLootModifiers.register(modBus);
        InfXRecipes.register(modBus);
        InfXMenus.register(modBus);
        InfXMobEffects.register(modBus);
        InfXCreativeTabs.register(modBus);
        ModGameTests.register(modBus);
        ModEquipmentGameTests.register(modBus);
        ModCompletionGameTests.register(modBus);
        ModMonsterGameTests.register(modBus);
        if (InfiniteXTestMode.isEnabled()) {
            LOGGER.warn("InfiniteX test mode is active; development overrides and vanilla server administration are enabled");
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
