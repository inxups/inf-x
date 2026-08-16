package com.pixulse.infx.entity;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.world.MoonPhase;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Deterministic policy and runtime helpers for INFX monster coordination. */
public final class MonsterTactics {
    /** Smart-flag persistent-data key shared with {@link ZombieEvents}. */
    public static final String SMART_KEY = "infx.is_smart";
    /** Dig state-machine persistent-data keys. */
    public static final String DIG_POS = "infx_monster_dig_pos";
    public static final String DIG_PROGRESS = "infx_monster_dig_progress";
    public static final String DIG_NEXT_HIT = "infx_monster_dig_next_hit";
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

    /**
     * Effective difficulty tension for a spawn point: MITE's chunk-residency tension, or a
     * day-based proxy when the tension ramp is disabled.
     */
    public static float difficultyTension(ServerLevel level, BlockPos pos) {
        if (com.pixulse.infx.world.Tension.enabled()) {
            return com.pixulse.infx.world.Tension.forBlock(level, pos);
        }
        return Math.min(1.0F, survivalDay(level) / 256.0F);
    }

    public static InfxMaterial maximumGearMaterial(float tension) {
        if (tension >= 0.8F) return InfxMaterial.ADAMANTIUM;
        if (tension >= 0.6F) return InfxMaterial.MITHRIL;
        if (tension >= 0.4F) return InfxMaterial.ANCIENT_METAL;
        if (tension >= 0.2F) return InfxMaterial.IRON;
        return InfxMaterial.COPPER;
    }

    /** MITE: 15% × tension chance to spawn wearing gear. */
    public static float equipmentChance(float tension) {
        return 0.15F * tension;
    }

