package com.pixulse.infx.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.world.BoneLordSummonRegistry;
import com.pixulse.infx.world.CactusKillTracker;
import com.pixulse.infx.world.MoonPhase;
import com.pixulse.infx.world.RiverBiomes;
import com.pixulse.infx.world.SpawnGate;
import com.pixulse.infx.world.Underworld;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Registration, spawn replacement and cross-family AI hooks for INFX mobs. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class MonsterEvents {
    private static boolean sharingTarget;

    private MonsterEvents() {}

    /**
     * InfX frenzy: during blood-moon nights (and under bone-lord inspiration) monster melee
     * gains half its base attack again per frenzy source, and the two stack. Endermen are
     * explicitly exempt in InfX.
     */
    @SubscribeEvent
    public static void applyFrenzyDamage(LivingIncomingDamageEvent event) {
        if (!InfXConfig.INSTANCE.mobs.enabled.getValue()
                || !(event.getSource().getEntity() instanceof Mob attacker)
                || !(attacker instanceof Enemy)
                || attacker instanceof InfxEnderman
                || !event.getSource().isDirect()
                || !(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        boolean bloodMoon = isBloodMoonFrenzied(level);
        boolean boneLord = attacker instanceof BoneLordInspired inspired && inspired.isInspired();
        float bonus = frenzyDamageBonus(bloodMoon, boneLord);
        if (bonus == 0.0F) {
            return;
        }
        var attack = attacker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            event.setAmount(event.getAmount() + bonus * (float) attack.getBaseValue());
        }
    }

    /** MITE: a burning mob with no weapon may ignite its target on a direct melee hit. */
    @SubscribeEvent
    public static void transferFireFromBurningMob(LivingIncomingDamageEvent event) {
        if (!InfXConfig.INSTANCE.mobs.enabled.getValue()
                || !(event.getSource().getDirectEntity() instanceof Mob attacker)
                || !(attacker instanceof Enemy)
                || !attacker.isOnFire()
                || !attacker.getMainHandItem().isEmpty()
                || !event.getSource().isDirect()) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        float difficulty = victim.level().getDifficulty().getId();
        if (burningMobTransferRoll(difficulty, attacker.getRandom())) {
            victim.igniteForSeconds(2.0F * difficulty);
        }
    }

    /** MITE fire transfer roll: a burning bare-handed mob has difficulty×0.3 chance to ignite. */
    public static boolean burningMobTransferRoll(float difficulty, net.minecraft.util.RandomSource random) {
        return difficulty > 0 && random.nextFloat() < difficulty * 0.3F;
    }

    /** MITE frenzy: blood-moon and bone-lord frenzy each contribute 50% of base attack, stacking. */
    public static float frenzyDamageBonus(boolean bloodMoon, boolean boneLord) {
        return (bloodMoon ? 0.5F : 0.0F) + (boneLord ? 0.5F : 0.0F);
    }

    /** MITE frenzy predicate: a blood-moon night in the overworld with frenzy enabled. */
    public static boolean isBloodMoonFrenzied(Level level) {
        return SpawnGate.isBloodMoonFrenzied(level);
    }

    /** InfX hostile-piglin predicate: single config gate for target AI, barter blocks and kill drops. */
    public static boolean isPiglinHostilityEnabled() {
        return SpawnGate.isPiglinHostilityEnabled();
    }

    /**
     * InfX phantoms are part of the blood-moon catastrophe (and its 128-day phantom-moon echo)
     * instead of a second sleep-deprivation punishment on top of the blood-moon sleep ban.
     * ALLOW bypasses vanilla's TimeSinceRest and difficulty rolls; DENY blocks the wave.
     */
    @SubscribeEvent
    public static void controlPhantomSpawns(PlayerSpawnPhantomsEvent event) {
        Level level = event.getEntity().level();
        int decision = SpawnGate.phantomWaveCount(level);
        if (decision < 0) {
            event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
        } else if (decision > 0) {
            event.setResult(PlayerSpawnPhantomsEvent.Result.ALLOW);
            event.setPhantomsToSpawn(decision);
        }
    }

    /**
     * InfX projectile numbers: blaze small fireballs hit for a flat 2; skeleton arrows carry a
     * per-variant floor (rusted 5, longdead's ancient 9) and stay at the floor unless the bow
     * is enchanted.
     */
    @SubscribeEvent
    public static void applyProjectileDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity()
                        instanceof net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball fireball
                && fireball.getOwner() instanceof InfxBlaze) {
            event.setAmount(2.0F);
            return;
        }
        if (event.getSource().getDirectEntity()
                        instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow
                && arrow.getOwner() instanceof InfxSkeleton skeleton) {
            float floor = skeleton.variant() == InfxSkeleton.Variant.LONGDEAD
                            || skeleton.variant() == InfxSkeleton.Variant.LONGDEAD_GUARDIAN
                    ? 9.0F
                    : 5.0F;
            var bow = skeleton.getMainHandItem();
            boolean enchantedBow = !bow.isEmpty() && bow.isEnchanted();
            event.setAmount(enchantedBow ? Math.max(event.getAmount(), floor) : floor);
        }
    }

    /**
     * InfX melee retaliation: punching a blaze or fire elemental without a tool always burns the
     * hand for one point; any other monster currently fighting back has a 1-in-8 chance.
     */
    @SubscribeEvent
    public static void retaliateAgainstBareHands(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Mob victim)
                || !(victim instanceof Enemy)
                || !(victim.level() instanceof ServerLevel level)) {
            return;
        }
        var source = event.getSource();
        if (!source.isDirect() || !(source.getEntity() instanceof Player attacker)) {
            return;
        }
        var weapon = attacker.getMainHandItem();
        boolean toolLike = weapon.has(net.minecraft.core.component.DataComponents.TOOL)
                || weapon.is(net.minecraft.world.item.Items.STICK)
                || weapon.is(net.minecraft.world.item.Items.BONE);
        if (toolLike) {
            return;
        }
        boolean alwaysRetaliates = victim instanceof InfxBlaze || victim instanceof FireElemental;
        if (alwaysRetaliates || (victim.getTarget() == attacker && victim.getRandom().nextFloat() < 0.125F)) {
            attacker.hurtServer(level, level.damageSources().mobAttack(victim), 1.0F);
        }
    }

    /** InfX's conspicuous-cactus trigger, mapped to a real cactus hit in the modern damage pipeline. */
    @SubscribeEvent
    public static void armCreeperFromCactus(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof InfxCreeper creeper)
                || !(creeper.level() instanceof ServerLevel level)
                || event.getHealthDamage() <= 0.0F
                || !event.getSource().is(DamageTypes.CACTUS)
                || !creeper.getRandom().nextBoolean()) {
            return;
        }
        CactusKillTracker.contactFor(creeper, level.getGameTime())
                .filter(pos -> CactusKillTracker.get(level).countForCactus(level, pos) > 1)
                .ifPresent(pos -> creeper.armCactusFuse());
    }

    /** MITE stores the kill tally on the supporting sand block after a lethal cactus touch. */
    @SubscribeEvent
    public static void recordCactusKill(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)
                || !event.getSource().is(DamageTypes.CACTUS)) {
            return;
        }
        CactusKillTracker.contactFor(event.getEntity(), level.getGameTime())
                .ifPresent(pos -> CactusKillTracker.get(level).incrementForCactus(level, pos));
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(InfXEntityTypes.INVISIBLE_STALKER.get(), InvisibleStalker.attributes().build());
        event.put(InfXEntityTypes.GHOUL.get(), Ghoul.attributes().build());
        event.put(InfXEntityTypes.SHADOW.get(), Shadow.attributes().build());
        event.put(InfXEntityTypes.WIGHT.get(), Wight.attributes().build());
        event.put(InfXEntityTypes.REVENANT.get(), Revenant.attributes().build());

        event.put(InfXEntityTypes.INFX_SKELETON.get(), InfxSkeleton.attributes(InfxSkeleton.Variant.SKELETON).build());
        event.put(InfXEntityTypes.INFX_WITHER_SKELETON.get(), InfxWitherSkeleton.attributes().build());
        event.put(InfXEntityTypes.LONGDEAD.get(), InfxSkeleton.attributes(InfxSkeleton.Variant.LONGDEAD).build());
        event.put(
                InfXEntityTypes.LONGDEAD_GUARDIAN.get(),
                InfxSkeleton.attributes(InfxSkeleton.Variant.LONGDEAD_GUARDIAN).build());
        event.put(InfXEntityTypes.BONE_LORD.get(), InfxSkeleton.attributes(InfxSkeleton.Variant.BONE_LORD).build());
        event.put(InfXEntityTypes.ANCIENT_BONE_LORD.get(), InfxSkeleton.attributes(InfxSkeleton.Variant.ANCIENT_BONE_LORD).build());

        event.put(InfXEntityTypes.INFX_SPIDER.get(), InfxSpider.attributes(InfxSpider.Variant.SPIDER).build());
        event.put(InfXEntityTypes.INFX_CAVE_SPIDER.get(), InfxSpider.attributes(InfxSpider.Variant.CAVE_SPIDER).build());
        event.put(InfXEntityTypes.BLACK_WIDOW_SPIDER.get(), InfxSpider.attributes(InfxSpider.Variant.BLACK_WIDOW).build());
        event.put(InfXEntityTypes.DEMON_SPIDER.get(), InfxSpider.attributes(InfxSpider.Variant.DEMON).build());
        event.put(InfXEntityTypes.WOOD_SPIDER.get(), InfxSpider.attributes(InfxSpider.Variant.WOOD).build());
        event.put(InfXEntityTypes.PHASE_SPIDER.get(), InfxSpider.attributes(InfxSpider.Variant.PHASE).build());

        event.put(InfXEntityTypes.INFX_CREEPER.get(), InfxCreeper.attributes(InfxCreeper.Variant.CREEPER).build());
        event.put(InfXEntityTypes.INFERNAL_CREEPER.get(), InfxCreeper.attributes(InfxCreeper.Variant.INFERNAL).build());

        event.put(InfXEntityTypes.INFX_SLIME.get(), InfxSlime.attributes(InfxSlime.Variant.SLIME).build());
        event.put(InfXEntityTypes.JELLY.get(), InfxSlime.attributes(InfxSlime.Variant.JELLY).build());
        event.put(InfXEntityTypes.BLOB.get(), InfxSlime.attributes(InfxSlime.Variant.BLOB).build());
        event.put(InfXEntityTypes.OOZE.get(), InfxSlime.attributes(InfxSlime.Variant.OOZE).build());
        event.put(InfXEntityTypes.PUDDING.get(), InfxSlime.attributes(InfxSlime.Variant.PUDDING).build());
        event.put(InfXEntityTypes.MAGMA_CUBE.get(), InfxMagmaCube.attributes().build());

        for (var type : List.of(InfXEntityTypes.NETHERSPAWN, InfXEntityTypes.COPPERSPINE, InfXEntityTypes.HOARY_SILVERFISH)) {
            event.put(type.get(), InfxSilverfish.attributes().build());
        }

        event.put(InfXEntityTypes.INFX_BAT.get(), InfxBat.attributes(InfxBat.Variant.NORMAL).build());
        event.put(InfXEntityTypes.VAMPIRE_BAT.get(), InfxBat.attributes(InfxBat.Variant.VAMPIRE).build());
        event.put(InfXEntityTypes.NIGHTWING.get(), InfxBat.attributes(InfxBat.Variant.NIGHTWING).build());
        event.put(InfXEntityTypes.GIANT_VAMPIRE_BAT.get(), InfxBat.attributes(InfxBat.Variant.GIANT_VAMPIRE).build());
        event.put(InfXEntityTypes.HELLHOUND.get(), InfxWolf.attributes(InfxWolf.Variant.HELLHOUND).build());
        event.put(InfXEntityTypes.DIRE_WOLF.get(), InfxWolf.attributes(InfxWolf.Variant.DIRE_WOLF).build());
        event.put(InfXEntityTypes.FIRE_ELEMENTAL.get(), FireElemental.attributes().build());
        event.put(InfXEntityTypes.EARTH_ELEMENTAL.get(), EarthElemental.attributes().build());
        event.put(InfXEntityTypes.CLAY_GOLEM.get(), ClayGolem.attributes().build());

        event.put(InfXEntityTypes.INFX_ENDERMAN.get(), InfxEnderman.attributes().build());
        event.put(InfXEntityTypes.INFX_SQUID.get(), InfxSquid.attributes().build());
        event.put(InfXEntityTypes.INFX_COD.get(), InfxCod.attributes().build());
        event.put(InfXEntityTypes.INFX_SALMON.get(), InfxSalmon.attributes().build());
        event.put(InfXEntityTypes.INFX_PUFFERFISH.get(), InfxPufferfish.attributes().build());
        event.put(InfXEntityTypes.INFX_TROPICAL_FISH.get(), InfxTropicalFish.attributes().build());
        event.put(InfXEntityTypes.INFX_WITCH.get(), InfxWitch.attributes().build());
        event.put(InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.get(), InfxZombifiedPiglin.attributes().build());
        event.put(InfXEntityTypes.INFX_BLAZE.get(), InfxBlaze.attributes().build());
        event.put(InfXEntityTypes.INFX_GHAST.get(), InfxGhast.attributes().build());

        event.put(InfXEntityTypes.INFX_COW.get(), InfxCow.attributes().build());
        event.put(InfXEntityTypes.INFX_CHICKEN.get(), InfxChicken.attributes().build());
        event.put(InfXEntityTypes.INFX_SHEEP.get(), InfxSheep.attributes().build());
        event.put(InfXEntityTypes.INFX_PIG.get(), InfxPig.attributes().build());
        event.put(InfXEntityTypes.INFX_HORSE.get(), InfxHorse.attributes().build());
        event.put(InfXEntityTypes.INFX_OCELOT.get(), InfxOcelot.attributes().build());
        event.put(InfXEntityTypes.INFX_WOLF.get(), VanillaWolf.attributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                EntityType.CREEPER,
                null,
                null,
                SpawnGate::checkCreeperNightSky,
                RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(
                EntityType.SPIDER,
                null,
                null,
                SpawnGate::checkSpiderNightSky,
                RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(
                EntityType.SLIME,
                null,
                null,
                SpawnGate::checkSlimeStoneAbove,
                RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(
                EntityType.GHAST,
                null,
                null,
                SpawnGate::checkGhastSpacing,
                RegisterSpawnPlacementsEvent.Operation.AND);
        for (var type : List.of(
                InfXEntityTypes.INVISIBLE_STALKER,
                InfXEntityTypes.GHOUL,
                InfXEntityTypes.SHADOW,
                InfXEntityTypes.WIGHT,
                InfXEntityTypes.REVENANT)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnGate::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(
                InfXEntityTypes.LONGDEAD,
                InfXEntityTypes.LONGDEAD_GUARDIAN,
                InfXEntityTypes.BONE_LORD,
                InfXEntityTypes.ANCIENT_BONE_LORD)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnGate::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(
                InfXEntityTypes.INFX_SPIDER,
                InfXEntityTypes.INFX_CAVE_SPIDER,
                InfXEntityTypes.INFX_CREEPER,
                InfXEntityTypes.INFX_SLIME,
                InfXEntityTypes.INFX_ENDERMAN)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnGate::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(
                InfXEntityTypes.BLACK_WIDOW_SPIDER,
                InfXEntityTypes.DEMON_SPIDER,
                InfXEntityTypes.WOOD_SPIDER,
                InfXEntityTypes.PHASE_SPIDER)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnGate::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        event.register(InfXEntityTypes.INFERNAL_CREEPER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> pos.getY() < 40
                        && SpawnGate.checkMonsterSpawnRules(type, level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        for (var type : List.of(InfXEntityTypes.JELLY, InfXEntityTypes.BLOB, InfXEntityTypes.OOZE, InfXEntityTypes.PUDDING)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnGate::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        event.register(InfXEntityTypes.MAGMA_CUBE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> MagmaCube.checkMagmaCubeSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        for (var type : List.of(InfXEntityTypes.NETHERSPAWN, InfXEntityTypes.COPPERSPINE, InfXEntityTypes.HOARY_SILVERFISH)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        for (var type : List.of(
                InfXEntityTypes.INFX_BAT,
                InfXEntityTypes.VAMPIRE_BAT,
                InfXEntityTypes.NIGHTWING,
                InfXEntityTypes.GIANT_VAMPIRE_BAT)) {
            event.register(type.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SpawnGate::checkBatSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
        event.register(InfXEntityTypes.HELLHOUND.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnGate::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(InfXEntityTypes.DIRE_WOLF.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Wolf.checkWolfSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(InfXEntityTypes.FIRE_ELEMENTAL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(InfXEntityTypes.EARTH_ELEMENTAL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnGate::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(InfXEntityTypes.CLAY_GOLEM.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnGate::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        registerAnimalSpawnPlacement(event, InfXEntityTypes.INFX_COW.get());
        registerAnimalSpawnPlacement(event, InfXEntityTypes.INFX_CHICKEN.get());
        registerAnimalSpawnPlacement(event, InfXEntityTypes.INFX_SHEEP.get());
        registerAnimalSpawnPlacement(event, InfXEntityTypes.INFX_PIG.get());
        registerAnimalSpawnPlacement(event, InfXEntityTypes.INFX_HORSE.get());
        event.register(InfXEntityTypes.INFX_OCELOT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING,
                (type, level, reason, pos, random) -> Ocelot.checkOcelotSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(InfXEntityTypes.INFX_WOLF.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Wolf.checkWolfSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        registerWaterAnimalSpawnPlacement(event, InfXEntityTypes.INFX_COD.get());
        registerWaterAnimalSpawnPlacement(event, InfXEntityTypes.INFX_SALMON.get());
        registerWaterAnimalSpawnPlacement(event, InfXEntityTypes.INFX_PUFFERFISH.get());
        event.register(
                InfXEntityTypes.INFX_TROPICAL_FISH.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> TropicalFish.checkTropicalFishSpawnRules(
                        asEntityType(type), level, reason, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** Match vanilla passive-animal ground and light spawn restrictions. */
    private static void registerAnimalSpawnPlacement(
            RegisterSpawnPlacementsEvent event, EntityType<? extends Animal> type) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static void registerWaterAnimalSpawnPlacement(
            RegisterSpawnPlacementsEvent event, EntityType<? extends WaterAnimal> type) {
        event.register(type, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /**
     * The vanilla spawn predicates are declared against the vanilla entity type, while this mod
     * registers subclasses with their own entity types. Keep passing the actual mod type at
     * runtime, but adapt the generic signature for NeoForge versions whose vanilla declarations
     * still use an exact parent type.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> asEntityType(EntityType<?> type) {
        return (EntityType<T>) type;
    }

    @SubscribeEvent
    public static void finalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity() instanceof EarthElemental elemental
                && event.getSpawnType() != EntitySpawnReason.LOAD) {
            elemental.initializeElementalForm();
        }
        if (event.getEntity() instanceof Monster monster
                && monster.level() instanceof ServerLevel level
                && SpawnGate.isWorldSpawn(event.getSpawnType())) {
            MonsterTactics.equipForWorldAge(level, monster);
        }
        if (event.getEntity().getType() == EntityType.WITCH
                && SpawnGate.shouldCancelVanillaWitch(event.getSpawnType())) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void attractToPlayerActivity(VanillaGameEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getCause() instanceof Player player)) return;
        // Keep the broad event query for inexpensive candidate collection; the actual
        // spherical distance is checked against each mob's FOLLOW_RANGE below.
        double radius = MoonPhase.BLOOD.isActiveInOverworldAtNight(level) ? 96.0 : 48.0;
        for (Mob mob : level.getEntitiesOfClass(
                Mob.class,
                new AABB(event.getEventPosition(), event.getEventPosition()).inflate(radius),
                candidate -> participatesInGenericTargeting(candidate)
                        && candidate.isAlive()
                        && canAcquireGenericPlayerTarget(candidate, player))) {
            if (mob.hasLineOfSight(player)) {
                mob.setTarget(player);
            } else {
                mob.getNavigation().moveTo(event.getEventPosition().x, event.getEventPosition().y, event.getEventPosition().z, 1.05);
            }
        }
    }

    @SubscribeEvent
    public static void coordinateAndSeekLight(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !(mob instanceof Enemy)
                || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        // InfX idle regeneration: non-undead monsters recover 10% of max health every 1000
        // ticks; fire elementals are the explicit exception.
        if (mob.tickCount % 1000 == 999
                && !(mob instanceof FireElemental)
                && !BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(mob.getType()).is(net.minecraft.tags.EntityTypeTags.UNDEAD)
                && mob.getHealth() < mob.getMaxHealth()) {
            mob.heal(mob.getMaxHealth() * 0.1F);
        }
        LivingEntity target = mob.getTarget();
        if (target != null && !withinFollowRange(mob, target)) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            target = null;
        }
        // Endermen remain neutral until their own stare, pearl, or damage rules choose a target.
        // They also must not inherit the generic flanking and block-digging behavior below.
        if (!participatesInGenericTargeting(mob)) {
            return;
        }
        if (target != null) {
            MonsterTactics.cooperate(level, mob);
            return;
        }
        if (mob.tickCount % 20 == 0) {
            double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
            Player illuminated = level.getNearestPlayer(
                    mob.getX(), mob.getY(), mob.getZ(), range,
                    entity -> entity instanceof Player player
                            && canAcquireGenericPlayerTarget(mob, player)
                            && mob.hasLineOfSight(player)
                            && level.getBrightness(LightLayer.BLOCK, player.blockPosition()) >= 7);
            if (illuminated != null) {
                mob.setTarget(illuminated);
                return;
            }
        }
    }

    /**
     * Vanilla applies arrow gravity after movement. Restoring this amount after the tick gives
     * INFX skeleton arrows their tuned effective air gravity without altering any other arrow.
     */
    @SubscribeEvent
    public static void reduceSkeletonArrowGravity(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)
                || !(arrow.getOwner() instanceof InfxSkeleton)
                || !(arrow.level() instanceof ServerLevel)
                || arrow.isNoPhysics()
                || arrow.isInWater()) {
            return;
        }
        Vec3 velocity = arrow.getDeltaMovement();
        if (velocity.lengthSqr() > 0.0D) {
            arrow.setDeltaMovement(velocity.add(0.0D, InfxSkeleton.skeletonArrowGravityCompensation(), 0.0D));
        }
    }

    /**
     * InfX's regular block spawners skip ordinary darkness checks, including torch light. Reusing
     * the trial-spawner reason for the second predicate check preserves every non-light placement
     * condition because both reasons remain spawner reasons in vanilla.
     */
    @SubscribeEvent
    public static void allowSpawnerLight(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() != EntitySpawnReason.SPAWNER
                || event.getDefaultResult()
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        boolean allowedIgnoringModernLight = SpawnPlacements.checkSpawnRules(
                asEntityType(event.getEntityType()),
                level,
                EntitySpawnReason.TRIAL_SPAWNER,
                event.getPos(),
                event.getRandom());
        boolean burnsInDirectSunlight = BuiltInRegistries.ENTITY_TYPE
                        .wrapAsHolder(event.getEntityType())
                        .is(EntityTypeTags.BURN_IN_DAYLIGHT)
                && SpawnGate.isExposedToSunlight(level, event.getPos());
        if (SpawnGate.allowSpawnerLight(
                event.getSpawnType(),
                event.getDefaultResult(),
                allowedIgnoringModernLight,
                burnsInDirectSunlight)) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
        }
    }

    @SubscribeEvent
    public static void limitSpawnerPopulation(MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() != EntitySpawnReason.SPAWNER
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        Mob spawning = event.getEntity();
        int nearby = level.getEntitiesOfClass(
                        Mob.class,
                        spawning.getBoundingBox().inflate(16.0),
                        mob -> mob.isAlive() && SpawnGate.sameSpawnFamily(mob.getType(), spawning.getType()))
                .size();
        if (SpawnGate.limitSpawnerPopulation(nearby)) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    @SubscribeEvent
    public static void preventObservedDespawn(MobDespawnEvent event) {
        Mob mob = event.getEntity();
        if (mob instanceof Enemy && mob.level() instanceof ServerLevel level
                && SpawnGate.preventDespawn(mob, level)) {
            event.setResult(MobDespawnEvent.Result.DENY);
        }
    }

    /** Releases roster slots immediately for dead troops and for a bone lord that no longer exists. */
    @SubscribeEvent
    public static void releaseBoneLordRosterOnDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        releaseBoneLordRosterEntity(level, event.getEntity());
    }

    /** Chunk unloads preserve roster entries; only irreversible removal frees a summon slot. */
    @SubscribeEvent
    public static void releaseDestroyedBoneLordRoster(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || event.getEntity().getRemovalReason() == null
                || !event.getEntity().getRemovalReason().shouldDestroy()) {
            return;
        }
        releaseBoneLordRosterEntity(level, event.getEntity());
    }

    private static void releaseBoneLordRosterEntity(ServerLevel level, Entity entity) {
        BoneLordSummonRegistry registry = BoneLordSummonRegistry.get(level);
        registry.releaseTroop(entity.getUUID());
        if (entity instanceof InfxSkeleton skeleton && skeleton.isBoneLord()) {
            registry.releaseLord(skeleton.getUUID());
        }
    }

    @SubscribeEvent
    public static void replaceVanillaSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Mob original)) {
            return;
        }
        EntityType<? extends Mob> replacementType = SpawnGate.replacementForSpawn(level, original);
        if (replacementType == null) {
            return;
        }

        Mob replacement = replacementType.create(level, original.getSpawnType());
        if (replacement == null) {
            return;
        }
        initializeReplacement(level, original, replacement);
        event.setCanceled(true);
        level.getServer().execute(() -> {
            if (!replacement.isRemoved()) {
                level.addFreshEntityWithPassengers(replacement);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static void initializeReplacement(ServerLevel level, Mob original, Mob replacement) {
        replacement.copyPosition(original);
        replacement.setDeltaMovement(original.getDeltaMovement());
        replacement.setCustomName(original.getCustomName());
        replacement.setCustomNameVisible(original.isCustomNameVisible());
        replacement.setSilent(original.isSilent());
        replacement.setInvulnerable(original.isInvulnerable());
        if (original.isPersistenceRequired()) replacement.setPersistenceRequired();
        replacement.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(replacement.blockPosition()),
                original.getSpawnType(),
                null);
        // The FinalizeSpawnEvent fired above already ran MonsterTactics.equipForWorldAge for
        // world spawns; equipping again here would roll the gear a second time.
        replacement.setHealth(replacement.getMaxHealth());
    }

    public static boolean trySpawnFireElemental(
            ServerLevel level, BlockPos lavaPos, FluidState fluidState, RandomSource random) {
        int rarity = level.dimension() == Level.OVERWORLD ? 16 + Math.max(0, lavaPos.getY()) : 16;
        if (random.nextInt(rarity) != 0
                || level.getEntitiesOfClass(
                                FireElemental.class,
                                new AABB(lavaPos).inflate(16.0D),
                                Entity::isAlive)
                        .size()
                        >= 2
                || !fluidState.is(FluidTags.LAVA)) {
            return false;
        }

        boolean canSpawn = false;
        if (level.dimension() == Level.NETHER
                && !level.getFluidState(lavaPos.below()).is(FluidTags.LAVA)
                && level.getBlockState(lavaPos.above()).isAir()
                && random.nextInt(4) == 0) {
            if (!fluidState.isSource()) {
                canSpawn = playerNear(level, lavaPos, 64.0D);
            } else if (random.nextInt(4) == 0) {
                canSpawn = playerNear(level, lavaPos, 16.0D);
            }
        }
        if (!canSpawn
                && random.nextInt(16) == 0
                && level.getFluidState(lavaPos.above()).isSource()
                && level.getFluidState(lavaPos.above()).is(FluidTags.LAVA)
                && level.getBlockState(lavaPos.above(2)).isAir()
                && level.getBlockState(lavaPos.above(3)).isAir()) {
            canSpawn = playerNear(level, lavaPos, 16.0D);
        }
        if (!canSpawn) return false;

        FireElemental elemental = InfXEntityTypes.FIRE_ELEMENTAL.get().create(level, EntitySpawnReason.EVENT);
        if (elemental == null) return false;
        elemental.snapTo(lavaPos.getX() + 0.5D, lavaPos.getY() + 0.1D, lavaPos.getZ() + 0.5D, 0.0F, 0.0F);
        elemental.finalizeSpawn(
                level, level.getCurrentDifficultyAt(lavaPos), EntitySpawnReason.EVENT, null);
        return level.noCollision(elemental) && level.addFreshEntity(elemental);
    }

    private static boolean playerNear(ServerLevel level, BlockPos pos, double radius) {
        return level.getNearestPlayer(
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        radius,
                        false)
                != null;
    }

    @SubscribeEvent
    public static void leadRangedProjectile(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)
                || !(event.getEntity() instanceof Projectile projectile)
                || !(projectile.getOwner() instanceof Mob shooter)
                || !(shooter instanceof InfxWitch || shooter instanceof InfxBlaze || shooter instanceof InfxGhast)) {
            return;
        }
        var target = shooter.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        Vec3 velocity = projectile.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 0.01) {
            return;
        }
        double flightTicks = Math.min(40.0, projectile.distanceTo(target) / speed);
        Vec3 targetVelocity = target.getDeltaMovement();
        Vec3 currentAim = target.getEyePosition().subtract(projectile.position());
        if (currentAim.lengthSqr() < 0.01 || targetVelocity.horizontalDistanceSqr() < 0.0001) {
            return;
        }
        Vec3 predictedAim = currentAim.add(
                targetVelocity.x * flightTicks,
                targetVelocity.y * Math.min(10.0, flightTicks),
                targetVelocity.z * flightTicks);
        double ballisticLift = velocity.y - currentAim.normalize().y * speed;
        Vec3 predictedVelocity = predictedAim.normalize().scale(speed);
        projectile.setDeltaMovement(
                predictedVelocity.x, predictedVelocity.y + ballisticLift, predictedVelocity.z);
    }

    public static EntityType<? extends Mob> replacementFor(EntityType<?> original) {
        return SpawnGate.replacementFor(original);
    }

    @SubscribeEvent
    public static void applyWitchMagicDefense(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof InfxWitch witch)
                || event.getSource().getEntity() == witch
                || !event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            return;
        }
        event.addReductionModifier(
                net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.INNATE_RESISTANCE,
                (container, vanillaReduction) -> InfxWitch.magicDefenseReduction(
                        event.getSource(), container.getNewDamage()));
    }

    @SubscribeEvent
    public static void shareTarget(LivingChangeTargetEvent event) {
        if (sharingTarget
                || !(event.getEntity() instanceof Mob mob)
                || !participatesInGenericTargeting(mob)
                || !(event.getNewAboutToBeSetTarget() instanceof Player player)
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        sharingTarget = true;
        try {
            propagateTarget(level, event.getEntity(), player);
        } finally {
            sharingTarget = false;
        }
    }

    public static int propagateTarget(ServerLevel level, LivingEntity source, Player player) {
        if (!(source instanceof Mob sourceMob)
                || !participatesInGenericTargeting(sourceMob)
                || !withinFollowRange(sourceMob, player)) {
            return 0;
        }
        int shared = 0;
        for (Mob nearby : level.getEntitiesOfClass(
                Mob.class,
                source.getBoundingBox().inflate(16.0),
                mob -> mob != source
                        && source.distanceToSqr(mob) <= 16.0 * 16.0
                        && participatesInGenericTargeting(mob)
                        && mob.getTarget() == null
                        && canAcquireGenericPlayerTarget(mob, player))) {
            nearby.setTarget(player);
            if (nearby.getTarget() == player) shared++;
        }
        return shared;
    }

    static boolean withinFollowRange(Mob mob, LivingEntity target) {
        return withinFollowRange(mob.distanceToSqr(target), mob.getAttributeValue(Attributes.FOLLOW_RANGE));
    }

    static boolean withinFollowRange(double distanceSqr, double followRange) {
        return followRange > 0.0 && distanceSqr <= followRange * followRange;
    }

    private static boolean canAcquireGenericPlayerTarget(Mob mob, Player player) {
        if (!mob.canAttack(player) || !withinFollowRange(mob, player)) {
            return false;
        }
        if (mob instanceof InfxSilverfish && mob.distanceToSqr(player) > 8.0 * 8.0) {
            return false;
        }
        if ((mob instanceof Slime || mob instanceof Ghast) && Math.abs(player.getY() - mob.getY()) > 4.0) {
            return false;
        }
        // The base spider's vanilla target goal is disabled by bright surroundings.
        return !(mob instanceof InfxSpider spider
                && spider.variant() == InfxSpider.Variant.SPIDER
                && spider.getLightLevelDependentMagicValue() >= 0.5F);
    }

    static boolean participatesInGenericTargeting(Mob mob) {
        // Pig zombies own their InfX 6/24-block player awareness and must not receive the
        // broad player-noise, illuminated-player, or cross-family target propagation rules.
        // Dire wolves share the Enemy flag with hellhounds but are tameable near-neutral
        // animals: the generic rules would make a sitting tamed dire wolf target lit players
        // and path toward the brightest block or a flank point via the cooperate hook,
        // bypassing the vanilla sit goal and following the player anyway.
        return mob instanceof Enemy
                && !(mob instanceof InfxEnderman || mob instanceof InfxZombifiedPiglin)
                && !(mob instanceof InfxWolf wolf && wolf.variant() == InfxWolf.Variant.DIRE_WOLF);
    }

    @SubscribeEvent
    public static void amplifyInfernalCreeperExplosion(ExplosionEvent.Start event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof InfxCreeper creeper)
                || creeper.variant() != InfxCreeper.Variant.INFERNAL
                || creeper.isAmplifyingExplosion()) {
            return;
        }
        event.setCanceled(true);
        creeper.setAmplifyingExplosion(true);
        try {
            float radius = creeper.isPowered() ? 12.0F : 6.0F;
            // InfX infernal creeper explosions are always flaming.
            event.getLevel().explode(
                    creeper,
                    creeper.getX(),
                    creeper.getY(),
                    creeper.getZ(),
                    radius,
                    true,
                    net.minecraft.world.level.Level.ExplosionInteraction.MOB);
        } finally {
            creeper.setAmplifyingExplosion(false);
        }
    }

    /** Ordinary INFX creepers cannot crack stone; infernal creepers retain normal blast terrain damage. */
    @SubscribeEvent
    public static void limitCreeperTerrainDamage(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof InfxCreeper creeper)) {
            return;
        }
        event.getAffectedBlocks().removeIf(pos -> {
            float hardness = event.getLevel().getBlockState(pos).getDestroySpeed(event.getLevel(), pos);
            return isCreeperTerrainProtected(creeper.variant(), hardness);
        });
    }

    static boolean isCreeperTerrainProtected(InfxCreeper.Variant variant, float hardness) {
        return hardness < 0.0F || (variant != InfxCreeper.Variant.INFERNAL && hardness >= 1.5F);
    }

    /** InfX netherspawn blasts leave their native netherrack and gold/quartz ore veins intact. */
    @SubscribeEvent
    public static void protectNetherspawnTerrain(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion().getDirectSourceEntity() instanceof InfxSilverfish silverfish)
                || silverfish.variant() != InfxSilverfish.Variant.NETHERSPAWN) {
            return;
        }
        event.getAffectedBlocks().removeIf(
                pos -> InfxSilverfish.isNetherspawnExplosionProtected(event.getLevel().getBlockState(pos)));
    }

    @EventBusSubscriber(modid = InfiniteX.MOD_ID)
    private static final class ModEvents {
        @SubscribeEvent
        public static void createAttributes(EntityAttributeCreationEvent event) {
            MonsterEvents.createAttributes(event);
        }

        @SubscribeEvent
        public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            MonsterEvents.registerSpawnPlacements(event);
        }
    }
}
