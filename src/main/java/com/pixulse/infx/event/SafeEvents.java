package com.pixulse.infx.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.block.SafeBlock;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.data.harvest.MiteMiningRules;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

/** Ownership and higher-tier multiplayer break checks for metal safes. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class SafeEvents {
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

}