    /** MITE: 10% × tension chance to enchant carried gear. */
    public static float enchantmentChance(float tension) {
        return 0.10F * tension;
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
        float tension = difficultyTension(level, mob.blockPosition());
        if (mob.getRandom().nextFloat() >= equipmentChance(tension)) return;
        int maximum = GEAR_MATERIALS.indexOf(maximumGearMaterial(tension));
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
        equip(level, mob, EquipmentSlot.MAINHAND, weaponMaterial, weaponType, tension);

        EquipmentType[] armor = {
            EquipmentType.HELMET,
            EquipmentType.CHESTPLATE,
            EquipmentType.LEGGINGS,
            EquipmentType.BOOTS
        };
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) {
            if (mob.getRandom().nextFloat() < equipmentChance(tension) * 0.65F) {
                equip(level, mob, slots[i], material, armor[i], tension);
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
            float tension) {
        ItemStack stack = InfXItems.catalog().equipment(material, type).holder().toStack();
        if (mob.getRandom().nextFloat() < enchantmentChance(tension)) {
            int cost = 5 + (int) (tension * mob.getRandom().nextInt(18));
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
        if (mob instanceof InfxZombieBase) {
            // InfX MITE zombie mobs arm themselves (the revenant) or spawn bare; the vanilla
            // zombie is the only zombie-family member that receives world-age gear.
            return false;
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
        if (mob.tickCount % 20 == 0 && (!mob.hasLineOfSight(target) || mob.getNavigation().isDone())) {
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

    /** Only the zombie family digs through obstructing blocks; elementals keep their own goal. */
    private static boolean isZombieFamilyDigger(Mob mob) {
        return mob instanceof Zombie
                && !(mob instanceof Shadow || mob instanceof Wight || mob instanceof InvisibleStalker);
    }

    /** MITE {@code isDiggingEnabled}: sword/short-stick/scythe stops digging; smart or frenzied digs bare-handed. */
    public static boolean diggingEnabled(Zombie zombie, ServerLevel level) {
        if (preventsDigging(zombie.getMainHandItem())) {
            return false;
        }
        boolean smart = zombie instanceof InfxZombieBase base && base.digsBareHanded()
                || zombie.getPersistentData().getBooleanOr(SMART_KEY, false);
        boolean frenzied = MoonPhase.BLOOD.isActiveInOverworldAtNight(level);
        return smart || frenzied || holdsDigTool(zombie.getMainHandItem());
    }

    private static boolean preventsDigging(ItemStack held) {
        var equipment = InfXItems.catalog().equipment(held);
        if (equipment == null) {
            return false;
        }
        return switch (equipment.key().type()) {
            case SWORD, DAGGER, KNIFE, SCYTHE, CUDGEL, CLUB -> true;
            default -> false;
        };
    }

    /** MITE {@code ItemTool}: pickaxes, shovels, axes, hoes and war hammers dig blocks. */
    private static boolean holdsDigTool(ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }
        var equipment = InfXItems.catalog().equipment(held);
        if (equipment != null) {
            return switch (equipment.key().type()) {
                case PICKAXE, SHOVEL, HATCHET, AXE, HOE, MATTOCK, BATTLE_AXE, WAR_HAMMER -> true;
                default -> false;
            };
        }
        return held.is(ItemTags.PICKAXES) || held.is(ItemTags.SHOVELS) || held.is(ItemTags.AXES) || held.is(ItemTags.HOES);
    }

    private static boolean holdsShovel(ItemStack held) {
        var equipment = InfXItems.catalog().equipment(held);
        if (equipment != null) {
            return equipment.key().type() == EquipmentType.SHOVEL || equipment.key().type() == EquipmentType.MATTOCK;
        }
        return held.is(ItemTags.SHOVELS);
    }

    /**
     * MITE {@code canDestroyBlock}: soft blocks are always diggable; tool-required blocks need an
     * effective tool, or frenzy for blocks a stone-tier digger could clear (cobble and below).
     */
    public static boolean canDestroyBlock(Zombie zombie, ServerLevel level, BlockPos pos, BlockState state) {
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || state.isAir() || state.is(Blocks.BEDROCK) || level.getBlockEntity(pos) != null) {
            return false;
        }
        if (state.getFluidState().is(FluidTags.LAVA) || state.getFluidState().is(FluidTags.WATER)) {
            return false;
        }
        if (state.is(Blocks.CACTUS)
                && BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(zombie.getType()).is(EntityTypeTags.UNDEAD)) {
            return false;
        }
        ItemStack tool = zombie.getMainHandItem();
        if (pos.getY() < zombie.getBlockY() && !holdsShovel(tool)) {
            return false;
        }
        if (isSoftBlock(state)) {
            return true;
        }
        if (holdsDigTool(tool)) {
            return true;
        }
        boolean frenzied = MoonPhase.BLOOD.isActiveInOverworldAtNight(level);
        return frenzied && hardness <= 2.0F && !state.requiresCorrectToolForDrops();
    }

    /** MITE soft-block whitelist that any digger clears without a tool. */
    private static boolean isSoftBlock(BlockState state) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.SAND)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.CLAY)
                || state.is(net.minecraft.tags.BlockTags.LEAVES)
                || state.is(Blocks.SPONGE)
                || state.is(Blocks.PUMPKIN)
                || state.is(Blocks.MELON)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.HAY_BLOCK)
                || state.is(Blocks.GLASS)
                || state.is(net.minecraft.tags.BlockTags.WOOL);
    }

    /** MITE refuses a second digger on the same block within 4 blocks. */
    private static boolean digClaimedByAnother(ServerLevel level, Zombie digger, BlockPos pos) {
        return level.getEntitiesOfClass(
                        Zombie.class,
                        digger.getBoundingBox().inflate(4.0),
                        other -> other != digger
                                && other.getPersistentData().getLong(DIG_POS).orElse(Long.MIN_VALUE) == pos.asLong())
                .stream()
                .findAny()
                .isPresent();
    }

    /** MITE dig rate: 300 × hardness ticks per hit, 10 hits to break; frenzy halves, tools speed up. */
    public static int cooloffForBlock(Zombie zombie, ServerLevel level, BlockState state) {
        float hardness = Math.max(0.25F, state.getDestroySpeed(level, zombie.blockPosition()));
        float cooloff = 300.0F * hardness;
        if (MoonPhase.BLOOD.isActiveInOverworldAtNight(level)) {
            cooloff /= 2.0F;
        }
        if (zombie instanceof Ghoul) {
            cooloff /= 2.0F;
        }
        ItemStack tool = zombie.getMainHandItem();
        if (holdsDigTool(tool)) {
            cooloff /= 1.0F + Math.max(0.0F, tool.getDestroySpeed(state)) * 0.5F;
        }
        return Math.max(1, (int) cooloff);
    }

    /**
     * InfX zombie pursuit digging, rewritten to the MITE cadence: a 1-in-20 per-tick start roll,
     * then a per-tick cooloff state machine that cracks the block after ten hits.
     */
    public static boolean tryDig(ServerLevel level, Mob mob) {
        if (mob instanceof InfxEnderman
                || !isZombieFamilyDigger(mob)
                || !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return stopDigging(level, mob);
        }
        Zombie zombie = (Zombie) mob;
        var target = zombie.getTarget();
        if (target == null || !target.isAlive() || !diggingEnabled(zombie, level)) {
            return stopDigging(level, mob);
        }
        double distanceSqr = zombie.distanceToSqr(target);
        if (distanceSqr > 16.0 * 16.0 || zombie.isWithinMeleeAttackRange(target)) {
            return stopDigging(level, mob);
        }

        var data = zombie.getPersistentData();
        long encoded = data.getLong(DIG_POS).orElse(Long.MIN_VALUE);
        BlockPos pos = encoded == Long.MIN_VALUE ? null : BlockPos.of(encoded);
        if (pos == null) {
            if (zombie.getRandom().nextInt(20) != 0) {
                return false;
            }
            var hit = level.clip(new ClipContext(
                    zombie.getEyePosition(),
                    target.getEyePosition(),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    zombie));
            if (hit.getType() != HitResult.Type.BLOCK) return false;
            pos = hit.getBlockPos();
            if (Vec3.atCenterOf(pos).distanceToSqr(zombie.position()) > 9.0) return false;
            if (digClaimedByAnother(level, zombie, pos)) return false;
            data.putLong(DIG_POS, pos.asLong());
            data.putInt(DIG_PROGRESS, 0);
        }
        BlockState state = level.getBlockState(pos);
        if (!canDestroyBlock(zombie, level, pos, state)) {
            return stopDigging(level, zombie);
        }
        int progress = data.getInt(DIG_PROGRESS).orElse(0);
        if (zombie.tickCount >= data.getInt(DIG_NEXT_HIT).orElse(0)) {
            progress++;
            data.putInt(DIG_PROGRESS, progress);
            data.putInt(DIG_NEXT_HIT, zombie.tickCount + cooloffForBlock(zombie, level, state));
            level.destroyBlockProgress(zombie.getId(), pos, Math.clamp(progress - 1, 0, 9));
            if (progress >= 10) {
                level.destroyBlock(pos, true, zombie);
                level.destroyBlockProgress(zombie.getId(), pos, -1);
                data.remove(DIG_POS);
                data.remove(DIG_PROGRESS);
                data.remove(DIG_NEXT_HIT);
                return true;
            }
        }
        return true;
    }

    /** True while a InfX monster is actively progressing through this module's block-dig task. */
    public static boolean isDigging(Mob mob) {
        return mob instanceof EarthElemental elemental
                ? elemental.isDigging()
                : isZombieFamilyDigger(mob)
                        && mob.getPersistentData().getLong(DIG_POS).orElse(Long.MIN_VALUE) != Long.MIN_VALUE;
    }

    private static boolean stopDigging(ServerLevel level, Mob mob) {
        var data = mob.getPersistentData();
        long encoded = data.getLong(DIG_POS).orElse(Long.MIN_VALUE);
        if (encoded != Long.MIN_VALUE) level.destroyBlockProgress(mob.getId(), BlockPos.of(encoded), -1);
        data.remove(DIG_POS);
        data.remove(DIG_PROGRESS);
        data.remove(DIG_NEXT_HIT);
        return false;
    }
}
