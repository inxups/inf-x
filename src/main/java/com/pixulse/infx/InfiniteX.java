package com.pixulse.infx;

import com.mojang.logging.LogUtils;
import com.pixulse.infx.data.ModDataGenerators;
import com.pixulse.infx.furnace.FurnaceEvents;
import com.pixulse.infx.harvest.HarvestEvents;
import com.pixulse.infx.crafting.TimedCraftingEvents;
import com.pixulse.infx.crafting.VanillaCraftingRecipeRemoval;
import com.pixulse.infx.gametest.ModEquipmentGameTests;
import com.pixulse.infx.gametest.ModGameTests;
import com.pixulse.infx.gametest.ModMonsterGameTests;
import com.pixulse.infx.gametest.ModCompletionGameTests;
import com.pixulse.infx.registry.ModBlockEntityTypes;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModBiomeModifiers;
import com.pixulse.infx.registry.ModPoiTypes;
import com.pixulse.infx.registry.ModCreativeTabs;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModLootModifiers;
import com.pixulse.infx.registry.ModRecipes;
import com.pixulse.infx.registry.ModSounds;
import com.pixulse.infx.registry.ModWorldCarvers;
import com.pixulse.infx.registry.ModMenus;
import com.pixulse.infx.registry.ModMobEffects;
import com.pixulse.infx.progression.ProgressionEvents;
import com.pixulse.infx.progression.PlayerProgressionEvents;
import com.pixulse.infx.progression.MiteCommands;
import com.pixulse.infx.progression.CreativeRestriction;
import com.pixulse.infx.progression.ModernContentAuditEvents;
import com.pixulse.infx.progression.AchievementEvents;
import com.pixulse.infx.equipment.EquipmentBehaviors;
import com.pixulse.infx.enchantment.EnchantmentEvents;
import com.pixulse.infx.item.BlockStackLimits;
import com.pixulse.infx.item.ManureEvents;
import com.pixulse.infx.equipment.RustedIronSources;
import com.pixulse.infx.entity.MonsterEvents;
import com.pixulse.infx.entity.AnimalEvents;
import com.pixulse.infx.entity.GelatinousCubeEvents;
import com.pixulse.infx.world.UnderworldPortalEvents;
import com.pixulse.infx.world.RunegateTeleportation;
import com.pixulse.infx.world.VillageProgression;
import com.pixulse.infx.world.MoonEvents;
import com.pixulse.infx.world.BedEvents;
import com.pixulse.infx.world.PhysicsEvents;
import com.pixulse.infx.world.EndEvents;
import com.pixulse.infx.world.SafeEvents;
import com.pixulse.infx.world.StructureSafetyEvents;
import com.pixulse.infx.agriculture.AgricultureEvents;
import com.pixulse.infx.agriculture.GrassTrampling;
import com.pixulse.infx.survival.SurvivalEvents;
import com.pixulse.infx.survival.FireCookingEvents;
import com.pixulse.infx.survival.FoodSourceEvents;
import com.pixulse.infx.network.Network;
import com.pixulse.infx.server.ServerRules;
import com.pixulse.infx.server.ExtremeDifficulty;
import com.pixulse.infx.curse.CurseEvents;

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
        Network.register(modBus);
        ModSounds.register(modBus);
        ModWorldCarvers.register(modBus);
        ModBiomeModifiers.register(modBus);
        ModAttachments.register(modBus);
        ModBlocks.register(modBus);
        ModPoiTypes.register(modBus);
        ModBlockEntityTypes.register(modBus);
        ModDataComponents.register(modBus);
        // Entity types must register before spawn eggs bind via Item.Properties#spawnEgg.
        ModEntityTypes.register(modBus);
        ModItems.register(modBus);
        BlockStackLimits.register(modBus);
        ModLootModifiers.register(modBus);
        ModRecipes.register(modBus);
        ModMenus.register(modBus);
        ModMobEffects.register(modBus);
        ModCreativeTabs.register(modBus);
        ModGameTests.register(modBus);
        ModEquipmentGameTests.register(modBus);
        ModCompletionGameTests.register(modBus);
        ModMonsterGameTests.register(modBus);
        modBus.addListener(ModDataGenerators::gatherData);
        FurnaceEvents.register(NeoForge.EVENT_BUS);
        HarvestEvents.register(NeoForge.EVENT_BUS);
        TimedCraftingEvents.register(NeoForge.EVENT_BUS);
        VanillaCraftingRecipeRemoval.register(NeoForge.EVENT_BUS);
        ProgressionEvents.register(NeoForge.EVENT_BUS);
        EquipmentBehaviors.register(modBus);
        EnchantmentEvents.register(NeoForge.EVENT_BUS);
        ManureEvents.register(NeoForge.EVENT_BUS);
        RustedIronSources.register(NeoForge.EVENT_BUS);
        MonsterEvents.register(modBus, NeoForge.EVENT_BUS);
        GelatinousCubeEvents.register(NeoForge.EVENT_BUS);
        AnimalEvents.register(NeoForge.EVENT_BUS);
        UnderworldPortalEvents.register(NeoForge.EVENT_BUS);
        RunegateTeleportation.register(NeoForge.EVENT_BUS);
        VillageProgression.register(NeoForge.EVENT_BUS);
        MoonEvents.register(NeoForge.EVENT_BUS);
        BedEvents.register(NeoForge.EVENT_BUS);
        CurseEvents.register(NeoForge.EVENT_BUS);
        PhysicsEvents.register(NeoForge.EVENT_BUS);
        EndEvents.register(NeoForge.EVENT_BUS);
        SafeEvents.register(NeoForge.EVENT_BUS);
        StructureSafetyEvents.register(NeoForge.EVENT_BUS);
        PlayerProgressionEvents.register(modBus, NeoForge.EVENT_BUS);
        SurvivalEvents.register(modBus, NeoForge.EVENT_BUS);
        FireCookingEvents.register(NeoForge.EVENT_BUS);
        FoodSourceEvents.register(NeoForge.EVENT_BUS);
        AgricultureEvents.register(NeoForge.EVENT_BUS);
        GrassTrampling.register(NeoForge.EVENT_BUS);
        MiteCommands.register(NeoForge.EVENT_BUS);
        ModernContentAuditEvents.register(NeoForge.EVENT_BUS);
        AchievementEvents.register(NeoForge.EVENT_BUS);
        ServerRules.register(NeoForge.EVENT_BUS);
        if (InfiniteXTestMode.isEnabled()) {
            LOGGER.warn("InfiniteX test mode is active; development overrides are enabled and online play is disabled");
        } else {
            CreativeRestriction.register(NeoForge.EVENT_BUS);
            ExtremeDifficulty.register(NeoForge.EVENT_BUS);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
