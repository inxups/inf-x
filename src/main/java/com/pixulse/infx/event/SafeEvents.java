package com.pixulse.infx.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.block.SafeBlock;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.data.harvest.MiteMiningRules;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXAttachments;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.data.food.SurvivalRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

/** Ownership, higher-tier multiplayer break checks and combat-disconnect penalty. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class SafeEvents {
    private static final String LAST_DANGER = "infx_last_danger_tick";
    private static final String DISCONNECT_PENALTY = "infx_disconnect_penalty";

    private SafeEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void protectSafe(BreakBlockEvent event) {
        if (!(event.getState().getBlock() instanceof SafeBlock safe)
                || !(event.getPlayer() instanceof ServerPlayer player)
                || player.hasInfiniteMaterials()) return;
        boolean owner = isOwner(event.getLevel().getBlockEntity(event.getPos()), player);
        MiteMaterial toolMaterial = toolMaterial(player.getMainHandItem());
        if (!mayBreak(safe.material(), owner, toolMaterial)) {
            event.setCanceled(true);
            event.setNotifyClient(true);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    owner ? "message.infx.safe_tool" : "message.infx.safe_foreign_tool"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void protectSafeDrops(BlockDropsEvent event) {
        if (!(event.getState().getBlock() instanceof SafeBlock safe)
                || mayDropSafeItem(event.getBreaker(), event.getBlockEntity())) {
            return;
        }
        event.getDrops().removeIf(drop -> drop.getItem().is(safe.asItem()));
    }

    /**
     * Only the owner (or creative) recovers the safe block item. Explosions and other
     * non-player breakers never drop the box itself, matching MITE {@code dropBlockAsEntityItem}.
     */
    public static boolean mayDropSafeItem(@Nullable Entity breaker, @Nullable BlockEntity blockEntity) {
        if (!(breaker instanceof Player player)) {
            return false;
        }
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        return blockEntity instanceof SafeBlockEntity safe && safe.isPortableTo(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void protectSafeBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!(event.getState().getBlock() instanceof SafeBlock safe)
                || event.getEntity().hasInfiniteMaterials()) {
            return;
        }
        event.getPosition().ifPresent(pos -> {
            boolean owner = isOwner(event.getEntity().level().getBlockEntity(pos), event.getEntity());
            if (!mayBreak(safe.material(), owner, toolMaterial(event.getEntity().getMainHandItem()))) {
                event.setNewSpeed(0.0F);
            }
        });
    }

    private static boolean isOwner(
            net.minecraft.world.level.block.entity.BlockEntity blockEntity,
            net.minecraft.world.entity.player.Player player) {
        return blockEntity instanceof SafeBlockEntity safe && safe.isPortableTo(player);
    }

    private static MiteMaterial toolMaterial(ItemStack tool) {
        var equipment = InfXItems.catalog().equipment(tool);
        if (equipment == null
                || equipment.key().type() != EquipmentType.PICKAXE
                        && equipment.key().type() != EquipmentType.WAR_HAMMER) {
            return null;
        }
        return equipment.key().material();
    }

    public static boolean mayBreak(MiteMaterial safe, boolean owner, MiteMaterial tool) {
        if (owner) {
            return true;
        }
        int requiredLevel = MiteMiningRules.harvestLevel(safe) + 1;
        return tool != null && MiteMiningRules.harvestLevel(tool) >= requiredLevel;
    }

    @SubscribeEvent
    public static void trackDanger(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() != null) {
            player.getPersistentData().putLong(LAST_DANGER, player.level().getGameTime());
        }
    }

    @SubscribeEvent
    public static void trackAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().putLong(LAST_DANGER, player.level().getGameTime());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.level().getServer().isDedicatedServer()
                || player.isSleeping()
                || player.isDeadOrDying()) return;
        long lastDanger = player.getPersistentData().getLong(LAST_DANGER).orElse(Long.MIN_VALUE);
        long elapsed = player.level().getGameTime() - lastDanger;
        if (elapsed >= 0L && elapsed <= 200L) {
            player.getPersistentData().putBoolean(DISCONNECT_PENALTY, true);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.getPersistentData().getBoolean(DISCONNECT_PENALTY).orElse(false)) return;
        player.getPersistentData().remove(DISCONNECT_PENALTY);
        player.setHealth(Math.max(1.0F, player.getHealth() * 0.5F));
        var data = player.getData(InfXAttachments.SURVIVAL)
                .consume(2.0D, 2_000, SurvivalRules.foodCap(player.experienceLevel));
        player.setData(InfXAttachments.SURVIVAL, data);
        SurvivalEvents.syncFoodData(player);
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.infx.disconnect_penalty"));
    }
}
