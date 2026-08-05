package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.AttackRange;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** MITE INFX's ordinary stick and bone item behavior. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class StickBoneItems {
    static final int STICK_STACK_LIMIT = 32;
    static final int BONE_STACK_LIMIT = 16;
    private static final float INFX_MELEE_REACH = 2.0F;
    private static final float INFX_CREATIVE_MELEE_REACH = 5.0F;
    private static final int STICK_BREAK_DENOMINATOR = 50;
    private static final int BONE_BREAK_DENOMINATOR = 100;

    private StickBoneItems() {}

    /** Registers the vanilla default-component patch on the mod event bus. */
    public static void register(IEventBus modBus) {
        modBus.addListener(StickBoneItems::modifyDefaultComponents);
    }

    /**
     * MITE {@code Item.stick}/{@code Item.bone}: both extend only melee reach by 0.5 blocks.
     * They do not extend block reach or entity-interaction reach.
     */
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(Items.STICK, (components, context, item) -> {
            int currentLimit = components.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
            components.set(DataComponents.MAX_STACK_SIZE, stackLimit(item, currentLimit));
            components.set(DataComponents.ATTACK_RANGE, meleeAttackRange());
        });
        event.modify(Items.BONE, (components, context, item) -> {
            int currentLimit = components.getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
            components.set(DataComponents.MAX_STACK_SIZE, stackLimit(item, currentLimit));
            components.set(DataComponents.ATTACK_RANGE, meleeAttackRange());
        });
    }

    /**
     * The base Item#hitEntity implementation consumes one held stick in 1/50 successful melee
     * hits, or one bone in 1/100.  Process this after normal damage listeners so a breaking final
     * stack cannot make the same strike count as an empty-hand attack.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void breakOnSuccessfulMeleeHit(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() <= 0.0F
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK)
                || player.hasInfiniteMaterials()) {
            return;
        }

        ItemStack held = player.getMainHandItem();
        int denominator = breakDenominator(held.getItem());
        if (denominator == 0 || player.getRandom().nextInt(denominator) != 0) {
            return;
        }

        Item brokenItem = held.getItem();
        ItemStack beforeBreak = held.copy();
        held.shrink(1);
        player.awardStat(Stats.ITEM_USED.get(brokenItem));
        if (held.isEmpty()) {
            EventHooks.onPlayerDestroyItem(player, beforeBreak, InteractionHand.MAIN_HAND);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        player.onEquippedItemBroken(brokenItem, EquipmentSlot.MAINHAND);
    }

    static int stackLimit(Item item, int currentLimit) {
        if (item == Items.STICK) return STICK_STACK_LIMIT;
        if (item == Items.BONE) return BONE_STACK_LIMIT;
        return currentLimit;
    }

    static AttackRange meleeAttackRange() {
        return new AttackRange(
                0.0F,
                INFX_MELEE_REACH,
                0.0F,
                INFX_CREATIVE_MELEE_REACH,
                0.0F,
                1.0F);
    }

    static int breakDenominator(Item item) {
        if (item == Items.STICK) return STICK_BREAK_DENOMINATOR;
        if (item == Items.BONE) return BONE_BREAK_DENOMINATOR;
        return 0;
    }
}
