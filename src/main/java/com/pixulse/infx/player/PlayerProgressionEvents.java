package com.pixulse.infx.player;

import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.harvest.HarvestSpeedRules;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.registry.InfinityXAttachments;
import com.pixulse.infx.registry.InfinityXItems;
import com.pixulse.infx.food.SurvivalEvents;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
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
        event.add(EntityType.PLAYER, Attributes.BLOCK_INTERACTION_RANGE, blockInteractionRange(testMode));
        event.add(EntityType.PLAYER, Attributes.ENTITY_INTERACTION_RANGE, entityInteractionRange(testMode));
    }

    static double blockInteractionRange(boolean testMode) {
        return testMode ? Player.DEFAULT_BLOCK_INTERACTION_RANGE : 2.75;
    }

    static double entityInteractionRange(boolean testMode) {
        return testMode ? Player.DEFAULT_ENTITY_INTERACTION_RANGE : 2.5;
    }

    private static void onExperienceChange(PlayerXpEvent.XpChange event) {
        event.setCanceled(true);
        Experience.add(event.getEntity(), event.getAmount());
        SurvivalEvents.recalculatePlayerLimits(event.getEntity());
    }

    private static void onLevelChange(PlayerXpEvent.LevelChange event) {
        event.setCanceled(true);
        Experience.addLevels(event.getEntity(), event.getLevels());
        SurvivalEvents.recalculatePlayerLimits(event.getEntity());
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Experience.setTotal(event.getEntity(), event.getEntity().totalExperience);
        SurvivalEvents.recalculatePlayerLimits(event.getEntity());
    }

    private static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()
                || keepsExperienceOnDeath(event.getEntity())
                || event.getOriginal().isSpectator()) {
            Experience.setTotal(event.getEntity(), event.getOriginal().totalExperience);
            SurvivalEvents.recalculatePlayerLimits(event.getEntity());
            return;
        }
        int previous = event.getOriginal().getPersistentData()
                .getInt(DEATH_TOTAL)
                .orElse(event.getOriginal().totalExperience);
        Experience.setTotal(event.getEntity(), Experience.deathTotal(previous));
        SurvivalEvents.recalculatePlayerLimits(event.getEntity());
    }

    private static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().putInt(DEATH_TOTAL, player.totalExperience);
            player.getPersistentData().putLong(DEATH_TIME, player.level().getGameTime());
        }
    }

    private static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (keepsExperienceOnDeath(player) || player.isSpectator()) {
                event.setDroppedExperience(0);
                return;
            }
            event.setDroppedExperience(Experience.droppedOnDeath(player.totalExperience));
        }
    }

    private static boolean keepsExperienceOnDeath(Player player) {
        return player.level() instanceof ServerLevel serverLevel
                && serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY);
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
        return Experience.meleeMultiplier(level);
    }

    public static boolean isWeakStrike(Player player) {
        if (player.getHealth() < 2.0F
                || !player.getData(InfinityXAttachments.SURVIVAL).hasFoodEnergy()
                || HarvestSpeedRules.isParalyzed(player)
                || HarvestSpeedRules.isInCobweb(player)) {
            return true;
        }
        Catalog.EquipmentEntry held = InfinityXItems.catalog().equipment(player.getMainHandItem());
        return held == null && player.getAttributeValue(Attributes.ATTACK_DAMAGE) <= 2.0;
    }
}
