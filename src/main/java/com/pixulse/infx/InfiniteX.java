package com.pixulse.infx;

import com.mojang.logging.LogUtils;
import com.pixulse.infx.data.ModDataGenerators;
import com.pixulse.infx.harvest.HarvestEvents;
import com.pixulse.infx.crafting.TimedCraftingEvents;
import com.pixulse.infx.gametest.ModGameTests;
import com.pixulse.infx.registry.ModCreativeTabs;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModLootModifiers;
import com.pixulse.infx.registry.ModRecipes;
import com.pixulse.infx.registry.ModMenus;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(InfiniteX.MOD_ID)
public final class InfiniteX {
    public static final String MOD_ID = "infx";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InfiniteX(IEventBus modBus) {
        ModBlocks.register(modBus);
        ModDataComponents.register(modBus);
        ModItems.register(modBus);
        ModLootModifiers.register(modBus);
        ModRecipes.register(modBus);
        ModMenus.register(modBus);
        ModCreativeTabs.register(modBus);
        ModGameTests.register(modBus);
        modBus.addListener(ModDataGenerators::gatherData);
        HarvestEvents.register(NeoForge.EVENT_BUS);
        TimedCraftingEvents.register(NeoForge.EVENT_BUS);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
