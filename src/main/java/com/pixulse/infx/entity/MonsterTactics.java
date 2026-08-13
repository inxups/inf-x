package com.pixulse.infx.entity;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Deterministic policy and runtime helpers for INFX monster coordination. */
public final class MonsterTactics {
    private static final String DIG_POS = "infx_monster_dig_pos";
    private static final String DIG_PROGRESS = "infx_monster_dig_progress";
    private static final List<InfxMaterial> GEAR_MATERIALS = List.of(
            InfxMaterial.COPPER,
            InfxMaterial.IRON,
            InfxMaterial.ANCIENT_METAL,
            InfxMaterial.MITHRIL,
            InfxMaterial.ADAMANTIUM);

    private MonsterTactics() {}

    public static long survivalDay(ServerLevel level) {
        return Math.max(1L, level.getOverworldClockTime() / 24_000L + 1L);
    }

    public static InfxMaterial maximumGearMaterial(long day) {
        if (day >= 256L) return InfxMaterial.ADAMANTIUM;
        if (day >= 128L) return InfxMaterial.MITHRIL;
        if (day >= 64L) return InfxMaterial.ANCIENT_METAL;
        if (day >= 32L) return InfxMaterial.IRON;
        return InfxMaterial.COPPER;
    }

    public static float equipmentChance(long day) {
        return Math.clamp(0.10F + day / 512.0F, 0.10F, 0.75F);
    }

    public static float enchantmentChance(long day) {
        return Math.clamp((day - 16L) / 384.0F, 0.0F, 0.65F);
    }

    public static boolean spawnerAtCap(int nearbyMatchingMobs) {
        return nearbyMatchingMobs >= 20;
    }

    /** InfX block spawners ignore torch light but do not create a mob that sunlight would burn. */
    public static boolean allowsSpawnerLightBypass(
            net.minecraft.world.entity.EntitySpawnReason spawnReason,
            boolean vanillaPlacementAllowed,
            boolean placementAllowedIgnoringLight,
            boolean burnsInDirectSunlight) {
        return spawnReason == net.minecraft.world.entity.EntitySpawnReason.SPAWNER
                && !vanillaPlacementAllowed
                && placementAllowedIgnoringLight
                && !burnsInDirectSunlight;
    }

    public static Vec3 flankOffset(int entityId, double radius) {
        double angle = Math.floorMod(entityId, 8) * Math.PI / 4.0;
        return new Vec3(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
    }

    public static void equipForWorldAge(ServerLevel level, Mob mob) {
        if (!wearsWorldAgeGear(mob)) return;
        long day = survivalDay(level);
        if (mob.getRandom().nextFloat() >= equipmentChance(day)) return;
        int maximum = GEAR_MATERIALS.indexOf(maximumGearMaterial(day));
        InfxMaterial material = GEAR_MATERIALS.get(mob.getRandom().nextInt(maximum + 1));

        EquipmentType weaponType = EquipmentType.SWORD;
        InfxMaterial weaponMaterial = material;
        if (mob instanceof AbstractSkeleton && mob.getRandom().nextBoolean()) {
            weaponType = EquipmentType.BOW;
            weaponMaterial = material.ordinal() >= InfxMaterial.MITHRIL.ordinal()
                    ? InfxMaterial.MITHRIL
                    : material.ordinal() == InfxMaterial.ANCIENT_METAL.ordinal()
                            ? InfxMaterial.ANCIENT_METAL
                            : InfxMaterial.WOOD;
        }
        equip(level, mob, EquipmentSlot.MAINHAND, weaponMaterial, weaponType, day);

        EquipmentType[] armor = {
            EquipmentType.HELMET,
            EquipmentType.CHESTPLATE,
            EquipmentType.LEGGINGS,
            EquipmentType.BOOTS
        };
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) {
            if (mob.getRandom().nextFloat() < equipmentChance(day) * 0.65F) {
                equip(level, mob, slots[i], material, armor[i], day);
            }
        }
    }

    /** InfX mobs drop naturally carried equipment at the default 8.5% chance. */
    public static void equip(
            ServerLevel level,
            Mob mob,
            EquipmentSlot slot,
            InfxMaterial material,
            EquipmentType type,
            long day) {
        ItemStack stack = InfXItems.catalog().equipment(material, type).holder().toStack();
        if (mob.getRandom().nextFloat() < enchantmentChance(day)) {
            int cost = Math.clamp(5 + (int) (day / 16L), 5, 40);
            stack = EnchantmentHelper.enchantItem(mob.getRandom(), stack, cost, level.registryAccess(), Optional.empty());
        }
        mob.setItemSlot(slot, stack);
        mob.setDropChance(slot, 0.085F);
    }

