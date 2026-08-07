package com.pixulse.infx.gametest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.authlib.GameProfile;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.equipment.EquipmentBehaviors;
import com.pixulse.infx.data.harvest.HarvestRequirements;
import com.pixulse.infx.data.harvest.HarvestTier;
import com.pixulse.infx.data.harvest.ToolWearCalculator;
import com.pixulse.infx.item.InfxArrowItem;
import com.pixulse.infx.item.InfxBowItem;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.EquipmentCategory;
import com.pixulse.infx.item.EquipmentKey;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.InfxFishingRodItem;
import com.pixulse.infx.item.InfxCarrotOnAStickItem;
import com.pixulse.infx.item.InfxWarpedFungusOnAStickItem;
import com.pixulse.infx.item.MiningFamily;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import com.pixulse.infx.registry.tag.InfXItemTags;
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
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModEquipmentGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);

    private static final List<String> TEST_NAMES = List.of(
            "equipment_components",
            "infx_equipment_components",
            "tool_actions_and_wear",
            "harvest_tier_catalog",
            "material_arrows",
            "material_bows",
            "fishing_rods",
            "armor_and_horse_armor",
            "horse_armor_loot",
            "height_advantage",
            "carrot_stick_boost");

    private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger();

    static {
        TEST_FUNCTIONS.register("equipment_components", () -> ModEquipmentGameTests::equipmentComponents);
        TEST_FUNCTIONS.register("infx_equipment_components", () -> ModEquipmentGameTests::stickBoneAttackBehavior);
        TEST_FUNCTIONS.register("tool_actions_and_wear", () -> ModEquipmentGameTests::toolActionsAndWear);
        TEST_FUNCTIONS.register("harvest_tier_catalog", () -> ModEquipmentGameTests::harvestTierCatalog);
        TEST_FUNCTIONS.register("material_arrows", () -> ModEquipmentGameTests::materialArrows);
        TEST_FUNCTIONS.register("material_bows", () -> ModEquipmentGameTests::materialBows);
        TEST_FUNCTIONS.register("fishing_rods", () -> ModEquipmentGameTests::fishingRods);
        TEST_FUNCTIONS.register("armor_and_horse_armor", () -> ModEquipmentGameTests::armorAndHorseArmor);
        TEST_FUNCTIONS.register("horse_armor_loot", () -> ModEquipmentGameTests::horseArmorLoot);
        TEST_FUNCTIONS.register("height_advantage", () -> ModEquipmentGameTests::heightAdvantage);
        TEST_FUNCTIONS.register("carrot_stick_boost", () -> ModEquipmentGameTests::carrotStickBoost);
    }

    private ModEquipmentGameTests() {}

    public static void register(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
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
        ItemStack stick = Items.STICK.getDefaultInstance();
        ItemStack bone = Items.BONE.getDefaultInstance();
        var stickRange = stick.get(DataComponents.ATTACK_RANGE);
        var boneRange = bone.get(DataComponents.ATTACK_RANGE);
        helper.assertTrue(stick.getMaxStackSize() == 32, "InfX sticks must stack to 32");
        helper.assertTrue(
                stickRange != null
                        && stickRange.maxReach() == 2.0F
                        && stickRange.maxCreativeReach() == 5.0F,
                "InfX sticks must add 0.5 melee reach only");
        helper.assertTrue(
                boneRange != null
                        && boneRange.maxReach() == 2.0F
                        && boneRange.maxCreativeReach() == 5.0F,
                "InfX bones must add 0.5 melee reach only");
        for (Catalog.EquipmentEntry entry : InfXItems.catalog().equipmentEntries()) {
            EquipmentKey key = entry.key();
            ItemStack stack = entry.holder().value().getDefaultInstance();
            if (key.durability() > 0) {
                int expectedDurability = key.material() == InfxMaterial.RUSTED_IRON
                        ? Math.max(1, Math.round(key.durability() * Quality.POOR.durabilityMultiplier()))
                        : key.durability();
                helper.assertTrue(
                        stack.getOrDefault(DataComponents.MAX_DAMAGE, 0) == expectedDurability,
                        key.path() + " max damage");
            }
            boolean melee = (key.type().category() == EquipmentCategory.TOOL
                            || key.type().category() == EquipmentCategory.WEAPON)
                    && key.type() != EquipmentType.FISHING_ROD
                    && key.type() != EquipmentType.BOW
                    && key.type() != EquipmentType.ARROW;
            if (melee) {
                helper.assertTrue(stack.has(DataComponents.WEAPON), key.path() + " weapon component");
                helper.assertTrue(stack.has(DataComponents.ATTRIBUTE_MODIFIERS), key.path() + " attributes");
                // Melee reach derives from the shared component-less rule (1.5 blocks) like vanilla
                // weapons; the vanilla pick path also excludes the player's own mount this way.
                helper.assertFalse(
                        stack.has(DataComponents.ATTACK_RANGE), key.path() + " must use vanilla attack reach");
            }
            if (key.type().armorForm() != EquipmentType.ArmorForm.NONE) {
                helper.assertTrue(stack.has(DataComponents.EQUIPPABLE), key.path() + " equippable component");
            }
            if (key.type() == EquipmentType.BOW) {
                helper.assertTrue(stack.getItem() instanceof InfxBowItem, key.path() + " bow class");
            } else if (key.type() == EquipmentType.ARROW) {
                helper.assertTrue(stack.getItem() instanceof InfxArrowItem, key.path() + " arrow class");
            } else if (key.type() == EquipmentType.FISHING_ROD) {
                helper.assertTrue(stack.getItem() instanceof InfxFishingRodItem, key.path() + " fishing class");
            }
        }
        helper.succeed();
    }

    private static void stickBoneAttackBehavior(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        ItemStack stick = new ItemStack(Items.STICK, 2);
        ItemStack bone = new ItemStack(Items.BONE, 2);

        helper.assertTrue(stick.getMaxStackSize() == 32, "InfX sticks must stack to 32 at runtime");
        helper.assertTrue(bone.getMaxStackSize() == 16, "InfX bones must stack to 16 at runtime");
        assertMeleeAttackRange(helper, player, stick, "stick");
        assertMeleeAttackRange(helper, player, bone, "bone");
        assertEmptyHandAttackRange(helper, player);

        double blockInteractionRange = player.blockInteractionRange();
        double entityInteractionRange = player.entityInteractionRange();
        helper.assertTrue(
                player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getBaseValue() == 1.5
                        && player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).getBaseValue() == 1.5,
                "player interaction attribute bases must be replaced with 1.5 blocks");
        helper.assertTrue(
                blockInteractionRange == 1.5 && entityInteractionRange == 1.5,
                "survival reach must use 1.5 blocks for block and entity interaction");
        BlockPos interactionBlock = helper.absolutePos(new BlockPos(4, 2, 1));
        var farTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 2, 1));
        boolean emptyHandBlockRange = player.isWithinBlockInteractionRange(interactionBlock, 0.0D);
        boolean emptyHandEntityRange = player.isWithinEntityInteractionRange(farTarget, 0.0D);

        player.setItemInHand(InteractionHand.MAIN_HAND, stick);
        var nearTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 2, 1));
        helper.assertTrue(
                player.isWithinAttackRange(stick, nearTarget.getBoundingBox(), 0.0D),
                "a stick must reach a target within two blocks");
        helper.assertFalse(
                player.isWithinAttackRange(stick, farTarget.getBoundingBox(), 0.0D),
                "a stick must not extend beyond its two-block melee reach");
        helper.assertTrue(
                player.blockInteractionRange() == blockInteractionRange
                        && player.entityInteractionRange() == entityInteractionRange,
                "a stick must not change block or entity interaction attributes");
        helper.assertTrue(
                player.isWithinBlockInteractionRange(interactionBlock, 0.0D) == emptyHandBlockRange
                        && player.isWithinEntityInteractionRange(farTarget, 0.0D) == emptyHandEntityRange,
                "a stick must not change block or entity interaction reach");

        attackWithGuaranteedBreak(helper, player, nearTarget, 50);
        helper.assertTrue(stick.getCount() == 1, "a successful stick hit must consume one stick on a 1/50 roll");

        player.setItemInHand(InteractionHand.MAIN_HAND, bone);
        var boneTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 2, 2));
        attackWithGuaranteedBreak(helper, player, boneTarget, 100);
        helper.assertTrue(bone.getCount() == 1, "a successful bone hit must consume one bone on a 1/100 roll");

        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        ItemStack creativeStick = new ItemStack(Items.STICK, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, creativeStick);
        var creativeReachTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 2, 1));
        helper.assertTrue(
                player.isWithinAttackRange(creativeStick, creativeReachTarget.getBoundingBox(), 0.0D),
                "creative sticks must retain the five-block attack reach");
        attackWithGuaranteedBreak(helper, player, creativeReachTarget, 50);
        helper.assertTrue(creativeStick.getCount() == 1, "creative stick attacks must not consume sticks");

        farTarget.discard();
        nearTarget.discard();
        boneTarget.discard();
        creativeReachTarget.discard();
        removePlayer(player);
        helper.succeed();
    }

    private static void assertEmptyHandAttackRange(GameTestHelper helper, ServerPlayer player) {
        var playerRange = player.getAttackRangeWith(ItemStack.EMPTY);
        helper.assertTrue(
                playerRange.maxReach() == 1.5F && playerRange.maxCreativeReach() == 5.0F,
                "empty hand must carry the INFX 1.5-block attack reach");
    }

    private static void assertMeleeAttackRange(
            GameTestHelper helper, ServerPlayer player, ItemStack stack, String description) {
        var component = stack.get(DataComponents.ATTACK_RANGE);
        var playerRange = player.getAttackRangeWith(stack);
        helper.assertTrue(
                component != null
                        && component.maxReach() == 2.0F
                        && component.maxCreativeReach() == 5.0F,
                description + " ItemStack must carry the InfX attack range component");
        helper.assertTrue(
                playerRange.maxReach() == 2.0F && playerRange.maxCreativeReach() == 5.0F,
                description + " Player#getAttackRangeWith must return the InfX attack range");
    }

    private static void attackWithGuaranteedBreak(
            GameTestHelper helper,
            ServerPlayer player,
            LivingEntity target,
            int denominator) {
        for (int tick = 0; tick < 20; tick++) {
            player.doTick();
        }
        long seed = 0L;
        while (RandomSource.create(seed++).nextInt(denominator) != 0) {
            // Find a deterministic seed so the runtime test exercises the break branch.
        }
        player.getRandom().setSeed(seed - 1L);
        float healthBefore = target.getHealth();
        player.attack(target);
        helper.assertTrue(target.getHealth() < healthBefore, "the seeded attack must damage its target");
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
                InfXItems.catalog()
                        .equipment(InfxMaterial.COPPER, EquipmentType.AXE)
                        .holder()
                        .value());
        helper.assertTrue(helper.getBlockState(axePos).is(Blocks.STRIPPED_OAK_LOG), "axe must strip logs");

        useOn(
                helper,
                player,
                shovelPos,
                Blocks.GRASS_BLOCK,
                InfXItems.catalog()
                        .equipment(InfxMaterial.COPPER, EquipmentType.SHOVEL)
                        .holder()
                        .value());
        helper.assertTrue(helper.getBlockState(shovelPos).is(Blocks.DIRT_PATH), "shovel must create paths");

        useOn(
                helper,
                player,
                hoePos,
                Blocks.DIRT,
                InfXItems.catalog()
                        .equipment(InfxMaterial.COPPER, EquipmentType.HOE)
                        .holder()
                        .value());
        helper.assertTrue(helper.getBlockState(hoePos).is(Blocks.FARMLAND), "hoe must till soil");

        Item mattock = InfXItems.catalog()
                .equipment(InfxMaterial.COPPER, EquipmentType.MATTOCK)
                .holder()
                .value();
        useOn(helper, player, mattockPos, Blocks.DIRT, mattock);
        helper.assertTrue(helper.getBlockState(mattockPos).is(Blocks.DIRT_PATH), "mattock shovel action");
        useOn(helper, player, mattockPos, Blocks.DIRT_PATH, mattock);
        helper.assertTrue(helper.getBlockState(mattockPos).is(Blocks.FARMLAND), "mattock hoe fallback");

        ItemStack scythe = InfXItems.catalog()
                .equipment(InfxMaterial.COPPER, EquipmentType.SCYTHE)
                .holder()
                .value()
                .getDefaultInstance();
        helper.assertTrue(
                scythe.getDestroySpeed(Blocks.WHEAT.defaultBlockState())
                        > scythe.getDestroySpeed(Blocks.STONE.defaultBlockState()),
                "scythe must be efficient against crops");

        var sheep = helper.spawn(EntityType.SHEEP, new BlockPos(6, 1, 1));
        ItemStack shears = InfXItems.catalog()
                .equipment(InfxMaterial.COPPER, EquipmentType.SHEARS)
                .holder()
                .value()
                .getDefaultInstance();
        helper.assertTrue(
                !shears.has(DataComponents.BLOCKS_ATTACKS),
                "material shears must not right-click block/parry");
        player.setItemInHand(InteractionHand.MAIN_HAND, shears);
        helper.assertTrue(
                shears.interactLivingEntity(player, sheep, InteractionHand.MAIN_HAND).consumesAction(),
                "material shears must interact with sheep");
        helper.assertTrue(sheep.isSheared(), "material shears must shear sheep");
        sheep.discard();

        var shearTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 2));
        for (int tick = 0; tick < 20; tick++) {
            player.doTick();
        }
        float shearTargetHealth = shearTarget.getHealth();
        int shearWearBeforeAttack = shears.getDamageValue();
        player.attack(shearTarget);
        helper.assertTrue(
                shearTarget.getHealth() < shearTargetHealth,
                "material shears must deal melee damage on left-click");
        helper.assertTrue(
                shears.getDamageValue()
                        == shearWearBeforeAttack
                                + new EquipmentKey(InfxMaterial.COPPER, EquipmentType.SHEARS).attackWear(),
                "material shears must apply INFX hit wear on left-click");

        var rightClickTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 3));
        float rightClickHealth = rightClickTarget.getHealth();
        int rightClickWear = shears.getDamageValue();
        helper.assertTrue(
                !shears.interactLivingEntity(player, rightClickTarget, InteractionHand.MAIN_HAND).consumesAction(),
                "material shears must not attack non-shearable entities on right-click");
        helper.assertTrue(
                rightClickTarget.getHealth() == rightClickHealth,
                "right-clicking a non-shearable entity with shears must not deal damage");
        helper.assertTrue(
                shears.getDamageValue() == rightClickWear,
                "right-clicking a non-shearable entity with shears must not consume durability");
        shearTarget.discard();
        rightClickTarget.discard();

        ItemStack sword = InfXItems.catalog()
                .equipment(InfxMaterial.COPPER, EquipmentType.SWORD)
                .holder()
                .value()
                .getDefaultInstance();

        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 1, 1));
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        float healthBefore = zombie.getHealth();
        player.attack(zombie);
        helper.assertTrue(zombie.getHealth() < healthBefore, "material sword must deal melee damage");
        helper.assertTrue(
                sword.getDamageValue()
                        == new EquipmentKey(InfxMaterial.COPPER, EquipmentType.SWORD).attackWear(),
                "material sword must apply INFX hit wear exactly once");
        zombie.discard();

        ItemStack blockShears = InfXItems.catalog()
                .equipment(InfxMaterial.COPPER, EquipmentType.SHEARS)
                .holder()
                .value()
                .getDefaultInstance();

        BlockPos leavesPos = new BlockPos(8, 1, 1);
        BlockPos leftClickPos = new BlockPos(8, 1, 3);
        helper.setBlock(leftClickPos.below(), Blocks.STONE);
        helper.setBlock(leftClickPos, Blocks.RED_WOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, blockShears);
        helper.assertTrue(
                player.gameMode.destroyBlock(helper.absolutePos(leftClickPos)),
                "left-click shears must destroy shears-effective blocks");
        helper.assertTrue(
                helper.getBlockState(leftClickPos).isAir(),
                "left-click shears must remove the wool block");
        helper.assertTrue(
                blockShears.getDamageValue() > 0,
                "a successful left-click block break must consume durability");

        BlockPos stonePos = new BlockPos(8, 1, 4);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.assertFalse(
                player.gameMode.destroyBlock(helper.absolutePos(stonePos)),
                "left-click shears must not destroy non-shears blocks");
        helper.assertTrue(
                helper.getBlockState(stonePos).is(Blocks.STONE),
                "left-click shears must leave stone in place");

        EmbeddedChannel playerChannel = (EmbeddedChannel) player.connection.getConnection().channel();
        while (playerChannel.readOutbound() != null) {}
        useOnBlock(helper, player, leavesPos, Blocks.OAK_LEAVES.defaultBlockState(), blockShears);
        helper.assertTrue(helper.getBlockState(leavesPos).isAir(), "right-click shears must cut leaves");
        boolean receivedShearsSound = false;
        Object outbound;
        while ((outbound = playerChannel.readOutbound()) != null) {
            if (outbound instanceof ClientboundSoundPacket soundPacket
                    && soundPacket.getSound().value() == SoundEvents.SHEARS_SNIP) {
                receivedShearsSound = true;
            }
        }
        helper.assertTrue(receivedShearsSound, "right-click block shearing must play the shears sound");

        BlockPos woolPos = new BlockPos(9, 1, 1);
        useOnBlock(helper, player, woolPos, Blocks.WHITE_WOOL.defaultBlockState(), blockShears);
        helper.assertTrue(helper.getBlockState(woolPos).isAir(), "right-click shears must cut wool");

        BlockPos blueberryPos = new BlockPos(10, 1, 1);
        BlockState matureBlueberry = InfXBlocks.BLUEBERRY_BUSH.get()
                .defaultBlockState()
                .setValue(SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE);
        useOnBlock(helper, player, blueberryPos, matureBlueberry, blockShears);
        helper.assertTrue(helper.getBlockState(blueberryPos).isAir(), "right-click shears must cut blueberry bushes");
        int blueberryDrops = helper.getLevel()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        AABB.ofSize(helper.absoluteVec(Vec3.atCenterOf(blueberryPos)), 4.0D, 4.0D, 4.0D),
                        item -> item.getItem().is(InfXItems.BLUEBERRIES))
                .stream()
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        helper.assertTrue(blueberryDrops == 1, "cutting a mature blueberry bush must drop one blueberry");
        int bushDrops = itemCount(helper, blueberryPos, InfXItems.BLUEBERRY_BUSH.get());
        helper.assertTrue(bushDrops == 1, "cutting a blueberry bush must drop the bush itself");
        helper.getLevel()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        AABB.ofSize(helper.absoluteVec(Vec3.atCenterOf(blueberryPos)), 4.0D, 4.0D, 4.0D),
                        item -> item.getItem().is(InfXItems.BLUEBERRIES)
                                || item.getItem().is(InfXItems.BLUEBERRY_BUSH))
                .forEach(ItemEntity::discard);
        helper.assertTrue(blockShears.getDamageValue() > 0, "right-click block shearing must consume durability");

        assertNotRightClickShearable(helper, player, new BlockPos(11, 1, 1), Blocks.RED_BED, blockShears, "beds");
        assertNotRightClickShearable(helper, player, new BlockPos(12, 1, 1), Blocks.BAMBOO, blockShears, "bamboo");
        assertNotRightClickShearable(
                helper, player, new BlockPos(13, 1, 1), Blocks.BAMBOO_SAPLING, blockShears, "bamboo saplings");
        assertNotRightClickShearable(
                helper, player, new BlockPos(14, 1, 1), Blocks.SUGAR_CANE, blockShears, "sugar cane");

        helper.setBlock(wearPos, Blocks.OAK_LOG);
        BlockPos absoluteWearPos = helper.absolutePos(wearPos);
        BlockState state = helper.getBlockState(wearPos);
        ItemStack hatchet = InfXItems.FLINT_HATCHET.get().getDefaultInstance();
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
        useOnBlock(helper, player, relativePos, block.defaultBlockState(), item.getDefaultInstance());
    }

    private static void useOnBlock(
            GameTestHelper helper,
            ServerPlayer player,
            BlockPos relativePos,
            BlockState state,
            ItemStack stack) {
        helper.setBlock(relativePos.below(), Blocks.STONE);
        helper.setBlock(relativePos, state);
        helper.setBlock(relativePos.above(), Blocks.AIR);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(relativePos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        InteractionResult result = stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(result.consumesAction(), stack.getItem() + " use action must succeed");
    }

    private static void assertNotRightClickShearable(
            GameTestHelper helper,
            ServerPlayer player,
            BlockPos relativePos,
            Block block,
            ItemStack stack,
            String description) {
        helper.setBlock(relativePos.below(), Blocks.STONE);
        helper.setBlock(relativePos, block.defaultBlockState());
        helper.setBlock(relativePos.above(), Blocks.AIR);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(relativePos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        int damageBefore = stack.getDamageValue();
        InteractionResult result = stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertFalse(result.consumesAction(), "right-click shears must not cut " + description);
        helper.assertTrue(helper.getBlockState(relativePos).is(block), description + " must remain in place");
        helper.assertTrue(stack.getDamageValue() == damageBefore, description + " must not consume shears durability");
    }

    private static int itemCount(GameTestHelper helper, BlockPos relativePos, Item item) {
        return helper.getLevel()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        AABB.ofSize(helper.absoluteVec(Vec3.atCenterOf(relativePos)), 4.0D, 4.0D, 4.0D),
                        entity -> entity.getItem().is(item))
                .stream()
                .mapToInt(entity -> entity.getItem().getCount())
                .sum();
    }

    private static void harvestTierCatalog(GameTestHelper helper) {
        Map<HarvestTier, EquipmentKey> representatives = Map.of(
                HarvestTier.FLINT,
                new EquipmentKey(InfxMaterial.FLINT, EquipmentType.HATCHET),
                HarvestTier.COPPER,
                new EquipmentKey(InfxMaterial.COPPER, EquipmentType.PICKAXE),
                HarvestTier.IRON,
                new EquipmentKey(InfxMaterial.IRON, EquipmentType.PICKAXE),
                HarvestTier.ANCIENT_METAL,
                new EquipmentKey(InfxMaterial.ANCIENT_METAL, EquipmentType.PICKAXE),
                HarvestTier.MITHRIL,
                new EquipmentKey(InfxMaterial.MITHRIL, EquipmentType.PICKAXE),
                HarvestTier.ADAMANTIUM,
                new EquipmentKey(InfxMaterial.ADAMANTIUM, EquipmentType.PICKAXE));

        representatives.forEach((tier, key) -> {
            ItemStack stack = InfXItems.catalog()
                    .equipment(key.material(), key.type())
                    .holder()
                    .value()
                    .getDefaultInstance();
            for (HarvestTier candidate : HarvestTier.values()) {
                helper.assertTrue(
                        stack.is(InfXItemTags.toolTier(candidate)) == (candidate == tier),
                        key.path() + " unexpected tier tag " + candidate);
            }
        });
        helper.assertTrue(HarvestTier.ADAMANTIUM.satisfies(HarvestTier.FLINT), "top tier must satisfy flint");
        helper.assertFalse(HarvestTier.FLINT.satisfies(HarvestTier.COPPER), "flint must not satisfy copper");
        helper.assertTrue(HarvestTier.IRON.satisfies(HarvestTier.ANCIENT_METAL),
                "iron and ancient metal share InfX level three");
        helper.assertTrue(HarvestTier.ANCIENT_METAL.satisfies(HarvestTier.IRON),
                "ancient metal and iron share InfX level three");

        assertHarvestLevel(helper, Blocks.COAL_BLOCK, 0);
        assertHarvestLevel(helper, Blocks.GLOWSTONE, 0);
        assertHarvestLevel(helper, Blocks.INFESTED_STONE, 0);
        assertHarvestLevel(helper, InfXBlocks.INFESTED_NETHERRACK.get(), 0);
        assertHarvestLevel(helper, Blocks.OAK_LOG, 1);
        assertHarvestLevel(helper, InfXBlocks.GRAVEL.get(), 1);
        assertHarvestLevel(helper, Blocks.TERRACOTTA, 1);
        assertHarvestLevel(helper, Blocks.SANDSTONE_SLAB, 1);
        assertHarvestLevel(helper, Blocks.STONE, 2);
        assertHarvestLevel(helper, Blocks.PETRIFIED_OAK_SLAB, 2);
        assertHarvestLevel(helper, Blocks.SANDSTONE_STAIRS, 2);
        assertHarvestLevel(helper, Blocks.SANDSTONE_WALL, 2);
        assertHarvestLevel(helper, Blocks.NETHERRACK, 3);
        assertHarvestLevel(helper, Blocks.CRIMSON_NYLIUM, 3);
        assertHarvestLevel(helper, Blocks.WARPED_NYLIUM, 3);
        assertHarvestLevel(helper, Blocks.COPPER_BLOCK, 3);
        assertHarvestLevel(helper, Blocks.IRON_BARS, 3);
        assertHarvestLevel(helper, Blocks.REDSTONE_BLOCK, 3);
        assertHarvestLevel(helper, InfXBlocks.MITHRIL_RUNE_STONE.get(), 3);
        assertHarvestLevel(helper, InfXBlocks.ADAMANTIUM_RUNE_STONE.get(), 3);
        assertHarvestLevel(helper, Blocks.DIAMOND_ORE, 4);
        assertHarvestLevel(helper, Blocks.DIAMOND_BLOCK, 5);
        assertHarvestLevel(helper, InfXBlocks.ADAMANTIUM_BLOCK.get(), 6);

        ItemStack pickaxe = equipmentStack(InfxMaterial.IRON, EquipmentType.PICKAXE);
        helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.GLOWSTONE.defaultBlockState()),
                "InfX glass material makes pickaxes effective against glowstone");
        helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.TORCH.defaultBlockState()),
                "InfX circuit material makes pickaxes effective against torches");
        helper.assertFalse(pickaxe.isCorrectToolForDrops(Blocks.PISTON.defaultBlockState()),
                "InfX piston material has no effective tool");
        helper.assertFalse(pickaxe.isCorrectToolForDrops(Blocks.ANVIL.defaultBlockState()),
                "InfX anvil material has no effective tool and relies on portability");

        ItemStack axe = equipmentStack(InfxMaterial.FLINT, EquipmentType.AXE);
        helper.assertTrue(axe.isCorrectToolForDrops(Blocks.SANDSTONE.defaultBlockState()),
                "axes must harvest the InfX sandstone block");
        helper.assertTrue(axe.isCorrectToolForDrops(Blocks.SANDSTONE_SLAB.defaultBlockState()),
                "InfX explicitly extends axe effectiveness to sandstone slabs");
        helper.assertFalse(axe.isCorrectToolForDrops(Blocks.SANDSTONE_STAIRS.defaultBlockState()),
                "sandstone stairs retain their stone level and are not axe-effective in InfX");
        helper.assertTrue(
                Math.abs(axe.getDestroySpeed(Blocks.SANDSTONE.defaultBlockState()) * 2.0F
                        - axe.getDestroySpeed(Blocks.SANDSTONE_SLAB.defaultBlockState())) < 1.0E-6F,
                "only the sandstone block receives InfX's half axe speed");
        helper.assertTrue(axe.isCorrectToolForDrops(Blocks.INFESTED_STONE.defaultBlockState()),
                "InfX infested blocks use axe-effective clay material");
        helper.assertTrue(axe.isCorrectToolForDrops(
                InfXBlocks.STRIPPED_LOG_WORKBENCHES.getFirst().flint().get().defaultBlockState()),
                "all tiered workbenches retain their wood-material axe effectiveness");

        ItemStack flintShovel = equipmentStack(InfxMaterial.FLINT, EquipmentType.SHOVEL);
        ItemStack copperShovel = equipmentStack(InfxMaterial.COPPER, EquipmentType.SHOVEL);
        ItemStack copperPickaxe = equipmentStack(InfxMaterial.COPPER, EquipmentType.PICKAXE);
        ItemStack flintAxe = equipmentStack(InfxMaterial.FLINT, EquipmentType.AXE);
        BlockState gravel = InfXBlocks.GRAVEL.get().defaultBlockState();
        for (Block block : List.of(Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM)) {
            BlockState state = block.defaultBlockState();
            helper.assertFalse(copperPickaxe.isCorrectToolForDrops(state),
                    block + " must reject copper pickaxes");
            helper.assertTrue(pickaxe.isCorrectToolForDrops(state),
                    block + " must accept iron pickaxes");
        }
        helper.assertTrue(gravel.requiresCorrectToolForDrops(), "gravel requires a correct tool for drops");
        helper.assertTrue(flintShovel.isCorrectToolForDrops(gravel), "flint shovels must harvest gravel");
        helper.assertTrue(copperShovel.isCorrectToolForDrops(gravel), "higher-tier shovels must harvest gravel");
        helper.assertFalse(copperPickaxe.isCorrectToolForDrops(gravel), "pickaxes must not harvest gravel");
        helper.assertFalse(flintAxe.isCorrectToolForDrops(gravel), "axes must not harvest gravel");
        helper.assertFalse(flintShovel.isCorrectToolForDrops(Blocks.GLASS.defaultBlockState()),
                "non-metal shovels must not harvest full glass");
        helper.assertTrue(copperShovel.isCorrectToolForDrops(Blocks.GLASS.defaultBlockState()),
                "metal shovels must harvest full glass");
        helper.assertTrue(flintShovel.isCorrectToolForDrops(Blocks.INFESTED_STONE.defaultBlockState()),
                "shovels must inherit infested clay effectiveness");

        ItemStack hoe = equipmentStack(InfxMaterial.COPPER, EquipmentType.HOE);
        helper.assertTrue(hoe.isCorrectToolForDrops(
                InfXBlocks.SANDSTONE_FURNACE.get().defaultBlockState()),
                "the INFX sandstone furnace uses hoe-effective sand material");
        helper.assertFalse(hoe.isCorrectToolForDrops(
                InfXBlocks.CLAY_FURNACE.get().defaultBlockState()),
                "hoes must remain ineffective against INFX clay material");

        ItemStack hammer = equipmentStack(InfxMaterial.COPPER, EquipmentType.WAR_HAMMER);
        helper.assertTrue(hammer.isCorrectToolForDrops(Blocks.CAKE.defaultBlockState()),
                "war hammers retain their InfX cake override");
        ItemStack cudgel = equipmentStack(InfxMaterial.WOOD, EquipmentType.CUDGEL);
        helper.assertTrue(cudgel.isCorrectToolForDrops(Blocks.CAKE.defaultBlockState()),
                "wooden cudgels retain their InfX cake effectiveness");
        helper.assertTrue(cudgel.isCorrectToolForDrops(Blocks.GLOWSTONE.defaultBlockState()),
                "wooden cudgels can harvest level-zero InfX glass material");
        helper.assertFalse(cudgel.isCorrectToolForDrops(Blocks.GLASS.defaultBlockState()),
                "wooden cudgels cannot meet full glass level one");
        helper.assertFalse(cudgel.isCorrectToolForDrops(Blocks.ICE.defaultBlockState()),
                "wooden cudgels cannot meet ice level one");
        ItemStack scythe = equipmentStack(InfxMaterial.COPPER, EquipmentType.SCYTHE);
        helper.assertTrue(scythe.isCorrectToolForDrops(Blocks.WHEAT.defaultBlockState()),
                "scythes must harvest wheat");
        helper.assertFalse(scythe.isCorrectToolForDrops(Blocks.CARROTS.defaultBlockState()),
                "root crops must remain shovel/hoe work rather than scythe work");
        ItemStack sword = equipmentStack(InfxMaterial.COPPER, EquipmentType.SWORD);
        ItemStack shears = equipmentStack(InfxMaterial.COPPER, EquipmentType.SHEARS);
        helper.assertTrue(sword.isCorrectToolForDrops(Blocks.HAY_BLOCK.defaultBlockState()),
                "swords must inherit InfX plant-material effectiveness");
        helper.assertTrue(shears.isCorrectToolForDrops(Blocks.NETHER_WART.defaultBlockState()),
                "shears must inherit InfX plant-material effectiveness");

        for (Block block : BuiltInRegistries.BLOCK) {
            BlockState state = block.defaultBlockState();
            int level = HarvestRequirements.requiredLevel(state);
            helper.assertTrue(level >= 0 && level <= HarvestRequirements.MAX_LEVEL,
                    block + " has invalid harvest level " + level);
            helper.assertTrue(HarvestRequirements.explicitLevelCount(state) <= 1,
                    block + " belongs to more than one explicit harvest level");
            if (level > 0 && block.defaultDestroyTime() >= 0.0F) {
                boolean portable = state.is(InfXBlockTags.PORTABLE_HAND_HARVEST);
                boolean hasEffectiveTool = java.util.Arrays.stream(MiningFamily.values())
                        .filter(family -> family != MiningFamily.NONE)
                        .anyMatch(family -> state.is(InfXBlockTags.effectiveWith(family)));
                helper.assertTrue(portable || hasEffectiveTool,
                        block + " level " + level + " has neither a InfX tool family nor portability");
            }
        }
        helper.succeed();
    }

    private static void assertHarvestLevel(GameTestHelper helper, Block block, int expected) {
        int actual = HarvestRequirements.requiredLevel(block.defaultBlockState());
        helper.assertTrue(actual == expected,
                block + " expected harvest level " + expected + " but got " + actual);
    }

    private static ItemStack equipmentStack(InfxMaterial material, EquipmentType type) {
        return InfXItems.catalog().equipment(material, type).holder().toStack();
    }

    private static void materialArrows(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        Vec3 position = player.position();
        for (InfxMaterial material : EquipmentType.ARROW.allowedMaterials()) {
            EquipmentKey key = new EquipmentKey(material, EquipmentType.ARROW);
            InfxArrowItem item = (InfxArrowItem) InfXItems.catalog()
                    .equipment(material, EquipmentType.ARROW)
                    .holder()
                    .value();
            ItemStack stack = item.getDefaultInstance();
            AbstractArrow fired = item.createArrow(helper.getLevel(), stack, player, null);
            AbstractArrow dispensed =
                    (AbstractArrow) item.asProjectile(helper.getLevel(), position, stack, Direction.NORTH);
            helper.assertTrue(fired.getPickupItemStackOrigin().is(item), key.path() + " fired pickup identity");
            helper.assertTrue(dispensed.getPickupItemStackOrigin().is(item), key.path() + " dispensed pickup identity");
            helper.assertTrue(
                    fired.pickup == AbstractArrow.Pickup.ALLOWED,
                    key.path() + " player-fired arrows are normally recoverable from the ground");
            helper.assertTrue(
                    dispensed.pickup == AbstractArrow.Pickup.DISALLOWED,
                    key.path() + " dispenser arrows stay non-pickup");
            helper.assertTrue(
                    Math.abs(item.baseDamage() - key.arrowBaseDamage()) < 1.0E-9,
                    key.path() + " damage");

            BlockHitResult impact = new BlockHitResult(
                    position,
                    Direction.UP,
                    BlockPos.containing(position),
                    false);
            EquipmentBehaviors.resolveArrowRecovery(dispensed, impact);
            helper.assertTrue(
                    dispensed.pickup == AbstractArrow.Pickup.DISALLOWED,
                    key.path() + " block impacts must not trigger recovery");
            EquipmentBehaviors.resolveArrowRecovery(fired, impact);
            helper.assertTrue(
                    fired.pickup == AbstractArrow.Pickup.ALLOWED,
                    key.path() + " block impacts preserve normal player-arrow pickup");

            ItemStack infiniteStack = stack.copy();
            infiniteStack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            AbstractArrow infinite = item.createArrow(helper.getLevel(), infiniteStack, player, null);
            helper.assertTrue(
                    infinite.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                    key.path() + " infinite arrow pickup boundary");
            EquipmentBehaviors.resolveArrowRecovery(infinite, impact);
            helper.assertTrue(
                    infinite.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                    key.path() + " infinite arrow never becomes recoverable");
        }

        InfxArrowItem flint = (InfxArrowItem) InfXItems.catalog()
                .equipment(InfxMaterial.FLINT, EquipmentType.ARROW)
                .holder()
                .value();
        AbstractArrow entityArrow = (AbstractArrow) flint.asProjectile(
                helper.getLevel(), position, flint.getDefaultInstance(), Direction.NORTH);
        long recoveringSeed = 0L;
        while (RandomSource.create(recoveringSeed).nextFloat()
                >= EquipmentBehaviors.recoveryChance(InfxMaterial.FLINT)) {
            recoveringSeed++;
        }
        entityArrow.getRandom().setSeed(recoveringSeed);
        var target = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(2, 2, 2));
        int itemsBefore = helper.getLevel()
                .getEntities(EntityType.ITEM, target.getBoundingBox().inflate(8.0), entity -> true)
                .size();
        EquipmentBehaviors.resolveArrowRecovery(entityArrow, new EntityHitResult(target));
        int itemsAfter = helper.getLevel()
                .getEntities(EntityType.ITEM, target.getBoundingBox().inflate(8.0), entity -> true)
                .size();
        helper.assertTrue(itemsAfter == itemsBefore + 1, "recovered entity hit drops exactly one material arrow");
        helper.assertTrue(
                entityArrow.pickup == AbstractArrow.Pickup.DISALLOWED,
                "recovered entity hit must not leave the projectile pickable");
        entityArrow.getRandom().setSeed(recoveringSeed + 1L);
        EquipmentBehaviors.resolveArrowRecovery(entityArrow, new EntityHitResult(target));
        int repeatedItems = helper.getLevel()
                .getEntities(EntityType.ITEM, target.getBoundingBox().inflate(8.0), entity -> true)
                .size();
        helper.assertTrue(repeatedItems == itemsAfter, "an entity hit receives only one recovery roll");
        target.discard();

        var acidSlime = helper.spawnWithNoFreeWill(InfXEntityTypes.OOZE.get(), new BlockPos(4, 2, 2));
        AbstractArrow unownedAcidArrow = (AbstractArrow) flint.asProjectile(
                helper.getLevel(), position, flint.getDefaultInstance(), Direction.NORTH);
        ProjectileImpactEvent unownedImpact =
                new ProjectileImpactEvent(unownedAcidArrow, new EntityHitResult(acidSlime));
        EquipmentBehaviors.onProjectileImpact(unownedImpact);
        helper.assertTrue(unownedImpact.isCanceled(), "acid slime must cancel an unowned corrosible arrow impact");
        helper.assertTrue(unownedAcidArrow.isRemoved(), "acid slime must consume an unowned corrosible arrow");

        ItemStack creativeArrowStack = flint.getDefaultInstance();
        creativeArrowStack.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        AbstractArrow creativeAcidArrow = flint.createArrow(helper.getLevel(), creativeArrowStack, player, null);
        ProjectileImpactEvent creativeImpact =
                new ProjectileImpactEvent(creativeAcidArrow, new EntityHitResult(acidSlime));
        EquipmentBehaviors.onProjectileImpact(creativeImpact);
        helper.assertTrue(creativeImpact.isCanceled(), "acid slime must cancel a creative-only corrosible arrow impact");
        helper.assertTrue(creativeAcidArrow.isRemoved(), "acid slime must consume a creative-only corrosible arrow");
        acidSlime.discard();
        removePlayer(player);
        helper.succeed();
    }

    private static void materialBows(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        Item silverArrow = InfXItems.catalog()
                .equipment(InfxMaterial.SILVER, EquipmentType.ARROW)
                .holder()
                .value();
        for (InfxMaterial material :
                List.of(InfxMaterial.WOOD, InfxMaterial.ANCIENT_METAL, InfxMaterial.MITHRIL)) {
            InfxBowItem bowItem = (InfxBowItem) InfXItems.catalog()
                    .equipment(material, EquipmentType.BOW)
                    .holder()
                    .value();
            ItemStack bow = bowItem.getDefaultInstance();
            player.setItemInHand(InteractionHand.MAIN_HAND, bow);
            player.getInventory().add(silverArrow.getDefaultInstance());
            int before = helper.getLevel()
                    .getEntities(EntityType.ARROW, player.getBoundingBox().inflate(32.0), arrow -> true)
                    .size();
            helper.assertTrue(
                    bowItem.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                    material.path() + " bow nock");
            helper.assertTrue(
                    "silver".equals(bow.get(InfXDataComponents.NOCKED_ARROW_MATERIAL.get())),
                    material.path() + " nocked model state");
            for (int tick = 0; tick < 20; tick++) {
                player.doTick();
            }
            player.releaseUsingItem();
            int after = helper.getLevel()
                    .getEntities(EntityType.ARROW, player.getBoundingBox().inflate(32.0), arrow -> true)
                    .size();
            helper.assertTrue(after == before + 1, material.path() + " bow must spawn one arrow");
            helper.assertTrue(
                    !bow.has(InfXDataComponents.NOCKED_ARROW_MATERIAL.get()),
                    material.path() + " bow must clear nocked state");
        }
        removePlayer(player);
        helper.succeed();
    }

    private static void fishingRods(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        player.setOnGround(true);
        for (InfxMaterial material : List.of(InfxMaterial.FLINT, InfxMaterial.IRON, InfxMaterial.ADAMANTIUM)) {
            InfxFishingRodItem rod = (InfxFishingRodItem) InfXItems.catalog()
                    .equipment(material, EquipmentType.FISHING_ROD)
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
        // InfX forbids casting with the head under liquid or while airborne.
        BlockPos waterPos = new BlockPos(1, 2, 4);
        helper.setBlock(waterPos, Blocks.WATER);
        helper.setBlock(waterPos.above(), Blocks.WATER);
        player.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(waterPos)), 0.0F, 0.0F);
        player.setOnGround(true);
        InfxFishingRodItem rod = (InfxFishingRodItem) InfXItems.catalog()
                .equipment(InfxMaterial.FLINT, EquipmentType.FISHING_ROD)
                .holder()
                .value();
        player.setItemInHand(InteractionHand.MAIN_HAND, rod.getDefaultInstance());
        helper.assertTrue(
                rod.use(helper.getLevel(), player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
                "rod must refuse to cast with the head under water");
        player.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 2, 1))), 0.0F, 0.0F);
        player.setOnGround(false);
        helper.assertTrue(
                rod.use(helper.getLevel(), player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
                "rod must refuse to cast while airborne");
        player.setOnGround(true);
        helper.assertTrue(
                rod.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).consumesAction(),
                "rod must cast again on dry ground");
        removePlayer(player);
        helper.succeed();
    }

    private static void heightAdvantage(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        player.setOnGround(true);
        ItemStack sword = InfXItems.catalog()
                .equipment(InfxMaterial.IRON, EquipmentType.SWORD)
                .holder()
                .toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        // INFX tools must use the vanilla component-less attack reach (and pick path), so
        // swords carry no attack-range component and inherit the INFX 1.5-block melee reach.
        helper.assertFalse(sword.has(DataComponents.ATTACK_RANGE), "sword must not carry an attack range");
        var swordRange = player.getAttackRangeWith(sword);
        helper.assertTrue(
                swordRange.maxReach() == 1.5F && swordRange.maxCreativeReach() == 5.0F,
                "sword must inherit the INFX 1.5-block attack reach");
        // The InfX height advantage still applies to items carrying an attack-range component
        // (sticks and bones). A pig two blocks below stands with its top about 2.7 blocks under
        // the player's eye: beyond the 2.0-block stick reach, inside the height-advantaged reach.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        var stickRange = player.getAttackRangeWith(new ItemStack(Items.STICK));
        Vec3 pigTopBelow = player.getEyePosition().add(0.0, -2.7, 0.0);
        helper.assertTrue(
                stickRange.isInRange(player, pigTopBelow),
                "stick must hit a target two blocks below via the InfX height advantage");
        helper.assertTrue(
                stickRange.isInRange(player, player.getEyePosition()),
                "eye-level target must stay in range");
        removePlayer(player);
        helper.succeed();
    }

    private static void carrotStickBoost(GameTestHelper helper) {
        var pig = helper.spawn(InfXEntityTypes.INFX_PIG.get(), new BlockPos(4, 2, 1));
        pig.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        ServerPlayer player = createPlayer(helper);
        InfxCarrotOnAStickItem stick = InfXItems.CARROT_ON_A_STICKS.get(InfxMaterial.IRON).value();
        ItemStack stack = stick.getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.startRiding(pig, true, false);
        helper.assertTrue(
                pig.getControllingPassenger() == player,
                "INFX pig must be controlled by a rider holding an InfX carrot stick");
        var result = stick.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(
                result.consumesAction(),
                "carrot stick use on INFX pig must succeed, got " + result);
        helper.assertTrue(
                player.getMainHandItem().getDamageValue() > 0,
                "carrot stick must take boost damage, got " + player.getMainHandItem().getDamageValue());
        // vanilla pig path
        var vanillaPig = helper.spawn(EntityType.PIG, new BlockPos(6, 2, 1));
        vanillaPig.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        player.stopRiding();
        player.startRiding(vanillaPig, true, false);
        helper.assertTrue(
                vanillaPig.getControllingPassenger() == player,
                "vanilla pig must be controlled by a rider holding an InfX carrot stick");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var result2 = stick.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(
                result2.consumesAction(),
                "carrot stick use on vanilla pig must succeed, got " + result2);
        // warped fungus on a stick / strider path
        var strider = helper.spawn(EntityType.STRIDER, new BlockPos(8, 2, 1));
        strider.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        player.stopRiding();
        player.startRiding(strider, true, false);
        InfxWarpedFungusOnAStickItem fungusStick =
                InfXItems.WARPED_FUNGUS_ON_A_STICKS.get(InfxMaterial.IRON).value();
        player.setItemInHand(InteractionHand.MAIN_HAND, fungusStick.getDefaultInstance());
        helper.assertTrue(
                strider.getControllingPassenger() == player,
                "strider must be controlled by a rider holding an InfX fungus stick");
        var result3 = fungusStick.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(
                result3.consumesAction(),
                "fungus stick use on strider must succeed, got " + result3);
        removePlayer(player);
        helper.succeed();
    }

    private static void armorAndHorseArmor(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        equipSet(helper, player, InfxMaterial.MITHRIL, EquipmentType.platePieces());
        player.doTick();
        double plateArmor = player.getAttributeValue(Attributes.ARMOR);
        helper.assertTrue(
                Math.abs(plateArmor - 9.0) < 1.0E-6,
                "mithril plate must sum to 9, got " + plateArmor);
        equipSet(helper, player, InfxMaterial.MITHRIL, EquipmentType.chainPieces());
        player.doTick();
        double chainArmor = player.getAttributeValue(Attributes.ARMOR);
        helper.assertTrue(
                Math.abs(chainArmor - 7.0) < 1.0E-6,
                "mithril chain must sum to 7, got " + chainArmor);

        var horse = helper.spawn(EntityType.HORSE, new BlockPos(6, 1, 1));
        EquipmentKey horseKey =
                new EquipmentKey(InfxMaterial.ADAMANTIUM, EquipmentType.HORSE_ARMOR);
        ItemStack horseArmor = InfXItems.catalog()
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
        Map<String, Set<InfxMaterial>> expected = Map.of(
                "simple_dungeon", Set.of(InfxMaterial.COPPER, InfxMaterial.GOLD, InfxMaterial.IRON),
                "nether_bridge", Set.of(InfxMaterial.COPPER, InfxMaterial.GOLD, InfxMaterial.IRON),
                "desert_pyramid", Set.of(InfxMaterial.SILVER, InfxMaterial.GOLD, InfxMaterial.IRON),
                "jungle_temple", Set.of(InfxMaterial.SILVER, InfxMaterial.GOLD, InfxMaterial.IRON),
                "stronghold_corridor", Set.of(InfxMaterial.COPPER, InfxMaterial.IRON));
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(Vec3.ZERO))
                .create(LootContextParamSets.CHEST);
        for (var structure : expected.entrySet()) {
            ResourceKey<LootTable> key = ResourceKey.create(
                    Registries.LOOT_TABLE,
                    Identifier.withDefaultNamespace("chests/" + structure.getKey()));
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
            Set<InfxMaterial> found = new HashSet<>();
            for (long seed = 0; seed < 2000 && !found.equals(structure.getValue()); seed++) {
                for (ItemStack stack : table.getRandomItems(params, seed)) {
                    for (InfxMaterial material : structure.getValue()) {
                        if (stack.is(InfXItems.catalog()
                                .equipment(material, EquipmentType.HORSE_ARMOR)
                                .holder()
                                .get())) {
                            found.add(material);
                        }
                    }
                }
            }
            helper.assertTrue(
                    found.equals(structure.getValue()),
                    structure.getKey() + " must expose exactly its INFX horse armor set, found " + found);
        }
        helper.succeed();
    }

    private static void equipSet(
            GameTestHelper helper,
            ServerPlayer player,
            InfxMaterial material,
            List<EquipmentType> pieces) {
        for (EquipmentSlot slot :
                List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        for (EquipmentType type : pieces) {
            EquipmentKey key = new EquipmentKey(material, type);
            ItemStack armor = InfXItems.catalog()
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
