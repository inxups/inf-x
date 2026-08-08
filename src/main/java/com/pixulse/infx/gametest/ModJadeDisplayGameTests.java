package com.pixulse.infx.gametest;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.compat.jade.InfxHarvestToolDisplay;
import com.pixulse.infx.registry.InfXBlocks;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Verifies that the Jade harvest-tool display resolves InfX mining rules, not vanilla tags. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModJadeDisplayGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);

    private static final List<String> TEST_NAMES = List.of("jade_harvest_tool_display");

    static {
        TEST_FUNCTIONS.register(
                "jade_harvest_tool_display", () -> ModJadeDisplayGameTests::jadeHarvestToolDisplay);
    }

    private ModJadeDisplayGameTests() {}

    public static void register(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("jade"), new TestEnvironmentDefinition.AllOf());
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

    private static void jadeHarvestToolDisplay(GameTestHelper helper) {
        // Stone needs the tier-two pickaxe family; vanilla Jade would show a wooden pickaxe.
        expect(helper, Blocks.STONE, "infx:copper_pickaxe");
        // Logs: the cheapest InfX axe (flint) suffices.
        expect(helper, Blocks.OAK_LOG, "infx:flint_axe");
        // Mithril ore needs level three: iron pickaxe.
        expect(helper, InfXBlocks.MITHRIL_ORE.get(), "infx:iron_pickaxe");
        // Diamond ore needs level four: mithril pickaxe.
        expect(helper, Blocks.DIAMOND_ORE, "infx:mithril_pickaxe");
        // Wool is cut by both the sword and the shears families.
        expect(helper, Blocks.WHITE_WOOL, "infx:copper_sword", "infx:copper_shears");
        // Cake: shovel (wood), hoe (copper), cudgel (wood) and the war hammer.
        expect(helper, Blocks.CAKE, "infx:copper_war_hammer", "infx:wood_shovel", "infx:copper_hoe", "infx:wood_cudgel");
        // Glass: pickaxe and axe families plus the metal-shovel special case.
        expect(helper, Blocks.GLASS, "infx:copper_pickaxe", "infx:copper_shovel", "infx:flint_axe");
        // Root crops: scythe is excluded; shovel, hoe, sword and shears apply.
        expect(
                helper,
                Blocks.CARROTS,
                "infx:wood_shovel",
                "infx:copper_hoe",
                "infx:copper_sword",
                "infx:copper_shears");
        // no_effective_tool blocks never report a tool.
        expect(helper, Blocks.PISTON);
        // Level six exceeds every InfX material tier: no tool can harvest it.
        expect(helper, InfXBlocks.ADAMANTIUM_BLOCK.get());
        helper.succeed();
    }

    private static void expect(GameTestHelper helper, Block block, String... expectedIds) {
        List<String> actual = InfxHarvestToolDisplay.toolsFor(block.defaultBlockState()).stream()
                .map(ItemStack::getItem)
                .map(item -> item.builtInRegistryHolder().getRegisteredName())
                .collect(Collectors.toList());
        helper.assertTrue(
                actual.equals(List.of(expectedIds)),
                "harvest tools of " + block + ": expected " + List.of(expectedIds) + " but got " + actual);
    }
}
