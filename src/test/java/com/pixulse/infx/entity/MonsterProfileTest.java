package com.pixulse.infx.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class MonsterProfileTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void earthElementalsUseTheHostileMobHierarchy() {
        assertEquals(Monster.class, EarthElemental.class.getSuperclass());
        assertEquals(EarthElemental.class, ClayGolem.class.getSuperclass());
        assertTrue(Enemy.class.isAssignableFrom(EarthElemental.class));
    }

    @Test
    void combatProfilesUseMinecraft261FollowRanges() {
        assertStats(Ghoul.attributes(), 20.0, 35.0, 0.28, 5.0);
        assertStats(InvisibleStalker.attributes(), 20.0, 35.0, 0.23, 4.0);
        assertStats(Shadow.attributes(), 20.0, 35.0, 0.23, 5.0);
        assertStats(Wight.attributes(), 20.0, 35.0, 0.25, 5.0);
        assertStats(Revenant.attributes(), 30.0, 35.0, 0.26, 7.0);
        for (AttributeSupplier.Builder builder : List.of(
                Ghoul.attributes(),
                InvisibleStalker.attributes(),
                Shadow.attributes(),
                Wight.attributes(),
                Revenant.attributes())) {
            assertEquals(0.0, stats(builder).getBaseValue(Attributes.ARMOR), EPSILON);
        }

        assertStats(InfxSkeleton.attributes(InfxSkeleton.Variant.SKELETON), 6.0, 16.0, 0.25, 4.0);
        assertStats(InfxWitherSkeleton.attributes(), 20.0, 16.0, 0.25, 4.0);
        assertStats(InfxSkeleton.attributes(InfxSkeleton.Variant.LONGDEAD), 12.0, 16.0, 0.29, 6.0);
        assertStats(InfxSkeleton.attributes(InfxSkeleton.Variant.LONGDEAD_GUARDIAN), 24.0, 16.0, 0.29, 8.0);
        assertEquals(2.0, stats(InfxSkeleton.attributes(InfxSkeleton.Variant.LONGDEAD_GUARDIAN))
                .getBaseValue(Attributes.ARMOR), EPSILON);
        assertStats(InfxSkeleton.attributes(InfxSkeleton.Variant.BONE_LORD), 20.0, 16.0, 0.26, 5.0);
        assertStats(InfxSkeleton.attributes(InfxSkeleton.Variant.ANCIENT_BONE_LORD), 24.0, 16.0, 0.27, 8.0);
        assertStats(VanillaWolf.attributes(), 8.0, 16.0, 0.30, 3.0);

        assertStats(InfxSpider.attributes(InfxSpider.Variant.SPIDER), 12.0, 16.0, 0.30, 4.0);
        assertStats(InfxSpider.attributes(InfxSpider.Variant.CAVE_SPIDER), 16.0, 16.0, 0.30, 4.0);
        assertStats(InfxSpider.attributes(InfxSpider.Variant.BLACK_WIDOW), 6.0, 16.0, 0.30, 1.0);
        assertStats(InfxSpider.attributes(InfxSpider.Variant.DEMON), 18.0, 16.0, 0.375, 5.0);
        assertStats(InfxSpider.attributes(InfxSpider.Variant.WOOD), 6.0, 16.0, 0.30, 1.0);
        assertStats(InfxSpider.attributes(InfxSpider.Variant.PHASE), 6.0, 16.0, 0.30, 3.0);

        assertStats(InfxCreeper.attributes(InfxCreeper.Variant.CREEPER), 20.0, 16.0, 0.25, 2.0);
        assertStats(InfxCreeper.attributes(InfxCreeper.Variant.INFERNAL), 20.0, 16.0, 0.25, 2.0);
        assertEquals(2.0, stats(InfxCreeper.attributes(InfxCreeper.Variant.INFERNAL))
                .getBaseValue(Attributes.ARMOR), EPSILON);

        assertStats(InfxSilverfish.attributes(), 8.0, 16.0, 0.25, 3.0);
        assertStats(FireElemental.attributes(), 20.0, 16.0, 0.25, 5.0);
        assertStats(EarthElemental.attributes(), 30.0, 16.0, 0.20, 12.0);
        AttributeSupplier earthElemental = stats(EarthElemental.attributes());
        assertEquals(4.0, earthElemental.getBaseValue(Attributes.ARMOR), EPSILON);
        assertEquals(0.0, earthElemental.getBaseValue(Attributes.KNOCKBACK_RESISTANCE), EPSILON);
        assertStats(ClayGolem.attributes(), 30.0, 16.0, 0.20, 6.0);
        AttributeSupplier clayGolem = stats(ClayGolem.attributes());
        assertEquals(0.0, clayGolem.getBaseValue(Attributes.ARMOR), EPSILON);
        assertEquals(0.0, clayGolem.getBaseValue(Attributes.KNOCKBACK_RESISTANCE), EPSILON);
        assertStats(InfxEnderman.attributes(), 40.0, 64.0, 0.30, 10.0);
        assertEquals(0.45, InfxEnderman.chasingMovementSpeed(0.30), EPSILON);
        assertStats(InfxWitch.attributes(), 26.0, 16.0, 0.25, 2.0);
        assertStats(InfxZombifiedPiglin.attributes(), 20.0, 24.0, 0.23, 8.0);
        assertEquals(0.0, stats(InfxZombifiedPiglin.attributes()).getBaseValue(Attributes.ARMOR), EPSILON);
        assertEquals(0.28, InfxZombifiedPiglin.chasingMovementSpeed(0.23), EPSILON);

        assertEquals(0, InfxSpider.initialWebCount(InfxSpider.Variant.SPIDER, 0));
        assertEquals(2, InfxSpider.initialWebCount(InfxSpider.Variant.SPIDER, 3));
        assertEquals(3, InfxSpider.initialWebCount(InfxSpider.Variant.CAVE_SPIDER, 3));
        assertEquals(3, InfxSpider.initialWebCount(InfxSpider.Variant.DEMON, 3));
        assertEquals(2, InfxSpider.initialWebCount(InfxSpider.Variant.BLACK_WIDOW, 3));
        assertEquals(0, InfxSpider.initialWebCount(InfxSpider.Variant.PHASE, 3));
        assertEquals(500, InfxSpider.webThrowInterval(InfxSpider.Variant.SPIDER));
        assertEquals(500, InfxSpider.webThrowInterval(InfxSpider.Variant.WOOD));
        assertEquals(200, InfxSpider.webThrowInterval(InfxSpider.Variant.CAVE_SPIDER));
        assertEquals(200, InfxSpider.webThrowInterval(InfxSpider.Variant.DEMON));
        assertTrue(InfxSpider.shouldThrowWebAtTick(InfxSpider.Variant.SPIDER, 0, 0));
        assertFalse(InfxSpider.shouldThrowWebAtTick(InfxSpider.Variant.SPIDER, 0, 1));
        assertTrue(InfxSpider.shouldThrowWebAtTick(InfxSpider.Variant.CAVE_SPIDER, 153, 1));
        assertTrue(InfxSpider.phaseEvasionEligible(false, false, false));
        assertFalse(InfxSpider.phaseEvasionEligible(true, false, false));
        assertFalse(InfxSpider.phaseEvasionEligible(false, true, false));
        assertFalse(InfxSpider.phaseEvasionEligible(false, false, true));
        assertTrue(InfxWebProjectile.isWebReplaceable(Blocks.AIR.defaultBlockState()));
        assertTrue(InfxWebProjectile.isWebReplaceable(Blocks.SNOW.defaultBlockState()));
        assertFalse(InfxWebProjectile.isWebReplaceable(Blocks.STONE.defaultBlockState()));
        assertTrue(InfxWebProjectile.canPlaceWebAfterBlockImpact(
                Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState()));
        assertFalse(InfxWebProjectile.canPlaceWebAfterBlockImpact(
                Blocks.WATER.defaultBlockState(), Blocks.AIR.defaultBlockState()));
        assertFalse(InfxWebProjectile.canPlaceWebAfterBlockImpact(
                Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState()));
        assertFalse(InfxWebProjectile.canPlaceWebAfterBlockImpact(
                Blocks.LAVA.defaultBlockState(), Blocks.AIR.defaultBlockState()));
        assertFalse(InfxWebProjectile.canPlaceWebAfterBlockImpact(
                Blocks.STONE.defaultBlockState(), Blocks.FIRE.defaultBlockState()));

        AttributeSupplier blaze = stats(InfxBlaze.attributes());
        assertEquals(48.0, blaze.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(0.23, blaze.getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        assertEquals(6.0, blaze.getBaseValue(Attributes.ATTACK_DAMAGE), EPSILON);
    }

    @Test
    void ordinarySkeletonWeaponSplitUsesTheSharedBowAndMeleeProgressionProfile() {
        assertOrdinarySkeletonWeapon(0.0F, 0.0F, InfxMaterial.WOOD, EquipmentType.CUDGEL);
        assertOrdinarySkeletonWeapon(0.09F, 0.249F, InfxMaterial.WOOD, EquipmentType.CUDGEL);
        assertOrdinarySkeletonWeapon(0.1F, 0.0F, InfxMaterial.WOOD, EquipmentType.CLUB);
        assertOrdinarySkeletonWeapon(0.19F, 0.249F, InfxMaterial.WOOD, EquipmentType.CLUB);
        assertOrdinarySkeletonWeapon(0.2F, 0.0F, InfxMaterial.RUSTED_IRON, EquipmentType.DAGGER);
        assertOrdinarySkeletonWeapon(0.29F, 0.249F, InfxMaterial.RUSTED_IRON, EquipmentType.DAGGER);
        assertOrdinarySkeletonWeapon(0.3F, 0.0F, InfxMaterial.RUSTED_IRON, EquipmentType.SWORD);
        assertOrdinarySkeletonWeapon(0.0F, 0.25F, InfxMaterial.WOOD, EquipmentType.BOW);
        assertOrdinarySkeletonWeapon(0.3F, 0.999F, InfxMaterial.WOOD, EquipmentType.BOW);
    }

    private static void assertOrdinarySkeletonWeapon(
            float tension, float roll, InfxMaterial material, EquipmentType type) {
        InfxSkeleton.OrdinarySkeletonWeapon weapon = InfxSkeleton.ordinarySpawnWeapon(roll, tension);
        assertEquals(material, weapon.material());
        assertEquals(type, weapon.type());
    }

    @Test
    void creeperSwellAndPowderRollsMatchR196() {
        assertEquals(4.5, InfxCreeper.swellStartDistanceSqr(InfxCreeper.Variant.CREEPER, false, 1.0F), EPSILON);
        assertEquals(9.0, InfxCreeper.swellStartDistanceSqr(InfxCreeper.Variant.CREEPER, false, 0.99F), EPSILON);
        assertEquals(16.0, InfxCreeper.swellStartDistanceSqr(InfxCreeper.Variant.CREEPER, true, 1.0F), EPSILON);
        assertEquals(9.0, InfxCreeper.swellStartDistanceSqr(InfxCreeper.Variant.INFERNAL, false, 1.0F), EPSILON);
        assertEquals(18.0, InfxCreeper.swellStartDistanceSqr(InfxCreeper.Variant.INFERNAL, false, 0.99F), EPSILON);
        assertEquals(32.0, InfxCreeper.swellStartDistanceSqr(InfxCreeper.Variant.INFERNAL, true, 1.0F), EPSILON);
        assertEquals(16.0, InfxCreeper.swellContinueDistanceSqr(InfxCreeper.Variant.CREEPER, 1.0F), EPSILON);
        assertEquals(40.0, InfxCreeper.swellContinueDistanceSqr(InfxCreeper.Variant.CREEPER, 0.0F), EPSILON);
        assertEquals(36.0, InfxCreeper.swellContinueDistanceSqr(InfxCreeper.Variant.INFERNAL, 1.0F), EPSILON);
        assertEquals(90.0, InfxCreeper.swellContinueDistanceSqr(InfxCreeper.Variant.INFERNAL, 0.4F), EPSILON);
        assertEquals(90.0, InfxCreeper.swellContinueDistanceSqr(InfxCreeper.Variant.INFERNAL, 0.0F), EPSILON);

        assertEquals(0, InfxCreeper.infernalPowderDropCount(0, 0, 0, true, 0));
        assertEquals(2, InfxCreeper.infernalPowderDropCount(0, 2, 0, true, 0));
        assertEquals(4, InfxCreeper.infernalPowderDropCount(3, 0, 1, true, 0));
        assertEquals(1, InfxCreeper.infernalPowderDropCount(3, 0, 0, false, 2));
        assertTrue(InfxCreeper.shouldDropInfernalPowder(true, 2));
        assertTrue(InfxCreeper.shouldDropInfernalPowder(false, 0));
        assertFalse(InfxCreeper.shouldDropInfernalPowder(false, 1));
    }

    @Test
    void infernalCreepersCanBreakStoneButOrdinaryCreepersCannot() {
        assertTrue(MonsterEvents.isCreeperTerrainProtected(InfxCreeper.Variant.CREEPER, 1.5F));
        assertTrue(MonsterEvents.isCreeperTerrainProtected(InfxCreeper.Variant.CREEPER, 3.0F));
        assertFalse(MonsterEvents.isCreeperTerrainProtected(InfxCreeper.Variant.INFERNAL, 1.5F));
        assertFalse(MonsterEvents.isCreeperTerrainProtected(InfxCreeper.Variant.INFERNAL, 3.0F));
        assertTrue(MonsterEvents.isCreeperTerrainProtected(InfxCreeper.Variant.INFERNAL, -1.0F));
    }

    @Test
    void newZombieMobsAreSeparateEnemyTypesThatDoNotReplaceVanillaSpawns() {
        assertTrue(Enemy.class.isAssignableFrom(Ghoul.class));
        assertTrue(Enemy.class.isAssignableFrom(InvisibleStalker.class));
        assertTrue(net.minecraft.world.entity.monster.zombie.Zombie.class.isAssignableFrom(Ghoul.class));
        assertTrue(InfxMob.class.isAssignableFrom(InvisibleStalker.class));
        assertTrue(InfxZombieBase.class.isAssignableFrom(Revenant.class));
    }

    @Test
    void endermanValuablesMatchR196PearlAwareness() {
        assertTrue(InfxEnderman.isPearlLike(Items.ENDER_PEARL));
        assertTrue(InfxEnderman.isPearlLike(Items.ENDER_EYE));
        assertFalse(InfxEnderman.isPearlLike(Items.DIAMOND));
    }

    @Test
    void nonstandardMobProfilesKeepTheirR196Limits() {
        AttributeSupplier slime = stats(InfxSlime.attributes());
        AttributeSupplier magmaCube = stats(InfxMagmaCube.attributes());
        AttributeSupplier squid = stats(InfxSquid.attributes());

        assertEquals(16.0, slime.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        for (InfxSlime.Variant variant : InfxSlime.Variant.values()) {
            double expectedSpeed = variant == InfxSlime.Variant.OOZE ? 0.05 : 0.30;
            assertEquals(expectedSpeed, stats(InfxSlime.attributes(variant)).getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        }
        assertEquals(0.30, InfxSlime.movementSpeedForSize(1), EPSILON);
        assertEquals(0.40, InfxSlime.movementSpeedForSize(2), EPSILON);
        assertEquals(0.60, InfxSlime.movementSpeedForSize(4), EPSILON);
        assertEquals(0.05, InfxSlime.movementSpeedFor(InfxSlime.Variant.OOZE, 1), EPSILON);
        assertEquals(0.05, InfxSlime.movementSpeedFor(InfxSlime.Variant.OOZE, 2), EPSILON);
        assertTrue(InfxSlime.usesCrawlAi(InfxSlime.Variant.OOZE));
        assertFalse(InfxSlime.usesCrawlAi(InfxSlime.Variant.SLIME));
        assertEquals(1.0, InfxSlime.attackDamageForSize(InfxSlime.Variant.SLIME, 1), EPSILON);
        assertEquals(4.0, InfxSlime.attackDamageForSize(InfxSlime.Variant.JELLY, 2), EPSILON);
        assertEquals(6.0, InfxSlime.attackDamageForSize(InfxSlime.Variant.BLOB, 2), EPSILON);
        assertEquals(6.0, InfxSlime.attackDamageForSize(InfxSlime.Variant.OOZE, 2), EPSILON);
        assertEquals(8.0, InfxSlime.attackDamageForSize(InfxSlime.Variant.PUDDING, 2), EPSILON);
        assertEquals(16.0, stats(InfxSlime.attributes(InfxSlime.Variant.OOZE))
                .getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(1, InfxSlime.experienceForSize(InfxSlime.Variant.SLIME, 1));
        assertEquals(4, InfxSlime.experienceForSize(InfxSlime.Variant.JELLY, 2));
        assertEquals(9, InfxSlime.experienceForSize(InfxSlime.Variant.BLOB, 3));
        assertEquals(8, InfxSlime.experienceForSize(InfxSlime.Variant.OOZE, 2));
        assertEquals(10, InfxSlime.experienceForSize(InfxSlime.Variant.PUDDING, 2));
        assertEquals(16.0, magmaCube.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertEquals(0.20, magmaCube.getBaseValue(Attributes.MOVEMENT_SPEED), EPSILON);
        assertEquals(2.0, InfxMagmaCube.attackDamageForSize(1), EPSILON);
        assertEquals(8.0, InfxMagmaCube.attackDamageForSize(4), EPSILON);
        assertEquals(2.0, InfxMagmaCube.armorForSize(1), EPSILON);
        assertEquals(8.0, InfxMagmaCube.armorForSize(4), EPSILON);
        assertEquals(0.20, InfxMagmaCube.movementSpeedForSize(1), EPSILON);
        assertEquals(0.20, InfxMagmaCube.movementSpeedForSize(4), EPSILON);
        assertEquals(10.0, squid.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(16.0, squid.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
        assertFalse(squid.hasAttribute(Attributes.ATTACK_DAMAGE));
        assertEquals(3.0, stats(InfxCod.attributes()).getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(3.0, stats(InfxSalmon.attributes()).getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(3.0, stats(InfxPufferfish.attributes()).getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(3.0, stats(InfxTropicalFish.attributes()).getBaseValue(Attributes.MAX_HEALTH), EPSILON);

        AttributeSupplier normalBat = stats(InfxBat.attributes(InfxBat.Variant.NORMAL));
        assertEquals(3.0, normalBat.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertFalse(normalBat.hasAttribute(Attributes.ATTACK_DAMAGE));
        assertFalse(Enemy.class.isAssignableFrom(InfxBat.class));
        assertEquals(1.25F, InfxBat.nightwingDimmingAfterSilverCoverage(0.0F), 1.0E-6F);
        assertEquals(0.625F, InfxBat.nightwingDimmingAfterSilverCoverage(1.0F), 1.0E-6F);
        assertBat(InfxBat.Variant.VAMPIRE, 3.0, 1.0);
        assertBat(InfxBat.Variant.NIGHTWING, 3.0, 1.0);
        assertBat(InfxBat.Variant.GIANT_VAMPIRE, 6.0, 2.0);

        assertStats(InfxWolf.attributes(InfxWolf.Variant.HELLHOUND), 20.0, 16.0, 0.40, 4.0);
        assertStats(InfxWolf.attributes(InfxWolf.Variant.DIRE_WOLF), 16.0, 16.0, 0.40, 5.0);
        assertEquals(24.0, InfxWolf.maximumHealth(InfxWolf.Variant.DIRE_WOLF, true), EPSILON);
        assertEquals(32.0, InfxWolf.followRange(InfxWolf.Variant.DIRE_WOLF, true), EPSILON);
        assertEquals(20.0, InfxWolf.maximumHealth(InfxWolf.Variant.HELLHOUND, true), EPSILON);
        assertEquals(16.0, InfxWolf.followRange(InfxWolf.Variant.HELLHOUND, true), EPSILON);

        AttributeSupplier ghast = stats(InfxGhast.attributes());
        assertEquals(10.0, ghast.getBaseValue(Attributes.MAX_HEALTH), EPSILON);
        assertEquals(100.0, ghast.getBaseValue(Attributes.FOLLOW_RANGE), EPSILON);
    }

    @Test
    void fireElementalWaterAttritionAndVampireBatFeedingUseCadences() {
        assertFalse(FireElemental.shouldApplyWaterAttrition(40, false));
        assertFalse(FireElemental.shouldApplyWaterAttrition(39, true));
        assertTrue(FireElemental.shouldApplyWaterAttrition(40, true));
        assertEquals(20, InfxBat.attackCooldownTicks());
        assertEquals(1_200, InfxBat.feedCooldownTicks());
    }

    @Test
    void grayOozeUsesCrawlGoalsAndCannotJumpFromGround() throws NoSuchMethodException {
        assertEquals(InfxSlime.class, InfxSlime.class.getDeclaredMethod("registerGoals").getDeclaringClass());
        assertEquals(InfxSlime.class, InfxSlime.class.getDeclaredMethod("jumpFromGround").getDeclaringClass());
    }

    @Test
    void netherspawnExplosionKeepsTheProtectedTerrain() {
        assertTrue(InfxSilverfish.isNetherspawnExplosionProtected(Blocks.NETHERRACK.defaultBlockState()));
        assertTrue(InfxSilverfish.isNetherspawnExplosionProtected(Blocks.NETHER_QUARTZ_ORE.defaultBlockState()));
        assertTrue(InfxSilverfish.isNetherspawnExplosionProtected(Blocks.NETHER_GOLD_ORE.defaultBlockState()));
        assertTrue(InfxSilverfish.isNetherspawnExplosionProtected(Blocks.GOLD_ORE.defaultBlockState()));
        assertTrue(InfxSilverfish.isNetherspawnExplosionProtected(Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState()));
        assertFalse(InfxSilverfish.isNetherspawnExplosionProtected(Blocks.DIRT.defaultBlockState()));
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
                "infx_skeleton", "infx_creeper", "infx_zombie", "infx_witch", "infx_zombified_piglin",
                "infx_blaze", "invisible_stalker", "ghoul", "shadow", "wight", "revenant", "longdead",
                "longdead_guardian", "bone_lord", "ancient_bone_lord", "infernal_creeper", "fire_elemental", "earth_elemental",
                "clay_golem");
        assertDimensions(entities, checked, 0.7F, 2.4F, "infx_wither_skeleton");
        assertDimensions(entities, checked, 1.4F, 0.9F, "infx_spider", "demon_spider");
        assertDimensions(entities, checked, 0.98F, 0.63F, "infx_cave_spider");
        assertDimensions(entities, checked, 0.84F, 0.54F, "black_widow_spider", "wood_spider", "phase_spider");
        assertDimensions(entities, checked, 0.5F, 0.5F,
                "infx_slime", "jelly", "blob", "ooze", "pudding", "magma_cube");
        assertDimensions(entities, checked, 0.6F, 2.9F, "infx_enderman");
        assertDimensions(entities, checked, 0.95F, 0.95F, "infx_squid");
        assertDimensions(entities, checked, 0.5F, 0.3F, "infx_cod");
        assertDimensions(entities, checked, 0.7F, 0.4F, "infx_salmon");
        assertDimensions(entities, checked, 0.7F, 0.7F, "infx_pufferfish");
        assertDimensions(entities, checked, 0.5F, 0.4F, "infx_tropical_fish");
        assertDimensions(entities, checked, 4.0F, 4.0F, "infx_ghast");
        assertDimensions(entities, checked, 0.3F, 0.7F, "netherspawn", "copperspine", "hoary_silverfish");
        assertDimensions(entities, checked, 0.5F, 0.9F, "infx_bat", "vampire_bat", "nightwing");
        assertDimensions(entities, checked, 0.75F, 1.35F, "giant_vampire_bat");
        InfXEntityTypes.EntityName vampireBat = entities.get("vampire_bat");
        InfXEntityTypes.EntityName giantVampireBat = entities.get("giant_vampire_bat");
        assertTrue(
                giantVampireBat.width() > vampireBat.width() && giantVampireBat.height() > vampireBat.height(),
                "giant vampire bat must remain larger than a vampire bat");
        assertDimensions(entities, checked, 0.6F, 0.8F, "hellhound", "dire_wolf");
        assertDimensions(entities, checked, 0.9F, 1.4F, "infx_cow");
        assertDimensions(entities, checked, 0.4F, 0.7F, "infx_chicken");
        assertDimensions(entities, checked, 0.9F, 1.3F, "infx_sheep");
        assertDimensions(entities, checked, 0.9F, 0.9F, "infx_pig");
        assertDimensions(entities, checked, 1.3964844F, 1.6F, "infx_horse");
        assertDimensions(entities, checked, 0.6F, 0.7F, "infx_ocelot");
        assertDimensions(entities, checked, 0.7F, 0.8F, "infx_wolf");
        assertEquals(entities.keySet(), checked);
    }

    private static void assertBat(InfxBat.Variant variant, double health, double attack) {
        AttributeSupplier attributes = stats(InfxBat.attributes(variant));
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
        add(names, "infx_skeleton", "Skeleton", "骷髅");
        add(names, "infx_wither_skeleton", "Wither Skeleton", "凋灵骷髅");
        add(names, "infx_spider", "Spider", "蜘蛛");
        add(names, "infx_cave_spider", "Cave Spider", "洞穴蜘蛛");
        add(names, "infx_creeper", "Creeper", "苦力怕");
        add(names, "infx_zombie", "Zombie", "僵尸");
        add(names, "infx_slime", "Slime", "史莱姆");
        add(names, "infx_enderman", "Enderman", "末影人");
        add(names, "infx_squid", "Squid", "鱿鱼");
        add(names, "infx_cod", "Cod", "鳕鱼");
        add(names, "infx_salmon", "Salmon", "鲑鱼");
        add(names, "infx_pufferfish", "Pufferfish", "河豚");
        add(names, "infx_tropical_fish", "Tropical Fish", "热带鱼");
        add(names, "infx_witch", "Witch", "女巫");
        add(names, "infx_zombified_piglin", "Zombified Piglin", "僵尸猪灵");
        add(names, "infx_blaze", "Blaze", "烈焰人");
        add(names, "infx_ghast", "Ghast", "恶魂");
        add(names, "invisible_stalker", "Invisible Stalker", "影子潜伏者");
        add(names, "ghoul", "Ghoul", "食尸鬼");
        add(names, "shadow", "Shadow", "暗影");
        add(names, "wight", "Wight", "尸妖");
        add(names, "revenant", "Revenant", "亡魂");
        add(names, "longdead", "Longdead", "古尸");
        add(names, "longdead_guardian", "Longdead Guardian", "古尸守卫");
        add(names, "bone_lord", "Bone Lord", "骷髅领主");
        add(names, "ancient_bone_lord", "Ancient Bone Lord", "远古骷髅领主");
        add(names, "black_widow_spider", "Black Widow Spider", "黑寡妇蜘蛛");
        add(names, "demon_spider", "Demon Spider", "恶魔蜘蛛");
        add(names, "wood_spider", "Wood Spider", "木蜘蛛");
        add(names, "phase_spider", "Phase Spider", "相位蜘蛛");
        add(names, "infernal_creeper", "Infernal Creeper", "地狱苦力怕");
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
        add(names, "infx_bat", "Bat", "蝙蝠");
        add(names, "vampire_bat", "Vampire Bat", "吸血蝙蝠");
        add(names, "nightwing", "Nightwing", "暗影蝙蝠");
        add(names, "giant_vampire_bat", "Giant Vampire Bat", "吸血巨蝠");
        add(names, "hellhound", "Hellhound", "地狱犬");
        add(names, "dire_wolf", "Dire Wolf", "惧狼");
        add(names, "infx_cow", "Cow", "牛");
        add(names, "infx_chicken", "Chicken", "鸡");
        add(names, "infx_sheep", "Sheep", "绵羊");
        add(names, "infx_pig", "Pig", "猪");
        add(names, "infx_horse", "Horse", "马");
        add(names, "infx_ocelot", "Ocelot", "豹猫");
        add(names, "infx_wolf", "Wolf", "狼");
        return names;
    }

    private static void add(Map<String, Names> names, String path, String english, String chinese) {
        names.put(path, new Names(english, chinese));
    }

    private record Names(String english, String chinese) {}
}
