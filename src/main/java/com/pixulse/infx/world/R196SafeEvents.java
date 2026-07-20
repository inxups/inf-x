package com.pixulse.infx.world;

import com.pixulse.infx.block.R196SafeBlock;
import com.pixulse.infx.block.entity.R196SafeBlockEntity;
import com.pixulse.infx.harvest.MiteMiningRules;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.survival.R196SurvivalRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Ownership, higher-tier multiplayer break checks and combat-disconnect penalty. */
public final class R196SafeEvents {
    private static final String LAST_DANGER = "infx_last_danger_tick";
    private static final String DISCONNECT_PENALTY = "infx_disconnect_penalty";

    private R196SafeEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(EventPriority.HIGH, R196SafeEvents::protectSafe);
        gameBus.addListener(EventPriority.HIGH, R196SafeEvents::protectSafeDrops);
        gameBus.addListener(EventPriority.HIGH, R196SafeEvents::protectSafeBreakSpeed);
        gameBus.addListener(R196SafeEvents::trackDanger);
        gameBus.addListener(R196SafeEvents::trackAttack);
        gameBus.addListener(R196SafeEvents::onLogout);
        gameBus.addListener(R196SafeEvents::onLogin);
    }

    private static void protectSafe(BreakBlockEvent event) {
        if (!(event.getState().getBlock() instanceof R196SafeBlock safe)
                || !(event.getPlayer() instanceof ServerPlayer player)
                || player.hasInfiniteMaterials()) return;
        boolean owner = isOwner(event.getLevel().getBlockEntity(event.getPos()), player);
        R196Material toolMaterial = toolMaterial(player.getMainHandItem());
        if (!mayBreak(safe.material(), owner, toolMaterial)) {
            event.setCanceled(true);
            event.setNotifyClient(true);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    owner ? "message.infx.safe_tool" : "message.infx.safe_foreign_tool"));
        }
    }

    private static void protectSafeDrops(PlayerEvent.HarvestCheck event) {
        if (event.getTargetBlock().getBlock() instanceof R196SafeBlock) {
            event.setCanHarvest(isOwner(event.getLevel().getBlockEntity(event.getPos()), event.getEntity()));
        }
    }

    private static void protectSafeBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!(event.getState().getBlock() instanceof R196SafeBlock safe)
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
        return blockEntity instanceof R196SafeBlockEntity safe && (safe.isUnowned() || safe.isOwner(player));
    }

    private static R196Material toolMaterial(ItemStack tool) {
        var equipment = ModItems.catalog().equipment(tool);
        if (equipment == null
                || equipment.key().type() != com.pixulse.infx.item.R196EquipmentType.PICKAXE
                        && equipment.key().type() != com.pixulse.infx.item.R196EquipmentType.WAR_HAMMER) {
            return null;
        }
        return equipment.key().material();
    }

    public static boolean mayBreak(R196Material safe, boolean owner, R196Material tool) {
        if (owner) {
            return true;
        }
        int requiredLevel = MiteMiningRules.harvestLevel(safe) + 1;
        return tool != null && MiteMiningRules.harvestLevel(tool) >= requiredLevel;
    }

    private static void trackDanger(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() != null) {
            player.getPersistentData().putLong(LAST_DANGER, player.level().getGameTime());
        }
    }

    private static void trackAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().putLong(LAST_DANGER, player.level().getGameTime());
        }
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
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

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.getPersistentData().getBoolean(DISCONNECT_PENALTY).orElse(false)) return;
        player.getPersistentData().remove(DISCONNECT_PENALTY);
        player.setHealth(Math.max(1.0F, player.getHealth() * 0.5F));
        var data = player.getData(ModAttachments.SURVIVAL)
                .consume(2.0D, 2_000, R196SurvivalRules.foodCap(player.experienceLevel));
        player.setData(ModAttachments.SURVIVAL, data);
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.infx.disconnect_penalty"));
    }
}