    /**
     * InfX carve-outs from world-age gear: arachnids, blazes, fire elementals and pig zombies
     * never carry scaled equipment, and the zombie/skeleton variants either spawn bare or bring
     * their own fixed InfX kit which must not be overwritten.
     */
    static boolean wearsWorldAgeGear(Mob mob) {
        if (mob instanceof InfxSpider
                || mob instanceof InfxZombifiedPiglin
                || mob instanceof InfxBlaze
                || mob instanceof FireElemental
                || mob instanceof InfxCreeper) {
            return false;
        }
        if (mob instanceof InfxZombie zombie) {
            return zombie.variant() == InfxZombie.Variant.ZOMBIE;
        }
        if (mob instanceof InfxSkeleton skeleton) {
            return skeleton.variant() == InfxSkeleton.Variant.SKELETON;
        }
        return true;
    }

    public static void cooperate(ServerLevel level, Mob mob) {
        if (mob instanceof InfxEnderman) {
            return;
        }
        var target = mob.getTarget();
        if (target == null || !target.isAlive()) return;
        if (!mob.hasLineOfSight(target) || mob.getNavigation().isDone()) {
            Vec3 flank = flankOffset(mob.getId(), 4.0);
            BlockPos destination = BlockPos.containing(target.position().add(flank));
            if (level.getBlockState(destination).isAir()
                    && level.getBlockState(destination.above()).isAir()
                    && level.getBlockState(destination.below()).isFaceSturdy(level, destination.below(), net.minecraft.core.Direction.UP)) {
                mob.getNavigation().moveTo(destination.getX() + .5, destination.getY(), destination.getZ() + .5, 1.1);
            }
        }
        if (!(mob instanceof EarthElemental)) {
            tryDig(level, mob);
        }
    }

    /**
     * InfX generic pursuit digging: only zombie variants mine through obstructing blocks;
     * every other Enemy (skeletons, spiders, creepers, ...) never receives this behavior.
     * Earth elementals keep their own dedicated dig goal instead.
     */
    public static boolean tryDig(ServerLevel level, Mob mob) {
        if (mob instanceof InfxEnderman
                || !(mob instanceof InfxZombie)
                || !level.getGameRules().get(GameRules.MOB_GRIEFING)
                || mob.getTarget() == null) {
            return stopDigging(level, mob);
        }
        var hit = level.clip(new ClipContext(
                mob.getEyePosition(),
                mob.getTarget().getEyePosition(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mob));
        if (hit.getType() != HitResult.Type.BLOCK) return stopDigging(level, mob);
        BlockPos pos = hit.getBlockPos();
        if (Vec3.atCenterOf(pos).distanceToSqr(mob.position()) > 9.0) return stopDigging(level, mob);
        var state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || state.isAir() || state.is(Blocks.BEDROCK) || level.getBlockEntity(pos) != null) {
            return stopDigging(level, mob);
        }

        ItemStack tool = mob.getMainHandItem();
        float speed = Math.max(1.0F, tool.getDestroySpeed(state));
        // InfX bare-handed diggers only clear soft blocks; stone (hardness 1.5) requires a tool.
        float maximumHardness = speed > 1.0F ? 12.0F : 1.4F;
        if (hardness > maximumHardness) return stopDigging(level, mob);
        // Dig at the same rate as a player: vanilla advances digSpeed / hardness / (30 or 100)
        // progress per tick and breaks at 1.0, i.e. 30 or 100 ticks times hardness over speed.
        // This module runs once per 20 ticks, so each call adds 20 ticks of progress.
        int divisor = speed > 1.0F ? 30 : 100;
        int required = Math.clamp(Mth.ceil(divisor * Math.max(0.25F, hardness) / speed), 10, 240);
        var data = mob.getPersistentData();
        long encoded = pos.asLong();
        int progress = data.getLong(DIG_POS).orElse(Long.MIN_VALUE) == encoded
                ? data.getInt(DIG_PROGRESS).orElse(0) + 20
                : 20;
        data.putLong(DIG_POS, encoded);
        data.putInt(DIG_PROGRESS, progress);
        level.destroyBlockProgress(mob.getId(), pos, Math.clamp(progress * 10 / required, 0, 9));
        if (progress < required) return true;
        level.destroyBlock(pos, true, mob);
        level.destroyBlockProgress(mob.getId(), pos, -1);
        data.remove(DIG_POS);
        data.remove(DIG_PROGRESS);
        return true;
    }

    /** True while a InfX monster is actively progressing through this module's block-dig task. */
    public static boolean isDigging(Mob mob) {
        return mob instanceof EarthElemental elemental
                ? elemental.isDigging()
                : mob instanceof InfxZombie && mob.getPersistentData().getInt(DIG_PROGRESS).orElse(0) > 0;
    }

    private static boolean stopDigging(ServerLevel level, Mob mob) {
        var data = mob.getPersistentData();
        long encoded = data.getLong(DIG_POS).orElse(Long.MIN_VALUE);
        if (encoded != Long.MIN_VALUE) level.destroyBlockProgress(mob.getId(), BlockPos.of(encoded), -1);
        data.remove(DIG_POS);
        data.remove(DIG_PROGRESS);
        return false;
    }
}
