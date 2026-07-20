package com.pixulse.infx;

import com.mojang.logging.LogUtils;
import com.pixulse.infx.data.ModDataGenerators;
import com.pixulse.infx.furnace.FurnaceEvents;
import com.pixulse.infx.harvest.HarvestEvents;
import com.pixulse.infx.crafting.TimedCraftingEvents;
import com.pixulse.infx.gametest.ModEquipmentGameTests;
import com.pixulse.infx.gametest.ModGameTests;
import com.pixulse.infx.gametest.ModMonsterGameTests;
import com.pixulse.infx.gametest.ModR196CompletionGameTests;
import com.pixulse.infx.registry.ModBlockEntityTypes;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.registry.ModBlocks;
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
import com.pixulse.infx.progression.R196Commands;
import com.pixulse.infx.progression.R196CreativeRestriction;
import com.pixulse.infx.progression.ModernContentAuditEvents;
import com.pixulse.infx.progression.R196AchievementEvents;
import com.pixulse.infx.equipment.R196EquipmentBehaviors;
import com.pixulse.infx.enchantment.R196EnchantmentEvents;
import com.pixulse.infx.item.R196ManureEvents;
import com.pixulse.infx.equipment.R196RustedIronSources;
import com.pixulse.infx.entity.R196MonsterEvents;
import com.pixulse.infx.entity.R196AnimalEvents;
import com.pixulse.infx.world.UnderworldPortalEvents;
import com.pixulse.infx.world.R196VillageProgression;
import com.pixulse.infx.world.R196MoonEvents;
import com.pixulse.infx.world.R196PhysicsEvents;
import com.pixulse.infx.world.R196EndEvents;
import com.pixulse.infx.world.R196SafeEvents;
import com.pixulse.infx.world.R196BucketEvents;
import com.pixulse.infx.world.R196StructureSafetyEvents;
import com.pixulse.infx.agriculture.R196AgricultureEvents;
import com.pixulse.infx.survival.R196SurvivalEvents;
import com.pixulse.infx.survival.R196FireCookingEvents;
import com.pixulse.infx.survival.R196FoodSourceEvents;
import com.pixulse.infx.network.R196Network;
import com.pixulse.infx.server.R196ServerRules;
import com.pixulse.infx.server.ExtremeDifficulty;

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
        R196Network.register(modBus);
        ModSounds.register(modBus);
        ModWorldCarvers.register(modBus);
        R196BucketEvents.register(modBus);
        ModAttachments.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntityTypes.register(modBus);
        ModDataComponents.register(modBus);
        ModItems.register(modBus);
        ModEntityTypes.register(modBus);
        ModLootModifiers.register(modBus);
        ModRecipes.register(modBus);
        ModMenus.register(modBus);
        ModMobEffects.register(modBus);
        ModCreativeTabs.register(modBus);
        ModGameTests.register(modBus);
        ModEquipmentGameTests.register(modBus);
        ModR196CompletionGameTests.register(modBus);
        ModMonsterGameTests.register(modBus);
        modBus.addListener(ModDataGenerators::gatherData);
        FurnaceEvents.register(NeoForge.EVENT_BUS);
        HarvestEvents.register(NeoForge.EVENT_BUS);
        TimedCraftingEvents.register(NeoForge.EVENT_BUS);
        ProgressionEvents.register(NeoForge.EVENT_BUS);
        R196EquipmentBehaviors.register(modBus);
        R196EnchantmentEvents.register(NeoForge.EVENT_BUS);
        R196ManureEvents.register(NeoForge.EVENT_BUS);
        R196RustedIronSources.register(NeoForge.EVENT_BUS);
        R196MonsterEvents.register(modBus, NeoForge.EVENT_BUS);
        R196AnimalEvents.register(NeoForge.EVENT_BUS);
        UnderworldPortalEvents.register(NeoForge.EVENT_BUS);
        R196VillageProgression.register(NeoForge.EVENT_BUS);
        R196MoonEvents.register(NeoForge.EVENT_BUS);
        R196PhysicsEvents.register(NeoForge.EVENT_BUS);
        R196EndEvents.register(NeoForge.EVENT_BUS);
        R196SafeEvents.register(NeoForge.EVENT_BUS);
        R196StructureSafetyEvents.register(NeoForge.EVENT_BUS);
        PlayerProgressionEvents.register(modBus, NeoForge.EVENT_BUS);
        R196SurvivalEvents.register(modBus, NeoForge.EVENT_BUS);
        R196FireCookingEvents.register(NeoForge.EVENT_BUS);
        R196FoodSourceEvents.register(NeoForge.EVENT_BUS);
        R196AgricultureEvents.register(NeoForge.EVENT_BUS);
        R196Commands.register(NeoForge.EVENT_BUS);
        ModernContentAuditEvents.register(NeoForge.EVENT_BUS);
        R196AchievementEvents.register(NeoForge.EVENT_BUS);
        R196ServerRules.register(NeoForge.EVENT_BUS);
        if (InfiniteXTestMode.isEnabled()) {
            LOGGER.warn("InfiniteX test mode is active; development overrides are enabled and multiplayer is disabled");
        } else {
            R196CreativeRestriction.register(NeoForge.EVENT_BUS);
            ExtremeDifficulty.register(NeoForge.EVENT_BUS);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
