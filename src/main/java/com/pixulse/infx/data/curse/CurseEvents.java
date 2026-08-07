package com.pixulse.infx.data.curse;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.block.SafeBlock;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.data.food.FoodIngestion;
import com.pixulse.infx.data.food.FoodProfiles;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

/** NeoForge event implementations for curse lifecycle and interaction gates. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class CurseEvents {
    private static final Player.BedSleepingProblem CURSED_SLEEP = new Player.BedSleepingProblem(
            Component.translatable("message.infx.curse.cannot_sleep"));

    private CurseEvents() {}

    @SubscribeEvent
    public static void tickPlayer(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CurseManager.tick(player);
        }
    }

    @SubscribeEvent
    public static void startUsingItem(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem();
        if (stack.is(InfXItems.BOTTLE_OF_DISENCHANTING)) return;

        CurseType curse = forbiddenIngestion(player, stack);
        if (curse == null) return;
        CurseManager.reveal(player, curse);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItem().is(InfXItems.BOTTLE_OF_DISENCHANTING)) {
            CurseManager.removeFromPlayer(player);
        }
    }

    private static @Nullable CurseType forbiddenIngestion(Player player, ItemStack stack) {
        if (stack.is(InfXItemTags.CURSE_ANIMAL_PRODUCTS)
                && CurseManager.hasCurse(player, CurseType.CANNOT_EAT_ANIMALS)) {
            return CurseType.CANNOT_EAT_ANIMALS;
        }
        if (stack.is(InfXItemTags.CURSE_PLANT_PRODUCTS)
                && CurseManager.hasCurse(player, CurseType.CANNOT_EAT_PLANTS)) {
            return CurseType.CANNOT_EAT_PLANTS;
        }
        var consumable = stack.get(DataComponents.CONSUMABLE);
        boolean drink = stack.is(InfXItemTags.CURSE_DRINKS)
                || consumable != null && consumable.animation() == ItemUseAnimation.DRINK;
        return drink && CurseManager.hasCurse(player, CurseType.CANNOT_DRINK)
                ? CurseType.CANNOT_DRINK
                : null;
    }

    @SubscribeEvent
    public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        var state = event.getLevel().getBlockState(event.getPos());
        CurseType curse = null;
        if ((state.is(Blocks.CHEST)
                        || state.is(Blocks.TRAPPED_CHEST)
                        || state.getBlock() instanceof SafeBlock)
                && canOtherwiseOpen(event, state)
                && CurseManager.hasCurse(player, CurseType.CANNOT_OPEN_CHESTS)) {
            curse = CurseType.CANNOT_OPEN_CHESTS;
        } else if (state.is(Blocks.CAKE)
                && event.getItemStack().isEmpty()
                && FoodIngestion.canIngest(player, FoodProfiles.cakeSlice())) {
            if (CurseManager.hasCurse(player, CurseType.CANNOT_EAT_ANIMALS)) {
                curse = CurseType.CANNOT_EAT_ANIMALS;
            } else if (CurseManager.hasCurse(player, CurseType.CANNOT_EAT_PLANTS)) {
                curse = CurseType.CANNOT_EAT_PLANTS;
            }
        }
        if (curse == null) return;

        CurseManager.reveal(player, curse);
        if (curse == CurseType.CANNOT_OPEN_CHESTS && !event.getLevel().isClientSide()) {
            event.getLevel().playSound(
                    null, event.getPos(), SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.2F, 1.0F);
        }
        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
    }

    private static boolean canOtherwiseOpen(
            PlayerInteractEvent.RightClickBlock event, BlockState state) {
        if (!(state.getBlock() instanceof SafeBlock)) {
            return state.getMenuProvider(event.getLevel(), event.getPos()) != null;
        }
        if (!event.getLevel()
                .getBlockState(event.getPos().above())
                .getCollisionShape(event.getLevel(), event.getPos().above())
                .isEmpty()) {
            return false;
        }
        return event.getLevel().getBlockEntity(event.getPos()) instanceof SafeBlockEntity safe
                && safe.canOpen(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void startSleeping(CanPlayerSleepEvent event) {
        ServerPlayer player = event.getEntity();
        if (event.getProblem() != null
                || !CurseManager.hasCurse(player, CurseType.CANNOT_SLEEP)) {
            return;
        }
        CurseManager.reveal(player, CurseType.CANNOT_SLEEP);
        event.setProblem(CURSED_SLEEP);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void continueSleeping(CanContinueSleepingEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !CurseManager.hasCurse(player, CurseType.CANNOT_SLEEP)) {
            return;
        }
        CurseManager.reveal(player, CurseType.CANNOT_SLEEP);
        player.sendOverlayMessage(Component.translatable("message.infx.curse.cannot_sleep"));
        event.setContinueSleeping(false);
    }

    @SubscribeEvent
    public static void attackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        CurseType curse = fearFor(target);
        // The target's random source and short-circuit order match InfX's canBeAttackedBy methods.
        if (curse == null
                || target.getRandom().nextInt(4) == 0
                || !CurseManager.hasCurse(player, curse)) {
            return;
        }
        CurseManager.reveal(player, curse);
        event.setCanceled(true);
    }

    static @Nullable CurseType fearFor(LivingEntity target) {
        if (target instanceof Spider) return CurseType.FEAR_OF_SPIDERS;
        if (target instanceof Wolf) return CurseType.FEAR_OF_WOLVES;
        if (target instanceof Creeper) return CurseType.FEAR_OF_CREEPERS;
        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(target.getType()).is(EntityTypeTags.UNDEAD)) {
            return CurseType.FEAR_OF_UNDEAD;
        }
        return null;
    }
}
