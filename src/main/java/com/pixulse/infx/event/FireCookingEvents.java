package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXItems;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.TriState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * InfX-style open-fire cooking for dropped food.
 *
 * <p>Progress is applied only when an item actually receives non-lava fire damage. Raw food turns
 * into its cooked counterpart at 100 progress; cooked food reaches the same threshold and burns
 * away. Raw food also schedules InfX's anti-bulk-cooking fire-extinguish check.
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class FireCookingEvents {
    private static final String COOKING_PROGRESS = "infx_fire_cooking_progress";
    private static final float COOKING_PROGRESS_REQUIRED = 100.0F;
    private static final float PROGRESS_PER_DAMAGE = 3.0F;
    private static final float CAMPFIRE_PROGRESS_PER_TICK = 1.0F;
    private static final Identifier DOUGH_ID = InfiniteX.id("dough");
    private static final Identifier WORM_ID = InfiniteX.id("worm");
    private static final Identifier COOKED_WORM_ID = InfiniteX.id("cooked_worm");
    private static final Map<Item, CookingResult> RAW_COOKING_RESULTS = Map.of(
            Items.BEEF, new CookingResult(Items.COOKED_BEEF, 4),
            Items.PORKCHOP, new CookingResult(Items.COOKED_PORKCHOP, 3),
            Items.CHICKEN, new CookingResult(Items.COOKED_CHICKEN, 3),
            Items.MUTTON, new CookingResult(Items.COOKED_MUTTON, 2),
            Items.COD, new CookingResult(Items.COOKED_COD, 3),
            Items.SALMON, new CookingResult(Items.COOKED_SALMON, 4),
            Items.POTATO, new CookingResult(Items.BAKED_POTATO, 0));
    private static final Map<Item, Integer> COOKED_EXPERIENCE = Map.of(
            Items.COOKED_BEEF, 4,
            Items.COOKED_PORKCHOP, 3,
            Items.COOKED_CHICKEN, 3,
            Items.COOKED_MUTTON, 2,
            Items.COOKED_COD, 3,
            Items.COOKED_SALMON, 4,
            Items.BAKED_POTATO, 0,
            Items.BREAD, 0);
    private static final Map<ServerLevel, NavigableMap<Long, Set<BlockPos>>> EXTINGUISH_CHECKS =
            new WeakHashMap<>();

    private FireCookingEvents() {}

    /**
     * Applies one real InfX-style fire-damage increment and consumes vanilla item damage when the
     * stack is an open-fire food. Damageable equipment burns durability instead of the vanilla
     * item-health timer and is only destroyed once its durability runs out; fire-immune equipment
     * never reaches this handler because {@code ItemEntity#hurtServer} rejects it earlier. Lava
     * deliberately remains destructive rather than cooking food, but still burns equipment.
     */
    public static boolean handleFireDamage(ServerLevel level, ItemEntity entity, DamageSource source, float damage) {
        if (damage <= 0.0F) return false;

        ItemStack stack = entity.getItem();
        if (stack.isDamageableItem()) {
            burnEquipmentDurability(level, entity, stack, damage);
            return true;
        }
        if (!isCookingFireDamage(source)) return false;

        CookingResult result = cookingResult(stack.getItem());
        if (result != null) {
            scheduleExtinguishChecks(level, entity);
            applyCookingProgress(level, entity, stack, result, damage);
            return true;
        }
        if (isCooked(stack)) {
            applyBurningProgress(entity, damage);
            return true;
        }
        return false;
    }

    /** InfX equipment burns one durability point per fire hit (four per lava hit) instead of vanishing. */
    private static void burnEquipmentDurability(ServerLevel level, ItemEntity entity, ItemStack stack, float damage) {
        Item item = stack.getItem();
        stack.hurtAndBreak(Math.max(1, (int) damage), level, null, broken -> {});
        if (stack.isEmpty()) {
            item.onDestroyed(entity);
            entity.discard();
        }
    }

    @SubscribeEvent
    public static void tickCampfireCooking(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity entity)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        BlockPos below = entity.blockPosition().below();
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(below);
        if (!(state.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock)
                || !state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) {
            return;
        }
        ItemStack stack = entity.getItem();
        CookingResult result = cookingResult(stack.getItem());
        if (result == null) return;
        float progress = entity.getPersistentData().getFloatOr(COOKING_PROGRESS, 0.0F) + CAMPFIRE_PROGRESS_PER_TICK;
        if (progress < COOKING_PROGRESS_REQUIRED) {
            entity.getPersistentData().putFloat(COOKING_PROGRESS, progress);
        } else {
            entity.setItem(new ItemStack(result.cooked(), stack.getCount()));
            entity.getPersistentData().remove(COOKING_PROGRESS);
            if (result.experience() > 0) {
                ExperienceOrb.award(level, entity.position(), result.experience() * stack.getCount());
            }
        }
    }

    @SubscribeEvent
    public static void tickScheduledExtinguishChecks(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        NavigableMap<Long, Set<BlockPos>> checks = EXTINGUISH_CHECKS.get(level);
        if (checks == null || checks.isEmpty()) return;

        long gameTime = level.getGameTime();
        Iterator<Map.Entry<Long, Set<BlockPos>>> iterator = checks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Set<BlockPos>> entry = iterator.next();
            if (entry.getKey() > gameTime) break;
            for (BlockPos firePos : entry.getValue()) {
                tryExtinguishForRawFood(level, firePos);
            }
            iterator.remove();
        }
        if (checks.isEmpty()) EXTINGUISH_CHECKS.remove(level);
    }

    @SubscribeEvent
    public static void preventHotPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getItemEntity().isOnFire() && isCookableOrCooked(event.getItemEntity().getItem())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void igniteCookedDrops(LivingDropsEvent event) {
        if (!event.getEntity().isOnFire()) return;
        for (ItemEntity drop : event.getDrops()) {
            if (isCooked(drop.getItem())) {
                // InfX transfers 2-8 seconds of the burning victim's fire to its dropped items.
                drop.igniteForSeconds(2.0F + drop.getRandom().nextInt(7));
            }
        }
    }

    public static Item cookedResult(Item raw) {
        CookingResult result = cookingResult(raw);
        return result == null ? null : result.cooked();
    }

    public static boolean isCooked(ItemStack stack) {
        return isCooked(stack.getItem());
    }

    static boolean isCooked(Item item) {
        return COOKED_EXPERIENCE.containsKey(item)
                || COOKED_WORM_ID.equals(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isCookableOrCooked(ItemStack stack) {
        return cookingResult(stack.getItem()) != null || isCooked(stack);
    }

    static int cookingExperience(Item cooked) {
        return COOKED_EXPERIENCE.getOrDefault(cooked, 0);
    }

    static float addCookingProgress(float currentProgress, float damage) {
        return currentProgress + damage * PROGRESS_PER_DAMAGE;
    }

    static float extinguishChance(int rawFoodCount) {
        if (rawFoodCount < 2) return 0.0F;
        if (rawFoodCount >= 7) return 1.0F;
        return 0.01F * (1 << rawFoodCount);
    }

    static boolean isCookingFireDamage(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE) && !source.is(DamageTypes.LAVA);
    }

    private static CookingResult cookingResult(Item raw) {
        CookingResult vanillaResult = RAW_COOKING_RESULTS.get(raw);
        if (vanillaResult != null) return vanillaResult;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(raw);
        if (DOUGH_ID.equals(itemId)) return new CookingResult(Items.BREAD, 0);
        if (WORM_ID.equals(itemId)) return new CookingResult(InfXItems.COOKED_WORM.get(), 0);
        return null;
    }

    private static void applyCookingProgress(
            ServerLevel level, ItemEntity entity, ItemStack rawStack, CookingResult result, float damage) {
        float progress = addCookingProgress(entity.getPersistentData().getFloatOr(COOKING_PROGRESS, 0.0F), damage);
        if (progress < COOKING_PROGRESS_REQUIRED) {
            entity.getPersistentData().putFloat(COOKING_PROGRESS, progress);
            return;
        }

        entity.setItem(new ItemStack(result.cooked(), rawStack.getCount()));
        entity.getPersistentData().remove(COOKING_PROGRESS);
        if (result.experience() > 0) {
            ExperienceOrb.award(level, entity.position(), result.experience() * rawStack.getCount());
        }
    }

    private static void applyBurningProgress(ItemEntity entity, float damage) {
        float progress = addCookingProgress(entity.getPersistentData().getFloatOr(COOKING_PROGRESS, 0.0F), damage);
        if (progress >= COOKING_PROGRESS_REQUIRED) {
            entity.discard();
        } else {
            entity.getPersistentData().putFloat(COOKING_PROGRESS, progress);
        }
    }

    private static void scheduleExtinguishChecks(ServerLevel level, ItemEntity entity) {
        long dueTick = (level.getGameTime() / 10L + 1L) * 10L;
        NavigableMap<Long, Set<BlockPos>> checks = EXTINGUISH_CHECKS.computeIfAbsent(level, ignored -> new TreeMap<>());
        Set<BlockPos> positions = checks.computeIfAbsent(dueTick, ignored -> new HashSet<>());
        BlockPos itemPos = entity.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(itemPos.offset(-1, 0, -1), itemPos.offset(1, 0, 1))) {
            if (level.getBlockState(pos).getBlock() instanceof BaseFireBlock) {
                positions.add(pos.immutable());
            }
        }
    }

    private static void tryExtinguishForRawFood(ServerLevel level, BlockPos firePos) {
        if (!(level.getBlockState(firePos).getBlock() instanceof BaseFireBlock)) return;

        AABB searchBox = new AABB(
                firePos.getX() - 0.125D,
                firePos.getY(),
                firePos.getZ() - 0.125D,
                firePos.getX() + 1.125D,
                firePos.getY() + 1.0D,
                firePos.getZ() + 1.125D);
        int rawFoodCount = level.getEntitiesOfClass(
                        ItemEntity.class, searchBox, item -> cookingResult(item.getItem().getItem()) != null)
                .stream()
                .mapToInt(item -> item.getItem().getCount())
                .sum();
        float chance = extinguishChance(rawFoodCount);
        if (chance <= 0.0F || level.getRandom().nextFloat() >= chance) return;

        level.removeBlock(firePos, false);
        level.playSound(null, firePos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.7F);
    }

    private record CookingResult(Item cooked, int experience) {}
}
