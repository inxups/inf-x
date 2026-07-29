package com.pixulse.infx.gametest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.authlib.GameProfile;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.recipe.BenchTier;
import com.pixulse.infx.recipe.TimedCraftingEngine;
import com.pixulse.infx.recipe.TimedCraftingMenu;
import com.pixulse.infx.entity.MiteChicken;
import com.pixulse.infx.item.equipment.EquipmentBehaviors;
import com.pixulse.infx.item.equipment.QualitySystem;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.entity.Livestock;
import com.pixulse.infx.item.MiteArrowItem;
import com.pixulse.infx.item.MiteBucketItem;
import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.screen.menu.MetalAnvilMenu;
import com.pixulse.infx.screen.menu.MiteEnchantmentMenu;
import com.pixulse.infx.screen.menu.TimedWorkbenchMenu;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXCreativeTabs;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXEnchantments;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.InfXAttachments;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXMenus;
import com.pixulse.infx.player.Experience;
import com.pixulse.infx.data.food.FoodProfile;
import com.pixulse.infx.data.food.FoodIngestion;
import com.pixulse.infx.data.food.FoodProfiles;
import com.pixulse.infx.data.food.SurvivalData;
import com.pixulse.infx.event.SurvivalEvents;
import com.pixulse.infx.data.food.SurvivalRules;
import com.pixulse.infx.network.Network;
import com.pixulse.infx.event.EndEvents;
import com.pixulse.infx.world.FluidDecayData;
import com.pixulse.infx.event.SafeEvents;
import com.pixulse.infx.world.SwimPhysics;
import com.pixulse.infx.world.SwimRules;
import com.pixulse.infx.world.Underworld;
import com.pixulse.infx.event.UnderworldPortalEvents;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModCompletionGameTests {
    private static final long DROP_RANDOM_MULTIPLIER = 341_873_128_712L;
    private static final long DROP_RANDOM_OFFSET = 132_897_987_541L;
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);
    private static final List<String> NAMES = List.of(
            "r196_special_behaviors",
            "r196_quality_coin",
            "r196_metal_anvil",
            "r196_underworld",
            "r196_portals",
            "r196_livestock",
            "r196_gravel_loot",
            "r196_hopper_xp",
            "r196_survival_core",
            "r196_sprinting_nutrition",
            "r196_survival_modes",
            "r196_behavior_hunger",
            "r196_safe_enchanting",
            "r196_enchantment_drops",
            "r196_creative_tabs",
            "r196_block_stack_limits",
            "r196_bucket_mechanics",
            "r196_swim_physics",
            "r196_fulltext_systems");
    private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger();

    static {
        FUNCTIONS.register("r196_special_behaviors", () -> ModCompletionGameTests::specialBehaviors);
        FUNCTIONS.register("r196_quality_coin", () -> ModCompletionGameTests::qualityAndCoin);
        FUNCTIONS.register("r196_metal_anvil", () -> ModCompletionGameTests::metalAnvil);
        FUNCTIONS.register("r196_underworld", () -> ModCompletionGameTests::underworld);
        FUNCTIONS.register("r196_portals", () -> ModCompletionGameTests::portals);
        FUNCTIONS.register("r196_livestock", () -> ModCompletionGameTests::livestock);
        FUNCTIONS.register("r196_gravel_loot", () -> ModCompletionGameTests::gravelLoot);
        FUNCTIONS.register("r196_hopper_xp", () -> ModCompletionGameTests::hopperExperience);
        FUNCTIONS.register("r196_survival_core", () -> ModCompletionGameTests::survivalCore);
        FUNCTIONS.register("r196_sprinting_nutrition", () -> ModCompletionGameTests::sprintingNutrition);
        FUNCTIONS.register("r196_survival_modes", () -> ModCompletionGameTests::survivalModes);
        FUNCTIONS.register("r196_behavior_hunger", () -> ModCompletionGameTests::behaviorHunger);
        FUNCTIONS.register("r196_safe_enchanting", () -> ModCompletionGameTests::safeAndEnchanting);
        FUNCTIONS.register("r196_enchantment_drops", () -> ModCompletionGameTests::enchantmentDrops);
        FUNCTIONS.register("r196_creative_tabs", () -> ModCompletionGameTests::creativeTabs);
        FUNCTIONS.register("r196_block_stack_limits", () -> ModCompletionGameTests::blockStackLimits);
        FUNCTIONS.register("r196_bucket_mechanics", () -> ModCompletionGameTests::bucketMechanics);
        FUNCTIONS.register("r196_swim_physics", () -> ModCompletionGameTests::swimPhysics);
        FUNCTIONS.register("r196_fulltext_systems", () -> ModCompletionGameTests::fulltextSystems);
    }

    private ModCompletionGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("r196_completion"), new TestEnvironmentDefinition.AllOf());
        for (String name : NAMES) {
            ResourceKey<Consumer<GameTestHelper>> function =
                    ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id(name));
            event.registerTest(
                    function.identifier(),
                    new FunctionGameTestInstance(
                            function,
                            new TestData<>(
                                    environment,
                                    Identifier.withDefaultNamespace("empty"),
                                    800,
                                    0,
                                    true,
                                    Rotation.NONE)));
        }
    }

    private static void specialBehaviors(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        var level = helper.getLevel();
        ItemStack silverSword = InfXItems.catalog()
                .equipment(MiteMaterial.SILVER, EquipmentType.SWORD)
                .holder()
                .toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, silverSword);

        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 2, 2));
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        float before = zombie.getHealth();
        zombie.hurtServer(level, level.damageSources().playerAttack(player), 4.0F);
        helper.assertTrue(Math.abs((before - zombie.getHealth()) - 5.0F) < .001F, "silver melee must deal 125% to undead");

        var cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(5, 2, 2));
        before = cow.getHealth();
        cow.hurtServer(level, level.damageSources().playerAttack(player), 4.0F);
        helper.assertTrue(Math.abs((before - cow.getHealth()) - 4.0F) < .001F, "silver melee must not boost normal targets");

        MiteArrowItem silverArrow = (MiteArrowItem) InfXItems.catalog()
                .equipment(MiteMaterial.SILVER, EquipmentType.ARROW)
                .holder()
                .get();
        AbstractArrow projectile = silverArrow.createArrow(level, silverArrow.getDefaultInstance(), player, null);
        var secondZombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 2, 2));
        secondZombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        before = secondZombie.getHealth();
        secondZombie.hurtServer(level, level.damageSources().arrow(projectile, player), 4.0F);
        helper.assertTrue(Math.abs((before - secondZombie.getHealth()) - 5.0F) < .001F, "silver arrows must deal 125% to undead");

        for (var entry : InfXItems.catalog().equipmentEntries()) {
            helper.assertTrue(
                    entry.holder().toStack().has(DataComponents.DAMAGE_RESISTANT)
                            == (entry.key().material() == MiteMaterial.ADAMANTIUM),
                    entry.path() + " fire resistance boundary");
        }
        for (var entry : InfXItems.catalog().rawEntries()) {
            boolean expected = entry.definition().material()
                    .map(material -> material == MiteMaterial.ADAMANTIUM)
                    .orElse(false);
            helper.assertTrue(
                    entry.holder().toStack().has(DataComponents.DAMAGE_RESISTANT) == expected,
                    entry.path() + " raw fire resistance boundary");
        }
        helper.assertTrue(InfXItems.ADAMANTIUM_ORE.toStack().has(DataComponents.DAMAGE_RESISTANT), "adamantium ore fire resistance");
        helper.assertTrue(InfXItems.ADAMANTIUM_BLOCK.toStack().has(DataComponents.DAMAGE_RESISTANT), "adamantium block fire resistance");
        helper.assertTrue(
                InfXBlocks.ADAMANTIUM_ANVIL.get().asItem().getDefaultInstance().has(DataComponents.DAMAGE_RESISTANT),
                "adamantium anvil fire resistance");

        ItemEntity adamantiumDrop = new ItemEntity(
                level, player.getX(), player.getY(), player.getZ(), InfXItems.ADAMANTIUM_INGOT.toStack());
        level.addFreshEntity(adamantiumDrop);
        helper.assertFalse(
                adamantiumDrop.hurtServer(level, level.damageSources().lava(), 10.0F),
                "adamantium item entity must reject lava damage");
        helper.assertTrue(adamantiumDrop.isAlive(), "adamantium item entity survives lava");
        ItemEntity ironDrop = new ItemEntity(
                level, player.getX() + 1, player.getY(), player.getZ(), new ItemStack(Items.IRON_INGOT));
        level.addFreshEntity(ironDrop);
        helper.assertTrue(
                ironDrop.hurtServer(level, level.damageSources().lava(), 10.0F),
                "iron still takes lava damage");
        helper.assertFalse(ironDrop.isAlive(), "iron item entity is destroyed by lava");

        helper.assertTrue(EquipmentBehaviors.armorDurabilityFactor(50, 100) == 1.0F, "half durability protection");
        helper.assertTrue(EquipmentBehaviors.armorDurabilityFactor(99, 100) < .03F, "critical durability protection");
        ItemStack fullChestplate = InfXItems.catalog()
                .equipment(MiteMaterial.IRON, EquipmentType.CHESTPLATE)
                .holder()
                .toStack();
        player.setItemSlot(EquipmentSlot.CHEST, fullChestplate);
        player.doTick();
        double fullArmor = player.getAttributeValue(Attributes.ARMOR);
        ItemStack criticalChestplate = fullChestplate.copy();
        criticalChestplate.setDamageValue(criticalChestplate.getMaxDamage() - 1);
        player.setItemSlot(EquipmentSlot.CHEST, criticalChestplate);
        player.doTick();
        double criticalArmor = player.getAttributeValue(Attributes.ARMOR);
        helper.assertTrue(fullArmor > 0.0 && criticalArmor < fullArmor * .03, "equipped armor decays near breaking");
        player.setItemSlot(EquipmentSlot.CHEST, fullChestplate.copy());
        player.doTick();
        helper.assertTrue(
                Math.abs(player.getAttributeValue(Attributes.ARMOR) - fullArmor) < .001,
                "repaired armor restores its attribute");
        removePlayer(player);
        helper.succeed();
    }

    private static void qualityAndCoin(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        ItemStack coins = InfXItems.catalog().raw("copper_coin").holder().toStack(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, coins);
        int beforeXp = player.totalExperience;
        coins.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(coins.getCount() == 1, "one server-side coin must be consumed");
        helper.assertTrue(player.totalExperience == beforeXp + 5, "copper coin must grant five XP");

        ItemStack pickaxe = InfXItems.IRON_PICKAXE.toStack();
        int baseDurability = pickaxe.getMaxDamage();
        int code = QualitySystem.toCode(Quality.FINE);
        QualitySystem.applySelectedQuality(pickaxe, code);
        helper.assertTrue(pickaxe.get(InfXDataComponents.QUALITY.get()) == Quality.FINE, "quality component persists");
        helper.assertTrue(pickaxe.getMaxDamage() == Math.round(baseDurability * 1.5F), "fine durability modifier");

        player.giveExperiencePoints(1_000);
        TimedCraftingMenu crafting = (TimedCraftingMenu) player.inventoryMenu;
        crafting.infx$craftingContainer().setItem(0, new ItemStack(Items.FLINT));
        crafting.infx$craftingContainer().setItem(1, new ItemStack(Items.STRING));
        crafting.infx$craftingContainer().setItem(2, new ItemStack(Items.STICK));
        crafting.infx$cycleResult(player);
        ItemStack qualityPreview = crafting.infx$resultContainer().getItem(0);
        helper.assertTrue(
                qualityPreview.is(InfXItems.catalog()
                        .equipment(MiteMaterial.FLINT, EquipmentType.KNIFE)
                        .holder()),
                "quality selection keeps the matched crafting result");
        helper.assertTrue(
                qualityPreview.get(InfXDataComponents.QUALITY.get()) == Quality.FINE,
                "server crafting preview carries selected quality");

        BlockPos workbench = helper.absolutePos(new BlockPos(8, 2, 4));
        helper.getLevel().setBlock(workbench, InfXBlocks.MITHRIL_WORKBENCH.get().defaultBlockState(), 3);
        TimedWorkbenchMenu runeCrafting = TimedWorkbenchMenu.server(
                78,
                player.getInventory(),
                BenchTier.MITHRIL,
                ContainerLevelAccess.create(helper.getLevel(), workbench),
                InfXBlocks.MITHRIL_WORKBENCH.get());
        player.containerMenu = runeCrafting;
        for (int slot : List.of(1, 3, 5, 7)) {
            runeCrafting.infx$craftingContainer().setItem(slot, InfXItems.MITHRIL_NUGGET.toStack());
        }
        runeCrafting.infx$craftingContainer().setItem(4, Items.OBSIDIAN.getDefaultInstance());
        helper.assertTrue(
                TimedCraftingEngine.refreshResult(runeCrafting, player, true),
                "mithril rune-stone recipe must produce a timed result");
        runeCrafting.clicked(0, 1, ContainerInput.PICKUP, player);
        ItemStack runePreview = runeCrafting.infx$resultContainer().getItem(0);
        helper.assertTrue(runePreview.is(InfXItems.MITHRIL_RUNE_STONE.get()), "right-click keeps rune-stone result");
        helper.assertTrue(RuneStoneBlock.rune(runePreview) == 1, "right-click advances the crafting rune type");
        removePlayer(player);
        helper.succeed();
    }

    private static void metalAnvil(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        BlockPos relative = new BlockPos(4, 2, 4);
        helper.setBlock(relative, InfXBlocks.IRON_ANVIL.get());
        BlockPos absolute = helper.absolutePos(relative);
        MetalAnvilBlock block = InfXBlocks.IRON_ANVIL.get();
        MetalAnvilBlockEntity entity = (MetalAnvilBlockEntity) helper.getLevel().getBlockEntity(absolute);

        MetalAnvilMenu menu = MetalAnvilMenu.server(
                77,
                player.getInventory(),
                MiteMaterial.IRON,
                ContainerLevelAccess.create(helper.getLevel(), absolute),
                block);
        ItemStack damaged = InfXItems.IRON_PICKAXE.toStack();
        damaged.setDamageValue(1_000);
        damaged.set(DataComponents.CUSTOM_NAME, Component.literal("Keep Me"));
        damaged.set(InfXDataComponents.QUALITY.get(), Quality.FINE);
        menu.getSlot(0).set(damaged);
        menu.getSlot(1).set(new ItemStack(Items.IRON_NUGGET, 3));
        ItemStack result = menu.getSlot(2).getItem();
        helper.assertFalse(result.isEmpty(), "valid metal repair must produce output");
        helper.assertTrue(result.getDamageValue() < damaged.getDamageValue(), "repair must restore durability");
        helper.assertTrue(result.get(DataComponents.CUSTOM_NAME).equals(Component.literal("Keep Me")), "custom name preserved");
        helper.assertTrue(result.get(InfXDataComponents.QUALITY.get()) == Quality.FINE, "quality preserved");
        menu.getSlot(2).onTake(player, result);
        helper.assertTrue(entity.damage() > 0, "repair must damage anvil by restored durability");

        int savedDamage = entity.damage();
        ItemStack dropped = Block.getDrops(helper.getBlockState(relative), helper.getLevel(), absolute, entity)
                .getFirst();
        helper.assertTrue(dropped.getDamageValue() == savedDamage, "anvil drop keeps persistent damage");
        BlockPos replayRelative = new BlockPos(5, 2, 4);
        helper.setBlock(replayRelative, block);
        BlockPos replayAbsolute = helper.absolutePos(replayRelative);
        block.setPlacedBy(
                helper.getLevel(),
                replayAbsolute,
                helper.getBlockState(replayRelative),
                player,
                dropped);
        MetalAnvilBlockEntity replayed =
                (MetalAnvilBlockEntity) helper.getLevel().getBlockEntity(replayAbsolute);
        helper.assertTrue(replayed.damage() == savedDamage, "placed anvil restores persistent damage");

        int beforeRodRepair = entity.damage();
        ItemStack fishingRod = InfXItems.catalog()
                .equipment(MiteMaterial.IRON, EquipmentType.FISHING_ROD)
                .holder()
                .toStack();
        fishingRod.setDamageValue(10);
        menu.getSlot(0).set(fishingRod);
        menu.getSlot(1).set(new ItemStack(Items.IRON_NUGGET));
        ItemStack repairedRod = menu.getSlot(2).getItem();
        helper.assertTrue(repairedRod.getDamageValue() == 0, "one nugget fully repairs a metal fishing rod");
        menu.getSlot(2).onTake(player, repairedRod);
        helper.assertTrue(
                entity.damage() == beforeRodRepair + 10 * 22,
                "metal fishing rod uses R196 anvil wear scaling");

        entity.addDamage(helper.getLevel(), block.maximumDamage() / 2);
        helper.assertTrue(helper.getBlockState(relative).getValue(MetalAnvilBlock.DAMAGE_STAGE) == 1, "50% anvil damage stage");
        removePlayer(player);
        helper.succeed();
    }

    private static void underworld(GameTestHelper helper) {
        var portalProbe = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(1, 2, 1));
        var registries = helper.getLevel().registryAccess();
        helper.assertTrue(helper.getLevel().getMinY() == -16, "Overworld bottom is Y=-16");
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        var underworldBiome = biomes.getOrThrow(Underworld.BIOME);
        for (var tag : List.of(
                BiomeTags.HAS_ANCIENT_CITY,
                BiomeTags.HAS_BURIED_TREASURE,
                BiomeTags.HAS_MINESHAFT,
                BiomeTags.HAS_MINESHAFT_MESA,
                BiomeTags.HAS_TRAIL_RUINS,
                BiomeTags.HAS_TRIAL_CHAMBERS)) {
            var structureBiomes = biomes.getOrThrow(tag);
            helper.assertTrue(structureBiomes.size() == 1, tag.location() + " only targets the Underworld");
            helper.assertTrue(
                    structureBiomes.contains(underworldBiome),
                    tag.location() + " includes the Underworld biome");
        }
        helper.assertTrue(
                biomes.getOrThrow(BiomeTags.HAS_STRONGHOLD).size() > 0,
                "stronghold biomes are restored for the End progression chain");
        helper.assertTrue(registries.lookupOrThrow(Registries.DIMENSION_TYPE).containsKey(Underworld.TYPE), "Underworld type registered");
        helper.assertTrue(registries.lookupOrThrow(Registries.BIOME).containsKey(Underworld.BIOME), "Underworld biome registered");
        helper.assertTrue(registries.lookupOrThrow(Registries.NOISE_SETTINGS).containsKey(Underworld.NOISE), "Underworld noise registered");

        // The dedicated return-spawn block stays in the Overworld and needs no second dimension.
        helper.assertTrue(helper.getLevel().getServer().getLevel(Underworld.LEVEL) == null, "GameTest has no custom levels");
        TeleportTransition transition = InfXBlocks.RETURN_SPAWN_PORTAL.get()
                .getPortalDestination(helper.getLevel(), portalProbe, portalProbe.blockPosition());
        helper.assertTrue(transition != null, "return-spawn portal must return a spawn transition");
        helper.assertTrue(transition.newLevel() == helper.getLevel(), "spawn route stays in the Overworld");

        BlockPos frameBase = helper.absolutePos(new BlockPos(14, 0, 14));
        frameBase = new BlockPos(frameBase.getX(), helper.getLevel().getMinY() + 1, frameBase.getZ());
        BlockPos eligibleOrigin = buildObsidianFrame(helper, frameBase, true);
        PortalShape eligibleShape = PortalShape.findEmptyPortalShape(
                        helper.getLevel(), eligibleOrigin, Direction.Axis.X)
                .orElseThrow();
        helper.assertTrue(
                UnderworldPortalEvents.tryCreateUnderworldPortal(
                        helper.getLevel(), eligibleOrigin, eligibleShape),
                "bottom-bedrock frame becomes an Underworld portal");
        helper.assertTrue(
                helper.getLevel().getBlockState(eligibleOrigin).is(InfXBlocks.UNDERWORLD_PORTAL.get()),
                "eligible portal interior converted");
        helper.assertTrue(
                InfXBlocks.UNDERWORLD_PORTAL.get()
                                .getPortalDestination(helper.getLevel(), portalProbe, eligibleOrigin)
                        == null,
                "bottom portal fails safely when the GameTest harness has no Underworld level");

        BlockPos raisedBase = frameBase.offset(6, 1, 0);
        BlockPos ineligibleOrigin = buildObsidianFrame(helper, raisedBase, false);
        PortalShape ineligibleShape = PortalShape.findEmptyPortalShape(
                        helper.getLevel(), ineligibleOrigin, Direction.Axis.X)
                .orElseThrow();
        helper.assertFalse(
                UnderworldPortalEvents.tryCreateUnderworldPortal(
                        helper.getLevel(), ineligibleOrigin, ineligibleShape),
                "raised frame must remain a Nether portal candidate");
        helper.assertFalse(
                helper.getLevel().getBlockState(ineligibleOrigin).is(InfXBlocks.UNDERWORLD_PORTAL.get()),
                "ineligible portal interior is not converted");
        helper.assertTrue(
                helper.getLevel().getBlockState(ineligibleOrigin).isAir(),
                "failed Underworld conversion must clear temporary portal faces");
        ineligibleShape.createPortalBlocks(helper.getLevel());
        helper.assertTrue(
                helper.getLevel().getBlockState(ineligibleOrigin).is(Blocks.NETHER_PORTAL),
                "legacy faces can be recreated for migration coverage");
        BlockPos migratedReturnArrival = InfXBlocks.RETURN_SPAWN_PORTAL.get()
                .findOrCreateArrivalPortal(helper.getLevel(), ineligibleOrigin);
        helper.assertTrue(
                helper.getLevel().getBlockState(ineligibleOrigin).is(InfXBlocks.RETURN_SPAWN_PORTAL.get()),
                "a compatible legacy portal is upgraded instead of opening another portal");
        helper.assertTrue(
                hasAdjacentPortal(helper, migratedReturnArrival, InfXBlocks.RETURN_SPAWN_PORTAL.get()),
                "legacy portal migration keeps the existing portal as the arrival point");

        BlockPos arrival = InfXBlocks.UNDERWORLD_PORTAL.get()
                .createArrivalPortal(helper.getLevel(), helper.absolutePos(new BlockPos(8, 2, 8)));
        helper.assertTrue(helper.getLevel().getBlockState(arrival.below()).is(Blocks.OBSIDIAN), "arrival has a floor");
        helper.assertTrue(
                helper.getLevel().getBlockState(arrival.relative(net.minecraft.core.Direction.NORTH))
                        .is(InfXBlocks.UNDERWORLD_PORTAL.get()),
                "arrival is beside the target portal");
        int underworldSurfaces = countPortalSurfaces(
                helper,
                InfXBlocks.UNDERWORLD_PORTAL.get(),
                arrival.offset(-8, -4, -8),
                arrival.offset(8, 4, 8));
        BlockPos reusedArrival = InfXBlocks.UNDERWORLD_PORTAL.get()
                .findOrCreateArrivalPortal(helper.getLevel(), arrival);
        helper.assertTrue(
                countPortalSurfaces(
                                helper,
                                InfXBlocks.UNDERWORLD_PORTAL.get(),
                                arrival.offset(-8, -4, -8),
                                arrival.offset(8, 4, 8))
                        == underworldSurfaces,
                "an existing Underworld portal is reused instead of creating another one");
        helper.assertTrue(
                hasAdjacentPortal(helper, reusedArrival, InfXBlocks.UNDERWORLD_PORTAL.get()),
                "reused arrival remains beside the existing Underworld portal");

        BlockPos netherArrival = InfXBlocks.NETHER_PORTAL.get()
                .createArrivalPortal(helper.getLevel(), arrival.offset(12, 0, 0));
        int netherSurfaces = countPortalSurfaces(
                helper,
                InfXBlocks.NETHER_PORTAL.get(),
                netherArrival.offset(-8, -4, -8),
                netherArrival.offset(8, 4, 8));
        BlockPos reusedNetherArrival = InfXBlocks.NETHER_PORTAL.get()
                .findOrCreateArrivalPortal(helper.getLevel(), arrival);
        helper.assertTrue(
                countPortalSurfaces(
                                helper,
                                InfXBlocks.NETHER_PORTAL.get(),
                                netherArrival.offset(-8, -4, -8),
                                netherArrival.offset(8, 4, 8))
                        == netherSurfaces,
                "a Nether portal reuses only an existing Nether portal");
        helper.assertTrue(
                hasAdjacentPortal(helper, reusedNetherArrival, InfXBlocks.NETHER_PORTAL.get()),
                "a Nether portal never reuses an Underworld portal surface");
        portalProbe.discard();
        helper.succeed();
    }

    private static void portals(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        BlockPos base = helper.absolutePos(new BlockPos(4, 2, 12));
        BlockPos origin = buildObsidianFrame(helper, base, false);
        PortalShape shape = PortalShape.findEmptyPortalShape(helper.getLevel(), origin, Direction.Axis.X)
                .orElseThrow();
        helper.assertTrue(
                UnderworldPortalEvents.tryCreateR196Portal(helper.getLevel(), origin, shape),
                "ordinary Overworld frame must become a return-spawn portal");
        helper.assertTrue(
                helper.getLevel().getBlockState(origin).is(InfXBlocks.RETURN_SPAWN_PORTAL.get()),
                "ordinary Overworld frame must not reuse the Underworld portal block");

        BlockPos[] corners = {
            base,
            base.offset(3, 0, 0),
            base.offset(0, 4, 0),
            base.offset(3, 4, 0)
        };
        for (int i = 0; i < corners.length; i++) {
            helper.getLevel().setBlock(
                    corners[i],
                    InfXBlocks.MITHRIL_RUNE_STONE.get()
                            .defaultBlockState()
                            .setValue(RuneStoneBlock.RUNE, i * 5),
                    3);
        }
        helper.assertTrue(
                UnderworldPortalBlock.hasRuneGate(helper.getLevel(), origin),
                "four same-material corner runes must override the ordinary gate");
        helper.assertTrue(
                helper.getLevel().getBlockState(origin).is(InfXBlocks.UNDERWORLD_PORTAL.get()),
                "a completed rune frame switches to the dedicated rune-capable portal block");
        helper.assertTrue(
                helper.getLevel().getBlockState(corners[3]).getValue(RuneStoneBlock.RUNE) == 15,
                "all sixteen rune states persist in the frame");
        helper.assertTrue(
                helper.getLevel().getBlockState(origin).getValue(UnderworldPortalBlock.RUNE_GATE),
                "four matching rune corners switch the active portal to the rune-gate surface");

        helper.getLevel().setBlock(
                corners[0], InfXBlocks.ADAMANTIUM_RUNE_STONE.get().defaultBlockState(), 3);
        helper.assertFalse(
                UnderworldPortalBlock.hasRuneGate(helper.getLevel(), origin),
                "mixed rune materials must not form a rune gate");
        helper.assertTrue(
                helper.getLevel().getBlockState(origin).is(InfXBlocks.RETURN_SPAWN_PORTAL.get()),
                "mixed rune corners restore the original return-spawn portal block");

        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        helper.assertTrue(player.gameMode.destroyBlock(origin), "creative mode must break a portal block");
        assertPortalInteriorCleared(helper, origin, "breaking one portal block must clear the whole portal");

        for (BlockPos corner : corners) {
            helper.getLevel().setBlock(corner, Blocks.OBSIDIAN.defaultBlockState(), 3);
        }
        PortalShape restoredShape = PortalShape.findEmptyPortalShape(helper.getLevel(), origin, Direction.Axis.X)
                .orElseThrow();
        helper.assertTrue(
                UnderworldPortalEvents.tryCreateR196Portal(helper.getLevel(), origin, restoredShape),
                "restored frame must recreate the portal");
        helper.assertTrue(
                player.gameMode.destroyBlock(base.offset(0, 2, 0)),
                "creative mode must break a portal frame block");
        assertPortalInteriorCleared(helper, origin, "breaking the portal frame must clear the whole portal");
        removePlayer(player);
        helper.succeed();
    }

    private static void livestock(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        var level = helper.getLevel();
        Cow cow = helper.spawn(InfXEntityTypes.R196_COW.get(), new BlockPos(2, 2, 8));
        helper.assertTrue(cow.getMaxHealth() == 20.0F, "R196 cows must have twenty health");
        helper.setBlock(new BlockPos(3, 1, 8), Blocks.WATER);
        ItemEntity wheat = new ItemEntity(
                level, cow.getX(), cow.getY(), cow.getZ(), new ItemStack(Items.WHEAT));
        level.addFreshEntity(wheat);

        setLivestockWellness(cow, 1.0F, 1.0F, 1.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        interactAt(player, cow);
        helper.assertTrue(player.getMainHandItem().is(Items.MILK_BUCKET), "healthy cow produces its daily milk bucket");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        interactAt(player, cow);
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a second same-day milk bucket must be denied");

        Cow bowlCow = helper.spawn(InfXEntityTypes.R196_COW.get(), new BlockPos(1, 2, 8));
        setLivestockWellness(bowlCow, 1.0F, 1.0F, 1.0F);
        for (int bowl = 0; bowl < 4; bowl++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
            interactAt(player, bowlCow);
            helper.assertTrue(
                    player.getMainHandItem().is(InfXItems.MILK_BOWL.get()),
                    "healthy cow must fill milk bowl " + (bowl + 1) + " of 4");
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
        interactAt(player, bowlCow);
        helper.assertTrue(
                player.getMainHandItem().is(Items.BOWL), "a fifth same-day milk bowl must be denied");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        interactAt(player, bowlCow);
        helper.assertTrue(
                player.getMainHandItem().is(Items.BUCKET),
                "after four bowls the daily quota is spent and buckets must be denied");

        setLivestockWellness(cow, 0.24F, 1.0F, 1.0F);
        List<ItemEntity> unwellCowDrops = new ArrayList<>(List.of(
                new ItemEntity(level, cow.getX(), cow.getY(), cow.getZ(), new ItemStack(Items.BEEF)),
                new ItemEntity(level, cow.getX(), cow.getY(), cow.getZ(), new ItemStack(Items.LEATHER))));
        NeoForge.EVENT_BUS.post(new LivingDropsEvent(
                cow, level.damageSources().playerAttack(player), unwellCowDrops, true));
        helper.assertTrue(
                itemCount(unwellCowDrops, Items.BEEF) == 0 && itemCount(unwellCowDrops, Items.LEATHER) == 1,
                "unwell MITE livestock keep leather but lose their meat yield");
        setLivestockWellness(cow, 1.0F, 1.0F, 1.0F);

        Sheep sheep = helper.spawn(InfXEntityTypes.R196_SHEEP.get(), new BlockPos(4, 2, 8));
        setLivestockWellness(sheep, 0.24F, 1.0F, 1.0F);
        helper.assertTrue(sheep.readyForShearing(), "MITE wool is gated by shearing state, not wellness");
        setLivestockWellness(sheep, 1.0F, 1.0F, 1.0F);
        helper.assertTrue(sheep.readyForShearing(), "well sheep must be shearable");
        sheep.hurtServer(level, level.damageSources().inFire(), 1.0F);
        helper.assertTrue(sheep.isSheared(), "fire damage must strip sheep wool");
        sheep.discard();

        Pig pig = helper.spawn(InfXEntityTypes.R196_PIG.get(), new BlockPos(5, 2, 8));
        helper.assertTrue(Livestock.isWell(pig), "R196 pigs must initialize their synced isWell flag");
        level.setBlockAndUpdate(pig.blockPosition().below(2), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pig.blockPosition().below(), Blocks.GRASS_BLOCK.defaultBlockState());
        setLivestockWellness(pig, 0.2F, 1.0F, 1.0F);
        helper.assertTrue(
                Livestock.update(level, pig).food() > 0.2F,
                "grass beneath livestock must improve the MITE food meter without a dropped breeding item");

        helper.setBlock(new BlockPos(5, 1, 6), Blocks.STONE);
        Pig feedingPig = helper.spawn(InfXEntityTypes.R196_PIG.get(), new BlockPos(5, 2, 6));
        feedingPig.goalSelector.removeAllGoals(goal -> true);
        feedingPig.setAge(6_000);
        setLivestockWellness(feedingPig, 0.2F, 1.0F, 1.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
        interactAt(player, feedingPig);
        helper.assertTrue(
                player.getMainHandItem().is(Items.CARROT)
                        && feedingPig.getPersistentData().getFloatOr("infx_livestock_food", -1.0F) == 0.2F,
                "breeding-cooldown interaction must neither consume food nor refresh the food meter");
        feedingPig.setAge(0);
        interactAt(player, feedingPig);
        helper.assertTrue(
                player.getMainHandItem().isEmpty()
                        && feedingPig.getPersistentData().getFloatOr("infx_livestock_food", 0.0F) > 0.2F,
                "a successful feeding interaction must consume food and restore the food meter");
        feedingPig.discard();

        // The empty template has no ground. Build the common livestock floor before spawning the
        // production chicken so its scheduled feather remains near its deterministic test site.
        for (int x = 4; x <= 14; x++) {
            for (int z = 4; z <= 14; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        BlockPos chickenPos = new BlockPos(6, 2, 8);
        MiteChicken chicken = helper.spawn(InfXEntityTypes.R196_CHICKEN.get(), chickenPos);
        chicken.setAge(0);
        setLivestockWellness(chicken, 1.0F, 1.0F, 1.0F);
        chicken.getPersistentData().putLong("infx_chicken_next_feather", level.getGameTime() - 1L);
        chicken.updateProduction(level);
        BlockPos cauldron = new BlockPos(7, 1, 8);
        helper.setBlock(cauldron, Blocks.WATER_CAULDRON);
        setLivestockWellness(chicken, 1.0F, 0.4F, 1.0F);
        helper.assertTrue(
                Livestock.update(level, chicken).water() >= 0.5F,
                "a nearby filled water cauldron must improve the MITE water meter");
        helper.setBlock(cauldron, Blocks.STONE);

        // Keep the panic assertion on settled animals with a deterministic open floor.
        BlockPos panicSourcePos = new BlockPos(8, 2, 9);
        BlockPos panickedChickenPos = new BlockPos(9, 2, 9);
        Cow panicSource = helper.spawn(InfXEntityTypes.R196_COW.get(), panicSourcePos);
        Chicken panickedChicken = helper.spawn(InfXEntityTypes.R196_CHICKEN.get(), panickedChickenPos);
        panicSource.goalSelector.removeAllGoals(goal -> true);
        panicSource.setDeltaMovement(Vec3.ZERO);
        panicSource.setOnGround(true);
        panickedChicken.goalSelector.removeAllGoals(goal -> true);
        panickedChicken.setDeltaMovement(Vec3.ZERO);
        panickedChicken.setOnGround(true);
        Livestock.LivestockPanicGoal panicGoal =
                new Livestock.LivestockPanicGoal(panickedChicken);

        // Build the dry approach before spawning the seeker so its first AI
        // tick cannot run while it is falling through uninitialized ground.
        for (int x = 7; x <= 11; x++) {
            for (int z = 3; z <= 5; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        BlockPos water = new BlockPos(11, 1, 4);
        helper.setBlock(water, Blocks.WATER);
        Livestock.update(level, cow);
        BlockPos grass = new BlockPos(11, 2, 3);
        helper.setBlock(grass.below(2), Blocks.STONE);
        helper.setBlock(grass.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(grass, Blocks.SHORT_GRASS);
        Cow foodSeeker = helper.spawn(InfXEntityTypes.R196_COW.get(), new BlockPos(8, 2, 3));
        foodSeeker.goalSelector.removeAllGoals(goal -> true);
        foodSeeker.setDeltaMovement(Vec3.ZERO);
        foodSeeker.setOnGround(true);
        setLivestockWellness(foodSeeker, 0.2F, 1.0F, 1.0F);
        Livestock.NeedsGoal foodGoal = new Livestock.NeedsGoal(foodSeeker);
        helper.assertTrue(foodGoal.canUse(), "hungry livestock must select a reachable grass approach");
        BlockPos foodTarget = foodGoal.selectedTarget();
        helper.assertTrue(
                foodTarget != null && foodTarget.distManhattan(helper.absolutePos(grass)) <= 2,
                "hungry livestock must target a standable position beside edible grass");
        foodGoal.start();
        helper.assertFalse(
                foodSeeker.getNavigation().isDone(),
                "grass-seeking livestock navigation must start");
        // The goal has selected a reachable grass-side position. Settle there before checking the
        // wellness effect so the assertion does not depend on variable physics-step timing.
        foodSeeker.getNavigation().stop();
        Vec3 foodTargetPosition = Vec3.atBottomCenterOf(foodTarget);
        foodSeeker.snapTo(foodTargetPosition.x, foodTargetPosition.y, foodTargetPosition.z, 0.0F, 0.0F);
        foodSeeker.setDeltaMovement(Vec3.ZERO);
        foodSeeker.setOnGround(true);
        float foodBeforeUpdate = foodSeeker.getPersistentData().getFloatOr("infx_livestock_food", 0.0F);
        helper.assertTrue(
                Livestock.update(level, foodSeeker).food() > foodBeforeUpdate,
                "grass-seeking livestock must improve its food meter at the selected source");

        Cow seeker = helper.spawn(InfXEntityTypes.R196_COW.get(), new BlockPos(8, 2, 4));
        // Keep the manually exercised goal in sole control. The production goal
        // is registered on join and can otherwise reach the water before this
        // assertion gets a chance to inspect its selected navigation target.
        seeker.goalSelector.removeAllGoals(goal -> true);
        // Ground navigation does not calculate until the first physics tick
        // marks a newly spawned mob as grounded. The test invokes the goal
        // directly, so establish that normal settled state explicitly.
        seeker.setDeltaMovement(Vec3.ZERO);
        seeker.setOnGround(true);
        setLivestockWellness(seeker, 1.0F, 0.2F, 1.0F);
        Livestock.Wellness thirsty = Livestock.update(level, seeker);
        helper.assertTrue(thirsty.water() < 0.5F, "seeker setup must begin thirsty");
        Livestock.NeedsGoal waterGoal = new Livestock.NeedsGoal(seeker);
        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    Livestock.Wellness needs = Livestock.update(level, cow);
                    helper.assertTrue(needs.water() >= 0.5F, "nearby water must satisfy livestock thirst");
                    helper.assertTrue(
                            needs.food() >= 0.5F && (wheat.getItem().isEmpty() || !wheat.isAlive()),
                            "livestock must consume suitable dropped food");
                    helper.assertTrue(
                            chicken.goalSelector.getAvailableGoals().stream().anyMatch(
                                    goal -> goal.getGoal() instanceof Livestock.LivestockPanicGoal),
                            "R196 livestock must install a goal that consumes the panic signal");
                    boolean wellBeforePanic = Livestock.isWell(panickedChicken);
                    helper.assertTrue(
                            panicSource.hurtServer(level, level.damageSources().playerAttack(player), 1.0F),
                            "the panic source must accept the test hit");
                    helper.assertTrue(
                            Livestock.isWell(panickedChicken) == wellBeforePanic,
                            "panic must not alter MITE food, water, or freedom");
                    helper.assertTrue(
                            Livestock.isPanicked(panicSource, level.getGameTime()),
                            "a successfully attacked R196 livestock animal must enter panic");
                    helper.assertTrue(
                            Livestock.isPanicked(panickedChicken, level.getGameTime()),
                            "nearby settled livestock must receive the panic signal");
                    helper.assertTrue(
                            Livestock.hasPanicMovementSpeedBoost(panicSource),
                            "panicked livestock must receive a temporary movement-speed increase");
                    panicSource.getPersistentData().putLong("infx_livestock_panic_until", level.getGameTime());
                    Livestock.serverTick(panicSource);
                    helper.assertFalse(
                            Livestock.hasPanicMovementSpeedBoost(panicSource),
                            "the panic movement-speed increase must end with the panic window");
                    Vec3 settledPanicPosition = helper.absoluteVec(Vec3.atBottomCenterOf(panickedChickenPos));
                    panickedChicken.snapTo(
                            settledPanicPosition.x,
                            settledPanicPosition.y,
                            settledPanicPosition.z,
                            0.0F,
                            0.0F);
                    panickedChicken.setDeltaMovement(Vec3.ZERO);
                    panickedChicken.setOnGround(true);
                    helper.assertTrue(
                            panicGoal.canUse(),
                            "panicked livestock must choose a reachable route away from the scare");
                    panicGoal.start();
                    helper.assertFalse(
                            panickedChicken.getNavigation().isDone(),
                            "the livestock panic goal must start escape navigation");
                    panicGoal.stop();
                    // The global hit now panics the whole nearby herd. Reset unrelated test
                    // actors before their manually driven production and needs assertions.
                    clearLivestockPanic(panicSource);
                    clearLivestockPanic(panickedChicken);
                    clearLivestockPanic(chicken);
                    clearLivestockPanic(foodSeeker);
                    clearLivestockPanic(seeker);
                })
                .thenExecute(foodSeeker::discard)
                .thenWaitUntil(() -> helper.assertTrue(
                        !level.getEntitiesOfClass(
                                        ItemEntity.class,
                                        new AABB(helper.absolutePos(chickenPos)).inflate(3.0),
                                        item -> item.getItem().is(Items.FEATHER))
                                .isEmpty(),
                        "healthy chickens must naturally shed feathers"))
                .thenWaitUntil(() -> helper.assertTrue(
                        waterGoal.canUse(),
                        "thirsty livestock must select a reachable water approach"))
                .thenExecute(() -> {
                    BlockPos selectedTarget = waterGoal.selectedTarget();
                    boolean targetTouchesWater = selectedTarget != null
                            && BlockPos.betweenClosedStream(
                                            selectedTarget.offset(-1, -1, -1),
                                            selectedTarget.offset(1, 1, 1))
                                    .anyMatch(pos -> level.getFluidState(pos)
                                            .is(net.minecraft.tags.FluidTags.WATER));
                    helper.assertTrue(
                            targetTouchesWater,
                            "thirsty livestock must path toward a standable position beside water");
                    waterGoal.start();
                    helper.assertFalse(
                            seeker.getNavigation().isDone(),
                            "water-seeking navigation must start");
                    // Path selection and navigation start are covered above. Settle at the chosen
                    // water-side target before checking the wellness effect so GameTest movement
                    // timing cannot make this unrelated assertion flaky.
                    seeker.getNavigation().stop();
                    Vec3 waterTarget = Vec3.atBottomCenterOf(selectedTarget);
                    seeker.snapTo(waterTarget.x, waterTarget.y, waterTarget.z, 0.0F, 0.0F);
                    seeker.setDeltaMovement(Vec3.ZERO);
                    seeker.setOnGround(true);
                    float waterBeforeUpdate =
                            seeker.getPersistentData().getFloatOr("infx_livestock_water", 0.0F);
                    helper.assertTrue(
                            Livestock.update(level, seeker).water() > waterBeforeUpdate,
                            "water-seeking livestock must improve its water meter at the selected source");
                })
                .thenExecute(() -> {
                    removePlayer(player);
                    seeker.discard();
                    panicSource.discard();
                    panickedChicken.discard();
                })
                .thenSucceed();
    }

    private static void gravelLoot(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        BlockPos relative = new BlockPos(4, 2, 4);
        helper.setBlock(relative, Blocks.GRAVEL);
        BlockPos absolute = helper.absolutePos(relative);
        int gravel = 0;
        int alternate = 0;
        boolean foundCopper = false;
        for (int sample = 0; sample < 2_048; sample++) {
            List<ItemStack> drops = Block.getDrops(
                    Blocks.GRAVEL.defaultBlockState(),
                    helper.getLevel(),
                    absolute,
                    null,
                    player,
                    player.getMainHandItem());
            helper.assertTrue(!drops.isEmpty(), "player-mined gravel must produce a real loot-table result");
            for (ItemStack stack : drops) {
                if (stack.is(Items.GRAVEL)) gravel += stack.getCount();
                else alternate += stack.getCount();
                if (stack.is(Items.COPPER_NUGGET)) foundCopper = true;
            }
        }
        double gravelRate = gravel / (double) (gravel + alternate);
        helper.assertTrue(gravelRate > .70 && gravelRate < .80, "real gravel rate must converge near 3/4: " + gravelRate);
        helper.assertTrue(foundCopper, "real gravel loot must reach the copper branch");
        removePlayer(player);
        helper.succeed();
    }

    private static void survivalCore(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        SurvivalEvents.recalculatePlayerLimits(player);
        helper.assertTrue(Math.abs(player.getMaxHealth() - 6.0F) < 0.001F, "level zero starts with three hearts");
        helper.assertTrue(Math.abs(SurvivalRules.foodCap(player.experienceLevel) - 6.0D) < 0.001D,
                "level zero starts with three food icons");

        Experience.setTotal(player, Experience.cumulativeForLevel(5));
        SurvivalEvents.recalculatePlayerLimits(player);
        helper.assertTrue(Math.abs(player.getMaxHealth() - 8.0F) < 0.001F,
                "level five adds one heart");
        helper.assertTrue(Math.abs(SurvivalRules.foodCap(player.experienceLevel) - 8.0D) < 0.001D,
                "level five adds one food icon");

        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(0, 2, 1, 1, 1, 0, 0));
        SurvivalData egg = player.getData(InfXAttachments.SURVIVAL)
                .eat(FoodProfile.mite(1, 3, 0, true, false, false), 8);
        helper.assertTrue(egg.satiation() == 1.0D && egg.nutrition() == 5.0D,
                "egg fills both energy layers");
        helper.assertTrue(egg.phytonutrients() == 1, "egg cannot cure phytonutrient malnutrition");

        FoodProfile bread = FoodProfiles.forStack(Items.BREAD.getDefaultInstance());
        helper.assertTrue(
                bread.satiation() == 8.0D && bread.nutrition() == 2.0D && bread.protein() == 0,
                "bread must keep the R196 8/2 energy value without animal nutrients");
        FoodProfile wheatSeeds = FoodProfiles.forStack(Items.WHEAT_SEEDS.getDefaultInstance());
        helper.assertTrue(
                wheatSeeds.essentialFats() == 2_000 && wheatSeeds.nutrition() == 0.0D,
                "wheat seeds retain R196's independent essential-fat bonus");
        FoodProfile driedKelp = FoodProfiles.forStack(Items.DRIED_KELP.getDefaultInstance());
        helper.assertTrue(
                driedKelp.satiation() == 0.0D && driedKelp.nutrition() == 1.0D,
                "dried kelp must restore nutrition without satiation");
        var redMushroomEffects = consumeStatusEffects(Items.RED_MUSHROOM.getDefaultInstance());
        helper.assertTrue(
                redMushroomEffects.stream().anyMatch(effect -> effect.is(MobEffects.POISON)
                        && effect.getDuration() == 200
                        && effect.getAmplifier() == 0),
                "red mushroom must apply R196 poison I for 200 ticks");
        helper.assertTrue(
                redMushroomEffects.stream().anyMatch(effect -> effect.is(MobEffects.NAUSEA)
                        && effect.getDuration() == 1_200
                        && effect.getAmplifier() == 4),
                "red mushroom must apply R196 nausea V for 1200 ticks");
        var goldenAppleEffects = consumeStatusEffects(Items.GOLDEN_APPLE.getDefaultInstance());
        helper.assertTrue(
                goldenAppleEffects.size() == 1
                        && goldenAppleEffects.stream().anyMatch(effect -> effect.is(MobEffects.REGENERATION)
                                && effect.getDuration() == 1_200
                                && effect.getAmplifier() == 0),
                "golden apple must apply only R196 regeneration I for 1200 ticks");
        var enchantedGoldenAppleEffects = consumeStatusEffects(Items.ENCHANTED_GOLDEN_APPLE.getDefaultInstance());
        helper.assertTrue(
                enchantedGoldenAppleEffects.size() == 3
                        && enchantedGoldenAppleEffects.stream().anyMatch(effect -> effect.is(MobEffects.REGENERATION)
                                && effect.getDuration() == 1_200
                                && effect.getAmplifier() == 1)
                        && enchantedGoldenAppleEffects.stream().anyMatch(effect -> effect.is(MobEffects.RESISTANCE)
                                && effect.getDuration() == 1_200
                                && effect.getAmplifier() == 0)
                        && enchantedGoldenAppleEffects.stream().anyMatch(effect -> effect.is(MobEffects.FIRE_RESISTANCE)
                                && effect.getDuration() == 1_200
                                && effect.getAmplifier() == 0),
                "enchanted golden apple must use only the three R196 1200-tick effects");
        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(8, 8, 0, 1, 1, 0, 0));
        helper.assertTrue(
                FoodIngestion.canIngest(player, FoodProfiles.forStack(Items.EGG.getDefaultInstance())),
                "protein deficiency must permit a protein food even when energy is full");
        helper.assertFalse(
                FoodIngestion.canIngest(player, FoodProfiles.forStack(Items.APPLE.getDefaultInstance())),
                "a full player cannot consume unrelated food while only protein is deficient");
        var eggConsumable = Items.EGG.getDefaultInstance().get(DataComponents.CONSUMABLE);
        var appleConsumable = Items.APPLE.getDefaultInstance().get(DataComponents.CONSUMABLE);
        helper.assertTrue(
                eggConsumable != null && eggConsumable.canConsume(player, Items.EGG.getDefaultInstance()),
                "Consumable gate must permit a curative R196 food at full energy");
        helper.assertFalse(
                appleConsumable != null && appleConsumable.canConsume(player, Items.APPLE.getDefaultInstance()),
                "Consumable gate must reject a non-curative R196 food at full energy");

        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(0, 0, 1, 1, 1, 96_000, 0));
        SurvivalEvents.applyFood(player, Items.APPLE.getDefaultInstance());
        SurvivalData diabeticApple = player.getData(InfXAttachments.SURVIVAL);
        helper.assertTrue(
                diabeticApple.satiation() == 1.0D && diabeticApple.insulinResponse() == 100_800,
                "moderate insulin resistance must reduce sugary satiation while retaining its response");
        helper.assertTrue(player.hasEffect(MobEffects.NAUSEA), "sugary food must apply R196 insulin nausea");
        player.removeEffect(MobEffects.NAUSEA);
        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(0, 8, 1, 1, 1, 0, 0));
        helper.assertTrue(player.canEat(false), "depleted satiation permits eating even when nutrition is full");
        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(8, 8, 1, 1, 1, 0, 0));
        helper.assertFalse(player.canEat(false), "both full energy layers prevent eating");
        helper.assertTrue(
                Items.SUGAR.getDefaultInstance().has(DataComponents.FOOD)
                        && Items.SUGAR.getDefaultInstance().has(DataComponents.CONSUMABLE),
                "small R196 foods are directly edible");

        ItemStack waterBowl = InfXItems.WATER_BOWL.toStack();
        var waterBowlConsumable = waterBowl.get(DataComponents.CONSUMABLE);
        helper.assertTrue(
                waterBowlConsumable != null
                        && waterBowlConsumable.animation() == ItemUseAnimation.DRINK
                        && waterBowlConsumable.sound() == SoundEvents.GENERIC_DRINK
                        && waterBowlConsumable.canConsume(player, waterBowl),
                "water bowl must be drinkable with the generic drinking sound");
        helper.assertTrue(
                waterBowl.finishUsingItem(helper.getLevel(), player).is(Items.BOWL),
                "drinking a water bowl must return an empty bowl");

        ItemStack milkBowl = InfXItems.MILK_BOWL.toStack();
        var milkBowlConsumable = milkBowl.get(DataComponents.CONSUMABLE);
        helper.assertTrue(
                milkBowlConsumable != null
                        && milkBowlConsumable.animation() == ItemUseAnimation.DRINK
                        && milkBowlConsumable.sound() == SoundEvents.GENERIC_DRINK
                        && milkBowlConsumable.canConsume(player, milkBowl),
                "milk bowl must use the drinking animation and sound");
        helper.assertTrue(
                milkBowl.finishUsingItem(helper.getLevel(), player).is(Items.BOWL),
                "drinking a milk bowl must return an empty bowl");

        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(8, 2, 1, 1, 1, 0, 0));
        SurvivalEvents.recalculatePlayerLimits(player);
        helper.assertTrue(
                player.getFoodData().getFoodLevel() == 2
                        && Math.abs(player.getFoodData().getSaturationLevel() - 8.0F) < 0.001F,
                "FoodData compatibility mirror must preserve independent satiation");
        player.getFoodData().addExhaustion(40.0F);
        player.getFoodData().tick(player);
        helper.assertTrue(
                player.getFoodData().getFoodLevel() == 2
                        && Math.abs(player.getFoodData().getSaturationLevel() - 8.0F) < 0.001F,
                "vanilla FoodData tick must not mutate R196 energy layers");
        removePlayer(player);
        helper.succeed();
    }

    private static void sprintingNutrition(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(0.5, 0, 1, 1, 1, 0, 0));
        player.setSprinting(true);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertFalse(player.isSprinting(), "empty Nutrition must stop sprinting");
        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(0, 0.5, 1, 1, 1, 0, 0));
        player.setSprinting(true);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.isSprinting(), "remaining Nutrition must permit sprinting");
        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(0, 0, 1, 1, 1, 0, 0));
        player.setSprinting(true);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertFalse(player.isSprinting(), "empty Nutrition must stop sprinting");
        removePlayer(player);
        helper.succeed();
    }

    private static List<MobEffectInstance> consumeStatusEffects(ItemStack stack) {
        var consumable = stack.get(DataComponents.CONSUMABLE);
        return consumable == null
                ? List.of()
                : consumable.onConsumeEffects().stream()
                        .filter(ApplyStatusEffectsConsumeEffect.class::isInstance)
                        .map(ApplyStatusEffectsConsumeEffect.class::cast)
                        .flatMap(effect -> effect.effects().stream())
                        .toList();
    }

    private static void survivalModes(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        helper.onEachTick(player::doTick);
        SurvivalData frozen = new SurvivalData(6, 6, 1_000, 1_000, 1_000, 100, 0);
        player.setData(InfXAttachments.SURVIVAL, frozen);
        player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);

        helper.startSequence()
                .thenExecuteAfter(40, () -> {
                    helper.assertTrue(
                            player.getData(InfXAttachments.SURVIVAL).equals(frozen),
                            "spectator mode must freeze R196 energy and nutrient metabolism");
                    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                })
                .thenExecuteAfter(40, () -> {
                    SurvivalData active = player.getData(InfXAttachments.SURVIVAL);
                    helper.assertTrue(active.hungerProgress() > 0.0D, "survival mode must accumulate hunger");
                    helper.assertTrue(active.protein() < frozen.protein(), "survival mode must decay nutrients");
                    removePlayer(player);
                })
                .thenSucceed();
    }

    private static void behaviorHunger(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        player.tickCount = 1;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));

        resetBehaviorHunger(player);
        player.awardStat(Stats.CUSTOM.get(Stats.WALK_ONE_CM), 100);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.0025D, "walking one block uses the R196 distance cost");

        resetBehaviorHunger(player);
        player.awardStat(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM), 100);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.0125D, "sprinting one block is five times walking");

        resetBehaviorHunger(player);
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        player.awardStat(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM), 100);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.0D, "creative movement is never billed after returning to survival");

        resetBehaviorHunger(player);
        player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent(player));
        player.awardStat(Stats.CUSTOM.get(Stats.WALK_ONE_CM), 100);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.0D, "spectator movement and jumps remain exempt");

        resetBehaviorHunger(player);
        player.setSprinting(false);
        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent(player));
        assertBehaviorHunger(helper, player, 0.05D, "a normal jump uses the R196 one-off cost");
        resetBehaviorHunger(player);
        player.setSprinting(true);
        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent(player));
        assertBehaviorHunger(helper, player, 0.2D, "a sprint jump uses the larger R196 cost");
        player.setSprinting(false);

        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(3, 2, 1));
        resetBehaviorHunger(player);
        NeoForge.EVENT_BUS.post(new AttackEntityEvent(player, cow));
        assertBehaviorHunger(helper, player, 0.075D, "attacking uses the R196 endurance action cost");

        BlockPos miningRelative = new BlockPos(2, 2, 1);
        BlockPos miningPos = helper.absolutePos(miningRelative);
        helper.setBlock(miningRelative, Blocks.STONE);
        player.setItemInHand(InteractionHand.MAIN_HAND, InfXItems.catalog()
                .equipment(MiteMaterial.COPPER, EquipmentType.PICKAXE)
                .holder()
                .toStack());
        helper.assertTrue(player.isWithinBlockInteractionRange(miningPos, 1.0D), "mining target is in interaction range");
        helper.assertTrue(helper.getLevel().mayInteract(player, miningPos), "mining target permits world interaction");
        helper.assertFalse(
                helper.getLevel().getServer().isUnderSpawnProtection(helper.getLevel(), miningPos, player),
                "mining target is outside spawn protection");
        helper.assertFalse(
                player.blockActionRestricted(helper.getLevel(), miningPos, player.gameMode.getGameModeForPlayer()),
                "survival player is allowed to start this block action");
        helper.assertTrue(
                helper.getBlockState(miningRelative).getDestroyProgress(player, helper.getLevel(), miningPos) > 0.0F,
                "mining target has positive destroy progress");
        resetBehaviorHunger(player);
        PlayerInteractEvent.LeftClickBlock miningStart = new PlayerInteractEvent.LeftClickBlock(
                player, miningPos, Direction.UP, PlayerInteractEvent.LeftClickBlock.Action.START);
        NeoForge.EVENT_BUS.post(miningStart);
        helper.assertFalse(miningStart.isCanceled(), "valid mining start event remains uncanceled");
        assertBehaviorHunger(helper, player, 0.0025D, "starting a valid mining session charges one mining tick");
        player.tickCount++;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.005D, "holding a mining session continues charging per tick");
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(
                player, miningPos, Direction.UP, PlayerInteractEvent.LeftClickBlock.Action.STOP));
        player.tickCount++;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.005D, "stopping mining ends the continuous cost");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.setBlock(miningRelative, Blocks.STONE);
        resetBehaviorHunger(player);
        PlayerInteractEvent.LeftClickBlock unrestrictedMiningStart = new PlayerInteractEvent.LeftClickBlock(
                player, miningPos, Direction.UP, PlayerInteractEvent.LeftClickBlock.Action.START);
        NeoForge.EVENT_BUS.post(unrestrictedMiningStart);
        helper.assertFalse(unrestrictedMiningStart.isCanceled(),
                "mining input validation remains delegated to vanilla");
        assertBehaviorHunger(helper, player, 0.0025D,
                "an unrestricted block click starts mining metabolism");
        player.tickCount++;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.005D,
                "an unrestricted mining session continues charging per tick");
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(
                player, miningPos, Direction.UP, PlayerInteractEvent.LeftClickBlock.Action.STOP));

        BlockPos placeRelative = new BlockPos(2, 2, 2);
        BlockPos placePos = helper.absolutePos(placeRelative);
        helper.setBlock(placeRelative, Blocks.AIR);
        BlockSnapshot snapshot = BlockSnapshot.create(helper.getLevel().dimension(), helper.getLevel(), placePos);
        helper.setBlock(placeRelative, Blocks.STONE);
        resetBehaviorHunger(player);
        NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(
                snapshot, Blocks.COBBLESTONE.defaultBlockState(), player));
        assertBehaviorHunger(helper, player, 0.375D, "placing stone uses its hardness-based R196 cost");

        helper.setBlock(placeRelative, Blocks.DIRT);
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.WOODEN_HOE.getDefaultInstance());
        UseOnContext tillContext = new UseOnContext(
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(placePos), Direction.UP, placePos, false));
        BlockEvent.BlockToolModificationEvent tillEvent = new BlockEvent.BlockToolModificationEvent(
                Blocks.DIRT.defaultBlockState(), tillContext, ItemAbilities.HOE_TILL, false);
        tillEvent.setFinalState(Blocks.FARMLAND.defaultBlockState());
        resetBehaviorHunger(player);
        NeoForge.EVENT_BUS.post(tillEvent);
        assertBehaviorHunger(helper, player, 0.0625D, "successful tilling uses half the source hardness");

        player.setItemInHand(InteractionHand.MAIN_HAND, Items.BOW.getDefaultInstance());
        player.startUsingItem(InteractionHand.MAIN_HAND);
        resetBehaviorHunger(player);
        player.tickCount++;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.0025D, "drawing a bow charges every held tick");
        player.stopUsingItem();

        var boat = helper.spawn(EntityType.OAK_BOAT, new BlockPos(1, 2, 2));
        player.startRiding(boat, true, false);
        player.setLastClientInput(new Input(true, false, false, false, false, false, false));
        resetBehaviorHunger(player);
        player.tickCount++;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.0025D, "the controlling rower pays for forward input");
        player.setLastClientInput(Input.EMPTY);
        player.tickCount++;
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        assertBehaviorHunger(helper, player, 0.0025D, "a stationary boat has no passive hunger cost");
        player.stopRiding();
        boat.discard();

        resetBehaviorHunger(player);
        DamageContainer damage = new DamageContainer(helper.getLevel().damageSources().cactus(), 1.0F);
        NeoForge.EVENT_BUS.post(new LivingDamageEvent.Post(player, damage));
        assertBehaviorHunger(helper, player, 0.075D, "armor-applicable damage uses the R196 damage cost");

        helper.setBlock(miningRelative, Blocks.AIR);
        helper.setBlock(placeRelative, Blocks.AIR);
        cow.discard();
        removePlayer(player);
        helper.succeed();
    }

    private static void resetBehaviorHunger(ServerPlayer player) {
        player.setData(InfXAttachments.SURVIVAL, new SurvivalData(6, 6, 1_000, 1_000, 1_000, 0, 0));
    }

    private static void assertBehaviorHunger(
            GameTestHelper helper, ServerPlayer player, double expected, String message) {
        double actual = player.getData(InfXAttachments.SURVIVAL).hungerProgress();
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-7D, message + ": " + actual);
    }

    private static void safeAndEnchanting(GameTestHelper helper) {
        ServerPlayer owner = createPlayer(helper);
        ServerPlayer visitor = createPlayer(helper);
        assertR196EnchantmentRegistry(helper);
        BlockPos safePos = new BlockPos(4, 2, 4);
        helper.setBlock(safePos, InfXBlocks.COPPER_SAFE.get());
        SafeBlockEntity safe = helper.getBlockEntity(safePos, SafeBlockEntity.class);
        helper.assertTrue(safe.canOpen(visitor), "unowned village safes are publicly accessible");
        safe.setOwner(owner);
        helper.assertTrue(safe.canOpen(owner), "safe owner can open");
        helper.assertFalse(safe.canOpen(visitor), "other players cannot open safe");
        owner.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        float ownerSafeProgress = helper.getBlockState(safePos)
                .getDestroyProgress(owner, helper.getLevel(), helper.absolutePos(safePos));
        helper.assertTrue(
                Math.abs(ownerSafeProgress - 1.0F / 128.0F) < 1.0E-6F,
                "a strongbox owner must receive the portable 128-tick progress: " + ownerSafeProgress);
        helper.assertTrue(owner.gameMode.destroyBlock(helper.absolutePos(safePos)),
                "MITE strongbox owners can carry their safe by hand");
        List<ItemEntity> ownerSafeDrops = helper.getLevel().getEntities(
                EntityType.ITEM,
                new AABB(helper.absolutePos(safePos)).inflate(2.0),
                entity -> entity.getItem().is(InfXBlocks.COPPER_SAFE.get().asItem()));
        helper.assertTrue(ownerSafeDrops.size() == 1,
                "a strongbox owner must recover the portable safe item");
        ownerSafeDrops.forEach(ItemEntity::discard);

        helper.setBlock(safePos, InfXBlocks.COPPER_SAFE.get());
        safe = helper.getBlockEntity(safePos, SafeBlockEntity.class);
        safe.setOwner(owner);
        visitor.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        visitor.setItemInHand(InteractionHand.MAIN_HAND, InfXItems.catalog()
                .equipment(MiteMaterial.SILVER, EquipmentType.PICKAXE).holder().toStack());
        helper.assertFalse(visitor.gameMode.destroyBlock(helper.absolutePos(safePos)),
                "a foreign copper safe rejects another level-two metal");
        visitor.setItemInHand(InteractionHand.MAIN_HAND, InfXItems.IRON_PICKAXE.toStack());
        visitor.setOnGround(true);
        visitor.getFoodData().setFoodLevel(20);
        visitor.experienceLevel = 0;
        float safeHardness = helper.getBlockState(safePos)
                .getDestroySpeed(helper.getLevel(), helper.absolutePos(safePos));
        float expectedVisitorProgress = new EquipmentKey(MiteMaterial.IRON, EquipmentType.PICKAXE)
                        .miningSpeed()
                / safeHardness
                / 512.0F;
        float visitorProgress = helper.getBlockState(safePos)
                .getDestroyProgress(visitor, helper.getLevel(), helper.absolutePos(safePos));
        helper.assertTrue(
                Math.abs(visitorProgress - expectedVisitorProgress) < 1.0E-6F,
                "a qualified visitor must keep MITE /512 progress without losing harvest capability: "
                        + visitorProgress);
        helper.assertTrue(visitor.gameMode.destroyBlock(helper.absolutePos(safePos)),
                "a foreign copper safe accepts MITE level-three iron");
        helper.assertTrue(
                helper.getLevel()
                        .getEntities(
                                EntityType.ITEM,
                                new AABB(helper.absolutePos(safePos)).inflate(2.0),
                                entity -> entity.getItem().is(InfXBlocks.COPPER_SAFE.get().asItem()))
                        .isEmpty(),
                "MITE foreign strongboxes break without dropping the safe item");

        helper.setBlock(safePos, InfXBlocks.COPPER_SAFE.get());
        safe = helper.getBlockEntity(safePos, SafeBlockEntity.class);
        safe.setOwner(owner);
        safe.setItem(0, new ItemStack(Items.IRON_INGOT));
        helper.assertTrue(
                safe.getSlotsForFace(Direction.DOWN).length == 0
                        && !safe.canTakeItemThroughFace(0, safe.getItem(0), Direction.DOWN)
                        && !safe.canPlaceItemThroughFace(0, new ItemStack(Items.IRON_INGOT), Direction.UP),
                "MITE strongboxes expose no hopper slots");
        helper.assertTrue(
                !SafeEvents.mayDropSafeItem(null, safe),
                "explosions must not drop the safe block item");
        var safeShape = helper.getBlockState(safePos).getShape(helper.getLevel(), helper.absolutePos(safePos));
        helper.assertTrue(
                Math.abs(safeShape.max(net.minecraft.core.Direction.Axis.Y) - 0.875D) < 1.0E-6D,
                "safe collision height matches MITE chest bounds: " + safeShape.max(net.minecraft.core.Direction.Axis.Y));

        BlockPos tableRelative = new BlockPos(8, 2, 8);
        helper.setBlock(tableRelative, InfXBlocks.DIAMOND_ENCHANTING_TABLE.get());
        BlockPos table = helper.absolutePos(tableRelative);
        helper.setBlock(tableRelative.above(), Blocks.STONE);
        BlockHitResult tableHit = new BlockHitResult(Vec3.atCenterOf(table), Direction.UP, table, false);
        InteractionResult obstructedTableInteraction = helper.getBlockState(tableRelative)
                .useWithoutItem(helper.getLevel(), owner, tableHit);
        helper.assertTrue(
                obstructedTableInteraction == InteractionResult.FAIL,
                "an obstructed enchanting table must reject opening");
        helper.assertTrue(
                owner.containerMenu == owner.inventoryMenu,
                "an obstructed enchanting table must not open a menu");
        helper.setBlock(tableRelative.above(), Blocks.AIR);
        for (int y = 0; y <= 1; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if ((Math.abs(x) == 2 || Math.abs(z) == 2)
                            && !(Math.abs(x) == 2 && Math.abs(z) == 2)) {
                        helper.getLevel().setBlockAndUpdate(table.offset(x, y, z), Blocks.BOOKSHELF.defaultBlockState());
                    }
                }
            }
        }
        helper.assertTrue(MiteEnchantmentMenu.bookshelfCount(helper.getLevel(), table) == 24,
                "R196 enchanting structure counts twenty-four shelves");
        MiteEnchantmentMenu menu = new MiteEnchantmentMenu(
                1,
                owner.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), table),
                MiteEnchantmentMenu.Kind.DIAMOND);
        helper.assertTrue(menu.getType() == InfXMenus.DIAMOND_ENCHANTING.get(),
                "diamond table synchronizes its custom client menu type");
        helper.assertTrue(menu.getSlot(1).mayPlace(Items.DIAMOND.getDefaultInstance()),
                "diamond table accepts diamonds");
        helper.assertFalse(menu.getSlot(1).mayPlace(Items.EMERALD.getDefaultInstance()),
                "diamond table rejects emeralds");
        helper.assertFalse(menu.getSlot(1).mayPlace(Items.LAPIS_LAZULI.getDefaultInstance()),
                "R196 tables reject vanilla lapis fuel");

        ItemStack copperTool = InfXItems.catalog()
                .equipment(MiteMaterial.COPPER, EquipmentType.PICKAXE)
                .holder()
                .toStack();
        menu.getSlot(0).setByPlayer(copperTool);
        helper.assertTrue(menu.costs[2] == EnchantmentRules.experienceCost(53),
                "a full diamond table must reduce copper's 30 enchantability to 53 effective power");

        ItemStack mithrilTool = InfXItems.catalog()
                .equipment(MiteMaterial.MITHRIL, EquipmentType.PICKAXE)
                .holder()
                .toStack();
        menu.getSlot(0).setByPlayer(mithrilTool.copy());
        helper.assertTrue(menu.costs[2] == EnchantmentRules.experienceCost(100),
                "a full diamond table must preserve mithril's 100 enchantability");
        helper.assertTrue(menu.enchantClue[2] >= 0,
                "a 100-strength mithril option must have a selectable enchantment");
        int seedBeforeReinsert = menu.getEnchantmentSeed();
        menu.getSlot(0).setByPlayer(ItemStack.EMPTY);
        menu.getSlot(0).setByPlayer(mithrilTool.copy());
        helper.assertTrue(menu.getEnchantmentSeed() != seedBeforeReinsert,
                "reinserting an item must refresh the enchanting seed");
        helper.assertTrue(menu.enchantClue[2] >= 0,
                "reinserting an item must produce a selectable enchantment");
        menu.getSlot(1).setByPlayer(new ItemStack(Items.DIAMOND, 3));
        Experience.setTotal(owner, menu.costs[2]);
        helper.assertTrue(owner.experienceLevel < 100,
                "10,000 raw XP must not require one hundred displayed levels");
        helper.assertTrue(menu.clickMenuButton(owner, 2),
                "a 10,000-XP third option must be enchantable with three diamonds");
        helper.assertTrue(owner.totalExperience == 0,
                "the 10,000-XP option must deduct its exact raw experience cost");
        helper.assertTrue(menu.getSlot(0).getItem().isEnchanted(),
                "the 10,000-XP option must apply an enchantment");
        helper.assertTrue(menu.getSlot(1).getItem().isEmpty(),
                "the 10,000-XP option must consume its three diamonds");

        menu.getSlot(0).setByPlayer(Items.BOOK.getDefaultInstance());
        helper.assertTrue(menu.costs[2] == EnchantmentRules.experienceCost(53),
                "books must use MITE's fixed enchantability of 30 instead of vanilla's value of one");

        helper.setBlock(tableRelative, InfXBlocks.EMERALD_ENCHANTING_TABLE.get());
        MiteEnchantmentMenu emeraldMenu = new MiteEnchantmentMenu(
                2,
                owner.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), table),
                MiteEnchantmentMenu.Kind.EMERALD);
        emeraldMenu.getSlot(0).setByPlayer(mithrilTool.copy());
        helper.assertTrue(emeraldMenu.costs[2] == EnchantmentRules.experienceCost(50),
                "the same mithril tool must be limited to 50 power at a full emerald table");

        helper.setBlock(tableRelative, InfXBlocks.DIAMOND_ENCHANTING_TABLE.get());
        ItemStack enchantingTool = InfXItems.catalog()
                .equipment(MiteMaterial.ADAMANTIUM, EquipmentType.PICKAXE)
                .holder()
                .toStack();
        menu.getSlot(0).setByPlayer(enchantingTool);
        menu.getSlot(1).setByPlayer(new ItemStack(Items.DIAMOND, 3));
        int experienceCost = menu.costs[2];
        helper.assertTrue(experienceCost == EnchantmentRules.experienceCost(65),
                "adamantium's 40 enchantability must reduce full diamond power to 65");
        helper.assertTrue(menu.enchantClue[2] >= 0,
                "the adamantium pickaxe must have a selectable enchantment");
        Experience.setTotal(owner, experienceCost - 1);
        helper.assertFalse(menu.clickMenuButton(owner, 2),
                "one missing experience point must reject enchanting");
        helper.assertTrue(owner.totalExperience == experienceCost - 1,
                "a rejected enchantment must not consume experience");
        helper.assertTrue(menu.getSlot(1).getItem().getCount() == 3,
                "a rejected enchantment must not consume its diamonds");
        Experience.setTotal(owner, experienceCost);
        helper.assertTrue(owner.experienceLevel < 65,
                "the raw XP test player must remain below the old level requirement");
        helper.assertTrue(menu.clickMenuButton(owner, 2),
                "raw experience must permit enchanting without the matching player level");
        helper.assertTrue(owner.totalExperience == 0,
                "enchanting must deduct the exact raw experience cost");
        helper.assertTrue(menu.getSlot(0).getItem().isEnchanted(),
                "a successful raw-XP purchase must enchant the item");
        helper.assertTrue(menu.getSlot(1).getItem().isEmpty(),
                "a successful third option must consume three diamonds");
        assertConversionOptions(
                helper,
                owner,
                menu,
                PotionContents.createItemStack(Items.POTION, Potions.WATER),
                InfXItems.BOTTLE_OF_DISENCHANTING.get(),
                Items.DIAMOND,
                "diamond water conversion");
        helper.setBlock(tableRelative, InfXBlocks.EMERALD_ENCHANTING_TABLE.get());
        assertConversionOptions(
                helper,
                owner,
                emeraldMenu,
                Items.GOLDEN_APPLE.getDefaultInstance(),
                Items.ENCHANTED_GOLDEN_APPLE,
                Items.EMERALD,
                "emerald golden-apple conversion");
        owner.setItemInHand(InteractionHand.MAIN_HAND, InfXItems.catalog()
                .equipment(MiteMaterial.ADAMANTIUM, EquipmentType.PICKAXE).holder().toStack());
        helper.assertTrue(EndEvents.hasAdamantiumCrystalTool(owner),
                "adamantium pickaxe meets crystal gate");
        removePlayer(visitor);
        removePlayer(owner);
        helper.succeed();
    }

    private static void assertR196EnchantmentRegistry(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Set<Holder<Enchantment>> expected = Set.copyOf(
                InfXEnchantments.ALL.stream().map(enchantments::getOrThrow).toList());
        for (var source : List.of(
                EnchantmentTags.IN_ENCHANTING_TABLE,
                EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT,
                EnchantmentTags.ON_TRADED_EQUIPMENT,
                EnchantmentTags.ON_RANDOM_LOOT,
                EnchantmentTags.TRADEABLE,
                EnchantmentTags.TRADES_DESERT_COMMON,
                EnchantmentTags.TRADES_JUNGLE_COMMON,
                EnchantmentTags.TRADES_PLAINS_COMMON,
                EnchantmentTags.TRADES_SAVANNA_COMMON,
                EnchantmentTags.TRADES_SNOW_COMMON,
                EnchantmentTags.TRADES_SWAMP_COMMON,
                EnchantmentTags.TRADES_TAIGA_COMMON)) {
            var actual = enchantments.getOrThrow(source);
            helper.assertTrue(
                    actual.size() == expected.size(),
                    source.location() + " has exactly 39 R196 and vanilla MITE entries");
            helper.assertTrue(actual.stream().allMatch(expected::contains), source.location() + " excludes modern entries");
        }
        for (ResourceKey<Enchantment> key : InfXEnchantments.ALL) {
            Holder<Enchantment> enchantment = enchantments.getOrThrow(key);
            helper.assertTrue(
                    enchantment.value().exclusiveSet().contains(enchantment),
                    key.identifier() + " is self-exclusive");
            helper.assertFalse(
                    Enchantment.areCompatible(enchantment, enchantment),
                    key.identifier() + " cannot be selected twice");
        }
        Holder<Enchantment> silkTouch = enchantments.getOrThrow(InfXEnchantments.VANILLA_SILK_TOUCH);
        Holder<Enchantment> fortune = enchantments.getOrThrow(InfXEnchantments.FORTUNE);
        Holder<Enchantment> efficiency = enchantments.getOrThrow(InfXEnchantments.VANILLA_EFFICIENCY);
        helper.assertFalse(
                Enchantment.areCompatible(silkTouch, fortune),
                "silk touch and fortune stay mutually exclusive");
        helper.assertTrue(
                Enchantment.areCompatible(silkTouch, efficiency),
                "silk touch combines with any other enchantment");
    }

    private static void assertConversionOptions(
            GameTestHelper helper,
            ServerPlayer player,
            MiteEnchantmentMenu menu,
            ItemStack input,
            Item result,
            Item currency,
            String description) {
        for (int option = 0; option < 3; option++) {
            menu.getSlot(0).setByPlayer(input.copy());
            menu.getSlot(1).setByPlayer(new ItemStack(currency, option + 1));
            int experienceCost = EnchantmentRules.experienceCost(2);
            helper.assertTrue(menu.costs[option] == experienceCost, description + " option " + option + " costs 200 XP");
            helper.assertTrue(menu.enchantClue[option] == -1, description + " option " + option + " has no enchantment choice");
            Experience.setTotal(player, experienceCost);
            helper.assertTrue(menu.clickMenuButton(player, option), description + " option " + option + " completes");
            helper.assertTrue(menu.getSlot(0).getItem().is(result), description + " yields its fixed result");
            helper.assertTrue(player.totalExperience == 0, description + " spends its exact XP cost");
            helper.assertTrue(menu.getSlot(1).getItem().isEmpty(), description + " consumes its exact currency tier");
        }
    }

    private static void enchantmentDrops(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack butcheringTool = InfXItems.catalog()
                .equipment(MiteMaterial.COPPER, EquipmentType.SWORD)
                .holder()
                .toStack();
        butcheringTool.enchant(enchantments.getOrThrow(InfXEnchantments.BUTCHERING), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, butcheringTool);
        assertButcheringDrop(
                helper,
                player,
                helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(2, 2, 2)),
                Items.BEEF,
                "cow");
        assertButcheringDrop(
                helper,
                player,
                helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 2, 2)),
                Items.PORKCHOP,
                "pig");
        assertButcheringDrop(
                helper,
                player,
                helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(4, 2, 2)),
                Items.MUTTON,
                "sheep");
        assertButcheringDrop(
                helper,
                player,
                helper.spawnWithNoFreeWill(EntityType.HORSE, new BlockPos(5, 2, 2)),
                Items.BEEF,
                "horse");
        assertButcheringDrop(
                helper,
                player,
                helper.spawnWithNoFreeWill(EntityType.SPIDER, new BlockPos(6, 2, 2)),
                Items.SPIDER_EYE,
                "spider");

        ItemStack harvestingScythe = InfXItems.catalog()
                .equipment(MiteMaterial.COPPER, EquipmentType.SCYTHE)
                .holder()
                .toStack();
        harvestingScythe.enchant(enchantments.getOrThrow(InfXEnchantments.HARVESTING), 5);
        ItemStack harvestingShovel = InfXItems.catalog()
                .equipment(MiteMaterial.COPPER, EquipmentType.SHOVEL)
                .holder()
                .toStack();
        harvestingShovel.enchant(enchantments.getOrThrow(InfXEnchantments.HARVESTING), 5);
        BlockPos cropPos = helper.absolutePos(new BlockPos(8, 2, 2));
        helper.assertTrue(
                Enchantments.level(helper.getLevel(), harvestingScythe, InfXEnchantments.HARVESTING) == 5,
                "harvesting test tool carries level five");
        assertHarvestingDrops(helper, player, harvestingScythe, matureCropState(Blocks.WHEAT), Items.WHEAT, Items.WHEAT_SEEDS, cropPos, "wheat");
        assertHarvestingDrops(helper, player, harvestingShovel, matureCropState(Blocks.CARROTS), Items.CARROT, null, cropPos, "carrot");
        assertHarvestingDrops(helper, player, harvestingShovel, matureCropState(Blocks.POTATOES), Items.POTATO, null, cropPos, "potato");
        assertHarvestingDrops(helper, player, harvestingShovel, matureCropState(Blocks.BEETROOTS), Items.BEETROOT, Items.BEETROOT_SEEDS, cropPos, "beetroot");
        List<ItemEntity> nonCropDrops = List.of(new ItemEntity(
                helper.getLevel(), cropPos.getX(), cropPos.getY(), cropPos.getZ(), new ItemStack(Items.NETHER_WART)));
        List<ItemEntity> mutableNonCropDrops = new ArrayList<>(nonCropDrops);
        NeoForge.EVENT_BUS.post(new BlockDropsEvent(
                helper.getLevel(),
                cropPos,
                Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 3),
                null,
                mutableNonCropDrops,
                player,
                harvestingShovel));
        helper.assertTrue(itemCount(mutableNonCropDrops, Items.NETHER_WART) == 1, "harvesting does not multiply nether wart");

        ItemStack fortuneTool = InfXItems.catalog()
                .equipment(MiteMaterial.COPPER, EquipmentType.PICKAXE)
                .holder()
                .toStack();
        fortuneTool.enchant(enchantments.getOrThrow(InfXEnchantments.FORTUNE), 3);
        BlockPos fortunePos = helper.absolutePos(new BlockPos(10, 2, 2));
        assertFortuneAddsDrop(helper, player, fortuneTool, Blocks.DIAMOND_ORE.defaultBlockState(), Items.DIAMOND, fortunePos, "diamond ore");
        assertFortuneAddsDrop(helper, player, fortuneTool, Blocks.REDSTONE_ORE.defaultBlockState(), Items.REDSTONE, fortunePos, "redstone ore");
        assertFortuneAddsDrop(helper, player, fortuneTool, Blocks.NETHER_GOLD_ORE.defaultBlockState(), Items.GOLD_NUGGET, fortunePos, "nether gold ore");
        assertNetherWartFortune(helper, player, fortuneTool, fortunePos);
        assertGrassFortune(helper, player, fortuneTool, fortunePos);
        List<ItemEntity> dirtDrops = new ArrayList<>(List.of(new ItemEntity(
                helper.getLevel(), fortunePos.getX(), fortunePos.getY(), fortunePos.getZ(), new ItemStack(Items.DIRT))));
        NeoForge.EVENT_BUS.post(new BlockDropsEvent(
                helper.getLevel(), fortunePos, Blocks.DIRT.defaultBlockState(), null, dirtDrops, player, fortuneTool));
        helper.assertTrue(itemCount(dirtDrops, Items.DIRT) == 1, "fortune does not multiply unrelated blocks");
        removePlayer(player);
        helper.succeed();
    }

    private static void assertButcheringDrop(
            GameTestHelper helper, ServerPlayer player, LivingEntity target, Item expected, String description) {
        boolean found = false;
        for (long seed = 0; seed < 128 && !found; seed++) {
            target.getRandom().setSeed(seed);
            List<ItemEntity> drops = new ArrayList<>();
            drops.add(new ItemEntity(target.level(), target.getX(), target.getY(), target.getZ(), new ItemStack(Items.APPLE)));
            NeoForge.EVENT_BUS.post(new LivingDropsEvent(
                    target, helper.getLevel().damageSources().playerAttack(player), drops, true));
            if (itemCount(drops, expected) > 0) {
                helper.assertTrue(itemCount(drops, Items.APPLE) == 1, description + " does not duplicate an unrelated food drop");
                found = true;
            }
        }
        helper.assertTrue(found, description + " receives its MITE butchering drop");
        target.discard();
    }

    private static void assertHarvestingDrops(
            GameTestHelper helper,
            ServerPlayer player,
            ItemStack tool,
            BlockState state,
            Item product,
            Item seed,
            BlockPos pos,
            String description) {
        helper.assertTrue(tool.isCorrectToolForDrops(state), description + " uses the correct harvesting tool");
        boolean found = false;
        for (long randomSeed = 0; randomSeed < 128 && !found; randomSeed++) {
            helper.getLevel().getRandom().setSeed(dropRandomSeed(randomSeed));
            List<ItemEntity> drops = new ArrayList<>();
            drops.add(new ItemEntity(helper.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(product)));
            if (seed != null) {
                drops.add(new ItemEntity(helper.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(seed)));
            }
            NeoForge.EVENT_BUS.post(new BlockDropsEvent(helper.getLevel(), pos, state, null, drops, player, tool));
            if (itemCount(drops, product) > 1) {
                if (seed != null) {
                    helper.assertTrue(itemCount(drops, seed) == 1, description + " leaves its seed drop unchanged");
                }
                found = true;
            }
        }
        helper.assertTrue(found, description + " gains only mature-crop product");
    }

    private static void assertFortuneAddsDrop(
            GameTestHelper helper,
            ServerPlayer player,
            ItemStack tool,
            BlockState state,
            Item product,
            BlockPos pos,
            String description) {
        boolean found = false;
        for (long randomSeed = 0; randomSeed < 128 && !found; randomSeed++) {
            helper.getLevel().getRandom().setSeed(dropRandomSeed(randomSeed));
            List<ItemEntity> drops = new ArrayList<>(List.of(new ItemEntity(
                    helper.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(product))));
            NeoForge.EVENT_BUS.post(new BlockDropsEvent(helper.getLevel(), pos, state, null, drops, player, tool));
            found = itemCount(drops, product) > 1;
        }
        helper.assertTrue(found, description + " receives MITE fortune bonus drops");
    }

    private static void assertNetherWartFortune(
            GameTestHelper helper, ServerPlayer player, ItemStack tool, BlockPos pos) {
        boolean found = false;
        BlockState state = Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 3);
        for (long randomSeed = 0; randomSeed < 128 && !found; randomSeed++) {
            helper.getLevel().getRandom().setSeed(dropRandomSeed(randomSeed));
            List<ItemEntity> drops = new ArrayList<>(List.of(new ItemEntity(
                    helper.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.NETHER_WART))));
            NeoForge.EVENT_BUS.post(new BlockDropsEvent(helper.getLevel(), pos, state, null, drops, player, tool));
            found = itemCount(drops, Items.NETHER_WART) > 1;
        }
        helper.assertTrue(found, "mature nether wart receives MITE fortune bonus drops");
    }

    private static void assertGrassFortune(GameTestHelper helper, ServerPlayer player, ItemStack tool, BlockPos pos) {
        helper.assertTrue(
                helper.getLevel().getBiome(pos).value().getBaseTemperature() > 0.15F,
                "GameTest grass position supports MITE worm drops");
        boolean found = false;
        for (long randomSeed = 0; randomSeed < 256 && !found; randomSeed++) {
            helper.getLevel().getRandom().setSeed(dropRandomSeed(randomSeed));
            List<ItemEntity> drops = new ArrayList<>(List.of(new ItemEntity(
                    helper.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.DIRT))));
            NeoForge.EVENT_BUS.post(new BlockDropsEvent(
                    helper.getLevel(), pos, Blocks.GRASS_BLOCK.defaultBlockState(), null, drops, player, tool));
            if (itemCount(drops, InfXItems.WORM.get()) > 0) {
                helper.assertTrue(itemCount(drops, Items.DIRT) == 0, "grass fortune replaces dirt with a worm");
                found = true;
            }
        }
        helper.assertTrue(found, "fortune raises the grass worm drop chance");
    }

    private static BlockState matureCropState(Block cropBlock) {
        CropBlock crop = (CropBlock) cropBlock;
        return crop.getStateForAge(crop.getMaxAge());
    }

    private static int itemCount(List<ItemEntity> drops, Item item) {
        return drops.stream()
                .filter(drop -> drop.getItem().is(item))
                .mapToInt(drop -> drop.getItem().getCount())
                .sum();
    }

    private static long dropRandomSeed(long sample) {
        return sample * DROP_RANDOM_MULTIPLIER + DROP_RANDOM_OFFSET;
    }

    private static void hopperExperience(GameTestHelper helper) {
        BlockPos furnacePos = new BlockPos(6, 3, 6);
        BlockPos hopperPos = furnacePos.below();
        helper.setBlock(
                furnacePos,
                Blocks.FURNACE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.NORTH));
        helper.setBlock(
                hopperPos,
                Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        FurnaceBlockEntity furnace = helper.getBlockEntity(furnacePos, FurnaceBlockEntity.class);
        HopperBlockEntity hopper = helper.getBlockEntity(hopperPos, HopperBlockEntity.class);
        furnace.setItem(0, new ItemStack(Items.RAW_GOLD, 2));
        furnace.setItem(1, Items.COAL.getDefaultInstance());
        BlockPos absolute = helper.absolutePos(furnacePos);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        hopper.getItem(0).is(Items.GOLD_INGOT) && hopper.getItem(0).getCount() >= 2,
                        "hopper must extract both furnace outputs"))
                .thenWaitUntil(() -> helper.assertTrue(
                        !helper.getLevel().getEntitiesOfClass(
                                        ExperienceOrb.class,
                                        new AABB(absolute).inflate(4.0))
                                .isEmpty(),
                        "automated output must pop accumulated XP at the furnace mouth"))
                .thenSucceed();
    }

    private static void creativeTabs(GameTestHelper helper) {
        List<CreativeModeTab> tabs = List.of(
                InfXCreativeTabs.MAIN.get(),
                InfXCreativeTabs.INGREDIENTS.get(),
                InfXCreativeTabs.FOOD_AND_CONSUMABLES.get(),
                InfXCreativeTabs.TOOLS_AND_UTILITIES.get(),
                InfXCreativeTabs.COMBAT_AND_EQUIPMENT.get(),
                InfXCreativeTabs.SPAWN_EGGS.get());
        // Blocks expand two rune stones (16 each): 45 - 2 + 32 = 75.
        // Tools: 135 baseline + 42 mob buckets + 7 powder-snow buckets = 184.
        List<Integer> expectedSizes = List.of(75, 31, 24, 184, 113, 52);
        CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(
                helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess());
        List<ItemStack> displayed = new ArrayList<>();

        for (int index = 0; index < tabs.size(); index++) {
            CreativeModeTab tab = tabs.get(index);
            tab.buildContents(parameters);
            helper.assertTrue(
                    !tab.getDisplayItems().isEmpty(),
                    "creative tab " + index + " must contain its registered category items");
            helper.assertTrue(
                    tab.getDisplayItems().size() == tab.getSearchTabDisplayItems().size(),
                    "every creative item is also visible in global search");
            tab.getDisplayItems().forEach(stack -> displayed.add(stack.copy()));
        }

        Set<Item> registered = new HashSet<>();
        InfXItems.ITEMS.getEntries().forEach(item -> registered.add(item.value()));
        Set<Item> uniqueDisplayed = new HashSet<>();
        displayed.forEach(stack -> uniqueDisplayed.add(stack.getItem()));
        helper.assertTrue(uniqueDisplayed.equals(registered), "creative categories exactly cover the item registry");
        long expectedDisplayCount = registered.stream()
                .mapToLong(item -> RuneStoneBlock.isRuneStone(item.getDefaultInstance())
                        ? RuneStoneBlock.RUNE_COUNT
                        : 1)
                .sum();
        helper.assertTrue(
                displayed.size() == expectedDisplayCount,
                "all registered items are displayed once, with every rune stone variant");
        helper.assertTrue(
                registered.stream().allMatch(item -> {
                    List<ItemStack> itemStacks = displayed.stream().filter(stack -> stack.is(item)).toList();
                    if (!RuneStoneBlock.isRuneStone(item.getDefaultInstance())) {
                        return itemStacks.size() == 1;
                    }
                    return itemStacks.size() == RuneStoneBlock.RUNE_COUNT
                            && itemStacks.stream().map(RuneStoneBlock::rune).distinct().count()
                                    == RuneStoneBlock.RUNE_COUNT;
                }),
                "creative categories do not duplicate regular items and include every rune variant");
        helper.assertTrue(
                tabs.getFirst().getDisplayItems().stream().allMatch(stack -> stack.getItem() instanceof BlockItem),
                "the blocks tab only contains registered BlockItems");
        helper.assertTrue(
                InfXBlocks.UNDERWORLD_PORTAL.get().asItem() == Items.AIR
                        && InfXBlocks.NETHER_PORTAL.get().asItem() == Items.AIR
                        && InfXBlocks.RETURN_SPAWN_PORTAL.get().asItem() == Items.AIR,
                "all portal surfaces remain without an item form");

        List<CreativeModeTab> sortedTabs = CreativeModeTabRegistry.getSortedCreativeModeTabs();
        CreativeModeTab vanillaSpawnEggs =
                BuiltInRegistries.CREATIVE_MODE_TAB.getValue(CreativeModeTabs.SPAWN_EGGS);
        int firstIndex = sortedTabs.indexOf(tabs.getFirst());
        List<Identifier> sortedNames = sortedTabs.stream().map(CreativeModeTabRegistry::getName).toList();
        helper.assertTrue(
                firstIndex == sortedTabs.indexOf(vanillaSpawnEggs) + 1,
                "InfiniteX creative tabs start immediately after vanilla spawn eggs: " + sortedNames);
        for (int index = 0; index < tabs.size(); index++) {
            helper.assertTrue(
                    sortedTabs.get(firstIndex + index) == tabs.get(index),
                    "InfiniteX creative tabs remain consecutive and ordered");
        }
        helper.succeed();
    }

    private static void blockStackLimits(GameTestHelper helper) {
        assertStackLimit(helper, 1, Items.FURNACE);
        assertStackLimit(helper, 1, Items.ANVIL);
        assertStackLimit(helper, 1, Items.OAK_DOOR);
        assertStackLimit(helper, 1, Items.RED_BED);
        assertStackLimit(helper, 1, Items.BLAST_FURNACE);
        assertStackLimit(helper, 1, Items.SMOKER);

        assertStackLimit(helper, 4, Items.STONE);
        assertStackLimit(helper, 4, Items.CHEST);
        assertStackLimit(helper, 4, Items.CRAFTING_TABLE);
        assertStackLimit(helper, 4, Items.DIAMOND_BLOCK);
        assertStackLimit(helper, 4, Items.CRAFTER);

        assertStackLimit(helper, 8, Items.OAK_PLANKS);
        assertStackLimit(helper, 8, Items.WHITE_WOOL);
        assertStackLimit(helper, 8, Items.WHITE_CARPET);
        assertStackLimit(helper, 8, Items.STONE_PRESSURE_PLATE);
        assertStackLimit(helper, 8, Items.OAK_FENCE);
        assertStackLimit(helper, 8, Items.LADDER);
        assertStackLimit(helper, 8, Items.MELON);
        assertStackLimit(helper, 8, Items.PUMPKIN);
        assertStackLimit(helper, 8, Items.RAIL);
        assertStackLimit(helper, 8, Items.STONE_SLAB);
        assertStackLimit(helper, 8, Items.VINE);
        assertStackLimit(helper, 8, Items.COBBLESTONE_WALL);
        assertStackLimit(helper, 8, Items.CAKE);

        assertStackLimit(helper, 16, Items.GLASS_PANE);
        assertStackLimit(helper, 16, Items.IRON_BARS);
        assertStackLimit(helper, 16, Items.OAK_SAPLING);
        assertStackLimit(helper, 16, Items.TORCH);
        assertStackLimit(helper, 16, Items.OAK_SIGN);
        assertStackLimit(helper, 16, Items.PLAYER_HEAD);
        assertStackLimit(helper, 16, Items.SUGAR_CANE);
        assertStackLimit(helper, 16, Items.REPEATER);
        assertStackLimit(helper, 16, Items.COMPARATOR);
        assertStackLimit(helper, 16, Items.BREWING_STAND);
        assertStackLimit(helper, 16, Items.FLOWER_POT);

        assertStackLimit(helper, 32, Items.DANDELION);
        assertStackLimit(helper, 32, Items.BROWN_MUSHROOM);
        assertStackLimit(helper, 32, Items.SHORT_GRASS);
        assertStackLimit(helper, 32, Items.LILY_PAD);
        assertStackLimit(helper, 32, Items.SNOW);

        assertStackLimit(helper, 1, Items.SHULKER_BOX);
        assertStackLimit(helper, 64, Items.WHEAT_SEEDS);
        assertStackLimit(helper, 64, Items.CARROT);
        assertStackLimit(helper, 64, Items.NETHER_WART);
        assertStackLimit(helper, 64, Items.REDSTONE);

        InfXItems.FURNACES.forEach(item -> assertStackLimit(helper, 1, item.value()));
        InfXItems.METAL_ANVILS.forEach(item -> assertStackLimit(helper, 1, item.value()));
        InfXItems.WORKBENCHES.forEach(item -> assertStackLimit(helper, 4, item.value()));
        InfXItems.ORES.forEach(item -> assertStackLimit(helper, 4, item.value()));
        InfXItems.METAL_STORAGE_BLOCKS.forEach(item -> assertStackLimit(helper, 4, item.value()));
        InfXItems.WORLD_BLOCKS.forEach(item -> assertStackLimit(helper, 4, item.value()));
        InfXItems.ENCHANTING_TABLES.forEach(item -> assertStackLimit(helper, 4, item.value()));
        InfXItems.METAL_SAFES.forEach(item -> assertStackLimit(helper, 4, item.value()));
        assertStackLimit(helper, 4, InfXItems.NETHER_GRAVEL.value());
        assertStackLimit(helper, 32, InfXItems.WITHERWOOD.value());
        assertStackLimit(helper, 8, InfXItems.BLUEBERRY_BUSH.value());
        assertStackLimit(helper, 4, InfXItems.CORE.value());
        helper.succeed();
    }

    private static void assertStackLimit(GameTestHelper helper, int expected, Item item) {
        int actual = item.getDefaultMaxStackSize();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        helper.assertTrue(actual == expected, id + " stack limit: expected " + expected + ", got " + actual);
    }

    private static void fulltextSystems(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        var level = helper.getLevel();
        helper.assertTrue(
                InfXItems.R196_BUCKETS.size() == 35,
                "seven materials expose five registered bucket variants");
        helper.assertTrue(
                InfXBlocks.NETHER_GRAVEL.get() instanceof net.minecraft.world.level.block.FallingBlock,
                "Nether gravel preserves falling-block behavior");
        helper.assertTrue(
                InfXBlocks.CORE.get().defaultDestroyTime() < 0.0F,
                "Core is unbreakable in survival");
        for (String river : List.of("desert_river", "jungle_river", "swamp_river")) {
            helper.assertTrue(
                    level.registryAccess().lookupOrThrow(Registries.BIOME).containsKey(
                            ResourceKey.create(Registries.BIOME, InfiniteX.id(river))),
                    river + " is registered");
        }
        for (ItemStack record : InfXItems.R196_RECORDS.stream().map(DeferredItem::toStack).toList()) {
            helper.assertTrue(record.has(DataComponents.JUKEBOX_PLAYABLE), "R196 record is jukebox-playable");
        }

        BlockPos bush = new BlockPos(6, 2, 6);
        helper.setBlock(bush.below(2), Blocks.STONE);
        helper.setBlock(bush.below(), InfXBlocks.NETHER_GRAVEL.get());
        helper.setBlock(bush, InfXBlocks.WITHERWOOD.get());
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, bush);
        cow.applyEffectsFromBlocks(cow.position(), cow.position());
        helper.assertTrue(
                cow.hasEffect(MobEffects.WITHER)
                        && cow.getEffect(MobEffects.WITHER).getDuration() > 0,
                "touching Witherwood applies Wither");

        BlockPos blueberry = new BlockPos(8, 2, 6);
        helper.setBlock(blueberry.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(
                blueberry,
                InfXBlocks.BLUEBERRY_BUSH.get()
                        .defaultBlockState()
                        .setValue(SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        BlockPos absoluteBlueberry = helper.absolutePos(blueberry);
        InteractionResult blueberryHarvest = player.gameMode.useItemOn(
                player,
                level,
                player.getMainHandItem(),
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absoluteBlueberry), Direction.UP, absoluteBlueberry, false));
        helper.assertTrue(blueberryHarvest == InteractionResult.SUCCESS, "a mature blueberry bush is harvested by hand");
        helper.assertTrue(
                helper.getBlockState(blueberry).getValue(SweetBerryBushBlock.AGE) == 0,
                "harvesting a blueberry bush resets it to the picked state");
        List<ItemEntity> blueberryDrops = level.getEntitiesOfClass(
                        ItemEntity.class,
                        AABB.ofSize(Vec3.atCenterOf(absoluteBlueberry), 4.0D, 4.0D, 4.0D),
                        item -> item.getItem().is(InfXItems.BLUEBERRIES));
        int harvestedBlueberries = blueberryDrops.stream()
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        helper.assertTrue(harvestedBlueberries == 1, "a mature blueberry bush yields exactly one blueberry");
        blueberryDrops.forEach(ItemEntity::discard);
        InfXBlocks.BLUEBERRY_BUSH.get().performBonemeal(
                level, level.getRandom(), absoluteBlueberry, helper.getBlockState(blueberry));
        int bonemealAge = helper.getBlockState(blueberry).getValue(SweetBerryBushBlock.AGE);
        helper.assertTrue(bonemealAge >= 1 && bonemealAge <= 2, "bone meal advances a blueberry bush by one or two stages");

        BlockPos sweetBerry = new BlockPos(12, 2, 6);
        helper.setBlock(sweetBerry.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(
                sweetBerry,
                Blocks.SWEET_BERRY_BUSH.defaultBlockState()
                        .setValue(SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE));
        BlockPos absoluteSweetBerry = helper.absolutePos(sweetBerry);
        helper.assertTrue(
                player.gameMode.destroyBlock(absoluteSweetBerry),
                "a player can break a mature vanilla sweet berry bush");
        int sweetBerryBlueberries = level.getEntitiesOfClass(
                        ItemEntity.class,
                        AABB.ofSize(Vec3.atCenterOf(absoluteSweetBerry), 4.0D, 4.0D, 4.0D),
                        item -> item.getItem().is(InfXItems.BLUEBERRIES))
                .stream()
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        helper.assertTrue(
                sweetBerryBlueberries == 0,
                "breaking a mature vanilla sweet berry bush never drops custom blueberries");

        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.LAVA).toStack());
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.WATER);
        helper.setBlock(new BlockPos(1, 3, 1), Blocks.WATER);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        player.getMainHandItem().is(InfXItems.bucket(
                                MiteMaterial.IRON, MiteBucketItem.Contents.STONE)),
                        "an immersed lava bucket solidifies without changing material"))
                .thenExecute(() -> player.setItemInHand(
                        InteractionHand.MAIN_HAND,
                        InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.MILK).toStack()))
                .thenWaitUntil(() -> helper.assertTrue(
                        player.getMainHandItem().is(InfXItems.bucket(
                                MiteMaterial.IRON, MiteBucketItem.Contents.EMPTY)),
                        "an immersed milk bucket leaks into its matching empty bucket"))
                .thenExecute(() -> removePlayer(player))
                .thenSucceed();
    }

    private static void swimPhysics(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        ServerLevel level = helper.getLevel();

        // A shallow west-to-east channel. Vanilla tapers the push by the 0.333 fluid height and would
        // apply roughly 0.0047; MITE normalises the summed flow and always applies 0.014.
        for (int x = 3; x <= 7; x++) helper.setBlock(new BlockPos(x, 1, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 2, 2), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 4));
        helper.setBlock(new BlockPos(5, 2, 2), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 5));
        helper.setBlock(new BlockPos(6, 2, 2), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 6));
        Vec3 channel = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(5, 2, 2)));
        player.snapTo(channel.x, channel.y, channel.z, 0.0F, 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        SwimPhysics.applyNormalizedCurrent(player, SwimRules.CURRENT_SCALE);
        Vec3 push = player.getDeltaMovement();
        helper.assertTrue(
                Math.abs(push.length() - SwimRules.CURRENT_SCALE) < 1.0E-6D,
                "a shallow stream must push at the full 0.014, not vanilla's depth-tapered value");
        helper.assertTrue(push.x > 0.0D, "the push must follow the channel's descending flow");
        helper.assertTrue(Math.abs(push.y) < 1.0E-6D, "a level channel must not push vertically");

        // The same channel through a live tick, which proves EntitySwimMixin is wired: MITE's
        // 0.014 survives the 0.8 water drag as 0.0112, where vanilla's tapered push leaves 0.0037.
        player.snapTo(channel.x, channel.y, channel.z, 0.0F, 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.doTick();
        double drift = player.getDeltaMovement().x;
        helper.assertTrue(
                drift > 0.008D,
                "a live tick must carry MITE's untapered current, measured " + drift);

        // A falling column cuts the swim-up impulse to 7/16 so waterfalls cannot be climbed.
        helper.setBlock(new BlockPos(9, 1, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(9, 2, 2), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 8));
        helper.setBlock(new BlockPos(9, 3, 2), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 8));
        helper.setBlock(new BlockPos(9, 4, 2), Blocks.WATER);
        Vec3 column = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(9, 2, 2)));
        player.snapTo(column.x, column.y, column.z, 0.0F, 0.0F);
        helper.assertTrue(
                Math.abs(SwimPhysics.swimUpImpulse(player)
                                - SwimRules.SWIM_UP_IMPULSE * SwimRules.SURFACE_FACTOR)
                        < 1.0E-6D,
                "a falling column must cut the swim-up impulse to 7/16");

        // Vanilla omits water gravity while sprinting. Seed the player with the reduced jump
        // impulse, then run a live tick to prove the waterfall-specific gravity redirect prevents
        // it from becoming an upward climb.
        player.setSprinting(true);
        player.setDeltaMovement(new Vec3(0.0D, SwimPhysics.swimUpImpulse(player), 0.0D));
        player.doTick();
        helper.assertTrue(
                player.getDeltaMovement().y < 0.0D,
                "a sprinting player must lose height after the reduced waterfall swim-up impulse, measured "
                        + player.getDeltaMovement().y);

        // Modern Player.travel gives sprint swimmers an extra upward pull toward their camera
        // direction. MITE has no equivalent, so looking straight up in a waterfall must not
        // restore the climb that the reduced jump impulse prevents.
        player.setSwimming(true);
        player.setXRot(-90.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.travel(Vec3.ZERO);
        helper.assertTrue(
                player.getDeltaMovement().y < 0.0D,
                "a sprinting player looking upward must still lose height in a waterfall, measured "
                        + player.getDeltaMovement().y);
        player.setSwimming(false);
        player.setXRot(0.0F);
        player.setSprinting(false);

        // A walled shaft so the column cannot drain while the sink rate is measured.
        helper.setBlock(new BlockPos(12, 1, 2), Blocks.STONE);
        for (int y = 2; y <= 6; y++) {
            helper.setBlock(new BlockPos(11, y, 2), Blocks.STONE);
            helper.setBlock(new BlockPos(13, y, 2), Blocks.STONE);
            helper.setBlock(new BlockPos(12, y, 1), Blocks.STONE);
            helper.setBlock(new BlockPos(12, y, 3), Blocks.STONE);
            helper.setBlock(new BlockPos(12, y, 2), Blocks.WATER);
        }
        Vec3 still = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(12, 4, 2)));
        player.snapTo(still.x, still.y, still.z, 0.0F, 0.0F);
        helper.assertTrue(
                Math.abs(SwimPhysics.swimUpImpulse(player) - SwimRules.SWIM_UP_IMPULSE) < 1.0E-6D,
                "still water must keep the full swim-up impulse");

        // Submerged sinking: MITE's 0.02/tick settles near -0.1, vanilla's 0.005/tick near -0.025.
        // Driving doTick directly also proves the travelInWater gravity mixin is wired.
        player.setDeltaMovement(Vec3.ZERO);
        for (int tick = 0; tick < 20; tick++) player.doTick();
        double sink = player.getDeltaMovement().y;
        helper.assertTrue(
                level.getFluidState(player.blockPosition()).is(FluidTags.WATER),
                "the sink measurement must stay inside the water column");
        helper.assertTrue(
                sink < -0.06D, "a submerged player must sink at MITE's 0.02/tick, measured " + sink);

        // Sprinting upstream against the channel's current must bleed the 0.9 sprint drag back
        // toward the non-sprint 0.8 factor; sprinting downstream keeps the full 0.9 benefit.
        player.setSprinting(true);
        player.snapTo(channel.x, channel.y, channel.z, 0.0F, 0.0F);
        player.setDeltaMovement(new Vec3(-0.3D, 0.0D, 0.0D));
        player.doTick();
        double upstreamRetention = player.getDeltaMovement().x / -0.3D;
        helper.assertTrue(
                upstreamRetention < 0.85D,
                "sprinting upstream must bleed drag back toward the non-sprint 0.8 factor, measured "
                        + upstreamRetention);

        player.snapTo(channel.x, channel.y, channel.z, 0.0F, 0.0F);
        player.setDeltaMovement(new Vec3(0.3D, 0.0D, 0.0D));
        player.doTick();
        double downstreamRetention = player.getDeltaMovement().x / 0.3D;
        helper.assertTrue(
                downstreamRetention > 0.88D,
                "sprinting downstream must keep the full 0.9 sprint factor, measured "
                        + downstreamRetention);

        removePlayer(player);
        helper.succeed();
    }

    private static void bucketMechanics(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        ServerLevel level = helper.getLevel();
        Item emptyIron = InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.EMPTY).value();
        helper.assertTrue(
                DispenserBlock.DISPENSER_REGISTRY.containsKey(emptyIron),
                "the empty bucket registers its own dispenser behavior");
        helper.assertTrue(
                DispenserBlock.DISPENSER_REGISTRY.containsKey(
                        InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.WATER).value()),
                "the water bucket registers its own dispenser behavior");
        helper.assertTrue(
                DispenserBlock.DISPENSER_REGISTRY.containsKey(InfXItems.powderSnowBucket(MiteMaterial.IRON).value()),
                "the powder-snow bucket registers its own dispenser behavior");
        helper.assertTrue(
                DispenserBlock.DISPENSER_REGISTRY.containsKey(
                        InfXItems.mobBucket(MiteMaterial.IRON, MobBucketKind.COD).value()),
                "the mob bucket registers its own dispenser behavior");

        // MITE ItemBucket: scooping leaves the liquid cell in place unless Ctrl is held.
        BlockPos water = new BlockPos(2, 2, 2);
        helper.setBlock(water.below(), Blocks.STONE);
        helper.setBlock(water, Blocks.WATER);
        player.setNoGravity(true);
        aimDownAt(helper, player, water.above());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(emptyIron));
        player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(
                player.getMainHandItem().is(InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.WATER)),
                "scooping water yields the same-material water bucket");
        helper.assertBlockPresent(Blocks.WATER, water);

        // MITE: pouring a liquid back into itself spends the vessel and changes nothing.
        player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(
                player.getMainHandItem().is(emptyIron),
                "pouring water back into water empties the bucket");
        helper.assertBlockPresent(Blocks.WATER, water);

        // MITE ctrl_is_down while filling: the source cell is consumed.
        player.getPersistentData().putBoolean(Network.CTRL_USE, true);
        player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND);
        player.getPersistentData().remove(Network.CTRL_USE);
        helper.assertTrue(
                player.getMainHandItem().is(InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.WATER)),
                "a Ctrl scoop still fills the bucket");
        helper.assertBlockPresent(Blocks.AIR, water);

        // Flowing liquid cells can also fill a bucket and remain in place without Ctrl.
        BlockPos flowingWater = new BlockPos(4, 2, 4);
        helper.setBlock(flowingWater.below(), Blocks.STONE);
        helper.setBlock(flowingWater, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 4));
        aimDownAt(helper, player, flowingWater.above());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(emptyIron));
        player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(
                player.getMainHandItem().is(InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.WATER)),
                "flowing water fills the same-material bucket");
        helper.assertTrue(
                helper.getBlockState(flowingWater).is(Blocks.WATER)
                        && !helper.getBlockState(flowingWater).getFluidState().isSource(),
                "scooping flowing water without Ctrl keeps its flowing cell");

        // The Ctrl packet path must apply the returned empty bucket instead of leaving an empty slot.
        BlockPos paidSource = new BlockPos(6, 2, 4);
        helper.setBlock(paidSource.below(), Blocks.STONE);
        helper.setBlock(paidSource, Blocks.AIR);
        aimDownAt(helper, player, paidSource.above());
        player.giveExperiencePoints(MiteBucketItem.SOURCE_EXPERIENCE_COST);
        int experienceBefore = player.totalExperience;
        MiteBucketItem waterBucket =
                InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.WATER).value();
        player.setItemInHand(InteractionHand.MAIN_HAND, waterBucket.getDefaultInstance());
        EmbeddedChannel playerChannel = (EmbeddedChannel) player.connection.getConnection().channel();
        while (playerChannel.readOutbound() != null) {}
        waterBucket.useWithCtrl(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().is(emptyIron), "Ctrl source placement returns the empty bucket");
        helper.assertTrue(
                helper.getBlockState(paidSource).getFluidState().isSource(),
                "Ctrl source placement leaves a permanent source cell");
        helper.assertTrue(
                player.totalExperience == experienceBefore - MiteBucketItem.SOURCE_EXPERIENCE_COST,
                "Ctrl source placement spends 100 XP");
        boolean receivedBucketSound = false;
        Object outbound;
        while ((outbound = playerChannel.readOutbound()) != null) {
            if (outbound instanceof ClientboundSoundPacket soundPacket
                    && soundPacket.getSound().value() == SoundEvents.BUCKET_EMPTY) {
                receivedBucketSound = true;
            }
        }
        helper.assertTrue(receivedBucketSound, "the Ctrl source placer receives the bucket-empty sound");
        helper.setBlock(paidSource, Blocks.AIR);
        bucketFluidInteractions(helper, player, level);
    }

    private static void bucketFluidInteractions(GameTestHelper helper, ServerPlayer player, ServerLevel level) {
        BucketItem waterBucket =
                InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.WATER).value();
        BucketItem lavaBucket =
                InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.LAVA).value();

        // MITE tryPlaceContainedLiquid: a dousing liquid aimed at fire only extinguishes it.
        BlockPos fire = helper.absolutePos(new BlockPos(4, 2, 2));
        level.setBlock(fire.below(), Blocks.NETHERRACK.defaultBlockState(), 3);
        level.setBlock(fire, Blocks.FIRE.defaultBlockState(), 3);
        helper.assertTrue(
                waterBucket.emptyContents(player, level, fire, null, null),
                "water aimed at fire reports a successful pour");
        helper.assertTrue(level.getBlockState(fire).isAir(), "the fire cell is cleared, not flooded");

        // MITE tryConvertLavaToCobblestoneOrObsidian: a full lava source becomes obsidian.
        BlockPos lava = helper.absolutePos(new BlockPos(6, 2, 2));
        level.setBlock(lava.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(lava, Blocks.LAVA.defaultBlockState(), 3);
        helper.assertTrue(
                waterBucket.emptyContents(player, level, lava, null, null),
                "water poured onto lava reports success");
        helper.assertTrue(
                level.getBlockState(lava).is(Blocks.OBSIDIAN),
                "water on a full lava source sets obsidian");

        // MITE tryConvertWaterToCobblestone: lava poured onto water always sets cobblestone.
        BlockPos pool = helper.absolutePos(new BlockPos(8, 2, 2));
        level.setBlock(pool.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(pool, Blocks.WATER.defaultBlockState(), 3);
        helper.assertTrue(
                lavaBucket.emptyContents(player, level, pool, null, null),
                "lava poured onto water reports success");
        helper.assertTrue(
                level.getBlockState(pool).is(Blocks.COBBLESTONE), "lava on water sets cobblestone");
        bucketSourceDecay(helper, player, level, waterBucket);
    }

    private static void bucketSourceDecay(
            GameTestHelper helper, ServerPlayer player, ServerLevel level, BucketItem waterBucket) {
        // MITE scheduleBlockChange: an unpaid pour lands as a source, then degrades to flowing.
        BlockPos poured = helper.absolutePos(new BlockPos(2, 2, 6));
        level.setBlock(poured.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(poured, Blocks.AIR.defaultBlockState(), 3);
        int pendingBefore = FluidDecayData.get(level).pending();
        helper.assertTrue(
                waterBucket.emptyContents(player, level, poured, null, null),
                "an unpaid pour still reports success");
        helper.assertTrue(
                level.getFluidState(poured).isSource(), "the pour lands as a source so it spreads once");
        helper.assertTrue(
                FluidDecayData.get(level).pending() > pendingBefore,
                "an unpaid pour queues the MITE degrade");

        helper.startSequence()
                .thenExecuteAfter(MiteBucketItem.WATER_DECAY_DELAY + 1, () -> helper.assertFalse(
                        level.getFluidState(poured).isSource(),
                        "the queued degrade turns the unpaid source into flowing water"))
                .thenExecute(() -> bucketEntityInteractions(helper, player, level, waterBucket))
                .thenExecute(() -> removePlayer(player))
                .thenSucceed();
    }

    private static void bucketEntityInteractions(
            GameTestHelper helper, ServerPlayer player, ServerLevel level, BucketItem waterBucket) {
        Item emptyIron = InfXItems.bucket(MiteMaterial.IRON, MiteBucketItem.Contents.EMPTY).value();

        // MITE ItemVessel#tryEntityInteraction: water satisfies a thirsty animal and spends the vessel.
        var cow = helper.spawnWithNoFreeWill(InfXEntityTypes.R196_COW.get(), new BlockPos(4, 2, 6));
        setLivestockWellness(cow, 1.0F, 0.2F, 1.0F);
        helper.assertTrue(
                Livestock.isThirsty(cow, level.getGameTime()), "the cow must start out thirsty");
        player.setItemInHand(InteractionHand.MAIN_HAND, waterBucket.getDefaultInstance());
        waterBucket.interactLivingEntity(
                player.getMainHandItem(), player, cow, InteractionHand.MAIN_HAND);
        helper.assertFalse(
                Livestock.isThirsty(cow, level.getGameTime()), "watering the cow clears its thirst");
        helper.assertTrue(
                player.getMainHandItem().is(emptyIron), "watering an animal spends the water bucket");

        // MITE ItemVessel: water quenches a fire elemental for 20 damage.
        var elemental = helper.spawnWithNoFreeWill(InfXEntityTypes.FIRE_ELEMENTAL.get(), new BlockPos(6, 2, 6));
        float before = elemental.getHealth();
        player.setItemInHand(InteractionHand.MAIN_HAND, waterBucket.getDefaultInstance());
        waterBucket.interactLivingEntity(
                player.getMainHandItem(), player, elemental, InteractionHand.MAIN_HAND);
        helper.assertTrue(
                !elemental.isAlive() || elemental.getHealth() < before,
                "water quenches a fire elemental");
        helper.assertTrue(
                player.getMainHandItem().is(emptyIron), "quenching spends the water bucket");

        ItemEntity nearbyDrop = new ItemEntity(
                level, player.getX(), player.getY(), player.getZ(), Items.DIAMOND.getDefaultInstance());
        level.addFreshEntity(nearbyDrop);
        player.getPersistentData()
                .putLong(
                        MiteBucketItem.MELT_PICKUP_BLOCK,
                        level.getGameTime() + MiteBucketItem.MELT_PICKUP_DELAY);
        nearbyDrop.playerTouch(player);
        helper.assertFalse(
                nearbyDrop.isRemoved(), "a player cannot immediately pick up an item after a bucket melts");
        player.getPersistentData().remove(MiteBucketItem.MELT_PICKUP_BLOCK);
        nearbyDrop.playerTouch(player);
        helper.assertTrue(nearbyDrop.isRemoved(), "item pickup resumes after the bucket melt grace expires");
    }

    /** Places the player one cell above a target and points straight down at it. */
    private static void aimDownAt(GameTestHelper helper, ServerPlayer player, BlockPos relativeStand) {
        Vec3 stand = helper.absoluteVec(Vec3.atBottomCenterOf(relativeStand));
        player.snapTo(stand.x, stand.y, stand.z, 0.0F, 90.0F);
    }

    private static BlockPos buildObsidianFrame(GameTestHelper helper, BlockPos base, boolean addBedrock) {
        for (int x = 0; x < 4; x++) {
            if (addBedrock) {
                helper.getLevel().setBlock(base.offset(x, -1, 0), Blocks.BEDROCK.defaultBlockState(), 3);
            }
            helper.getLevel().setBlock(base.offset(x, 0, 0), Blocks.OBSIDIAN.defaultBlockState(), 3);
            helper.getLevel().setBlock(base.offset(x, 4, 0), Blocks.OBSIDIAN.defaultBlockState(), 3);
        }
        for (int y = 1; y < 4; y++) {
            helper.getLevel().setBlock(base.offset(0, y, 0), Blocks.OBSIDIAN.defaultBlockState(), 3);
            helper.getLevel().setBlock(base.offset(3, y, 0), Blocks.OBSIDIAN.defaultBlockState(), 3);
            helper.getLevel().setBlock(base.offset(1, y, 0), Blocks.AIR.defaultBlockState(), 3);
            helper.getLevel().setBlock(base.offset(2, y, 0), Blocks.AIR.defaultBlockState(), 3);
        }
        return base.offset(1, 1, 0);
    }

    private static void assertPortalInteriorCleared(GameTestHelper helper, BlockPos origin, String message) {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                helper.assertTrue(
                        helper.getLevel().getBlockState(origin.offset(x, y, 0)).isAir(),
                        message + " at " + origin.offset(x, y, 0));
            }
        }
    }

    private static int countPortalSurfaces(
            GameTestHelper helper, Block block, BlockPos first, BlockPos second) {
        return (int) BlockPos.betweenClosedStream(first, second)
                .filter(pos -> helper.getLevel().getBlockState(pos).is(block))
                .count();
    }

    private static boolean hasAdjacentPortal(GameTestHelper helper, BlockPos pos, Block portal) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (helper.getLevel().getBlockState(pos.relative(direction)).is(portal)) {
                return true;
            }
        }
        return false;
    }

    private static void setLivestockWellness(Animal animal, float food, float water, float freedom) {
        var data = animal.getPersistentData();
        data.putFloat("infx_livestock_food", food);
        data.putFloat("infx_livestock_water", water);
        data.putFloat("infx_livestock_freedom", freedom);
        data.putBoolean("infx_livestock_wellness_initialized", true);
        Livestock.setWell(animal, Livestock.isWell(animal));
    }

    private static void clearLivestockPanic(Animal animal) {
        var data = animal.getPersistentData();
        data.remove("infx_livestock_panic_until");
        data.remove("infx_livestock_panic_origin");
        Livestock.serverTick(animal);
    }

    private static void interactAt(ServerPlayer player, net.minecraft.world.entity.Mob target) {
        var result = net.neoforged.neoforge.common.CommonHooks.onInteractEntityAt(
                player, target, target.position(), InteractionHand.MAIN_HAND);
        if (result == null) {
            target.interact(player, InteractionHand.MAIN_HAND, target.position());
        }
    }

    static ServerPlayer createPlayer(GameTestHelper helper) {
        GameProfile profile = new GameProfile(
                UUID.randomUUID(), "infx-r196-" + PLAYER_SEQUENCE.incrementAndGet());
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(), helper.getLevel(), profile, cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 2, 1)));
        player.snapTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        return player;
    }

    static void removePlayer(ServerPlayer player) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        player.level().getServer().getPlayerList().remove(player);
    }
}
