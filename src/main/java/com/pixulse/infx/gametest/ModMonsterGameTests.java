package com.pixulse.infx.gametest;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.curse.R196CurseData;
import com.pixulse.infx.entity.R196EarthElemental;
import com.pixulse.infx.entity.R196Enderman;
import com.pixulse.infx.entity.R196Mob;
import com.pixulse.infx.entity.R196MonsterEvents;
import com.pixulse.infx.entity.R196MonsterTactics;
import com.pixulse.infx.entity.R196Slime;
import com.pixulse.infx.entity.R196Witch;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.ModEntityTypes;
import com.pixulse.infx.world.R196RiverBiomes;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runtime coverage for roster construction and vanilla natural-spawn replacement. */
public final class ModMonsterGameTests {
    private static final float DAMAGE_EPSILON = 0.001F;
    private static final String ROSTER = "r196_monster_roster";
    private static final String ATTRIBUTES = "r196_monster_attributes";
    private static final String REPLACEMENT = "r196_monster_replacement";
    private static final String BEHAVIORS = "r196_monster_behaviors";
    private static final String NETHERSPAWN = "r196_netherspawn_mechanics";
    private static final String ENDERMAN = "r196_enderman";
    private static final String WITCH_CURSE = "r196_witch_curse";
    private static final String TACTICS = "r196_monster_tactics";
    private static final String SPAWNS = "r196_spawn_tables";
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, InfiniteX.MOD_ID);

    static {
        FUNCTIONS.register(ROSTER, () -> ModMonsterGameTests::roster);
        FUNCTIONS.register(ATTRIBUTES, () -> ModMonsterGameTests::attributes);
        FUNCTIONS.register(REPLACEMENT, () -> ModMonsterGameTests::replacement);
        FUNCTIONS.register(BEHAVIORS, () -> ModMonsterGameTests::behaviors);
        FUNCTIONS.register(NETHERSPAWN, () -> ModMonsterGameTests::netherspawnMechanics);
        FUNCTIONS.register(ENDERMAN, () -> ModMonsterGameTests::enderman);
        FUNCTIONS.register(WITCH_CURSE, () -> ModMonsterGameTests::witchCurse);
        FUNCTIONS.register(TACTICS, () -> ModMonsterGameTests::tactics);
        FUNCTIONS.register(SPAWNS, () -> ModMonsterGameTests::spawnTables);
    }

    private ModMonsterGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
        modBus.addListener(ModMonsterGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                InfiniteX.id("r196_monsters"), new TestEnvironmentDefinition.AllOf());
        for (String name : List.of(
                ROSTER, ATTRIBUTES, REPLACEMENT, BEHAVIORS, NETHERSPAWN, ENDERMAN, WITCH_CURSE, TACTICS, SPAWNS)) {
            ResourceKey<Consumer<GameTestHelper>> function =
                    ResourceKey.create(Registries.TEST_FUNCTION, InfiniteX.id(name));
            event.registerTest(
                    function.identifier(),
                    new FunctionGameTestInstance(
                            function,
                            new TestData<>(
                                    environment,
                                    Identifier.withDefaultNamespace("empty"),
                                    name.equals(WITCH_CURSE) ? 400 : 200,
                                    0,
                                    true,
                                    Rotation.NONE)));
        }
    }

    private static void roster(GameTestHelper helper) {
        var passiveReplacements = Set.of(
                ModEntityTypes.R196_COW,
                ModEntityTypes.R196_CHICKEN,
                ModEntityTypes.R196_SHEEP,
                ModEntityTypes.R196_PIG,
                ModEntityTypes.R196_HORSE,
                ModEntityTypes.R196_OCELOT,
                ModEntityTypes.R196_WOLF);
        for (var holder : ModEntityTypes.ALL) {
            var entity = holder.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            if (!passiveReplacements.contains(holder)) {
                helper.assertTrue(entity instanceof R196Mob, holder.getId() + " must implement R196Mob");
            }
            helper.assertTrue(
                    entity instanceof LivingEntity living && living.getMaxHealth() > 0.0F,
                    holder.getId() + " must have a registered positive max-health attribute");
            entity.discard();
        }
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(ModEntityTypes.R196_ZOMBIE.get())
                        .is(EntityTypeTags.UNDEAD),
                "replacement zombies must retain vanilla undead semantics");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(ModEntityTypes.R196_SKELETON.get())
                        .is(EntityTypeTags.SKELETONS),
                "replacement skeletons must retain the vanilla skeleton family tag");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(ModEntityTypes.PHASE_SPIDER.get())
                        .is(EntityTypeTags.ARTHROPOD),
                "R196 spiders must retain arthropod semantics");
        helper.assertTrue(
                BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(ModEntityTypes.R196_SQUID.get())
                        .is(EntityTypeTags.AQUATIC),
                "replacement squid must retain aquatic semantics");
        helper.assertTrue(
                ModEntityTypes.R196_SQUID.get().getCategory() == MobCategory.WATER_CREATURE
                        && ModEntityTypes.R196_SQUID.get().isAllowedInPeaceful(),
                "replacement squid must use the peaceful water-creature cap");
        for (var type : List.of(
                ModEntityTypes.R196_COD,
                ModEntityTypes.R196_SALMON,
                ModEntityTypes.R196_PUFFERFISH,
                ModEntityTypes.R196_TROPICAL_FISH)) {
            helper.assertTrue(
                    BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type.get()).is(EntityTypeTags.AQUATIC)
                            && BuiltInRegistries.ENTITY_TYPE
                                    .wrapAsHolder(type.get())
                                    .is(EntityTypeTags.AXOLOTL_HUNT_TARGETS)
                            && BuiltInRegistries.ENTITY_TYPE
                                    .wrapAsHolder(type.get())
                                    .is(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH)
                            && type.get().getCategory() == MobCategory.WATER_AMBIENT
                            && type.get().isAllowedInPeaceful(),
                    type.getId() + " must retain vanilla aquatic interaction semantics");
        }
        helper.assertTrue(
                ModEntityTypes.VAMPIRE_BAT.get().getCategory() == MobCategory.AMBIENT
                        && ModEntityTypes.NIGHTWING.get().getCategory() == MobCategory.AMBIENT,
                "hostile cave bats must retain the R196 ambient spawn pool");
        helper.assertTrue(
                ModEntityTypes.DIRE_WOLF.get().getCategory() == MobCategory.CREATURE
                        && ModEntityTypes.DIRE_WOLF.get().isAllowedInPeaceful(),
                "dire wolves must use animal spawning rather than the hostile cap");
        helper.succeed();
    }

    private static void attributes(GameTestHelper helper) {
        int zombieX = 1;
        for (var type : List.of(
                ModEntityTypes.R196_ZOMBIE,
                ModEntityTypes.INVISIBLE_STALKER,
                ModEntityTypes.GHOUL,
                ModEntityTypes.SHADOW,
                ModEntityTypes.WIGHT,
                ModEntityTypes.REVENANT)) {
            var zombie = helper.spawnWithNoFreeWill(type.get(), new BlockPos(zombieX++, 2, 1));
            helper.assertTrue(
                    zombie.getAttributeBaseValue(Attributes.ARMOR) == 0.0D,
                    type.getId() + " must not inherit modern zombie armor");
        }
        var stalker = helper.spawnWithNoFreeWill(ModEntityTypes.INVISIBLE_STALKER.get(), new BlockPos(10, 2, 1));
        helper.assertTrue(
                stalker.canBreakDoors() && !stalker.isInvisible() && !stalker.canPickUpLoot(),
                "invisible stalkers must break doors without inheriting vanilla invisibility or zombie loot pickup");
        var piglin = helper.spawn(ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), new BlockPos(8, 2, 1));
        helper.assertTrue(
                piglin.getAttributeBaseValue(Attributes.ARMOR) == 0.0D,
                "R196 zombified piglins must not inherit modern zombie armor");
        helper.assertTrue(
                piglin.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.23D,
                "R196 zombified piglins must use the normalized modern base movement speed");
        helper.assertTrue(
                !piglin.canBreakDoors() && !piglin.canPickUpLoot(),
                "R196 zombified piglins must not inherit modern zombie door breaking or item pickup");
        var piglinTarget = ModR196CompletionGameTests.createPlayer(helper);
        piglin.setTarget(piglinTarget);
        // The replacement adjusts its modifier from customServerAiStep, which is
        // invoked by the real entity tick rather than by the client-side aiStep hook.
        piglin.tick();
        helper.assertTrue(
                Math.abs(piglin.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.28D) < DAMAGE_EPSILON,
                "R196 zombified piglins must use the normalized +0.05 chase-speed bonus");
        var enderman = helper.spawnWithNoFreeWill(ModEntityTypes.R196_ENDERMAN.get(), new BlockPos(7, 2, 1));
        helper.assertTrue(
                enderman.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.30D,
                "R196 endermen must retain their 0.30 standing movement speed");
        enderman.setTarget(piglinTarget);
        helper.assertTrue(
                Math.abs(enderman.getAttributeValue(Attributes.MOVEMENT_SPEED) - 6.50D) < DAMAGE_EPSILON,
                "R196 endermen must retain MITE's 6.50 chase-speed total");
        enderman.setTarget(null);
        helper.assertTrue(
                Math.abs(enderman.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.30D) < DAMAGE_EPSILON,
                "R196 endermen must remove the chase-speed modifier without a target");
        piglin.setTarget(null);
        ModR196CompletionGameTests.removePlayer(piglinTarget);
        piglin.tick();
        helper.assertTrue(
                Math.abs(piglin.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.23D) < DAMAGE_EPSILON,
                "R196 zombified piglins must remove the chase-speed modifier without a target");
        var blaze = helper.spawnWithNoFreeWill(ModEntityTypes.R196_BLAZE.get(), new BlockPos(9, 2, 1));
        helper.assertTrue(
                Math.abs(blaze.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - 0.23D) < DAMAGE_EPSILON,
                "R196 blazes must retain the modern baseline movement speed");

        int spiderX = 1;
        for (var type : List.of(
                ModEntityTypes.R196_SPIDER,
                ModEntityTypes.R196_CAVE_SPIDER,
                ModEntityTypes.BLACK_WIDOW_SPIDER,
                ModEntityTypes.DEMON_SPIDER,
                ModEntityTypes.WOOD_SPIDER,
                ModEntityTypes.PHASE_SPIDER)) {
            var spider = helper.spawnWithNoFreeWill(type.get(), new BlockPos(spiderX++, 2, 2));
            double expectedSpeed = type == ModEntityTypes.R196_SPIDER
                            || type == ModEntityTypes.R196_CAVE_SPIDER
                            || type == ModEntityTypes.DEMON_SPIDER
                    ? 0.375D
                    : 0.30D;
            helper.assertTrue(
                    spider.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == expectedSpeed,
                    type.getId() + " must retain its normalized arachnid movement speed");
        }

        int silverfishX = 1;
        for (var type : List.of(
                ModEntityTypes.NETHERSPAWN,
                ModEntityTypes.COPPERSPINE,
                ModEntityTypes.HOARY_SILVERFISH)) {
            var silverfish = helper.spawnWithNoFreeWill(type.get(), new BlockPos(silverfishX++, 2, 3));
            helper.assertTrue(
                    silverfish.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.25D,
                    type.getId() + " must retain the modern silverfish movement baseline");
        }

        int slimeX = 1;
        for (var type : List.of(
                ModEntityTypes.R196_SLIME,
                ModEntityTypes.JELLY,
                ModEntityTypes.BLOB,
                ModEntityTypes.OOZE,
                ModEntityTypes.PUDDING)) {
            var slime = helper.spawnWithNoFreeWill(type.get(), new BlockPos(slimeX++, 2, 4));
            for (int size : List.of(1, 2, 4)) {
                slime.setSize(size, true);
                double expectedSpeed = slime.variant() == R196Slime.Variant.OOZE
                        ? 0.05D
                        : 0.20D + 0.10D * slime.getSize();
                helper.assertTrue(
                        slime.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == expectedSpeed,
                        type.getId() + " must retain its expected movement speed after size " + slime.getSize());
            }
        }
        var magmaCube = helper.spawnWithNoFreeWill(ModEntityTypes.MAGMA_CUBE.get(), new BlockPos(7, 2, 4));
        for (int size : List.of(1, 4)) {
            magmaCube.setSize(size, true);
            helper.assertTrue(
                    magmaCube.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.20D,
                    "R196 magma cubes must retain 0.20 movement speed after size " + size);
            helper.assertTrue(
                    magmaCube.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == size * 2.0D,
                    "R196 magma cubes must retain two damage per size");
            helper.assertTrue(
                    magmaCube.getAttributeBaseValue(Attributes.ARMOR) == size * 2.0D,
                    "R196 magma cubes must retain two defense per size");
        }

        var level = helper.getLevel();
        ItemEntity magicOwner = new ItemEntity(level, 0.0, 0.0, 0.0, Items.SNOWBALL.getDefaultInstance());
        Snowball magicProjectile = new Snowball(level, 0.0, 0.0, 0.0, Items.SNOWBALL.getDefaultInstance());
        var directMagic = helper.spawnWithNoFreeWill(ModEntityTypes.R196_WITCH.get(), new BlockPos(1, 2, 8));
        float before = directMagic.getHealth();
        helper.assertTrue(
                directMagic.hurtServer(level, level.damageSources().magic(), 20.0F),
                "direct magic must damage the R196 witch");
        helper.assertTrue(
                Math.abs(directMagic.getHealth() - (before - 20.0F)) < DAMAGE_EPSILON,
                "direct magic must not receive the R196 witch's indirect-magic defense");

        var indirectNonMagic = helper.spawnWithNoFreeWill(ModEntityTypes.R196_WITCH.get(), new BlockPos(4, 2, 8));
        before = indirectNonMagic.getHealth();
        helper.assertTrue(
                indirectNonMagic.hurtServer(level, level.damageSources().thrown(magicProjectile, magicOwner), 20.0F),
                "indirect non-magic damage must damage the R196 witch");
        helper.assertTrue(
                Math.abs(indirectNonMagic.getHealth() - (before - 20.0F)) < DAMAGE_EPSILON,
                "indirect non-magic damage must not receive the R196 witch's defense");

        var indirectMagic = helper.spawnWithNoFreeWill(ModEntityTypes.R196_WITCH.get(), new BlockPos(7, 2, 8));
        before = indirectMagic.getHealth();
        helper.assertTrue(
                indirectMagic.hurtServer(level, level.damageSources().indirectMagic(magicProjectile, magicOwner), 20.0F),
                "indirect magic must damage the R196 witch");
        helper.assertTrue(
                Math.abs(indirectMagic.getHealth() - (before - 10.0F)) < DAMAGE_EPSILON,
                "indirect magic must receive exactly ten points of R196 witch defense");
        helper.succeed();
    }

    private static void replacement(GameTestHelper helper) {
        BlockPos naturalPos = new BlockPos(2, 2, 2);
        BlockPos explicitPos = new BlockPos(5, 2, 2);
        BlockPos explicitWitchPos = new BlockPos(5, 2, 5);
        helper.spawn(EntityTypes.ZOMBIE, naturalPos, EntitySpawnReason.NATURAL);
        BlockPos triggeredPos = new BlockPos(8, 2, 2);
        helper.setBlock(triggeredPos.east(), Blocks.COPPER_ORE);
        helper.spawn(EntityTypes.SILVERFISH, triggeredPos, EntitySpawnReason.TRIGGERED);
        var explicit = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        Vec3 explicitLocation = helper.absoluteVec(Vec3.atBottomCenterOf(explicitPos));
        explicit.snapTo(explicitLocation.x, explicitLocation.y, explicitLocation.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(explicit);
        var explicitWitch = EntityTypes.WITCH.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        Vec3 explicitWitchLocation = helper.absoluteVec(Vec3.atBottomCenterOf(explicitWitchPos));
        explicitWitch.snapTo(
                explicitWitchLocation.x, explicitWitchLocation.y, explicitWitchLocation.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(explicitWitch);
        helper.startSequence()
                // Replacement insertion is deliberately scheduled after the
                // vanilla join event, so the test must not inspect tick-zero's
                // pre-transaction entity index.
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(ModEntityTypes.R196_ZOMBIE.get(), naturalPos, 2.0D);
                    helper.assertEntityPresent(EntityTypes.ZOMBIE, explicitPos);
                    helper.assertEntityPresent(ModEntityTypes.COPPERSPINE.get(), triggeredPos, 2.0D);
                    helper.assertEntityPresent(ModEntityTypes.R196_WITCH.get(), explicitWitchPos, 2.0D);
                })
                .thenExecute(() -> {
                    helper.assertEntityNotPresent(EntityTypes.ZOMBIE, naturalPos);
                    helper.assertEntityNotPresent(EntityTypes.SILVERFISH, triggeredPos);
                    helper.assertEntityNotPresent(EntityTypes.WITCH, explicitWitchPos);
                    Vec3 replacementPosition = helper.absoluteVec(Vec3.atBottomCenterOf(naturalPos));
                    var replacement = helper.getLevel()
                            .getEntitiesOfClass(
                                    com.pixulse.infx.entity.R196Zombie.class,
                                    new AABB(replacementPosition, replacementPosition).inflate(2.0D),
                                    entity -> entity.getType() == ModEntityTypes.R196_ZOMBIE.get())
                            .getFirst();
                    helper.assertTrue(
                            replacement.getAttributeBaseValue(Attributes.FOLLOW_RANGE) == 40.0D,
                            "replacement initialization must retain the R196 follow range");
                    helper.assertTrue(
                            replacement.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 5.0D,
                            "replacement initialization must retain the R196 attack damage; actual="
                                    + replacement.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
                })
                .thenSucceed();
    }

    private static void spawnTables(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        MobSpawnSettings plains = biomes.getOrThrow(Biomes.PLAINS).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(plains, MobCategory.CREATURE)).equals(Set.of(
                        ModEntityTypes.R196_SHEEP.get(),
                        ModEntityTypes.R196_PIG.get(),
                        ModEntityTypes.R196_CHICKEN.get(),
                        ModEntityTypes.R196_COW.get(),
                        ModEntityTypes.R196_HORSE.get())),
                "plains must use only the R196 livestock and horse table");
        helper.assertTrue(
                !spawnTypes(plains, MobCategory.MONSTER).contains(EntityTypes.DROWNED)
                        && spawnTypes(plains, MobCategory.MONSTER).contains(ModEntityTypes.GHOUL.get()),
                "Overworld monster tables must replace modern biome additions");
        for (var type : List.of(
                ModEntityTypes.R196_COW,
                ModEntityTypes.R196_CHICKEN,
                ModEntityTypes.R196_SHEEP,
                ModEntityTypes.R196_PIG,
                ModEntityTypes.R196_HORSE,
                ModEntityTypes.R196_WOLF)) {
            helper.assertTrue(
                    SpawnPlacements.getPlacementType(type.get()) == SpawnPlacementTypes.ON_GROUND
                            && SpawnPlacements.getHeightmapType(type.get())
                                    == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    type.getId() + " must retain vanilla ground spawn placement restrictions");
        }
        helper.assertTrue(
                SpawnPlacements.getPlacementType(ModEntityTypes.R196_OCELOT.get())
                                == SpawnPlacementTypes.ON_GROUND
                        && SpawnPlacements.getHeightmapType(ModEntityTypes.R196_OCELOT.get())
                                == Heightmap.Types.MOTION_BLOCKING,
                "R196 ocelots must retain vanilla ocelot spawn placement restrictions");
        for (var type : List.of(
                ModEntityTypes.R196_COD,
                ModEntityTypes.R196_SALMON,
                ModEntityTypes.R196_PUFFERFISH,
                ModEntityTypes.R196_TROPICAL_FISH)) {
            helper.assertTrue(
                    SpawnPlacements.getPlacementType(type.get()) == SpawnPlacementTypes.IN_WATER
                            && SpawnPlacements.getHeightmapType(type.get())
                                    == Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    type.getId() + " must retain vanilla water spawn placement restrictions");
        }

        MobSpawnSettings ocean = biomes.getOrThrow(Biomes.OCEAN).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(ocean, MobCategory.WATER_AMBIENT).equals(List.of(ModEntityTypes.R196_COD.get())),
                "normal oceans must spawn only InfiniteX cod");
        MobSpawnSettings coldOcean = biomes.getOrThrow(Biomes.COLD_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(coldOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        ModEntityTypes.R196_COD.get(), ModEntityTypes.R196_SALMON.get())),
                "cold oceans must replace cod and salmon with InfiniteX fish");
        MobSpawnSettings lukewarmOcean = biomes.getOrThrow(Biomes.LUKEWARM_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(lukewarmOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        ModEntityTypes.R196_COD.get(),
                        ModEntityTypes.R196_PUFFERFISH.get(),
                        ModEntityTypes.R196_TROPICAL_FISH.get())),
                "lukewarm oceans must use InfiniteX cod, pufferfish and tropical fish");
        MobSpawnSettings warmOcean = biomes.getOrThrow(Biomes.WARM_OCEAN).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(warmOcean, MobCategory.WATER_AMBIENT)).equals(Set.of(
                        ModEntityTypes.R196_PUFFERFISH.get(), ModEntityTypes.R196_TROPICAL_FISH.get())),
                "warm oceans must use InfiniteX pufferfish and tropical fish");
        MobSpawnSettings river = biomes.getOrThrow(Biomes.RIVER).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(river, MobCategory.WATER_AMBIENT).equals(List.of(ModEntityTypes.R196_SALMON.get())),
                "rivers must use InfiniteX salmon");
        MobSpawnSettings lushCaves = biomes.getOrThrow(Biomes.LUSH_CAVES).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(lushCaves, MobCategory.WATER_AMBIENT)
                        .equals(List.of(ModEntityTypes.R196_TROPICAL_FISH.get())),
                "lush caves must use InfiniteX tropical fish");

        MobSpawnSettings mushroom = biomes.getOrThrow(Biomes.MUSHROOM_FIELDS).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(mushroom, MobCategory.MONSTER).isEmpty()
                        && spawnTypes(mushroom, MobCategory.CREATURE).isEmpty()
                        && spawnTypes(mushroom, MobCategory.WATER_CREATURE).isEmpty(),
                "mushroom fields must remain free of monsters, animals and squid");
        helper.assertTrue(
                Set.copyOf(spawnTypes(mushroom, MobCategory.AMBIENT)).equals(Set.of(
                        EntityTypes.BAT,
                        ModEntityTypes.VAMPIRE_BAT.get(),
                        ModEntityTypes.NIGHTWING.get())),
                "mushroom fields retain the inherited R196 cave-bat pool");

        MobSpawnSettings jungle = biomes.getOrThrow(Biomes.JUNGLE).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(jungle, MobCategory.MONSTER).contains(ModEntityTypes.BLACK_WIDOW_SPIDER.get()),
                "jungle biomes must include black widow spiders");
        helper.assertTrue(
                spawnTypes(jungle, MobCategory.CREATURE).stream()
                                .filter(type -> type == ModEntityTypes.R196_CHICKEN.get())
                                .count()
                        == 2,
                "jungle biomes must retain the additional R196 chicken entry");

        MobSpawnSettings jungleRiver = biomes.getOrThrow(R196RiverBiomes.JUNGLE_RIVER).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(jungleRiver, MobCategory.CREATURE).isEmpty()
                        && !spawnTypes(jungleRiver, MobCategory.MONSTER)
                                .contains(ModEntityTypes.BLACK_WIDOW_SPIDER.get()),
                "jungle rivers must use river rather than jungle animal overrides");

        MobSpawnSettings nether = biomes.getOrThrow(Biomes.NETHER_WASTES).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(nether, MobCategory.MONSTER)).equals(Set.of(
                        EntityTypes.GHAST,
                        EntityTypes.ZOMBIFIED_PIGLIN,
                        EntityTypes.MAGMA_CUBE,
                        ModEntityTypes.EARTH_ELEMENTAL.get()))
                        && spawnTypes(nether, MobCategory.CREATURE).isEmpty(),
                "Nether biomes must use the exact four-entry R196 pool");

        MobSpawnSettings end = biomes.getOrThrow(Biomes.END_HIGHLANDS).value().getMobSettings();
        helper.assertTrue(
                Set.copyOf(spawnTypes(end, MobCategory.MONSTER)).equals(Set.of(
                        EntityTypes.ENDERMAN, ModEntityTypes.EARTH_ELEMENTAL.get())),
                "End biomes must use only endermen and earth elementals");

        MobSpawnSettings underworld = biomes.getOrThrow(Underworld.BIOME).value().getMobSettings();
        helper.assertTrue(
                spawnTypes(underworld, MobCategory.WATER_CREATURE).equals(List.of(EntityTypes.SQUID))
                        && spawnTypes(underworld, MobCategory.CREATURE).isEmpty(),
                "Underworld must retain aquatic spawning without blue-moon livestock");
        helper.assertTrue(
                spawnTypes(underworld, MobCategory.MONSTER).contains(ModEntityTypes.CLAY_GOLEM.get()),
                "Underworld must retain MITE's clay-golem spawn entry");
        helper.succeed();
    }

    private static List<net.minecraft.world.entity.EntityType<?>> spawnTypes(
            MobSpawnSettings settings, MobCategory category) {
        return settings.getMobs(category).unwrap().stream()
                .<net.minecraft.world.entity.EntityType<?>>map(entry -> entry.value().type())
                .toList();
    }

    private static void assertEarthForm(
            GameTestHelper helper,
            R196EarthElemental elemental,
            net.minecraft.world.level.block.state.BlockState ground,
            boolean heated,
            R196EarthElemental.Form expected) {
        elemental.initializeMiteForm(ground, heated);
        helper.assertTrue(
                elemental.form() == expected && elemental.isMagma() == expected.isMagmaForm(),
                "earth elemental form must match " + expected);
    }

    private static void behaviors(GameTestHelper helper) {
        var level = helper.getLevel();
        var skeleton = helper.spawnWithNoFreeWill(ModEntityTypes.R196_SKELETON.get(), new BlockPos(1, 2, 1));
        var vanillaSkeleton = helper.spawnWithNoFreeWill(EntityTypes.SKELETON, new BlockPos(2, 2, 1));
        Arrow arrow = EntityTypes.ARROW.create(level, EntitySpawnReason.COMMAND);
        float before = skeleton.getHealth();
        helper.assertTrue(
                !skeleton.hurtServer(level, level.damageSources().arrow(arrow, vanillaSkeleton), 4.0F),
                "R196 skeletons must reject arrows fired by another skeleton");
        helper.assertTrue(skeleton.getHealth() == before, "skeleton arrows must not reduce R196 skeleton health");

        var blaze = helper.spawnWithNoFreeWill(ModEntityTypes.R196_BLAZE.get(), new BlockPos(3, 2, 1));
        before = blaze.getHealth();
        helper.assertTrue(
                !blaze.hurtServer(level, level.damageSources().mobAttack(vanillaSkeleton), 4.0F),
                "mundane unenchanted attacks must not hurt the R196 blaze");
        helper.assertTrue(blaze.getHealth() == before, "rejected blaze damage must not change health");
        Snowball snowball = new Snowball(level, vanillaSkeleton, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                blaze.hurtServer(level, level.damageSources().thrown(snowball, vanillaSkeleton), 3.0F),
                "snowballs must hurt the R196 blaze");
        helper.assertTrue(blaze.getHealth() == before - 3.0F, "snowballs must deal three damage to the R196 blaze");

        var player = ModR196CompletionGameTests.createPlayer(helper);
        before = blaze.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
        player.igniteForSeconds(8.0F);
        helper.assertTrue(
                !blaze.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "burning players still cannot hurt blazes with unenchanted weapons");
        helper.assertTrue(blaze.getHealth() == before, "burning unenchanted hits must not change blaze health");

        blaze.invulnerableTime = 0;
        var enchantedSword = Items.IRON_SWORD.getDefaultInstance();
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        enchantedSword.enchant(
                registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING), 1);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, enchantedSword);
        player.igniteForSeconds(8.0F);
        helper.assertTrue(
                blaze.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "burning players must still hurt blazes with non-fire enchanted weapons");
        helper.assertTrue(
                blaze.getHealth() < before,
                "non-fire enchanted hits must deal damage while the attacker is burning");

        var magma = helper.spawnWithNoFreeWill(ModEntityTypes.MAGMA_CUBE.get(), new BlockPos(4, 2, 1));
        before = magma.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
        helper.assertTrue(
                !magma.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "swords must not hurt the R196 magma cube");
        helper.assertTrue(magma.getHealth() == before, "sword hits must not change magma cube health");
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, ModItems.IRON_PICKAXE.toStack());
        helper.assertTrue(
                magma.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "pickaxes must hurt the R196 magma cube");
        helper.assertTrue(magma.getHealth() == before - 4.0F, "pickaxe hits must deal magma cube damage");

        magma.invulnerableTime = 0;
        before = magma.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND,
                ModItems.catalog()
                        .equipment(R196Material.IRON, R196EquipmentType.WAR_HAMMER)
                        .holder()
                        .toStack());
        helper.assertTrue(
                magma.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "war hammers must hurt the R196 magma cube");
        helper.assertTrue(magma.getHealth() == before - 4.0F, "war hammer hits must deal magma cube damage");

        var earth = helper.spawnWithNoFreeWill(ModEntityTypes.EARTH_ELEMENTAL.get(), new BlockPos(5, 2, 1));
        helper.assertTrue(
                earth.fireImmune()
                        && earth.getMaxSpawnClusterSize() == 1
                        && earth.getNavigation().getNodeEvaluator().canOpenDoors(),
                "earth elementals must be fire/lava immune, spawn singly and path to break doors");
        before = earth.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
        helper.assertTrue(
                !earth.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "swords must not hurt the earth elemental");
        helper.assertTrue(earth.getHealth() == before, "sword hits must not change earth elemental health");
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, ModItems.IRON_PICKAXE.toStack());
        helper.assertTrue(
                earth.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "pickaxes must hurt the earth elemental");
        helper.assertTrue(earth.getHealth() < before, "pickaxe hits must deal earth elemental damage");

        assertEarthForm(helper, earth, Blocks.STONE.defaultBlockState(), false, R196EarthElemental.Form.STONE_NORMAL);
        assertEarthForm(helper, earth, Blocks.STONE.defaultBlockState(), true, R196EarthElemental.Form.STONE_MAGMA);
        assertEarthForm(helper, earth, Blocks.OBSIDIAN.defaultBlockState(), false, R196EarthElemental.Form.OBSIDIAN_NORMAL);
        assertEarthForm(helper, earth, Blocks.OBSIDIAN.defaultBlockState(), true, R196EarthElemental.Form.OBSIDIAN_MAGMA);
        assertEarthForm(helper, earth, Blocks.NETHERRACK.defaultBlockState(), false, R196EarthElemental.Form.NETHERRACK_NORMAL);
        assertEarthForm(helper, earth, Blocks.NETHERRACK.defaultBlockState(), true, R196EarthElemental.Form.NETHERRACK_MAGMA);
        assertEarthForm(helper, earth, Blocks.END_STONE.defaultBlockState(), false, R196EarthElemental.Form.END_STONE_NORMAL);
        assertEarthForm(helper, earth, Blocks.END_STONE.defaultBlockState(), true, R196EarthElemental.Form.END_STONE_MAGMA);
        helper.assertTrue(earth.quench(level), "water-bucket quenching must cool molten mineral bodies");
        helper.assertTrue(
                earth.form() == R196EarthElemental.Form.END_STONE_NORMAL && earth.heat() == 0,
                "quenching must restore the matching normal mineral form");

        earth.initializeMiteForm(Blocks.NETHERRACK.defaultBlockState(), true);
        float earthHealth = earth.getHealth();
        Snowball earthSnowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                !earth.hurtServer(level, level.damageSources().thrown(earthSnowball, player), 1.0F)
                        && earth.form() == R196EarthElemental.Form.NETHERRACK_NORMAL
                        && earth.getHealth() == earthHealth,
                "snowballs must quench mineral bodies without dealing damage");

        var clay = helper.spawnWithNoFreeWill(ModEntityTypes.CLAY_GOLEM.get(), new BlockPos(8, 2, 1));
        clay.initializeMiteForm(Blocks.CLAY.defaultBlockState(), false);
        helper.assertTrue(
                clay.form() == R196EarthElemental.Form.CLAY_NORMAL && !clay.isMagma()
                        && clay.doorBreakTicks(true) == 480 && clay.fireImmune()
                        && clay.getMaxSpawnClusterSize() == 1,
                "normal clay golems must retain their non-magma body and fourfold door-break speed");
        float clayHealth = clay.getHealth();
        Snowball claySnowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                clay.hurtServer(level, level.damageSources().thrown(claySnowball, player), 1.0F)
                        && clay.form() == R196EarthElemental.Form.CLAY_NORMAL
                        && clay.getHealth() < clayHealth,
                "normal clay must take ordinary snowball damage instead of quenching");
        clay.invulnerableTime = 0;
        clay.convertToMagma();
        helper.assertTrue(
                clay.form() == R196EarthElemental.Form.CLAY_HARDENED && !clay.isMagma()
                        && clay.doorBreakTicks(true) == 320 && !clay.quench(level),
                "heated clay must harden permanently without entering a magma state");

        var fire = helper.spawnWithNoFreeWill(ModEntityTypes.FIRE_ELEMENTAL.get(), new BlockPos(6, 2, 1));
        before = fire.getHealth();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
        helper.assertTrue(
                !fire.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "unenchanted swords must not hurt the fire elemental");
        helper.assertTrue(fire.getHealth() == before, "rejected fire elemental damage must not change health");
        Snowball fireSnowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                fire.hurtServer(level, level.damageSources().thrown(fireSnowball, player), 3.0F),
                "snowballs must hurt the fire elemental");
        helper.assertTrue(
                fire.getHealth() == before - 3.0F, "snowballs must deal three damage to the fire elemental");
        fire.invulnerableTime = 0;
        before = fire.getHealth();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, enchantedSword);
        helper.assertTrue(
                fire.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
                "non-fire enchanted weapons must hurt the fire elemental");
        helper.assertTrue(
                fire.getHealth() < before, "non-fire enchanted hits must deal fire elemental damage");
        helper.assertTrue(fire.isOnFire(), "fire elementals must always render as burning");
        helper.assertTrue(
                !fire.isSensitiveToWater(),
                "fire elementals must not stack the modern per-tick water damage on MITE's own drain");

        var enderman = helper.spawnWithNoFreeWill(ModEntityTypes.R196_ENDERMAN.get(), new BlockPos(8, 2, 1));
        Arrow endermanArrow = EntityTypes.ARROW.create(level, EntitySpawnReason.COMMAND);
        before = enderman.getHealth();
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().arrow(endermanArrow, player), 4.0F),
                "R196 endermen must take projectile damage");
        helper.assertTrue(
                enderman.getHealth() < before,
                "R196 projectile hits must reduce enderman health");
        helper.assertTrue(
                enderman.getTarget() == player,
                "R196 projectile hits must keep the living shooter as the enderman target");
        enderman.invulnerableTime = 0;
        Arrow dispenserArrow = EntityTypes.ARROW.create(level, EntitySpawnReason.COMMAND);
        before = enderman.getHealth();
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().arrow(dispenserArrow, null), 3.0F),
                "unowned projectile damage must not fall back to vanilla enderman immunity");
        helper.assertTrue(
                enderman.getHealth() < before,
                "unowned projectiles must still damage R196 endermen");
        enderman.invulnerableTime = 0;
        enderman.setTarget(player);
        Snowball indirectMagic = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());
        helper.assertTrue(
                enderman.hurtServer(level, level.damageSources().indirectMagic(indirectMagic, player), 2.0F),
                "R196 endermen must take non-projectile indirect damage");
        helper.assertTrue(
                enderman.getTarget() == null && enderman.getLastHurtByMob() == null,
                "non-projectile indirect damage must make R196 endermen blink and drop aggression");

        var sharingSource = helper.spawnWithNoFreeWill(ModEntityTypes.R196_ZOMBIE.get(), new BlockPos(14, 2, 12));
        var neutralEnderman = helper.spawnWithNoFreeWill(ModEntityTypes.R196_ENDERMAN.get(), new BlockPos(12, 2, 12));
        R196MonsterEvents.propagateTarget(level, sharingSource, player);
        helper.assertTrue(
                neutralEnderman.getTarget() == null,
                "shared monster targets must not override R196 enderman neutrality");
        helper.assertTrue(
                R196MonsterEvents.propagateTarget(level, neutralEnderman, player) == 0,
                "R196 endermen must not propagate their own targets to nearby monsters");
        neutralEnderman.setTarget(player);
        helper.assertFalse(
                R196MonsterTactics.tryDig(level, neutralEnderman),
                "R196 endermen must never receive generic pursuit block digging");
        ModR196CompletionGameTests.removePlayer(player);

        BlockPos squidPos = new BlockPos(3, 2, 7);
        helper.setBlock(squidPos, Blocks.WATER);
        var squid = helper.spawnWithNoFreeWill(ModEntityTypes.R196_SQUID.get(), squidPos);
        var squidPrey = helper.spawnWithNoFreeWill(ModEntityTypes.R196_COW.get(), squidPos);
        squid.aiStep();
        helper.assertTrue(
                squidPrey.hasEffect(MobEffects.SLOWNESS),
                "R196 squid must hunt and slow nearby land animals in water");
        var preyBoat = helper.spawn(EntityTypes.OAK_BOAT, squidPos);
        squidPrey.startRiding(preyBoat, true, false);
        for (int hit = 0; hit < 5; hit++) {
            // A natural collision pushes the squid away from the boat. Reposition it for the
            // next independent collision while still exercising LivingEntity#doPush.
            squid.setDeltaMovement(Vec3.ZERO);
            squid.snapTo(preyBoat.getX(), preyBoat.getY(), preyBoat.getZ(), 0.0F, 0.0F);
            squid.tick();
        }
        helper.assertFalse(
                preyBoat.isRemoved(),
                "R196 squid must not destroy a pursued boat before six collisions");
        squid.setDeltaMovement(Vec3.ZERO);
        squid.snapTo(preyBoat.getX(), preyBoat.getY(), preyBoat.getZ(), 0.0F, 0.0F);
        squid.tick();
        helper.assertTrue(
                preyBoat.isRemoved(),
                "R196 squid must destroy a boat on its sixth collision while pursuing its passenger");

        BlockPos infernalStone = new BlockPos(1, 2, 3);
        helper.setBlock(infernalStone, Blocks.STONE);
        var infernal = helper.spawnWithNoFreeWill(ModEntityTypes.INFERNAL_CREEPER.get(), new BlockPos(1, 2, 4));
        var cow = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(9, 2, 4));
        var infernalTarget = ModR196CompletionGameTests.createPlayer(helper);
        var infernalSwell = helper.spawn(ModEntityTypes.INFERNAL_CREEPER.get(), new BlockPos(6, 2, 1));
        infernalSwell.setTarget(infernalTarget);
        helper.startSequence()
                // Explosions query the level's entity index. Waiting for both
                // entities prevents a tick-zero explosion from observing an
                // empty index while spawn registration is still pending.
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(ModEntityTypes.INFERNAL_CREEPER.get(), new BlockPos(1, 2, 4), 2.0D);
                    helper.assertEntityPresent(EntityTypes.COW, new BlockPos(9, 2, 4), 2.0D);
                })
                .thenWaitUntil(() -> helper.assertTrue(
                        infernalSwell.getSwellDir() > 0,
                        "infernal creepers must begin swelling from MITE's expanded no-path range"))
                .thenExecute(() -> {
                    float cowHealthBefore = cow.getHealth();
                    level.explode(
                            infernal,
                            infernal.getX(),
                            infernal.getY(),
                            infernal.getZ(),
                            3.0F,
                            Level.ExplosionInteraction.MOB);
                    helper.assertTrue(
                            cow.getHealth() < cowHealthBefore,
                            "infernal creeper explosions must use the amplified six-block radius");
                    helper.assertTrue(
                            helper.getBlockState(infernalStone).isAir(),
                            "infernal creeper explosions must break stone");
                })
                .thenExecute(() -> ModR196CompletionGameTests.removePlayer(infernalTarget))
                .thenSucceed();
    }

    private static void enderman(GameTestHelper helper) {
        var level = helper.getLevel();
        R196Enderman enderman = helper.spawn(ModEntityTypes.R196_ENDERMAN.get(), new BlockPos(3, 2, 3));
        ItemEntity pearl = new ItemEntity(
                level, enderman.getX() + 1.0, enderman.getY(), enderman.getZ(), Items.ENDER_PEARL.getDefaultInstance());
        pearl.setNoPickUpDelay();
        level.addFreshEntity(pearl);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertEntityPresent(ModEntityTypes.R196_ENDERMAN.get(), new BlockPos(3, 2, 3), 2.0D))
                .thenWaitUntil(() -> helper.assertTrue(
                        pearl.isRemoved(),
                        "R196 endermen must collect nearby dropped ender pearls"))
                .thenExecute(() -> helper.assertTrue(
                        enderman.requiresCustomPersistence(),
                        "R196 endermen carrying valuables must not despawn"))
                .thenSucceed();
    }

    private static void witchCurse(GameTestHelper helper) {
        var level = helper.getLevel();
        R196Witch witch = helper.spawn(ModEntityTypes.R196_WITCH.get(), new BlockPos(7, 2, 7));
        List<ServerPlayer> players = List.of(
                ModR196CompletionGameTests.createPlayer(helper),
                ModR196CompletionGameTests.createPlayer(helper),
                ModR196CompletionGameTests.createPlayer(helper),
                ModR196CompletionGameTests.createPlayer(helper));
        List<BlockPos> positions = List.of(
                new BlockPos(1, 2, 1), new BlockPos(2, 2, 1), new BlockPos(1, 2, 2), new BlockPos(2, 2, 2));
        for (int index = 0; index < players.size(); index++) {
            ServerPlayer player = players.get(index);
            Vec3 position = helper.absoluteVec(Vec3.atBottomCenterOf(positions.get(index)));
            player.snapTo(position.x, position.y, position.z, 0.0F, 0.0F);
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
            player.setHealth(100.0F);
        }
        // MITE only excludes a creative/disable-damage player. A survival player with the
        // entity-level Invulnerable flag must still receive a pending witch curse.
        ServerPlayer curseDeliveryProbe = players.getFirst();
        curseDeliveryProbe.setInvulnerable(true);

        helper.startSequence()
                .thenExecuteAfter(10, () -> helper.assertTrue(
                        witch.getTarget() instanceof ServerPlayer,
                        "R196 witches must acquire a valid player target before cursing"))
                .thenWaitUntil(() -> helper.assertTrue(
                        R196CurseData.get(level.getServer())
                                .entry(curseDeliveryProbe.getUUID())
                                .isPresent(),
                        "R196 witches must create a pending curse for a survival player marked Invulnerable"))
                .thenExecute(() -> {
                    witch.discard();
                    for (ServerPlayer player : players) {
                        R196CurseData.get(level.getServer()).remove(player.getUUID());
                        ModR196CompletionGameTests.removePlayer(player);
                    }
                })
                .thenSucceed();
    }

    private static void netherspawnMechanics(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = ModR196CompletionGameTests.createPlayer(helper);
        var snowballTarget = helper.spawnWithNoFreeWill(ModEntityTypes.NETHERSPAWN.get(), new BlockPos(2, 2, 2));
        var waterTarget = helper.spawnWithNoFreeWill(ModEntityTypes.NETHERSPAWN.get(), new BlockPos(5, 2, 2));
        var terrainSource = helper.spawnWithNoFreeWill(ModEntityTypes.NETHERSPAWN.get(), new BlockPos(8, 2, 2));
        BlockPos waterSafetyProbe = new BlockPos(6, 2, 2);
        BlockPos netherrack = new BlockPos(9, 2, 2);
        BlockPos quartz = new BlockPos(7, 2, 2);
        BlockPos netherGold = new BlockPos(8, 2, 3);
        BlockPos gold = new BlockPos(8, 2, 1);
        BlockPos deepslateGold = new BlockPos(8, 3, 2);
        helper.setBlock(netherrack, Blocks.NETHERRACK);
        helper.setBlock(waterSafetyProbe, Blocks.TORCH);
        helper.setBlock(quartz, Blocks.NETHER_QUARTZ_ORE);
        helper.setBlock(netherGold, Blocks.NETHER_GOLD_ORE);
        helper.setBlock(gold, Blocks.GOLD_ORE);
        helper.setBlock(deepslateGold, Blocks.DEEPSLATE_GOLD_ORE);
        Snowball snowball = new Snowball(level, player, Items.SNOWBALL.getDefaultInstance());

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(ModEntityTypes.NETHERSPAWN.get(), new BlockPos(2, 2, 2), 2.0D);
                    helper.assertEntityPresent(ModEntityTypes.NETHERSPAWN.get(), new BlockPos(5, 2, 2), 2.0D);
                    helper.assertEntityPresent(ModEntityTypes.NETHERSPAWN.get(), new BlockPos(8, 2, 2), 2.0D);
                })
                .thenExecute(() -> {
                    float healthBefore = snowballTarget.getHealth();
                    helper.assertTrue(
                            snowballTarget.hurtServer(level, level.damageSources().thrown(snowball, player), 0.0F),
                            "Netherspawn must accept snowball damage");
                    helper.assertTrue(
                            Math.abs(snowballTarget.getHealth() - (healthBefore - 2.0F)) < DAMAGE_EPSILON,
                            "MITE snowballs must deal two damage to netherspawn");
                    waterTarget.hurtServer(level, level.damageSources().drown(), 8.0F);
                    helper.assertFalse(waterTarget.isAlive(), "drowning-source water damage must kill netherspawn safely");
                    level.explode(
                            terrainSource,
                            terrainSource.getX(),
                            terrainSource.getY(),
                            terrainSource.getZ(),
                            4.0F,
                            true,
                            Level.ExplosionInteraction.MOB);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(
                            helper.getBlockState(waterSafetyProbe).is(Blocks.TORCH),
                            "drowning-source water kills must not trigger a netherspawn explosion");
                    helper.assertTrue(helper.getBlockState(netherrack).is(Blocks.NETHERRACK), "netherspawn must preserve netherrack");
                    helper.assertTrue(helper.getBlockState(quartz).is(Blocks.NETHER_QUARTZ_ORE), "netherspawn must preserve quartz ore");
                    helper.assertTrue(helper.getBlockState(netherGold).is(Blocks.NETHER_GOLD_ORE), "netherspawn must preserve nether gold ore");
                    helper.assertTrue(helper.getBlockState(gold).is(Blocks.GOLD_ORE), "netherspawn must preserve gold ore");
                    helper.assertTrue(
                            helper.getBlockState(deepslateGold).is(Blocks.DEEPSLATE_GOLD_ORE),
                            "netherspawn must preserve the modern gold-ore variant");
                    ModR196CompletionGameTests.removePlayer(player);
                })
                .thenSucceed();
    }

    private static void tactics(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = ModR196CompletionGameTests.createPlayer(helper);
        Vec3 playerPos = helper.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(7, 2, 7)));
        player.snapTo(playerPos.x, playerPos.y, playerPos.z, 0.0F, 0.0F);

        var leader = helper.spawn(
                ModEntityTypes.R196_ZOMBIE.get(), new BlockPos(2, 2, 2));
        var ally = helper.spawn(
                ModEntityTypes.R196_SKELETON.get(), new BlockPos(3, 2, 2));
        var piglin = helper.spawnWithNoFreeWill(
                ModEntityTypes.R196_ZOMBIFIED_PIGLIN.get(), new BlockPos(4, 2, 2));
        leader.setTarget(player);
        R196MonsterEvents.propagateTarget(level, leader, player);
        helper.assertTrue(ally.getTarget() == player, "R196 monsters must share a newly acquired player target");
        helper.assertTrue(
                piglin.getTarget() == null,
                "R196 zombified piglins must retain their own close-range target rules");
        leader.setTarget(null);
        ally.setTarget(null);
        level.gameEvent(GameEvent.BLOCK_DESTROY, player.position(), GameEvent.Context.of(player));
        helper.assertTrue(leader.getTarget() == player, "player block noise must attract nearby monsters");
        helper.assertTrue(ally.getTarget() == player, "activity attraction applies across hostile families");
        helper.assertTrue(
                piglin.getTarget() == null,
                "player activity must not bypass R196 zombified-piglin awareness range");

        var digger = helper.spawnWithNoFreeWill(
                ModEntityTypes.R196_ZOMBIE.get(), new BlockPos(2, 2, 7));
        digger.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                ModItems.catalog()
                        .equipment(R196Material.IRON, R196EquipmentType.PICKAXE)
                        .holder()
                        .toStack());
        digger.setTarget(player);
        BlockPos wall = new BlockPos(4, 3, 7);
        helper.setBlock(wall, Blocks.STONE);
        for (int attempt = 0; attempt < 30 && !helper.getBlockState(wall).isAir(); attempt++) {
            R196MonsterTactics.tryDig(level, digger);
        }
        helper.assertTrue(helper.getBlockState(wall).isAir(), "tool-equipped blocked monster must mine through stone");
        ModR196CompletionGameTests.removePlayer(player);
        helper.succeed();
    }
}
