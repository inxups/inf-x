package com.pixulse.infx.curse;

import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.block.entity.R196SafeBlockEntity;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.tag.ModTags;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

/** NeoForge event implementations for curse lifecycle and interaction gates. */
public final class R196CurseEvents {
    private static final Player.BedSleepingProblem CURSED_SLEEP = new Player.BedSleepingProblem(
            Component.translatable("message.infx.curse.cannot_sleep"));

    private R196CurseEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196CurseEvents::tickPlayer);
        gameBus.addListener(R196CurseEvents::startUsingItem);
        gameBus.addListener(R196CurseEvents::finishUsingItem);
        gameBus.addListener(R196CurseEvents::useBlock);
        gameBus.addListener(EventPriority.HIGH, R196CurseEvents::startSleeping);
        gameBus.addListener(EventPriority.HIGH, R196CurseEvents::continueSleeping);
        gameBus.addListener(R196CurseEvents::attackEntity);
    }

    private static void tickPlayer(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            R196CurseManager.tick(player);
        }
    }

    private static void startUsingItem(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem();
        if (stack.is(ModItems.BOTTLE_OF_DISENCHANTING)) return;

        R196CurseType curse = forbiddenIngestion(player, stack);
        if (curse == null) return;
        R196CurseManager.reveal(player, curse);
        event.setCanceled(true);
    }

    private static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItem().is(ModItems.BOTTLE_OF_DISENCHANTING)) {
            R196CurseManager.removeFromPlayer(player);
        }
    }

    private static @Nullable R196CurseType forbiddenIngestion(Player player, ItemStack stack) {
        if (stack.is(ModTags.Items.CURSE_ANIMAL_PRODUCTS)
                && R196CurseManager.hasCurse(player, R196CurseType.CANNOT_EAT_ANIMALS)) {
            return R196CurseType.CANNOT_EAT_ANIMALS;
        }
        if (stack.is(ModTags.Items.CURSE_PLANT_PRODUCTS)
                && R196CurseManager.hasCurse(player, R196CurseType.CANNOT_EAT_PLANTS)) {
            return R196CurseType.CANNOT_EAT_PLANTS;
        }
        var consumable = stack.get(DataComponents.CONSUMABLE);
        boolean drink = stack.is(ModTags.Items.CURSE_DRINKS)
                || consumable != null && consumable.animation() == ItemUseAnimation.DRINK;
        return drink && R196CurseManager.hasCurse(player, R196CurseType.CANNOT_DRINK)
                ? R196CurseType.CANNOT_DRINK
                : null;
    }

    private static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        var state = event.getLevel().getBlockState(event.getPos());
        R196CurseType curse = null;
        if ((state.is(Blocks.CHEST)
                        || state.is(Blocks.TRAPPED_CHEST)
                        || state.getBlock() instanceof R196SafeBlock)
                && canOtherwiseOpen(event, state)
                && R196CurseManager.hasCurse(player, R196CurseType.CANNOT_OPEN_CHESTS)) {
            curse = R196CurseType.CANNOT_OPEN_CHESTS;
        } else if (state.is(Blocks.CAKE)
                && event.getItemStack().isEmpty()
                && player.canEat(false)) {
            if (R196CurseManager.hasCurse(player, R196CurseType.CANNOT_EAT_ANIMALS)) {
                curse = R196CurseType.CANNOT_EAT_ANIMALS;
            } else if (R196CurseManager.hasCurse(player, R196CurseType.CANNOT_EAT_PLANTS)) {
                curse = R196CurseType.CANNOT_EAT_PLANTS;
            }
        }
        if (curse == null) return;

        R196CurseManager.reveal(player, curse);
        if (curse == R196CurseType.CANNOT_OPEN_CHESTS && !event.getLevel().isClientSide()) {
            event.getLevel().playSound(
                    null, event.getPos(), SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.2F, 1.0F);
        }
        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
    }

    private static boolean canOtherwiseOpen(
            PlayerInteractEvent.RightClickBlock event, BlockState state) {
        if (!(state.getBlock() instanceof R196SafeBlock)) {
            return state.getMenuProvider(event.getLevel(), event.getPos()) != null;
        }
        if (!event.getLevel()
                .getBlockState(event.getPos().above())
                .getCollisionShape(event.getLevel(), event.getPos().above())
                .isEmpty()) {
            return false;
        }
        return event.getLevel().getBlockEntity(event.getPos()) instanceof R196SafeBlockEntity safe
                && safe.canOpen(event.getEntity());
    }

    private static void startSleeping(CanPlayerSleepEvent event) {
        ServerPlayer player = event.getEntity();
        if (event.getProblem() != null
                || !R196CurseManager.hasCurse(player, R196CurseType.CANNOT_SLEEP)) {
            return;
        }
        R196CurseManager.reveal(player, R196CurseType.CANNOT_SLEEP);
        event.setProblem(CURSED_SLEEP);
    }

    private static void continueSleeping(CanContinueSleepingEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !R196CurseManager.hasCurse(player, R196CurseType.CANNOT_SLEEP)) {
            return;
        }
        R196CurseManager.reveal(player, R196CurseType.CANNOT_SLEEP);
        player.sendOverlayMessage(Component.translatable("message.infx.curse.cannot_sleep"));
        event.setContinueSleeping(false);
    }

    private static void attackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        R196CurseType curse = fearFor(target);
        // The target's random source and short-circuit order match MITE's canBeAttackedBy methods.
        if (curse == null
                || target.getRandom().nextInt(4) == 0
                || !R196CurseManager.hasCurse(player, curse)) {
            return;
        }
        R196CurseManager.reveal(player, curse);
        event.setCanceled(true);
    }

    static @Nullable R196CurseType fearFor(LivingEntity target) {
        if (target instanceof Spider) return R196CurseType.FEAR_OF_SPIDERS;
        if (target instanceof Wolf) return R196CurseType.FEAR_OF_WOLVES;
        if (target instanceof Creeper) return R196CurseType.FEAR_OF_CREEPERS;
        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(target.getType()).is(EntityTypeTags.UNDEAD)) {
            return R196CurseType.FEAR_OF_UNDEAD;
        }
        return null;
    }
}
