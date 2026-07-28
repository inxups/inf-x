package com.pixulse.infx.effect.curse;

import com.pixulse.infx.block.SafeBlock;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.registry.tag.ModTags;
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
public final class CurseEvents {
    private static final Player.BedSleepingProblem CURSED_SLEEP = new Player.BedSleepingProblem(
            Component.translatable("message.infx.curse.cannot_sleep"));

    private CurseEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(CurseEvents::tickPlayer);
        gameBus.addListener(CurseEvents::startUsingItem);
        gameBus.addListener(CurseEvents::finishUsingItem);
        gameBus.addListener(CurseEvents::useBlock);
        gameBus.addListener(EventPriority.HIGH, CurseEvents::startSleeping);
        gameBus.addListener(EventPriority.HIGH, CurseEvents::continueSleeping);
        gameBus.addListener(CurseEvents::attackEntity);
    }

    private static void tickPlayer(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CurseManager.tick(player);
        }
    }

    private static void startUsingItem(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem();
        if (stack.is(ModItems.BOTTLE_OF_DISENCHANTING)) return;

        CurseType curse = forbiddenIngestion(player, stack);
        if (curse == null) return;
        CurseManager.reveal(player, curse);
        event.setCanceled(true);
    }

    private static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItem().is(ModItems.BOTTLE_OF_DISENCHANTING)) {
            CurseManager.removeFromPlayer(player);
        }
    }

    private static @Nullable CurseType forbiddenIngestion(Player player, ItemStack stack) {
        if (stack.is(ModTags.Items.CURSE_ANIMAL_PRODUCTS)
                && CurseManager.hasCurse(player, CurseType.CANNOT_EAT_ANIMALS)) {
            return CurseType.CANNOT_EAT_ANIMALS;
        }
        if (stack.is(ModTags.Items.CURSE_PLANT_PRODUCTS)
                && CurseManager.hasCurse(player, CurseType.CANNOT_EAT_PLANTS)) {
            return CurseType.CANNOT_EAT_PLANTS;
        }
        var consumable = stack.get(DataComponents.CONSUMABLE);
        boolean drink = stack.is(ModTags.Items.CURSE_DRINKS)
                || consumable != null && consumable.animation() == ItemUseAnimation.DRINK;
        return drink && CurseManager.hasCurse(player, CurseType.CANNOT_DRINK)
                ? CurseType.CANNOT_DRINK
                : null;
    }

    private static void useBlock(PlayerInteractEvent.RightClickBlock event) {
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
                && player.canEat(false)) {
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

    private static void startSleeping(CanPlayerSleepEvent event) {
        ServerPlayer player = event.getEntity();
        if (event.getProblem() != null
                || !CurseManager.hasCurse(player, CurseType.CANNOT_SLEEP)) {
            return;
        }
        CurseManager.reveal(player, CurseType.CANNOT_SLEEP);
        event.setProblem(CURSED_SLEEP);
    }

    private static void continueSleeping(CanContinueSleepingEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !CurseManager.hasCurse(player, CurseType.CANNOT_SLEEP)) {
            return;
        }
        CurseManager.reveal(player, CurseType.CANNOT_SLEEP);
        player.sendOverlayMessage(Component.translatable("message.infx.curse.cannot_sleep"));
        event.setContinueSleeping(false);
    }

    private static void attackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        CurseType curse = fearFor(target);
        // The target's random source and short-circuit order match MITE's canBeAttackedBy methods.
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
