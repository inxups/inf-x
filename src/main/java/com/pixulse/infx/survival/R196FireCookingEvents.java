package com.pixulse.infx.survival;

import com.pixulse.infx.registry.ModItems;
import java.util.Map;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** R196 open-fire cooking: raw food cooks, cooked food later burns away. */
public final class R196FireCookingEvents {
    private static final String COOK_AT = "infx_fire_cook_at";
    private static final String BURN_AT = "infx_fire_burn_at";
    private static final String COOKING_XP = "infx_fire_cooking_xp";
    private static final int MAX_STACKS_PER_FIRE = 16;
    private static final Map<Item, Item> COOKED = Map.ofEntries(
            Map.entry(Items.BEEF, Items.COOKED_BEEF),
            Map.entry(Items.PORKCHOP, Items.COOKED_PORKCHOP),
            Map.entry(Items.CHICKEN, Items.COOKED_CHICKEN),
            Map.entry(Items.MUTTON, Items.COOKED_MUTTON),
            Map.entry(Items.RABBIT, Items.COOKED_RABBIT),
            Map.entry(Items.COD, Items.COOKED_COD),
            Map.entry(Items.SALMON, Items.COOKED_SALMON));

    private R196FireCookingEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196FireCookingEvents::tickItem);
        gameBus.addListener(R196FireCookingEvents::preventHotPickup);
        gameBus.addListener(R196FireCookingEvents::awardCookingExperience);
        gameBus.addListener(R196FireCookingEvents::preserveCookingFood);
        gameBus.addListener(R196FireCookingEvents::igniteCookedDrops);
    }

    private static void tickItem(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity entity) || entity.level().isClientSide()) return;
        ItemStack stack = entity.getItem();
        if (!isCookableOrCooked(stack)) return;
        var data = entity.getPersistentData();
        if (!entity.isOnFire()) {
            data.remove(COOK_AT);
            data.remove(BURN_AT);
            return;
        }
        if (entity.tickCount % 20 == 0 && overcrowded(entity)) {
            extinguishSupportingFire(entity);
            return;
        }
        Item cooked = cookedResult(stack.getItem());
        if (cooked != null) {
            int cookAt = data.getInt(COOK_AT).orElse(-1);
            if (cookAt < 0) {
                data.putInt(COOK_AT, entity.tickCount + 180 + entity.getRandom().nextInt(61));
            } else if (entity.tickCount >= cookAt) {
                int count = stack.getCount();
                entity.setItem(new ItemStack(cooked, count));
                data.remove(COOK_AT);
                data.putInt(BURN_AT, entity.tickCount + 120 + entity.getRandom().nextInt(121));
                data.putInt(COOKING_XP, cookingExperience(cooked) * count);
            }
            return;
        }
        int burnAt = data.getInt(BURN_AT).orElse(-1);
        if (burnAt < 0) {
            data.putInt(BURN_AT, entity.tickCount + 120 + entity.getRandom().nextInt(121));
        } else if (entity.tickCount >= burnAt) {
            entity.discard();
        }
    }

    private static void preventHotPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getItemEntity().isOnFire() && isCookableOrCooked(event.getItemEntity().getItem())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    private static void awardCookingExperience(ItemEntityPickupEvent.Post event) {
        int experience = event.getItemEntity().getPersistentData().getInt(COOKING_XP).orElse(0);
        if (experience > 0) {
            event.getPlayer().giveExperiencePoints(experience);
            event.getItemEntity().getPersistentData().remove(COOKING_XP);
        }
    }

    private static void preserveCookingFood(EntityInvulnerabilityCheckEvent event) {
        if (event.getEntity() instanceof ItemEntity item
                && isCookableOrCooked(item.getItem())
                && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setInvulnerable(true);
        }
    }

    private static void igniteCookedDrops(LivingDropsEvent event) {
        if (!event.getSource().is(DamageTypeTags.IS_FIRE)) return;
        for (ItemEntity drop : event.getDrops()) {
            if (isCooked(drop.getItem())) {
                drop.igniteForSeconds(12.0F);
                drop.getPersistentData().putInt(BURN_AT, drop.tickCount + 120 + drop.getRandom().nextInt(121));
            }
        }
    }

    private static boolean overcrowded(ItemEntity entity) {
        return entity.level().getEntitiesOfClass(
                        ItemEntity.class,
                        entity.getBoundingBox().inflate(1.5D),
                        item -> item.isOnFire() && isCookableOrCooked(item.getItem()))
                .size() > MAX_STACKS_PER_FIRE;
    }

    private static void extinguishSupportingFire(ItemEntity entity) {
        for (var pos : net.minecraft.core.BlockPos.betweenClosed(
                entity.blockPosition().offset(-1, -1, -1), entity.blockPosition().offset(1, 0, 1))) {
            if (entity.level().getBlockState(pos).getBlock() instanceof BaseFireBlock) {
                entity.level().removeBlock(pos, false);
                break;
            }
        }
    }

    public static Item cookedResult(Item raw) {
        if (raw == ModItems.WORM.get()) return ModItems.COOKED_WORM.get();
        return COOKED.get(raw);
    }

    public static boolean isCooked(ItemStack stack) {
        return stack.is(ModItems.COOKED_WORM.get()) || COOKED.containsValue(stack.getItem());
    }

    public static boolean isCookableOrCooked(ItemStack stack) {
        return cookedResult(stack.getItem()) != null || isCooked(stack);
    }

    static int cookingExperience(Item cooked) {
        if (cooked == Items.COOKED_BEEF || cooked == Items.COOKED_SALMON) return 4;
        if (cooked == Items.COOKED_PORKCHOP || cooked == Items.COOKED_CHICKEN) return 3;
        if (cooked == Items.COOKED_COD || cooked == Items.COOKED_RABBIT) return 2;
        return 1;
    }
}
