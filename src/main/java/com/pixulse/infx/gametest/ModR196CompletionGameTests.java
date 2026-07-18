package com.pixulse.infx.gametest;

import com.mojang.authlib.GameProfile;
import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.block.entity.MetalAnvilBlockEntity;
import com.pixulse.infx.crafting.TimedCraftingMenu;
import com.pixulse.infx.equipment.R196EquipmentBehaviors;
import com.pixulse.infx.equipment.R196QualitySystem;
import com.pixulse.infx.item.R196ArrowItem;
import com.pixulse.infx.item.R196CoinItem;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.material.R196Quality;
import com.pixulse.infx.menu.MetalAnvilMenu;
import com.pixulse.infx.registry.ModBlocks;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.world.Underworld;
import com.pixulse.infx.world.UnderworldPortalEvents;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModR196CompletionGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);
    private static final List<String> NAMES = List.of(
            "r196_special_behaviors", "r196_quality_coin", "r196_metal_anvil", "r196_underworld");
    private static final AtomicInteger PLAYER_SEQUENCE = new AtomicInteger();

    static {
        FUNCTIONS.register("r196_special_behaviors", () -> ModR196CompletionGameTests::specialBehaviors);
        FUNCTIONS.register("r196_quality_coin", () -> ModR196CompletionGameTests::qualityAndCoin);
        FUNCTIONS.register("r196_metal_anvil", () -> ModR196CompletionGameTests::metalAnvil);
        FUNCTIONS.register("r196_underworld", () -> ModR196CompletionGameTests::underworld);
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
                                    400,
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
        helper.assertTrue(registries.lookupOrThrow(Registries.DIMENSION_TYPE).containsKey(Underworld.TYPE), "Underworld type registered");
        helper.assertTrue(registries.lookupOrThrow(Registries.BIOME).containsKey(Underworld.BIOME), "Underworld biome registered");
        helper.assertTrue(registries.lookupOrThrow(Registries.NOISE_SETTINGS).containsKey(Underworld.NOISE), "Underworld noise registered");

        // GameTestServer intentionally builds a flat world from an empty extra-dimension set.
        // Verify that the portal fails safely in that harness; normal server startup verifies
        // that the generated LevelStem is materialized as infx:underworld.
        helper.assertTrue(helper.getLevel().getServer().getLevel(Underworld.LEVEL) == null, "GameTest has no custom levels");
        TeleportTransition transition = ModBlocks.UNDERWORLD_PORTAL.get()
                .getPortalDestination(helper.getLevel(), player, player.blockPosition());
        helper.assertTrue(transition == null, "portal must fail safely when the target level is unavailable");

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

    private static ServerPlayer createPlayer(GameTestHelper helper) {
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

    private static void removePlayer(ServerPlayer player) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        player.level().getServer().getPlayerList().remove(player);
    }
}
