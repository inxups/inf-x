package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class MonsterProfileTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void combatProfilesMatchTheR196SourceAttributes() {
        assertStats(MiteZombie.attributes(MiteZombie.Variant.ZOMBIE), 20.0, 40.0, 0.23, 5.0);
        assertStats(MiteZombie.attributes(MiteZombie.Variant.INVISIBLE_STALKER), 20.0, 40.0, 0.23, 4.0);
        assertStats(MiteZombie.attributes(MiteZombie.Variant.GHOUL), 20.0, 40.0, 0.28, 5.0);
        assertStats(MiteZombie.attributes(MiteZombie.Variant.SHADOW), 20.0, 40.0, 0.23, 5.0);
        assertStats(MiteZombie.attributes(MiteZombie.Variant.WIGHT), 20.0, 40.0, 0.25, 5.0);
        assertStats(MiteZombie.attributes(MiteZombie.Variant.REVENANT), 30.0, 40.0, 0.26, 7.0);
        for (MiteZombie.Variant variant : MiteZombie.Variant.values()) {
            assertEquals(0.0, stats(MiteZombie.attributes(variant)).getBaseValue(Attributes.ARMOR), EPSILON);
        }

        assertStats(MiteSkeleton.attributes(MiteSkeleton.Variant.SKELETON), 6.0, 32.0, 0.30, 4.0);
        assertStats(MiteSkeleton.attributes(MiteSkeleton.Variant.LONGDEAD), 12.0, 40.0, 0.29, 6.0);
        assertStats(MiteSkeleton.attributes(MiteSkeleton.Variant.BONE_LORD), 20.0, 40.0, 0.26, 5.0);
        assertStats(MiteSkeleton.attributes(MiteSkeleton.Variant.ANCIENT_BONE_LORD), 24.0, 40.0, 0.27, 8.0);

        assertStats(MiteSpider.attributes(MiteSpider.Variant.SPIDER), 12.0, 28.0, 0.375, 4.0);
        assertStats(MiteSpider.attributes(MiteSpider.Variant.CAVE_SPIDER), 16.0, 28.0, 0.375, 4.0);
        assertStats(MiteSpider.attributes(MiteSpider.Variant.BLACK_WIDOW), 6.0, 28.0, 0.30, 1.0);
        assertStats(MiteSpider.attributes(MiteSpider.Variant.DEMON), 18.0, 28.0, 0.375, 5.0);
        assertStats(MiteSpider.attributes(MiteSpider.Variant.WOOD), 6.0, 28.0, 0.30, 1.0);
        assertStats(MiteSpider.attributes(MiteSpider.Variant.PHASE), 6.0, 28.0, 0.30, 3.0);

        assertStats(MiteCreeper.attributes(MiteCreeper.Variant.CREEPER), 20.0, 32.0, 0.25, 2.0);
        assertStats(MiteCreeper.attributes(MiteCreeper.Variant.INFERNAL), 20.0, 32.0, 0.25, 2.0);
        assertEquals(2.0, stats(MiteCreeper.attributes(MiteCreeper.Variant.INFERNAL))
                .getBaseValue(Attributes.ARMOR), EPSILON);

        assertStats(MiteSilverfish.attributes(), 8.0, 32.0, 0.25, 3.0);
        assertStats(FireElemental.attributes(), 20.0, 40.0, 0.25, 5.0);
        assertStats(EarthElemental.attributes(), 30.0, 20.0, 0.20, 12.0);
        AttributeSupplier earthElemental = stats(EarthElemental.attributes());
        assertEquals(4.0, earthElemental.getBaseValue(Attributes.ARMOR), EPSILON);
        assertEquals(0.0, earthElemental.getBaseValue(Attributes.KNOCKBACK_RESISTANCE), EPSILON);
        assertStats(ClayGolem.attributes(), 30.0, 20.0, 0.20, 6.0);
        AttributeSupplier clayGolem = stats(ClayGolem.attributes());
        assertEquals(0.0, clayGolem.getBaseValue(Attributes.ARMOR), EPSILON);
        assertEquals(0.0, clayGolem.getBaseValue(Attributes.KNOCKBACK_RESISTANCE), EPSILON);
        assertStats(MiteEnderman.attributes(), 40.0, 64.0, 0.30, 10.0);
        assertEquals(6.5, MiteEnderman.chasingMovementSpeed(0.30), EPSILON);
        assertStats(MiteWitch.attributes(), 26.0, 32.0, 0.25, 2.0);
        assertStats(MiteZombifiedPiglin.attributes(), 20.0, 40.0, 0.23, 8.0);
        assertEquals(0.0, stats(MiteZombifiedPiglin.attributes()).getBaseValue(Attributes.ARMOR), EPSILON);
        assertEquals(0.28, MiteZombifiedPiglin.chasingMovementSpeed(0.23), EPSILON);

        assertEquals(0, MiteSpider.initialWebCount(MiteSpider.Variant.SPIDER, 0));
        assertEquals(2, MiteSpider.initialWebCount(MiteSpider.Variant.SPIDER, 3));
        assertEquals(3, MiteSpider.initialWebCount(MiteSpider.Variant.CAVE_SPIDER, 3));
        assertEquals(3, MiteSpider.initialWebCount(MiteSpider.Variant.DEMON, 3));
        assertEquals(2, MiteSpider.initialWebCount(MiteSpider.Variant.BLACK_WIDOW, 3));
        assertEquals(0, MiteSpider.initialWebCount(MiteSpider.Variant.PHASE, 3));
        assertEquals(500, MiteSpider.webThrowInterval(MiteSpider.Variant.SPIDER));
        assertEquals(500, MiteSpider.webThrowInterval(MiteSpider.Variant.WOOD));
        assertEquals(200, MiteSpider.webThrowInterval(MiteSpider.Variant.CAVE_SPIDER));
        assertEquals(200, MiteSpider.webThrowInterval(MiteSpider.Variant.DEMON));
        assertTrue(MiteSpider.shouldThrowWebAtTick(MiteSpider.Variant.SPIDER, 0, 0));
        assertFalse(MiteSpider.shouldThrowWebAtTick(MiteSpider.Variant.SPIDER, 0, 1));
        assertTrue(MiteSpider.shouldThrowWebAtTick(MiteSpider.Variant.CAVE_SPIDER, 153, 1));
        assertTrue(MiteSpider.canPhaseChaseAcrossVerticalDistance(2.0));
        assertTrue(MiteSpider.canPhaseChaseAcrossVerticalDistance(-2.0));
        assertFalse(MiteSpider.canPhaseChaseAcrossVerticalDistance(2.001));
        assertFalse(MiteSpider.canPhaseChaseAcrossVerticalDistance(-2.001));

        AttributeSupplier blaze = stats(MiteBlaze.attributes());
        assertEquals(32.0, blaze.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(0.23, blaze.getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        assertEquals(6.0, blaze.getBaseValue(Attributes.ATTACK_DAMAGE), EPSILON);
    }

    @Test
    void creeperSwellAndPowderRollsMatchR196() {
        assertEquals(4.5, MiteCreeper.swellStartDistanceSqr(MiteCreeper.Variant.CREEPER, false, 1.0F), EPSILON);
        assertEquals(9.0, MiteCreeper.swellStartDistanceSqr(MiteCreeper.Variant.CREEPER, false, 0.99F), EPSILON);
        assertEquals(16.0, MiteCreeper.swellStartDistanceSqr(MiteCreeper.Variant.CREEPER, true, 1.0F), EPSILON);
        assertEquals(9.0, MiteCreeper.swellStartDistanceSqr(MiteCreeper.Variant.INFERNAL, false, 1.0F), EPSILON);
        assertEquals(18.0, MiteCreeper.swellStartDistanceSqr(MiteCreeper.Variant.INFERNAL, false, 0.99F), EPSILON);
        assertEquals(32.0, MiteCreeper.swellStartDistanceSqr(MiteCreeper.Variant.INFERNAL, true, 1.0F), EPSILON);
        assertEquals(16.0, MiteCreeper.swellContinueDistanceSqr(MiteCreeper.Variant.CREEPER, 1.0F), EPSILON);
        assertEquals(40.0, MiteCreeper.swellContinueDistanceSqr(MiteCreeper.Variant.CREEPER, 0.0F), EPSILON);
        assertEquals(36.0, MiteCreeper.swellContinueDistanceSqr(MiteCreeper.Variant.INFERNAL, 1.0F), EPSILON);
        assertEquals(90.0, MiteCreeper.swellContinueDistanceSqr(MiteCreeper.Variant.INFERNAL, 0.4F), EPSILON);
        assertEquals(90.0, MiteCreeper.swellContinueDistanceSqr(MiteCreeper.Variant.INFERNAL, 0.0F), EPSILON);

        assertEquals(0, MiteCreeper.infernalPowderDropCount(0, 0, 0, true, 0));
        assertEquals(2, MiteCreeper.infernalPowderDropCount(0, 2, 0, true, 0));
        assertEquals(4, MiteCreeper.infernalPowderDropCount(3, 0, 1, true, 0));
        assertEquals(1, MiteCreeper.infernalPowderDropCount(3, 0, 0, false, 2));
        assertTrue(MiteCreeper.shouldDropInfernalPowder(true, 2));
        assertTrue(MiteCreeper.shouldDropInfernalPowder(false, 0));
        assertFalse(MiteCreeper.shouldDropInfernalPowder(false, 1));
    }

    @Test
    void infernalCreepersCanBreakStoneButOrdinaryCreepersCannot() {
        assertTrue(MonsterEvents.isCreeperTerrainProtected(MiteCreeper.Variant.CREEPER, 1.5F));
        assertTrue(MonsterEvents.isCreeperTerrainProtected(MiteCreeper.Variant.CREEPER, 3.0F));
        assertFalse(MonsterEvents.isCreeperTerrainProtected(MiteCreeper.Variant.INFERNAL, 1.5F));
        assertFalse(MonsterEvents.isCreeperTerrainProtected(MiteCreeper.Variant.INFERNAL, 3.0F));
        assertTrue(MonsterEvents.isCreeperTerrainProtected(MiteCreeper.Variant.INFERNAL, -1.0F));
    }

    @Test
    void invisibleStalkerDoesNotInheritZombieOnlyRules() {
        assertTrue(MiteZombie.breaksDoors(MiteZombie.Variant.INVISIBLE_STALKER));
        assertFalse(MiteZombie.burnsInSunlight(MiteZombie.Variant.INVISIBLE_STALKER));
        assertFalse(MiteZombie.zombifiesVillagers(MiteZombie.Variant.INVISIBLE_STALKER));
        assertFalse(MiteZombie.targetsAnimals(MiteZombie.Variant.INVISIBLE_STALKER));
        assertTrue(MiteZombie.targetsAnimals(MiteZombie.Variant.ZOMBIE));
    }

    @Test
    void endermanValuablesMatchR196PearlAwareness() {
        assertTrue(MiteEnderman.isPearlLike(Items.ENDER_PEARL));
        assertTrue(MiteEnderman.isPearlLike(Items.ENDER_EYE));
        assertFalse(MiteEnderman.isPearlLike(Items.DIAMOND));
    }

    @Test
    void nonstandardMobProfilesKeepTheirR196Limits() {
        AttributeSupplier slime = stats(MiteSlime.attributes());
        AttributeSupplier magmaCube = stats(MiteMagmaCube.attributes());
        AttributeSupplier squid = stats(MiteSquid.attributes());

        assertEquals(16.0, slime.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        for (MiteSlime.Variant variant : MiteSlime.Variant.values()) {
            double expectedSpeed = variant == MiteSlime.Variant.OOZE ? 0.05 : 0.30;
            assertEquals(expectedSpeed, stats(MiteSlime.attributes(variant)).getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        }
        assertEquals(0.30, MiteSlime.movementSpeedForSize(1), EPSILON);
        assertEquals(0.40, MiteSlime.movementSpeedForSize(2), EPSILON);
        assertEquals(0.60, MiteSlime.movementSpeedForSize(4), EPSILON);
        assertEquals(0.05, MiteSlime.movementSpeedFor(MiteSlime.Variant.OOZE, 1), EPSILON);
        assertEquals(0.05, MiteSlime.movementSpeedFor(MiteSlime.Variant.OOZE, 2), EPSILON);
        assertTrue(MiteSlime.usesCrawlAi(MiteSlime.Variant.OOZE));
        assertFalse(MiteSlime.usesCrawlAi(MiteSlime.Variant.SLIME));
        assertEquals(1.0, MiteSlime.attackDamageForSize(MiteSlime.Variant.SLIME, 1), EPSILON);
        assertEquals(4.0, MiteSlime.attackDamageForSize(MiteSlime.Variant.JELLY, 2), EPSILON);
        assertEquals(6.0, MiteSlime.attackDamageForSize(MiteSlime.Variant.BLOB, 2), EPSILON);
        assertEquals(6.0, MiteSlime.attackDamageForSize(MiteSlime.Variant.OOZE, 2), EPSILON);
        assertEquals(8.0, MiteSlime.attackDamageForSize(MiteSlime.Variant.PUDDING, 2), EPSILON);
        assertEquals(32.0, stats(MiteSlime.attributes(MiteSlime.Variant.OOZE))
                .getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(1, MiteSlime.experienceForSize(MiteSlime.Variant.SLIME, 1));
        assertEquals(4, MiteSlime.experienceForSize(MiteSlime.Variant.JELLY, 2));
        assertEquals(9, MiteSlime.experienceForSize(MiteSlime.Variant.BLOB, 3));
        assertEquals(8, MiteSlime.experienceForSize(MiteSlime.Variant.OOZE, 2));
        assertEquals(10, MiteSlime.experienceForSize(MiteSlime.Variant.PUDDING, 2));
        assertEquals(16.0, magmaCube.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(0.20, magmaCube.getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        assertEquals(2.0, MiteMagmaCube.attackDamageForSize(1), EPSILON);
        assertEquals(8.0, MiteMagmaCube.attackDamageForSize(4), EPSILON);
        assertEquals(2.0, MiteMagmaCube.armorForSize(1), EPSILON);
        assertEquals(8.0, MiteMagmaCube.armorForSize(4), EPSILON);
        assertEquals(0.20, MiteMagmaCube.movementSpeedForSize(1), EPSILON);
        assertEquals(0.20, MiteMagmaCube.movementSpeedForSize(4), EPSILON);
        assertEquals(10.0, squid.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(16.0, squid.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertFalse(squid.hasAttribute(Attributes.ATTACK_DAMAGE));
        assertEquals(3.0, stats(MiteCod.attributes()).getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(3.0, stats(MiteSalmon.attributes()).getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(3.0, stats(MitePufferfish.attributes()).getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(3.0, stats(MiteTropicalFish.attributes()).getBaseValue(Attributes.MAX_HEALTH), EPSILON);

        assertBat(MiteBat.Variant.VAMPIRE, 3.0, 1.0);
        assertBat(MiteBat.Variant.NIGHTWING, 3.0, 1.0);
        assertBat(MiteBat.Variant.GIANT_VAMPIRE, 6.0, 2.0);

        assertStats(MiteWolf.attributes(MiteWolf.Variant.HELLHOUND), 20.0, 16.0, 0.40, 4.0);
        assertStats(MiteWolf.attributes(MiteWolf.Variant.DIRE_WOLF), 16.0, 16.0, 0.40, 5.0);
        assertEquals(24.0, MiteWolf.maximumHealth(MiteWolf.Variant.DIRE_WOLF, true), EPSILON);
        assertEquals(32.0, MiteWolf.followRange(MiteWolf.Variant.DIRE_WOLF, true), EPSILON);
        assertEquals(20.0, MiteWolf.maximumHealth(MiteWolf.Variant.HELLHOUND, true), EPSILON);
        assertEquals(16.0, MiteWolf.followRange(MiteWolf.Variant.HELLHOUND, true), EPSILON);

        AttributeSupplier ghast = stats(MiteGhast.attributes());
        assertEquals(10.0, ghast.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(100.0, ghast.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
    }

    @Test
    void fireElementalWaterAttritionAndVampireBatFeedingUseMiteCadences() {
        assertFalse(FireElemental.shouldApplyWaterAttrition(40, false));
        assertFalse(FireElemental.shouldApplyWaterAttrition(39, true));
        assertTrue(FireElemental.shouldApplyWaterAttrition(40, true));
        assertEquals(20, MiteBat.attackCooldownTicks());
        assertEquals(1_200, MiteBat.feedCooldownTicks());
    }

    @Test
    void villagerZombieRareDropsUseTheMiteVillagerRate() {
        assertEquals(0.025F, MiteZombie.rareDropChance(MiteZombie.Variant.ZOMBIE, false, 0), EPSILON);
        assertEquals(0.035F, MiteZombie.rareDropChance(MiteZombie.Variant.ZOMBIE, false, 1), EPSILON);
        assertEquals(0.10F, MiteZombie.rareDropChance(MiteZombie.Variant.ZOMBIE, true, 0), EPSILON);
        assertEquals(0.14F, MiteZombie.rareDropChance(MiteZombie.Variant.ZOMBIE, true, 1), EPSILON);
        assertEquals(0.14F, MiteZombie.rareDropChance(MiteZombie.Variant.REVENANT, false, 1), EPSILON);
    }

    @Test
    void grayOozeUsesCrawlGoalsAndCannotJumpFromGround() throws NoSuchMethodException {
        assertEquals(MiteSlime.class, MiteSlime.class.getDeclaredMethod("registerGoals").getDeclaringClass());
        assertEquals(MiteSlime.class, MiteSlime.class.getDeclaredMethod("jumpFromGround").getDeclaringClass());
    }

    @Test
    void netherspawnExplosionKeepsTheMiteProtectedTerrain() {
        assertTrue(MiteSilverfish.isNetherspawnExplosionProtected(Blocks.NETHERRACK.defaultBlockState()));
        assertTrue(MiteSilverfish.isNetherspawnExplosionProtected(Blocks.NETHER_QUARTZ_ORE.defaultBlockState()));
        assertTrue(MiteSilverfish.isNetherspawnExplosionProtected(Blocks.NETHER_GOLD_ORE.defaultBlockState()));
        assertTrue(MiteSilverfish.isNetherspawnExplosionProtected(Blocks.GOLD_ORE.defaultBlockState()));
        assertTrue(MiteSilverfish.isNetherspawnExplosionProtected(Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState()));
        assertFalse(MiteSilverfish.isNetherspawnExplosionProtected(Blocks.DIRT.defaultBlockState()));
    }

    @Test
    void allRegisteredMobNamesAndDimensionsMatchTheR196Roster() {
        Map<String, InfXEntityTypes.EntityName> entities = InfXEntityTypes.names().stream()
                .collect(Collectors.toMap(InfXEntityTypes.EntityName::path, entity -> entity));
        assertEquals(expectedNames(), entities.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new Names(entry.getValue().english(), entry.getValue().chinese()))));

        Set<String> checked = new HashSet<>();
        assertDimensions(entities, checked, 0.6F, 1.8F,
                "r196_zombie", "r196_skeleton", "r196_creeper", "r196_witch", "r196_zombified_piglin",
                "r196_blaze", "invisible_stalker", "ghoul", "shadow", "wight", "revenant", "longdead",
                "bone_lord", "ancient_bone_lord", "infernal_creeper", "fire_elemental", "earth_elemental",
                "clay_golem");
        assertDimensions(entities, checked, 1.4F, 0.9F, "r196_spider", "demon_spider");
        assertDimensions(entities, checked, 0.98F, 0.63F, "r196_cave_spider");
        assertDimensions(entities, checked, 0.84F, 0.54F, "black_widow_spider", "wood_spider", "phase_spider");
        assertDimensions(entities, checked, 0.5F, 0.5F,
                "r196_slime", "jelly", "blob", "ooze", "pudding", "magma_cube");
        assertDimensions(entities, checked, 0.6F, 2.9F, "r196_enderman");
        assertDimensions(entities, checked, 0.95F, 0.95F, "r196_squid");
        assertDimensions(entities, checked, 0.5F, 0.3F, "r196_cod");
        assertDimensions(entities, checked, 0.7F, 0.4F, "r196_salmon");
        assertDimensions(entities, checked, 0.7F, 0.7F, "r196_pufferfish");
        assertDimensions(entities, checked, 0.5F, 0.4F, "r196_tropical_fish");
        assertDimensions(entities, checked, 4.0F, 4.0F, "r196_ghast");
        assertDimensions(entities, checked, 0.3F, 0.7F, "netherspawn", "copperspine", "hoary_silverfish");
        assertDimensions(entities, checked, 0.5F, 0.9F, "vampire_bat", "nightwing");
        assertDimensions(entities, checked, 1.0F, 1.8F, "giant_vampire_bat");
        assertDimensions(entities, checked, 0.6F, 0.8F, "hellhound", "dire_wolf");
        assertDimensions(entities, checked, 0.9F, 1.4F, "r196_cow");
        assertDimensions(entities, checked, 0.4F, 0.7F, "r196_chicken");
        assertDimensions(entities, checked, 0.9F, 1.3F, "r196_sheep");
        assertDimensions(entities, checked, 0.9F, 0.9F, "r196_pig");
        assertDimensions(entities, checked, 1.3964844F, 1.6F, "r196_horse");
        assertDimensions(entities, checked, 0.6F, 0.7F, "r196_ocelot");
        assertDimensions(entities, checked, 0.7F, 0.8F, "r196_wolf");
        assertEquals(entities.keySet(), checked);
    }

    private static void assertBat(MiteBat.Variant variant, double health, double attack) {
        AttributeSupplier attributes = stats(MiteBat.attributes(variant));
        assertEquals(health, attributes.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(16.0, attributes.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(attack, attributes.getBaseValue(Attributes.ATTACK_DAMAGE), EPSILON);
    }

    private static void assertStats(
            AttributeSupplier.Builder builder, double health, double followRange, double movementSpeed, double attackDamage) {
        AttributeSupplier attributes = stats(builder);
        assertEquals(health, attributes.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(followRange, attributes.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(movementSpeed, attributes.getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        assertEquals(attackDamage, attributes.getBaseValue(Attributes.ATTACK_DAMAGE), EPSILON);
    }

    private static AttributeSupplier stats(AttributeSupplier.Builder builder) {
        return builder.build();
    }

    private static void assertDimensions(
            Map<String, InfXEntityTypes.EntityName> entities,
            Set<String> checked,
            float width,
            float height,
            String... paths) {
        for (String path : paths) {
            InfXEntityTypes.EntityName entity = entities.get(path);
            assertEquals(width, entity.width(), EPSILON, path + " width");
            assertEquals(height, entity.height(), EPSILON, path + " height");
            checked.add(path);
        }
    }

    private static Map<String, Names> expectedNames() {
        Map<String, Names> names = new HashMap<>();
        add(names, "r196_zombie", "Zombie", "僵尸");
        add(names, "r196_skeleton", "Skeleton", "骷髅");
        add(names, "r196_spider", "Spider", "蜘蛛");
        add(names, "r196_cave_spider", "Cave Spider", "洞穴蜘蛛");
        add(names, "r196_creeper", "Creeper", "苦力怕");
        add(names, "r196_slime", "Slime", "史莱姆");
        add(names, "r196_enderman", "Enderman", "末影人");
        add(names, "r196_squid", "Squid", "鱿鱼");
        add(names, "r196_cod", "Cod", "鳕鱼");
        add(names, "r196_salmon", "Salmon", "鲑鱼");
        add(names, "r196_pufferfish", "Pufferfish", "河豚");
        add(names, "r196_tropical_fish", "Tropical Fish", "热带鱼");
        add(names, "r196_witch", "Witch", "女巫");
        add(names, "r196_zombified_piglin", "Zombie Pigman", "僵尸猪人");
        add(names, "r196_blaze", "Blaze", "烈焰人");
        add(names, "r196_ghast", "Ghast", "恶魂");
        add(names, "invisible_stalker", "Invisible Stalker", "影子潜伏者");
        add(names, "ghoul", "Ghoul", "食尸鬼");
        add(names, "shadow", "Shadow", "黑色食尸鬼");
        add(names, "wight", "Wight", "尸妖");
        add(names, "revenant", "Revenant", "亡魂");
        add(names, "longdead", "Longdead", "古尸");
        add(names, "bone_lord", "Bone Lord", "骷髅领主");
        add(names, "ancient_bone_lord", "Ancient Bone Lord", "远古骷髅领主");
        add(names, "black_widow_spider", "Black Widow Spider", "黑寡妇蜘蛛");
        add(names, "demon_spider", "Demon Spider", "恶魔蜘蛛");
        add(names, "wood_spider", "Wood Spider", "木蜘蛛");
        add(names, "phase_spider", "Phase Spider", "相位蜘蛛");
        add(names, "infernal_creeper", "Infernal Creeper", "地狱爬行者");
        add(names, "fire_elemental", "Fire Elemental", "火元素");
        add(names, "earth_elemental", "Earth Elemental", "土元素");
        add(names, "clay_golem", "Clay Golem", "黏土元素");
        add(names, "jelly", "Jelly", "褐色史莱姆");
        add(names, "blob", "Blob", "红色史莱姆");
        add(names, "ooze", "Ooze", "灰色史莱姆");
        add(names, "pudding", "Pudding", "黑色史莱姆");
        add(names, "magma_cube", "Magma Cube", "岩浆怪");
        add(names, "netherspawn", "Netherspawn", "爆炸蠹虫");
        add(names, "copperspine", "Copperspine", "铜毒蠹虫");
        add(names, "hoary_silverfish", "Hoary Silverfish", "白化蠹虫");
        add(names, "vampire_bat", "Vampire Bat", "吸血蝙蝠");
        add(names, "nightwing", "Nightwing", "暗影蝙蝠");
        add(names, "giant_vampire_bat", "Giant Vampire Bat", "吸血巨蝠");
        add(names, "hellhound", "Hellhound", "地狱犬");
        add(names, "dire_wolf", "Dire Wolf", "惧狼");
        add(names, "r196_cow", "Cow", "牛");
        add(names, "r196_chicken", "Chicken", "鸡");
        add(names, "r196_sheep", "Sheep", "羊");
        add(names, "r196_pig", "Pig", "猪");
        add(names, "r196_horse", "Horse", "马");
        add(names, "r196_ocelot", "Ocelot", "豹猫");
        add(names, "r196_wolf", "Wolf", "狼");
        return names;
    }

    private static void add(Map<String, Names> names, String path, String english, String chinese) {
        names.put(path, new Names(english, chinese));
    }

    private record Names(String english, String chinese) {}
}
