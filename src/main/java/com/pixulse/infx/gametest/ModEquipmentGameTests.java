package com.pixulse.infx.gametest;

import com.mojang.authlib.GameProfile;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.equipment.R196EquipmentBehaviors;
import com.pixulse.infx.harvest.HarvestRequirements;
import com.pixulse.infx.harvest.HarvestTier;
import com.pixulse.infx.harvest.ToolWearCalculator;
import com.pixulse.infx.item.R196ArrowItem;
import com.pixulse.infx.item.R196BowItem;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentCategory;
import com.pixulse.infx.item.R196EquipmentKey;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.item.R196FishingRodItem;
import com.pixulse.infx.item.R196MiningFamily;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.tag.ModTags;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEquipmentGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);

    private static final List<String> TEST_NAMES = List.of(
            "equipment_components",
            "tool_actions_and_wear",
            "harvest_tier_catalog",
            "material_arrows",
            "material_bows",
            "fishing_rods",
            "armor_and_horse_armor",
            "horse_armor_loot");

    private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger();

    static {
        TEST_FUNCTIONS.register("equipment_components", () -> ModEquipmentGameTests::equipmentComponents);
        TEST_FUNCTIONS.register("tool_actions_and_wear", () -> ModEquipmentGameTests::toolActionsAndWear);
        TEST_FUNCTIONS.register("harvest_tier_catalog", () -> ModEquipmentGameTests::harvestTierCatalog);
        TEST_FUNCTIONS.register("material_arrows", () -> ModEquipmentGameTests::materialArrows);
        TEST_FUNCTIONS.register("material_bows", () -> ModEquipmentGameTests::materialBows);
        TEST_FUNCTIONS.register("fishing_rods", () -> ModEquipmentGameTests::fishingRods);
        TEST_FUNCTIONS.register("armor_and_horse_armor", () -> ModEquipmentGameTests::armorAndHorseArmor);
        TEST_FUNCTIONS.register("horse_armor_loot", () -> ModEquipmentGameTests::horseArmorLoot);
    }

    private ModEquipmentGameTests() {}

    public static void register(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
        modBus.addListener(ModEquipmentGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("equipment"), new TestEnvironmentDefinition.AllOf());
        for (String name : TEST_NAMES) {
            ResourceKey<Consumer<GameTestHelper>> function =
                    ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id(name));
            TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                    environment,
                    Identifier.withDefaultNamespace("empty"),
                    200,
                    0,
                    true,
                    Rotation.NONE);
            event.registerTest(function.identifier(), new FunctionGameTestInstance(function, data));
        }
    }

    private static void equipmentComponents(GameTestHelper helper) {
        for (R196Catalog.EquipmentEntry entry : ModItems.catalog().equipmentEntries()) {
            R196EquipmentKey key = entry.key();
            ItemStack stack = entry.holder().value().getDefaultInstance();
            if (key.durability() > 0) {
                int expectedDurability = key.material() == R196Material.RUSTED_IRON
                        ? Math.max(1, Math.round(key.durability() * com.pixulse.infx.material.R196Quality.POOR.durabilityMultiplier()))
                        : key.durability();
                helper.assertTrue(
                        stack.getOrDefault(DataComponents.MAX_DAMAGE, 0) == expectedDurability,
                        key.path() + " max damage");
            }
            boolean melee = (key.type().category() == R196EquipmentCategory.TOOL
                            || key.type().category() == R196EquipmentCategory.WEAPON)
                    && key.type() != R196EquipmentType.FISHING_ROD
                    && key.type() != R196EquipmentType.BOW
                    && key.type() != R196EquipmentType.ARROW;
            if (melee) {
                helper.assertTrue(stack.has(DataComponents.WEAPON), key.path() + " weapon component");
                helper.assertTrue(stack.has(DataComponents.ATTRIBUTE_MODIFIERS), key.path() + " attributes");
            }
            if (key.type().armorForm() != R196EquipmentType.ArmorForm.NONE) {
                helper.assertTrue(stack.has(DataComponents.EQUIPPABLE), key.path() + " equippable component");
            }
            if (key.type() == R196EquipmentType.BOW) {
                helper.assertTrue(stack.getItem() instanceof R196BowItem, key.path() + " bow class");
            } else if (key.type() == R196EquipmentType.ARROW) {
                helper.assertTrue(stack.getItem() instanceof R196ArrowItem, key.path() + " arrow class");
            } else if (key.type() == R196EquipmentType.FISHING_ROD) {
                helper.assertTrue(stack.getItem() instanceof R196FishingRodItem, key.path() + " fishing class");
            }
        }
        helper.succeed();
    }

    private static void toolActionsAndWear(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        BlockPos axePos = new BlockPos(1, 1, 1);
        BlockPos shovelPos = new BlockPos(2, 1, 1);
        BlockPos hoePos = new BlockPos(3, 1, 1);
        BlockPos mattockPos = new BlockPos(4, 1, 1);
        BlockPos wearPos = new BlockPos(5, 1, 1);

        useOn(
                helper,
                player,
                axePos,
                Blocks.OAK_LOG,
                ModItems.catalog()
                        .equipment(R196Material.COPPER, R196EquipmentType.AXE)
                        .holder()
                        .value());
        helper.assertTrue(helper.getBlockState(axePos).is(Blocks.STRIPPED_OAK_LOG), "axe must strip logs");

        useOn(
                helper,
                player,
                shovelPos,
                Blocks.GRASS_BLOCK,
                ModItems.catalog()
                        .equipment(R196Material.COPPER, R196EquipmentType.SHOVEL)
                        .holder()
                        .value());
        helper.assertTrue(helper.getBlockState(shovelPos).is(Blocks.DIRT_PATH), "shovel must create paths");

        useOn(
                helper,
                player,
                hoePos,
                Blocks.DIRT,
                ModItems.catalog()
                        .equipment(R196Material.COPPER, R196EquipmentType.HOE)
                        .holder()
                        .value());
        helper.assertTrue(helper.getBlockState(hoePos).is(Blocks.FARMLAND), "hoe must till soil");

        Item mattock = ModItems.catalog()
                .equipment(R196Material.COPPER, R196EquipmentType.MATTOCK)
                .holder()
                .value();
        useOn(helper, player, mattockPos, Blocks.DIRT, mattock);
        helper.assertTrue(helper.getBlockState(mattockPos).is(Blocks.DIRT_PATH), "mattock shovel action");
        useOn(helper, player, mattockPos, Blocks.DIRT_PATH, mattock);
        helper.assertTrue(helper.getBlockState(mattockPos).is(Blocks.FARMLAND), "mattock hoe fallback");

        ItemStack scythe = ModItems.catalog()
                .equipment(R196Material.COPPER, R196EquipmentType.SCYTHE)
                .holder()
                .value()
                .getDefaultInstance();
        helper.assertTrue(
                scythe.getDestroySpeed(Blocks.WHEAT.defaultBlockState())
                        > scythe.getDestroySpeed(Blocks.STONE.defaultBlockState()),
                "scythe must be efficient against crops");

        var sheep = helper.spawn(EntityTypes.SHEEP, new BlockPos(6, 1, 1));
        ItemStack shears = ModItems.catalog()
                .equipment(R196Material.COPPER, R196EquipmentType.SHEARS)
                .holder()
                .value()
                .getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, shears);
        helper.assertTrue(
                shears.interactLivingEntity(player, sheep, InteractionHand.MAIN_HAND).consumesAction(),
                "material shears must interact with sheep");
        helper.assertTrue(sheep.isSheared(), "material shears must shear sheep");
        sheep.discard();

        var zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(7, 1, 1));
        ItemStack sword = ModItems.catalog()
                .equipment(R196Material.COPPER, R196EquipmentType.SWORD)
                .holder()
                .value()
                .getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        float healthBefore = zombie.getHealth();
        player.attack(zombie);
        helper.assertTrue(zombie.getHealth() < healthBefore, "material sword must deal melee damage");
        helper.assertTrue(
                sword.getDamageValue()
                        == new R196EquipmentKey(R196Material.COPPER, R196EquipmentType.SWORD).attackWear(),
                "material sword must apply R196 hit wear exactly once");
        zombie.discard();

        helper.setBlock(wearPos, Blocks.OAK_LOG);
        BlockPos absoluteWearPos = helper.absolutePos(wearPos);
        BlockState state = helper.getBlockState(wearPos);
        ItemStack hatchet = ModItems.FLINT_HATCHET.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, hatchet);
        hatchet.mineBlock(helper.getLevel(), state, absoluteWearPos, player);
        int expected = ToolWearCalculator.damageForBreaking(
                state.getDestroySpeed(helper.getLevel(), absoluteWearPos), 4.0F / 3.0F);
        helper.assertTrue(
                hatchet.getDamageValue() == expected,
                "hardness wear expected " + expected + " but got " + hatchet.getDamageValue());

        removePlayer(player);
        helper.succeed();
    }

    private static void useOn(
            GameTestHelper helper, ServerPlayer player, BlockPos relativePos, Block block, Item item) {
        helper.setBlock(relativePos.below(), Blocks.STONE);
        helper.setBlock(relativePos, block);
        helper.setBlock(relativePos.above(), Blocks.AIR);
        player.setItemInHand(InteractionHand.MAIN_HAND, item.getDefaultInstance());
        BlockPos absolute = helper.absolutePos(relativePos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        InteractionResult result = item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(result.consumesAction(), item + " use action must succeed");
    }

    private static void harvestTierCatalog(GameTestHelper helper) {
        Map<HarvestTier, R196EquipmentKey> representatives = Map.of(
                HarvestTier.FLINT,
                new R196EquipmentKey(R196Material.FLINT, R196EquipmentType.HATCHET),
                HarvestTier.COPPER,
                new R196EquipmentKey(R196Material.COPPER, R196EquipmentType.PICKAXE),
                HarvestTier.IRON,
                new R196EquipmentKey(R196Material.IRON, R196EquipmentType.PICKAXE),
                HarvestTier.ANCIENT_METAL,
                new R196EquipmentKey(R196Material.ANCIENT_METAL, R196EquipmentType.PICKAXE),
                HarvestTier.MITHRIL,
                new R196EquipmentKey(R196Material.MITHRIL, R196EquipmentType.PICKAXE),
                HarvestTier.ADAMANTIUM,
                new R196EquipmentKey(R196Material.ADAMANTIUM, R196EquipmentType.PICKAXE));

        representatives.forEach((tier, key) -> {
            ItemStack stack = ModItems.catalog()
                    .equipment(key.material(), key.type())
                    .holder()
                    .value()
                    .getDefaultInstance();
            for (HarvestTier candidate : HarvestTier.values()) {
                helper.assertTrue(
                        stack.is(ModTags.Items.toolTier(candidate)) == (candidate == tier),
                        key.path() + " unexpected tier tag " + candidate);
            }
        });
        helper.assertTrue(HarvestTier.ADAMANTIUM.satisfies(HarvestTier.FLINT), "top tier must satisfy flint");
        helper.assertFalse(HarvestTier.FLINT.satisfies(HarvestTier.COPPER), "flint must not satisfy copper");
        helper.assertTrue(HarvestTier.IRON.satisfies(HarvestTier.ANCIENT_METAL),
                "iron and ancient metal share MITE level three");
        helper.assertTrue(HarvestTier.ANCIENT_METAL.satisfies(HarvestTier.IRON),
                "ancient metal and iron share MITE level three");

        assertHarvestLevel(helper, Blocks.COAL_BLOCK, 0);
        assertHarvestLevel(helper, Blocks.GLOWSTONE, 0);
        assertHarvestLevel(helper, Blocks.INFESTED_STONE, 0);
        assertHarvestLevel(helper, com.pixulse.infx.registry.ModBlocks.INFESTED_NETHERRACK.get(), 0);
        assertHarvestLevel(helper, Blocks.OAK_LOG, 1);
        assertHarvestLevel(helper, Blocks.TERRACOTTA, 1);
        assertHarvestLevel(helper, Blocks.SANDSTONE_SLAB, 1);
        assertHarvestLevel(helper, Blocks.STONE, 2);
        assertHarvestLevel(helper, Blocks.PETRIFIED_OAK_SLAB, 2);
        assertHarvestLevel(helper, Blocks.SANDSTONE_STAIRS, 2);
        assertHarvestLevel(helper, Blocks.SANDSTONE_WALL, 2);
        assertHarvestLevel(helper, Blocks.COPPER_BLOCK.weathering().unaffected(), 3);
        assertHarvestLevel(helper, Blocks.IRON_BARS, 3);
        assertHarvestLevel(helper, Blocks.REDSTONE_BLOCK, 3);
        assertHarvestLevel(helper, com.pixulse.infx.registry.ModBlocks.MITHRIL_RUNE_STONE.get(), 3);
        assertHarvestLevel(helper, com.pixulse.infx.registry.ModBlocks.ADAMANTIUM_RUNE_STONE.get(), 3);
        assertHarvestLevel(helper, Blocks.DIAMOND_ORE, 4);
        assertHarvestLevel(helper, Blocks.DIAMOND_BLOCK, 5);
        assertHarvestLevel(helper, com.pixulse.infx.registry.ModBlocks.ADAMANTIUM_BLOCK.get(), 6);

        ItemStack pickaxe = equipmentStack(R196Material.IRON, R196EquipmentType.PICKAXE);
        helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.GLOWSTONE.defaultBlockState()),
                "MITE glass material makes pickaxes effective against glowstone");
        helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.TORCH.defaultBlockState()),
                "MITE circuit material makes pickaxes effective against torches");
        helper.assertFalse(pickaxe.isCorrectToolForDrops(Blocks.PISTON.defaultBlockState()),
                "MITE piston material has no effective tool");
        helper.assertFalse(pickaxe.isCorrectToolForDrops(Blocks.ANVIL.defaultBlockState()),
                "MITE anvil material has no effective tool and relies on portability");

        ItemStack axe = equipmentStack(R196Material.FLINT, R196EquipmentType.AXE);
        helper.assertTrue(axe.isCorrectToolForDrops(Blocks.SANDSTONE.defaultBlockState()),
                "axes must harvest the MITE sandstone block");
        helper.assertTrue(axe.isCorrectToolForDrops(Blocks.SANDSTONE_SLAB.defaultBlockState()),
                "MITE explicitly extends axe effectiveness to sandstone slabs");
        helper.assertFalse(axe.isCorrectToolForDrops(Blocks.SANDSTONE_STAIRS.defaultBlockState()),
                "sandstone stairs retain their stone level and are not axe-effective in MITE");
        helper.assertTrue(
                Math.abs(axe.getDestroySpeed(Blocks.SANDSTONE.defaultBlockState()) * 2.0F
                        - axe.getDestroySpeed(Blocks.SANDSTONE_SLAB.defaultBlockState())) < 1.0E-6F,
                "only the sandstone block receives MITE's half axe speed");
        helper.assertTrue(axe.isCorrectToolForDrops(Blocks.INFESTED_STONE.defaultBlockState()),
                "MITE infested blocks use axe-effective clay material");
        helper.assertTrue(axe.isCorrectToolForDrops(
                com.pixulse.infx.registry.ModBlocks.FLINT_WORKBENCH.get().defaultBlockState()),
                "all tiered workbenches retain their wood-material axe effectiveness");

        ItemStack flintShovel = equipmentStack(R196Material.FLINT, R196EquipmentType.SHOVEL);
        ItemStack copperShovel = equipmentStack(R196Material.COPPER, R196EquipmentType.SHOVEL);
        helper.assertFalse(flintShovel.isCorrectToolForDrops(Blocks.GLASS.defaultBlockState()),
                "non-metal shovels must not harvest full glass");
        helper.assertTrue(copperShovel.isCorrectToolForDrops(Blocks.GLASS.defaultBlockState()),
                "metal shovels must harvest full glass");
        helper.assertTrue(flintShovel.isCorrectToolForDrops(Blocks.INFESTED_STONE.defaultBlockState()),
                "shovels must inherit infested clay effectiveness");

        ItemStack hoe = equipmentStack(R196Material.COPPER, R196EquipmentType.HOE);
        helper.assertTrue(hoe.isCorrectToolForDrops(
                com.pixulse.infx.registry.ModBlocks.SANDSTONE_FURNACE.get().defaultBlockState()),
                "the R196 sandstone furnace uses hoe-effective sand material");
        helper.assertFalse(hoe.isCorrectToolForDrops(
                com.pixulse.infx.registry.ModBlocks.CLAY_FURNACE.get().defaultBlockState()),
                "hoes must remain ineffective against R196 clay material");

        ItemStack hammer = equipmentStack(R196Material.COPPER, R196EquipmentType.WAR_HAMMER);
        helper.assertTrue(hammer.isCorrectToolForDrops(Blocks.CAKE.defaultBlockState()),
                "war hammers retain their MITE cake override");
        ItemStack cudgel = equipmentStack(R196Material.WOOD, R196EquipmentType.CUDGEL);
        helper.assertTrue(cudgel.isCorrectToolForDrops(Blocks.CAKE.defaultBlockState()),
                "wooden cudgels retain their MITE cake effectiveness");
        helper.assertTrue(cudgel.isCorrectToolForDrops(Blocks.GLOWSTONE.defaultBlockState()),
                "wooden cudgels can harvest level-zero MITE glass material");
        helper.assertFalse(cudgel.isCorrectToolForDrops(Blocks.GLASS.defaultBlockState()),
                "wooden cudgels cannot meet full glass level one");
        helper.assertFalse(cudgel.isCorrectToolForDrops(Blocks.ICE.defaultBlockState()),
                "wooden cudgels cannot meet ice level one");
        ItemStack scythe = equipmentStack(R196Material.COPPER, R196EquipmentType.SCYTHE);
        helper.assertTrue(scythe.isCorrectToolForDrops(Blocks.WHEAT.defaultBlockState()),
                "scythes must harvest wheat");
        helper.assertFalse(scythe.isCorrectToolForDrops(Blocks.CARROTS.defaultBlockState()),
                "root crops must remain shovel/hoe work rather than scythe work");
        ItemStack sword = equipmentStack(R196Material.COPPER, R196EquipmentType.SWORD);
        ItemStack shears = equipmentStack(R196Material.COPPER, R196EquipmentType.SHEARS);
        helper.assertTrue(sword.isCorrectToolForDrops(Blocks.HAY_BLOCK.defaultBlockState()),
                "swords must inherit MITE plant-material effectiveness");
        helper.assertTrue(shears.isCorrectToolForDrops(Blocks.NETHER_WART.defaultBlockState()),
                "shears must inherit MITE plant-material effectiveness");

        for (Block block : BuiltInRegistries.BLOCK) {
            BlockState state = block.defaultBlockState();
            int level = HarvestRequirements.requiredLevel(state);
            helper.assertTrue(level >= 0 && level <= HarvestRequirements.MAX_LEVEL,
                    block + " has invalid harvest level " + level);
            helper.assertTrue(HarvestRequirements.explicitLevelCount(state) <= 1,
                    block + " belongs to more than one explicit harvest level");
            if (level > 0 && block.defaultDestroyTime() >= 0.0F) {
                boolean portable = state.is(ModTags.Blocks.PORTABLE_HAND_HARVEST);
                boolean hasEffectiveTool = java.util.Arrays.stream(R196MiningFamily.values())
                        .filter(family -> family != R196MiningFamily.NONE)
                        .anyMatch(family -> state.is(ModTags.Blocks.effectiveWith(family)));
                helper.assertTrue(portable || hasEffectiveTool,
                        block + " level " + level + " has neither a MITE tool family nor portability");
            }
        }
        helper.succeed();
    }

    private static void assertHarvestLevel(GameTestHelper helper, Block block, int expected) {
        int actual = HarvestRequirements.requiredLevel(block.defaultBlockState());
        helper.assertTrue(actual == expected,
                block + " expected harvest level " + expected + " but got " + actual);
    }

    private static ItemStack equipmentStack(R196Material material, R196EquipmentType type) {
        return ModItems.catalog().equipment(material, type).holder().toStack();
    }

    private static void materialArrows(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        Vec3 position = player.position();
        for (R196Material material : R196EquipmentType.ARROW.allowedMaterials()) {
            R196EquipmentKey key = new R196EquipmentKey(material, R196EquipmentType.ARROW);
            R196ArrowItem item = (R196ArrowItem) ModItems.catalog()
                    .equipment(material, R196EquipmentType.ARROW)
                    .holder()
                    .value();
            ItemStack stack = item.getDefaultInstance();
            AbstractArrow fired = item.createArrow(helper.getLevel(), stack, player, null);
            AbstractArrow dispensed =
                    (AbstractArrow) item.asProjectile(helper.getLevel(), position, stack, Direction.NORTH);
            helper.assertTrue(fired.getPickupItemStackOrigin().is(item), key.path() + " fired pickup identity");
            helper.assertTrue(dispensed.getPickupItemStackOrigin().is(item), key.path() + " dispensed pickup identity");
            helper.assertTrue(
                    dispensed.pickup == AbstractArrow.Pickup.DISALLOWED,
                    key.path() + " dispenser pickup must await one recovery roll");
            helper.assertTrue(
                    Math.abs(item.baseDamage() - key.arrowBaseDamage()) < 1.0E-9,
                    key.path() + " damage");

            long seed = 1_000L + material.ordinal();
            boolean expectedRecovery = RandomSource.create(seed).nextFloat()
                    < R196EquipmentBehaviors.recoveryChance(material);
            dispensed.getRandom().setSeed(seed);
            BlockHitResult impact = new BlockHitResult(
                    position,
                    Direction.UP,
                    BlockPos.containing(position),
                    false);
            R196EquipmentBehaviors.resolveArrowRecovery(dispensed, impact);
            helper.assertTrue(
                    dispensed.pickup
                            == (expectedRecovery
                                    ? AbstractArrow.Pickup.ALLOWED
                                    : AbstractArrow.Pickup.DISALLOWED),
                    key.path() + " recovery roll");
            AbstractArrow.Pickup resolved = dispensed.pickup;
            dispensed.getRandom().setSeed(seed + 100L);
            R196EquipmentBehaviors.resolveArrowRecovery(dispensed, impact);
            helper.assertTrue(dispensed.pickup == resolved, key.path() + " recovery is checked only once");

            ItemStack infiniteStack = stack.copy();
            infiniteStack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            AbstractArrow infinite = item.createArrow(helper.getLevel(), infiniteStack, player, null);
            helper.assertTrue(
                    infinite.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                    key.path() + " infinite arrow pickup boundary");
            R196EquipmentBehaviors.resolveArrowRecovery(infinite, impact);
            helper.assertTrue(
                    infinite.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                    key.path() + " infinite arrow never becomes recoverable");
        }

        R196ArrowItem flint = (R196ArrowItem) ModItems.catalog()
                .equipment(R196Material.FLINT, R196EquipmentType.ARROW)
                .holder()
                .value();
        AbstractArrow entityArrow = (AbstractArrow) flint.asProjectile(
                helper.getLevel(), position, flint.getDefaultInstance(), Direction.NORTH);
        long recoveringSeed = 0L;
        while (RandomSource.create(recoveringSeed).nextFloat()
                >= R196EquipmentBehaviors.recoveryChance(R196Material.FLINT)) {
            recoveringSeed++;
        }
        entityArrow.getRandom().setSeed(recoveringSeed);
        var target = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(2, 2, 2));
        int itemsBefore = helper.getLevel()
                .getEntities(EntityTypes.ITEM, target.getBoundingBox().inflate(8.0), entity -> true)
                .size();
        R196EquipmentBehaviors.resolveArrowRecovery(entityArrow, new EntityHitResult(target));
        int itemsAfter = helper.getLevel()
                .getEntities(EntityTypes.ITEM, target.getBoundingBox().inflate(8.0), entity -> true)
                .size();
        helper.assertTrue(itemsAfter == itemsBefore + 1, "recovered entity hit drops exactly one material arrow");
        target.discard();
        removePlayer(player);
        helper.succeed();
    }

    private static void materialBows(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        Item silverArrow = ModItems.catalog()
                .equipment(R196Material.SILVER, R196EquipmentType.ARROW)
                .holder()
                .value();
        for (R196Material material :
                List.of(R196Material.WOOD, R196Material.ANCIENT_METAL, R196Material.MITHRIL)) {
            R196BowItem bowItem = (R196BowItem) ModItems.catalog()
                    .equipment(material, R196EquipmentType.BOW)
                    .holder()
                    .value();
            ItemStack bow = bowItem.getDefaultInstance();
            player.setItemInHand(InteractionHand.MAIN_HAND, bow);
            player.getInventory().add(silverArrow.getDefaultInstance());
            int before = helper.getLevel()
                    .getEntities(EntityTypes.ARROW, player.getBoundingBox().inflate(32.0), arrow -> true)
                    .size();
            helper.assertTrue(
                    bowItem.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                    material.path() + " bow nock");
            helper.assertTrue(
                    "silver".equals(bow.get(ModDataComponents.NOCKED_ARROW_MATERIAL.get())),
                    material.path() + " nocked model state");
            for (int tick = 0; tick < 20; tick++) {
                player.doTick();
            }
            player.releaseUsingItem();
            int after = helper.getLevel()
                    .getEntities(EntityTypes.ARROW, player.getBoundingBox().inflate(32.0), arrow -> true)
                    .size();
            helper.assertTrue(after == before + 1, material.path() + " bow must spawn one arrow");
            helper.assertTrue(
                    !bow.has(ModDataComponents.NOCKED_ARROW_MATERIAL.get()),
                    material.path() + " bow must clear nocked state");
        }
        removePlayer(player);
        helper.succeed();
    }

    private static void fishingRods(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        for (R196Material material : List.of(R196Material.FLINT, R196Material.IRON, R196Material.ADAMANTIUM)) {
            R196FishingRodItem rod = (R196FishingRodItem) ModItems.catalog()
                    .equipment(material, R196EquipmentType.FISHING_ROD)
                    .holder()
                    .value();
            player.setItemInHand(InteractionHand.MAIN_HAND, rod.getDefaultInstance());
            helper.assertTrue(
                    rod.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                    material.path() + " rod cast");
            helper.assertTrue(player.fishing != null, material.path() + " rod must create hook");
            helper.assertTrue(
                    rod.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                    material.path() + " rod retrieve");
            helper.assertTrue(player.fishing == null, material.path() + " retrieve must clear hook");
            helper.assertTrue(player.getMainHandItem().is(rod), material.path() + " rod identity");
        }
        removePlayer(player);
        helper.succeed();
    }

    private static void armorAndHorseArmor(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        equipSet(helper, player, R196Material.MITHRIL, R196EquipmentType.platePieces());
        player.doTick();
        double plateArmor = player.getAttributeValue(Attributes.ARMOR);
        helper.assertTrue(
                Math.abs(plateArmor - 9.0) < 1.0E-6,
                "mithril plate must sum to 9, got " + plateArmor);
        equipSet(helper, player, R196Material.MITHRIL, R196EquipmentType.chainPieces());
        player.doTick();
        double chainArmor = player.getAttributeValue(Attributes.ARMOR);
        helper.assertTrue(
                Math.abs(chainArmor - 7.0) < 1.0E-6,
                "mithril chain must sum to 7, got " + chainArmor);

        var horse = helper.spawn(EntityTypes.HORSE, new BlockPos(6, 1, 1));
        R196EquipmentKey horseKey =
                new R196EquipmentKey(R196Material.ADAMANTIUM, R196EquipmentType.HORSE_ARMOR);
        ItemStack horseArmor = ModItems.catalog()
                .equipment(horseKey.material(), horseKey.type())
                .holder()
                .value()
                .getDefaultInstance();
        horse.setItemSlot(EquipmentSlot.BODY, horseArmor);
        horse.tick();
        double horseArmorValue = horse.getAttributeValue(Attributes.ARMOR);
        helper.assertTrue(
                Math.abs(horseArmorValue - 7.0) < 1.0E-6,
                "adamantium horse armor must add 7, got " + horseArmorValue);
        Equippable equippable = horseArmor.get(DataComponents.EQUIPPABLE);
        helper.assertTrue(
                equippable != null
                        && equippable.assetId().orElseThrow().equals(horseKey.equipmentAsset()),
                "horse equipment asset");

        horse.discard();
        removePlayer(player);
        helper.succeed();
    }

    private static void horseArmorLoot(GameTestHelper helper) {
        Map<String, Set<R196Material>> expected = Map.of(
                "simple_dungeon", Set.of(R196Material.COPPER, R196Material.GOLD, R196Material.IRON),
                "nether_bridge", Set.of(R196Material.COPPER, R196Material.GOLD, R196Material.IRON),
                "desert_pyramid", Set.of(R196Material.SILVER, R196Material.GOLD, R196Material.IRON),
                "jungle_temple", Set.of(R196Material.SILVER, R196Material.GOLD, R196Material.IRON),
                "stronghold_corridor", Set.of(R196Material.COPPER, R196Material.IRON));
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(Vec3.ZERO))
                .create(LootContextParamSets.CHEST);
        for (var structure : expected.entrySet()) {
            ResourceKey<LootTable> key = ResourceKey.create(
                    Registries.LOOT_TABLE,
                    Identifier.withDefaultNamespace("chests/" + structure.getKey()));
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
            Set<R196Material> found = new HashSet<>();
            for (long seed = 0; seed < 2000 && !found.equals(structure.getValue()); seed++) {
                for (ItemStack stack : table.getRandomItems(params, seed)) {
                    for (R196Material material : structure.getValue()) {
                        if (stack.is(ModItems.catalog()
                                .equipment(material, R196EquipmentType.HORSE_ARMOR)
                                .holder()
                                .get())) {
                            found.add(material);
                        }
                    }
                }
            }
            helper.assertTrue(
                    found.equals(structure.getValue()),
                    structure.getKey() + " must expose exactly its R196 horse armor set, found " + found);
        }
        helper.succeed();
    }

    private static void equipSet(
            GameTestHelper helper,
            ServerPlayer player,
            R196Material material,
            List<R196EquipmentType> pieces) {
        for (EquipmentSlot slot :
                List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        for (R196EquipmentType type : pieces) {
            R196EquipmentKey key = new R196EquipmentKey(material, type);
            ItemStack armor = ModItems.catalog()
                    .equipment(material, type)
                    .holder()
                    .value()
                    .getDefaultInstance();
            EquipmentSlot slot = type.armorType().orElseThrow().getSlot();
            player.setItemSlot(slot, armor);
            Equippable equippable = armor.get(DataComponents.EQUIPPABLE);
            helper.assertTrue(
                    equippable != null
                            && equippable.assetId().orElseThrow().equals(key.equipmentAsset()),
                    key.path() + " equipment asset");
        }
    }

    private static ServerPlayer createPlayer(GameTestHelper helper) {
        GameProfile profile = new GameProfile(
                UUID.randomUUID(), "infx-equipment-" + PLAYER_SEQUENCE.incrementAndGet());
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(), helper.getLevel(), profile, cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(20);
        Vec3 position = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 2, 1)));
        player.snapTo(position.x, position.y, position.z, 0.0F, 0.0F);
        return player;
    }

    private static void removePlayer(ServerPlayer player) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        player.level().getServer().getPlayerList().remove(player);
    }
}
