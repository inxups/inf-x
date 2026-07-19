package com.pixulse.infx.gametest;

import com.mojang.authlib.GameProfile;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.block.RuneStoneBlock;
import com.pixulse.infx.block.UnderworldPortalBlock;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.block.entity.R196SafeBlockEntity;
import com.pixulse.infx.crafting.TimedCraftingMenu;
import com.pixulse.infx.equipment.R196EquipmentBehaviors;
import com.pixulse.infx.equipment.R196QualitySystem;
import com.pixulse.infx.entity.R196Livestock;
import com.pixulse.infx.item.R196ArrowItem;
import com.pixulse.infx.item.R196BucketItem;
import com.pixulse.infx.item.R196CoinItem;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.material.R196Quality;
import com.pixulse.infx.menu.MetalAnvilMenu;
import com.pixulse.infx.menu.R196EnchantmentMenu;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModCreativeTabs;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.registry.ModMenus;
import com.pixulse.infx.progression.R196Experience;
import com.pixulse.infx.survival.R196FoodProfile;
import com.pixulse.infx.survival.R196SurvivalData;
import com.pixulse.infx.survival.R196SurvivalEvents;
import com.pixulse.infx.survival.R196SurvivalRules;
import com.pixulse.infx.world.R196EndEvents;
import com.pixulse.infx.world.Underworld;
import com.pixulse.infx.world.UnderworldPortalEvents;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModR196CompletionGameTests {
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
            "r196_safe_enchanting",
            "r196_creative_tabs",
            "r196_fulltext_systems");
    private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger();

    static {
        FUNCTIONS.register("r196_special_behaviors", () -> ModR196CompletionGameTests::specialBehaviors);
        FUNCTIONS.register("r196_quality_coin", () -> ModR196CompletionGameTests::qualityAndCoin);
        FUNCTIONS.register("r196_metal_anvil", () -> ModR196CompletionGameTests::metalAnvil);
        FUNCTIONS.register("r196_underworld", () -> ModR196CompletionGameTests::underworld);
        FUNCTIONS.register("r196_portals", () -> ModR196CompletionGameTests::portals);
        FUNCTIONS.register("r196_livestock", () -> ModR196CompletionGameTests::livestock);
        FUNCTIONS.register("r196_gravel_loot", () -> ModR196CompletionGameTests::gravelLoot);
        FUNCTIONS.register("r196_hopper_xp", () -> ModR196CompletionGameTests::hopperExperience);
        FUNCTIONS.register("r196_survival_core", () -> ModR196CompletionGameTests::survivalCore);
        FUNCTIONS.register("r196_safe_enchanting", () -> ModR196CompletionGameTests::safeAndEnchanting);
        FUNCTIONS.register("r196_creative_tabs", () -> ModR196CompletionGameTests::creativeTabs);
        FUNCTIONS.register("r196_fulltext_systems", () -> ModR196CompletionGameTests::fulltextSystems);
    }

    private ModR196CompletionGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
        modBus.addListener(ModR196CompletionGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
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
        ItemStack silverSword = ModItems.catalog()
                .equipment(R196Material.SILVER, R196EquipmentType.SWORD)
                .holder()
                .toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, silverSword);

        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(4, 2, 2));
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        float before = zombie.getHealth();
        zombie.hurtServer(level, level.damageSources().playerAttack(player), 4.0F);
        helper.assertTrue(Math.abs((before - zombie.getHealth()) - 5.0F) < .001F, "silver melee must deal 125% to undead");

        var cow = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(5, 2, 2));
        before = cow.getHealth();
        cow.hurtServer(level, level.damageSources().playerAttack(player), 4.0F);
        helper.assertTrue(Math.abs((before - cow.getHealth()) - 4.0F) < .001F, "silver melee must not boost normal targets");

        R196ArrowItem silverArrow = (R196ArrowItem) ModItems.catalog()
                .equipment(R196Material.SILVER, R196EquipmentType.ARROW)
                .holder()
                .get();
        AbstractArrow projectile = silverArrow.createArrow(level, silverArrow.getDefaultInstance(), player, null);
        var secondZombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(6, 2, 2));
        secondZombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        before = secondZombie.getHealth();
        secondZombie.hurtServer(level, level.damageSources().arrow(projectile, player), 4.0F);
        helper.assertTrue(Math.abs((before - secondZombie.getHealth()) - 5.0F) < .001F, "silver arrows must deal 125% to undead");

        for (var entry : ModItems.catalog().equipmentEntries()) {
            helper.assertTrue(
                    entry.holder().toStack().has(DataComponents.DAMAGE_RESISTANT)
                            == (entry.key().material() == R196Material.ADAMANTIUM),
                    entry.path() + " fire resistance boundary");
        }
        for (var entry : ModItems.catalog().rawEntries()) {
            boolean expected = entry.definition().material()
                    .map(material -> material == R196Material.ADAMANTIUM)
                    .orElse(false);
            helper.assertTrue(
                    entry.holder().toStack().has(DataComponents.DAMAGE_RESISTANT) == expected,
                    entry.path() + " raw fire resistance boundary");
        }
        helper.assertTrue(ModItems.ADAMANTIUM_ORE.toStack().has(DataComponents.DAMAGE_RESISTANT), "adamantium ore fire resistance");
        helper.assertTrue(ModItems.ADAMANTIUM_BLOCK.toStack().has(DataComponents.DAMAGE_RESISTANT), "adamantium block fire resistance");
        helper.assertTrue(
                ModBlocks.ADAMANTIUM_ANVIL.get().asItem().getDefaultInstance().has(DataComponents.DAMAGE_RESISTANT),
                "adamantium anvil fire resistance");

        ItemEntity adamantiumDrop = new ItemEntity(
                level, player.getX(), player.getY(), player.getZ(), ModItems.ADAMANTIUM_INGOT.toStack());
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

        helper.assertTrue(R196EquipmentBehaviors.armorDurabilityFactor(50, 100) == 1.0F, "half durability protection");
        helper.assertTrue(R196EquipmentBehaviors.armorDurabilityFactor(99, 100) < .03F, "critical durability protection");
        ItemStack fullChestplate = ModItems.catalog()
                .equipment(R196Material.IRON, R196EquipmentType.CHESTPLATE)
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
        ItemStack coins = ModItems.catalog().raw("copper_coin").holder().toStack(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, coins);
        int beforeXp = player.totalExperience;
        ((R196CoinItem) coins.getItem()).use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(coins.getCount() == 1, "one server-side coin must be consumed");
        helper.assertTrue(player.totalExperience == beforeXp + 5, "copper coin must grant five XP");

        ItemStack pickaxe = ModItems.IRON_PICKAXE.toStack();
        int baseDurability = pickaxe.getMaxDamage();
        int code = R196QualitySystem.toCode(R196Quality.FINE);
        R196QualitySystem.applySelectedQuality(pickaxe, code);
        helper.assertTrue(pickaxe.get(ModDataComponents.QUALITY.get()) == R196Quality.FINE, "quality component persists");
        helper.assertTrue(pickaxe.getMaxDamage() == Math.round(baseDurability * 1.5F), "fine durability modifier");

        player.giveExperiencePoints(1_000);
        TimedCraftingMenu crafting = (TimedCraftingMenu) player.inventoryMenu;
        crafting.infx$craftingContainer().setItem(0, new ItemStack(Items.FLINT));
        crafting.infx$craftingContainer().setItem(1, new ItemStack(Items.STRING));
        crafting.infx$craftingContainer().setItem(2, new ItemStack(Items.STICK));
        crafting.infx$cycleQuality(player);
        ItemStack qualityPreview = crafting.infx$resultContainer().getItem(0);
        helper.assertTrue(
                qualityPreview.is(ModItems.catalog()
                        .equipment(R196Material.FLINT, R196EquipmentType.KNIFE)
                        .holder()),
                "quality selection keeps the matched crafting result");
        helper.assertTrue(
                qualityPreview.get(ModDataComponents.QUALITY.get()) == R196Quality.FINE,
                "server crafting preview carries selected quality");
        removePlayer(player);
        helper.succeed();
    }

    private static void metalAnvil(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        BlockPos relative = new BlockPos(4, 2, 4);
        helper.setBlock(relative, ModBlocks.IRON_ANVIL.get());
        BlockPos absolute = helper.absolutePos(relative);
        MetalAnvilBlock block = ModBlocks.IRON_ANVIL.get();
        MetalAnvilBlockEntity entity = (MetalAnvilBlockEntity) helper.getLevel().getBlockEntity(absolute);

        MetalAnvilMenu menu = MetalAnvilMenu.server(
                77,
                player.getInventory(),
                R196Material.IRON,
                ContainerLevelAccess.create(helper.getLevel(), absolute),
                block);
        ItemStack damaged = ModItems.IRON_PICKAXE.toStack();
        damaged.setDamageValue(1_000);
        damaged.set(DataComponents.CUSTOM_NAME, Component.literal("Keep Me"));
        damaged.set(ModDataComponents.QUALITY.get(), R196Quality.FINE);
        menu.getSlot(0).set(damaged);
        menu.getSlot(1).set(new ItemStack(Items.IRON_NUGGET, 3));
        ItemStack result = menu.getSlot(2).getItem();
        helper.assertFalse(result.isEmpty(), "valid metal repair must produce output");
        helper.assertTrue(result.getDamageValue() < damaged.getDamageValue(), "repair must restore durability");
        helper.assertTrue(result.get(DataComponents.CUSTOM_NAME).equals(Component.literal("Keep Me")), "custom name preserved");
        helper.assertTrue(result.get(ModDataComponents.QUALITY.get()) == R196Quality.FINE, "quality preserved");
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
        ItemStack fishingRod = ModItems.catalog()
                .equipment(R196Material.IRON, R196EquipmentType.FISHING_ROD)
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
        ServerPlayer player = createPlayer(helper);
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

        // A non-bottom Overworld gate is the first of the five ordinary R196 routes: it
        // returns to the shared world spawn without needing a second dimension.
        helper.assertTrue(helper.getLevel().getServer().getLevel(Underworld.LEVEL) == null, "GameTest has no custom levels");
        TeleportTransition transition = ModBlocks.UNDERWORLD_PORTAL.get()
                .getPortalDestination(helper.getLevel(), player, player.blockPosition());
        helper.assertTrue(transition != null, "non-bottom Overworld portal must return a spawn transition");
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
                helper.getLevel().getBlockState(eligibleOrigin).is(ModBlocks.UNDERWORLD_PORTAL.get()),
                "eligible portal interior converted");
        helper.assertTrue(
                ModBlocks.UNDERWORLD_PORTAL.get()
                                .getPortalDestination(helper.getLevel(), player, eligibleOrigin)
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
                helper.getLevel().getBlockState(ineligibleOrigin).is(ModBlocks.UNDERWORLD_PORTAL.get()),
                "ineligible portal interior is not converted");

        BlockPos arrival = ModBlocks.UNDERWORLD_PORTAL.get()
                .createArrivalPortal(helper.getLevel(), helper.absolutePos(new BlockPos(8, 2, 8)));
        helper.assertTrue(helper.getLevel().getBlockState(arrival.below()).is(Blocks.OBSIDIAN), "arrival has a floor");
        helper.assertTrue(
                helper.getLevel().getBlockState(arrival.relative(net.minecraft.core.Direction.NORTH))
                        .is(ModBlocks.UNDERWORLD_PORTAL.get()),
                "arrival is beside the target portal");
        removePlayer(player);
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
                "ordinary Overworld frame must become an R196 portal");

        BlockPos[] corners = {
            base,
            base.offset(3, 0, 0),
            base.offset(0, 4, 0),
            base.offset(3, 4, 0)
        };
        for (int i = 0; i < corners.length; i++) {
            helper.getLevel().setBlock(
                    corners[i],
                    ModBlocks.MITHRIL_RUNE_STONE.get()
                            .defaultBlockState()
                            .setValue(RuneStoneBlock.RUNE, i * 5),
                    3);
        }
        helper.assertTrue(
                UnderworldPortalBlock.hasRuneGate(helper.getLevel(), origin),
                "four same-material corner runes must override the ordinary gate");
        helper.assertTrue(
                helper.getLevel().getBlockState(corners[3]).getValue(RuneStoneBlock.RUNE) == 15,
                "all sixteen rune states persist in the frame");

        helper.getLevel().setBlock(
                corners[0], ModBlocks.ADAMANTIUM_RUNE_STONE.get().defaultBlockState(), 3);
        helper.assertFalse(
                UnderworldPortalBlock.hasRuneGate(helper.getLevel(), origin),
                "mixed rune materials must not form a rune gate");
        removePlayer(player);
        helper.succeed();
    }

    private static void livestock(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        var level = helper.getLevel();
        Cow cow = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 8));
        helper.assertTrue(cow.getMaxHealth() == 20.0F, "R196 cows must have twenty health");
        helper.setBlock(new BlockPos(3, 1, 8), Blocks.WATER);
        ItemEntity wheat = new ItemEntity(
                level, cow.getX(), cow.getY(), cow.getZ(), new ItemStack(Items.WHEAT));
        level.addFreshEntity(wheat);
        R196Livestock.Needs needs = R196Livestock.update(level, cow);
        helper.assertTrue(needs.watered(), "nearby water must satisfy livestock thirst");
        helper.assertTrue(
                needs.fed() && (wheat.getItem().isEmpty() || !wheat.isAlive()),
                "livestock must consume suitable dropped food");

        cow.getPersistentData().putBoolean("infx_livestock_healthy", true);
        cow.getPersistentData().putBoolean("infx_livestock_diseased", false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        interactAt(player, cow);
        helper.assertTrue(player.getMainHandItem().is(Items.MILK_BUCKET), "healthy cow produces its daily milk bucket");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        interactAt(player, cow);
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a second same-day milk bucket must be denied");

        Sheep sheep = helper.spawn(EntityTypes.SHEEP, new BlockPos(4, 2, 8));
        sheep.getPersistentData().putBoolean("infx_livestock_healthy", true);
        sheep.getPersistentData().putBoolean("infx_livestock_diseased", true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
        interactAt(player, sheep);
        helper.assertFalse(sheep.isSheared(), "diseased sheep must not be shearable");
        sheep.getPersistentData().putBoolean("infx_livestock_diseased", false);
        sheep.hurtServer(level, level.damageSources().inFire(), 1.0F);
        helper.assertTrue(sheep.isSheared(), "fire damage must strip sheep wool");

        Chicken chicken = helper.spawn(EntityTypes.CHICKEN, new BlockPos(6, 2, 8));
        chicken.setAge(0);
        chicken.getPersistentData().putBoolean("infx_livestock_healthy", true);
        chicken.getPersistentData().putBoolean("infx_livestock_diseased", false);
        chicken.getPersistentData().putLong("infx_chicken_next_feather", -1L);
        com.pixulse.infx.entity.R196AnimalEvents.updateChicken(level, chicken);
        helper.assertTrue(
                !level.getEntitiesOfClass(
                                ItemEntity.class,
                                chicken.getBoundingBox().inflate(3.0),
                                item -> item.getItem().is(Items.FEATHER))
                        .isEmpty(),
                "healthy chickens must naturally shed feathers");

        Cow seeker = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(8, 2, 4));
        seeker.getPersistentData().putLong("infx_livestock_last_water", level.getGameTime() - 24_001L);
        seeker.getPersistentData().putLong("infx_livestock_last_food", level.getGameTime());
        for (int x = 7; x <= 11; x++) {
            for (int z = 3; z <= 5; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        BlockPos water = new BlockPos(11, 1, 4);
        helper.setBlock(water, Blocks.WATER);
        R196Livestock.Needs thirsty = R196Livestock.update(level, seeker);
        helper.assertFalse(thirsty.watered(), "seeker setup must begin thirsty");
        R196Livestock.NeedsGoal waterGoal = new R196Livestock.NeedsGoal(seeker);
        R196Livestock.panic(level, cow);
        helper.assertFalse(
                chicken.getPersistentData().getBooleanOr("infx_livestock_healthy", true),
                "panic must propagate across animal species");
        helper.startSequence()
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
                    removePlayer(player);
                    seeker.discard();
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
        R196SurvivalEvents.recalculatePlayerLimits(player);
        helper.assertTrue(Math.abs(player.getMaxHealth() - 6.0F) < 0.001F, "level zero starts with three hearts");
        helper.assertTrue(Math.abs(R196SurvivalRules.foodCap(player.experienceLevel) - 6.0D) < 0.001D,
                "level zero starts with three food icons");

        R196Experience.setTotal(player, R196Experience.cumulativeForLevel(5));
        R196SurvivalEvents.recalculatePlayerLimits(player);
        helper.assertTrue(Math.abs(player.getMaxHealth() - 8.0F) < 0.001F,
                "level five adds one heart");
        helper.assertTrue(Math.abs(R196SurvivalRules.foodCap(player.experienceLevel) - 8.0D) < 0.001D,
                "level five adds one food icon");

        player.setData(ModAttachments.SURVIVAL, new R196SurvivalData(0, 2, 1, 1, 1, 0, 0));
        R196SurvivalData egg = player.getData(ModAttachments.SURVIVAL)
                .eat(new R196FoodProfile(1, 3, 12_000, 0, 2_000, 0), 8);
        helper.assertTrue(egg.satiation() == 1.0D && egg.nutrition() == 5.0D,
                "egg fills both energy layers");
        helper.assertTrue(egg.phytonutrients() == 1, "egg cannot cure phytonutrient malnutrition");
        player.setData(ModAttachments.SURVIVAL, new R196SurvivalData(0, 8, 1, 1, 1, 0, 0));
        helper.assertTrue(player.canEat(false), "depleted satiation permits eating even when nutrition is full");
        player.setData(ModAttachments.SURVIVAL, new R196SurvivalData(8, 8, 1, 1, 1, 0, 0));
        helper.assertFalse(player.canEat(false), "both full energy layers prevent eating");
        helper.assertTrue(
                Items.SUGAR.getDefaultInstance().has(DataComponents.FOOD)
                        && Items.SUGAR.getDefaultInstance().has(DataComponents.CONSUMABLE),
                "small R196 foods are directly edible");
        removePlayer(player);
        helper.succeed();
    }

    private static void safeAndEnchanting(GameTestHelper helper) {
        ServerPlayer owner = createPlayer(helper);
        ServerPlayer visitor = createPlayer(helper);
        BlockPos safePos = new BlockPos(4, 2, 4);
        helper.setBlock(safePos, ModBlocks.COPPER_SAFE.get());
        R196SafeBlockEntity safe = helper.getBlockEntity(safePos, R196SafeBlockEntity.class);
        helper.assertTrue(safe.canOpen(visitor), "unowned village safes are publicly accessible");
        safe.setOwner(owner);
        helper.assertTrue(safe.canOpen(owner), "safe owner can open");
        helper.assertFalse(safe.canOpen(visitor), "other players cannot open safe");

        BlockPos tableRelative = new BlockPos(8, 2, 8);
        helper.setBlock(tableRelative, ModBlocks.DIAMOND_ENCHANTING_TABLE.get());
        BlockPos table = helper.absolutePos(tableRelative);
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
        helper.assertTrue(R196EnchantmentMenu.bookshelfCount(helper.getLevel(), table) == 24,
                "R196 enchanting structure counts twenty-four shelves");
        R196EnchantmentMenu menu = new R196EnchantmentMenu(
                1,
                owner.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), table),
                R196EnchantmentMenu.Kind.DIAMOND);
        helper.assertTrue(menu.getType() == ModMenus.DIAMOND_ENCHANTING.get(),
                "diamond table synchronizes its custom client menu type");
        helper.assertTrue(menu.getSlot(1).mayPlace(Items.DIAMOND.getDefaultInstance()),
                "diamond table accepts diamonds");
        helper.assertFalse(menu.getSlot(1).mayPlace(Items.EMERALD.getDefaultInstance()),
                "diamond table rejects emeralds");
        helper.assertFalse(menu.getSlot(1).mayPlace(Items.LAPIS_LAZULI.getDefaultInstance()),
                "R196 tables reject vanilla lapis fuel");
        owner.setItemInHand(InteractionHand.MAIN_HAND, ModItems.catalog()
                .equipment(R196Material.ADAMANTIUM, R196EquipmentType.PICKAXE).holder().toStack());
        helper.assertTrue(R196EndEvents.hasAdamantiumCrystalTool(owner),
                "adamantium pickaxe meets crystal gate");
        removePlayer(visitor);
        removePlayer(owner);
        helper.succeed();
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
                ModCreativeTabs.MAIN.get(),
                ModCreativeTabs.INGREDIENTS.get(),
                ModCreativeTabs.FOOD_AND_CONSUMABLES.get(),
                ModCreativeTabs.TOOLS_AND_UTILITIES.get(),
                ModCreativeTabs.COMBAT_AND_EQUIPMENT.get());
        List<Integer> expectedSizes = List.of(50, 31, 24, 135, 108);
        CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(
                helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess());
        List<Item> displayed = new ArrayList<>();

        for (int index = 0; index < tabs.size(); index++) {
            CreativeModeTab tab = tabs.get(index);
            tab.buildContents(parameters);
            helper.assertTrue(
                    tab.getDisplayItems().size() == expectedSizes.get(index),
                    "creative tab " + index + " has the expected category size");
            helper.assertTrue(
                    tab.getDisplayItems().size() == tab.getSearchTabDisplayItems().size(),
                    "every creative item is also visible in global search");
            tab.getDisplayItems().stream().map(ItemStack::getItem).forEach(displayed::add);
        }

        Set<Item> registered = new HashSet<>();
        ModItems.ITEMS.getEntries().forEach(item -> registered.add(item.value()));
        Set<Item> uniqueDisplayed = new HashSet<>(displayed);
        helper.assertTrue(displayed.size() == registered.size(), "all registered items are displayed");
        helper.assertTrue(uniqueDisplayed.size() == displayed.size(), "creative categories do not overlap");
        helper.assertTrue(uniqueDisplayed.equals(registered), "creative categories exactly cover the item registry");
        helper.assertTrue(
                tabs.getFirst().getDisplayItems().stream().allMatch(stack -> stack.getItem() instanceof BlockItem),
                "the blocks tab only contains registered BlockItems");
        helper.assertTrue(
                ModBlocks.UNDERWORLD_PORTAL.get().asItem() == Items.AIR,
                "the Underworld portal remains the only block without an item form");

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

    private static void fulltextSystems(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        var level = helper.getLevel();
        helper.assertTrue(
                ModItems.R196_BUCKETS.size() == 35,
                "seven materials expose five registered bucket variants");
        helper.assertTrue(
                ModBlocks.NETHER_GRAVEL.get() instanceof net.minecraft.world.level.block.FallingBlock,
                "Nether gravel preserves falling-block behavior");
        helper.assertTrue(
                ModBlocks.CORE.get().defaultDestroyTime() < 0.0F,
                "Core is unbreakable in survival");
        helper.assertTrue(
                level.registryAccess().lookupOrThrow(Registries.CONFIGURED_CARVER).containsKey(
                        ResourceKey.create(Registries.CONFIGURED_CARVER, InfiniteX.id("large_cave"))),
                "the distant large-cave carver is registered");
        for (String river : List.of("desert_river", "jungle_river", "swamp_river")) {
            helper.assertTrue(
                    level.registryAccess().lookupOrThrow(Registries.BIOME).containsKey(
                            ResourceKey.create(Registries.BIOME, InfiniteX.id(river))),
                    river + " is registered");
        }
        for (ItemStack record : ModItems.R196_RECORDS.stream().map(item -> item.toStack()).toList()) {
            helper.assertTrue(record.has(DataComponents.JUKEBOX_PLAYABLE), "R196 record is jukebox-playable");
        }

        BlockPos bush = new BlockPos(6, 2, 6);
        helper.setBlock(bush.below(2), Blocks.STONE);
        helper.setBlock(bush.below(), ModBlocks.NETHER_GRAVEL.get());
        helper.setBlock(bush, ModBlocks.WITHERWOOD.get());
        Cow cow = helper.spawnWithNoFreeWill(EntityTypes.COW, bush);
        cow.applyEffectsFromBlocks(cow.position(), cow.position());
        helper.assertTrue(
                cow.hasEffect(MobEffects.WITHER)
                        && cow.getEffect(MobEffects.WITHER).getDuration() > 0,
                "touching Witherwood applies Wither");

        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                ModItems.bucket(R196Material.IRON, R196BucketItem.Contents.LAVA).toStack());
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.WATER);
        helper.setBlock(new BlockPos(1, 3, 1), Blocks.WATER);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(
                        player.getMainHandItem().is(ModItems.bucket(
                                R196Material.IRON, R196BucketItem.Contents.STONE)),
                        "an immersed lava bucket solidifies without changing material"))
                .thenExecute(() -> player.setItemInHand(
                        InteractionHand.MAIN_HAND,
                        ModItems.bucket(R196Material.IRON, R196BucketItem.Contents.MILK).toStack()))
                .thenWaitUntil(() -> helper.assertTrue(
                        player.getMainHandItem().is(ModItems.bucket(
                                R196Material.IRON, R196BucketItem.Contents.EMPTY)),
                        "an immersed milk bucket leaks into its matching empty bucket"))
                .thenExecute(() -> removePlayer(player))
                .thenSucceed();
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
