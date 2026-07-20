package com.pixulse.infx.progression;

import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.harvest.HarvestSpeedRules;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.registry.ModItems;
import com.pixulse.infx.survival.R196SurvivalEvents;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-authoritative R196 level progression, bonuses, death debt and auto-respawn. */
public final class PlayerProgressionEvents {
    private static final String DEATH_TOTAL = "infx_r196_death_total";
    private static final String DEATH_TIME = "infx_r196_death_time";
    private static final int AUTO_RESPAWN_TICKS = 120 * 20;

    private PlayerProgressionEvents() {}

    public static void register(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener(PlayerProgressionEvents::modifyPlayerRanges);
        gameBus.addListener(PlayerProgressionEvents::onExperienceChange);
        gameBus.addListener(PlayerProgressionEvents::onLevelChange);
        gameBus.addListener(PlayerProgressionEvents::onLogin);
        gameBus.addListener(PlayerProgressionEvents::onClone);
        gameBus.addListener(PlayerProgressionEvents::onDeath);
        gameBus.addListener(PlayerProgressionEvents::onExperienceDrop);
        gameBus.addListener(PlayerProgressionEvents::onPlayerTick);
        gameBus.addListener(EventPriority.HIGH, PlayerProgressionEvents::applyMeleeLevelBonus);
        gameBus.addListener(EventPriority.LOWEST, PlayerProgressionEvents::enforceWeakStrike);
    }

    private static void modifyPlayerRanges(EntityAttributeModificationEvent event) {
        boolean testMode = InfiniteXTestMode.isEnabled();
        event.add(EntityTypes.PLAYER, Attributes.BLOCK_INTERACTION_RANGE, blockInteractionRange(testMode));
        event.add(EntityTypes.PLAYER, Attributes.ENTITY_INTERACTION_RANGE, entityInteractionRange(testMode));
    }

    static double blockInteractionRange(boolean testMode) {
        return testMode ? Player.DEFAULT_BLOCK_INTERACTION_RANGE : 2.75;
    }

    static double entityInteractionRange(boolean testMode) {
        return testMode ? Player.DEFAULT_ENTITY_INTERACTION_RANGE : 2.5;
    }

    private static void onExperienceChange(PlayerXpEvent.XpChange event) {
        event.setCanceled(true);
        R196Experience.add(event.getEntity(), event.getAmount());
        R196SurvivalEvents.recalculatePlayerLimits(event.getEntity());
    }

    private static void onLevelChange(PlayerXpEvent.LevelChange event) {
        event.setCanceled(true);
        R196Experience.addLevels(event.getEntity(), event.getLevels());
        R196SurvivalEvents.recalculatePlayerLimits(event.getEntity());
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        R196Experience.setTotal(event.getEntity(), event.getEntity().totalExperience);
        R196SurvivalEvents.recalculatePlayerLimits(event.getEntity());
    }

    private static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            R196Experience.setTotal(event.getEntity(), event.getOriginal().totalExperience);
            R196SurvivalEvents.recalculatePlayerLimits(event.getEntity());
            return;
        }
        int previous = event.getOriginal().getPersistentData()
                .getInt(DEATH_TOTAL)
                .orElse(event.getOriginal().totalExperience);
        R196Experience.setTotal(event.getEntity(), R196Experience.deathTotal(previous));
        R196SurvivalEvents.recalculatePlayerLimits(event.getEntity());
    }

    private static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().putInt(DEATH_TOTAL, player.totalExperience);
            player.getPersistentData().putLong(DEATH_TIME, player.level().getGameTime());
        }
    }

    private static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setDroppedExperience(R196Experience.droppedOnDeath(player.totalExperience));
        }
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isDeadOrDying()) {
            return;
        }
        long deathTime = player.getPersistentData()
                .getLong(DEATH_TIME)
                .orElse(player.level().getGameTime());
        if (player.level().getGameTime() - deathTime < AUTO_RESPAWN_TICKS) {
            return;
        }
        player.getPersistentData().putLong(DEATH_TIME, Long.MAX_VALUE);
        player.level().getServer().execute(() -> {
            if (player.isDeadOrDying()) {
                player.connection.handleClientCommand(new ServerboundClientCommandPacket(
                        ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
            }
        });
    }

    private static void applyMeleeLevelBonus(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || !event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK)) {
            return;
        }
        event.setAmount(event.getAmount() * meleeMultiplier(player.experienceLevel));
    }

    private static void enforceWeakStrike(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || !event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK)
                || !isWeakStrike(player)) {
            return;
        }
        event.setAmount(Math.min(event.getAmount(), 1.0F));
    }

    public static float meleeMultiplier(int level) {
        return 1.0F + Math.max(0, Math.min(level, R196Experience.MAX_DISPLAY_LEVEL)) * 0.005F;
    }

    public static boolean isWeakStrike(Player player) {
        if (player.getHealth() < 2.0F
                || player.getFoodData().getFoodLevel() <= 0
                || HarvestSpeedRules.isParalyzed(player)
                || HarvestSpeedRules.isInCobweb(player)) {
            return true;
        }
        R196Catalog.EquipmentEntry held = ModItems.catalog().equipment(player.getMainHandItem());
        return held == null && player.getAttributeValue(Attributes.ATTACK_DAMAGE) <= 2.0;
    }
}
