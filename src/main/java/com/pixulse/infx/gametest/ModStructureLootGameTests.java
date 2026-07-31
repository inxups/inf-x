package com.pixulse.infx.gametest;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.loot.ModernProgressionLootFilter;
import com.pixulse.infx.registry.InfXItems;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runtime checks that the MITE supplements are reachable through real vanilla chest tables. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ModStructureLootGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> STRUCTURE_LOOT =
            ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id("structure_loot"));
    private static final List<StructureCase> CASES = List.of(
            new StructureCase(
                    "simple_dungeon",
                    stack -> stack.is(InfXItems.ONION.get()),
                    "dungeon onion"),
            new StructureCase(
                    "bastion_treasure",
                    stack -> stack.is(InfXItems.catalog().raw("diamond_shard").holder()),
                    "bastion diamond shard"),
            new StructureCase(
                    "village/village_toolsmith",
                    stack -> stack.is(InfXItems.catalog()
                            .equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.CHESTPLATE)
                            .holder()),
                    "village ancient-metal chestplate"),
            new StructureCase(
                    "end_city_treasure",
                    stack -> stack.is(InfXItems.catalog()
                            .equipment(InfxMaterial.ANCIENT_METAL, EquipmentType.SWORD)
                            .holder()),
                    "end-city ancient-metal sword"),
            new StructureCase(
                    "trial_chambers/reward",
                    stack -> stack.is(InfXItems.catalog().raw("diamond_shard").holder()),
                    "trial-chamber diamond shard"));

    static {
        TEST_FUNCTIONS.register("structure_loot", () -> ModStructureLootGameTests::structureLoot);
    }

    private ModStructureLootGameTests() {}

    public static void register(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(
                InfiniteX.id("structure_loot"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(
                STRUCTURE_LOOT.identifier(),
                new FunctionGameTestInstance(
                        STRUCTURE_LOOT,
                        new TestData<>(
                                environment,
                                Identifier.withDefaultNamespace("empty"),
                                80,
                                0,
                                true,
                                Rotation.NONE)));
    }

    private static void structureLoot(GameTestHelper helper) {
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(new Vec3(0.5D, 64.0D, 0.5D)))
                .create(LootContextParamSets.CHEST);
        for (StructureCase structure : CASES) {
            assertStructureReward(helper, params, structure);
        }
        helper.succeed();
    }

    private static void assertStructureReward(
            GameTestHelper helper, LootParams params, StructureCase structure) {
        ResourceKey<LootTable> key = ResourceKey.create(
                Registries.LOOT_TABLE,
                Identifier.withDefaultNamespace("chests/" + structure.path()));
        LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        Set<Identifier> observed = new HashSet<>();
        boolean foundReward = false;
        boolean foundForbidden = false;
        for (long seed = 0; seed < 1024; seed++) {
            for (ItemStack stack : table.getRandomItems(params, seed)) {
                Identifier item = BuiltInRegistries.ITEM.getKey(stack.getItem());
                observed.add(item);
                foundReward |= structure.reward().test(stack);
                foundForbidden |= ModernProgressionLootFilter.isForbidden(stack);
            }
        }
        helper.assertTrue(
                !foundForbidden,
                structure.path() + " must not expose filtered vanilla progression items: " + observed);
        helper.assertTrue(
                foundReward,
                structure.description() + " must be reachable from " + structure.path()
                        + "; observed " + observed);
    }

    private record StructureCase(String path, Predicate<ItemStack> reward, String description) {}
}
