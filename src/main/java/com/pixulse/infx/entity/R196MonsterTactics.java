package com.pixulse.infx.entity;

import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Deterministic policy and runtime helpers for R196 monster coordination. */
public final class R196MonsterTactics {
    private static final String DIG_POS = "infx_monster_dig_pos";
    private static final String DIG_PROGRESS = "infx_monster_dig_progress";
    private static final List<R196Material> GEAR_MATERIALS = List.of(
            R196Material.COPPER,
            R196Material.IRON,
            R196Material.ANCIENT_METAL,
            R196Material.MITHRIL,
            R196Material.ADAMANTIUM);

    private R196MonsterTactics() {}

    public static long survivalDay(ServerLevel level) {
        return Math.max(1L, level.getOverworldClockTime() / 24_000L + 1L);
    }

    public static R196Material maximumGearMaterial(long day) {
        if (day >= 256L) return R196Material.ADAMANTIUM;
        if (day >= 128L) return R196Material.MITHRIL;
        if (day >= 64L) return R196Material.ANCIENT_METAL;
        if (day >= 32L) return R196Material.IRON;
        return R196Material.COPPER;
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

    public static Vec3 flankOffset(int entityId, double radius) {
        double angle = Math.floorMod(entityId, 8) * Math.PI / 4.0;
        return new Vec3(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
    }

    public static void equipForWorldAge(ServerLevel level, Mob mob) {
        long day = survivalDay(level);
        if (mob.getRandom().nextFloat() >= equipmentChance(day)) return;
        int maximum = GEAR_MATERIALS.indexOf(maximumGearMaterial(day));
        R196Material material = GEAR_MATERIALS.get(mob.getRandom().nextInt(maximum + 1));

        R196EquipmentType weaponType = R196EquipmentType.SWORD;
        R196Material weaponMaterial = material;
        if (mob instanceof AbstractSkeleton && mob.getRandom().nextBoolean()) {
            weaponType = R196EquipmentType.BOW;
            weaponMaterial = material.ordinal() >= R196Material.MITHRIL.ordinal()
                    ? R196Material.MITHRIL
                    : material.ordinal() >= R196Material.ANCIENT_METAL.ordinal()
                            ? R196Material.ANCIENT_METAL
                            : R196Material.WOOD;
        }
        equip(level, mob, EquipmentSlot.MAINHAND, weaponMaterial, weaponType, day);

        R196EquipmentType[] armor = {
            R196EquipmentType.HELMET,
            R196EquipmentType.CHESTPLATE,
            R196EquipmentType.LEGGINGS,
            R196EquipmentType.BOOTS
        };
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) {
            if (mob.getRandom().nextFloat() < equipmentChance(day) * 0.65F) {
                equip(level, mob, slots[i], material, armor[i], day);
            }
        }
    }

    private static void equip(
            ServerLevel level,
            Mob mob,
            EquipmentSlot slot,
            R196Material material,
            R196EquipmentType type,
            long day) {
        ItemStack stack = ModItems.catalog().equipment(material, type).holder().toStack();
        if (mob.getRandom().nextFloat() < enchantmentChance(day)) {
            int cost = Math.clamp(5 + (int) (day / 16L), 5, 40);
            stack = EnchantmentHelper.enchantItem(mob.getRandom(), stack, cost, level.registryAccess(), Optional.empty());
        }
        mob.setItemSlot(slot, stack);
        mob.setDropChance(slot, 0.05F);
    }

    public static void cooperate(ServerLevel level, Mob mob) {
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
        tryDig(level, mob);
    }

    public static boolean tryDig(ServerLevel level, Mob mob) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING) || mob.getTarget() == null) return false;
        var hit = level.clip(new ClipContext(
                mob.getEyePosition(),
                mob.getTarget().getEyePosition(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mob));
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos pos = hit.getBlockPos();
        if (Vec3.atCenterOf(pos).distanceToSqr(mob.position()) > 9.0) return false;
        var state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || state.isAir() || state.is(Blocks.BEDROCK) || level.getBlockEntity(pos) != null) return false;

        ItemStack tool = mob.getMainHandItem();
        float speed = Math.max(1.0F, tool.getDestroySpeed(state));
        float maximumHardness = speed > 1.0F ? 12.0F : 2.0F;
        if (hardness > maximumHardness) return false;
        int required = Math.clamp(Mth.ceil(40.0F * Math.max(0.25F, hardness) / speed), 10, 240);
        var data = mob.getPersistentData();
        long encoded = pos.asLong();
        int progress = data.getLong(DIG_POS).orElse(Long.MIN_VALUE) == encoded
                ? data.getInt(DIG_PROGRESS).orElse(0) + 10
                : 10;
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
}
