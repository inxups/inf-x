package com.pixulse.infx.survival;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.registry.ModMobEffects;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.enchantment.R196Enchantments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Applies player caps, metabolism, long-term nutrition and slow natural healing. */
public final class R196SurvivalEvents {
    private static final String INITIALIZED = "infx_r196_survival_initialized";
    private static final net.minecraft.resources.Identifier EMPTY_AIR_SPEED =
            InfiniteX.id("empty_air_speed");

    private R196SurvivalEvents() {}

    public static void register(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener(R196SurvivalEvents::modifyVanillaFoodComponents);
        gameBus.addListener(R196SurvivalEvents::onLogin);
        gameBus.addListener(R196SurvivalEvents::onClone);
        gameBus.addListener(R196SurvivalEvents::onFoodFinished);
        gameBus.addListener(R196SurvivalEvents::onPlayerTick);
        gameBus.addListener(R196SurvivalEvents::onAttack);
        gameBus.addListener(R196SurvivalEvents::onBlockBroken);
        gameBus.addListener(R196SurvivalEvents::onContinueSleeping);
    }

    private static void modifyVanillaFoodComponents(ModifyDefaultComponentsEvent event) {
        FoodProperties mushroom = new FoodProperties.Builder()
                .nutrition(1)
                .saturationModifier(0.1F)
                .build();
        FoodProperties egg = new FoodProperties.Builder()
                .nutrition(1)
                .saturationModifier(0.1F)
                .build();
        event.modify(Items.BROWN_MUSHROOM, (components, context, item) -> components
                .set(DataComponents.FOOD, mushroom)
                .set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD));
        event.modify(Items.RED_MUSHROOM, (components, context, item) -> components
                .set(DataComponents.FOOD, mushroom)
                .set(
                        DataComponents.CONSUMABLE,
                        Consumables.defaultFood()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        java.util.List.of(
                                                new MobEffectInstance(MobEffects.POISON, 240, 1),
                                                new MobEffectInstance(MobEffects.NAUSEA, 240, 0))))
                                .build()));
        event.modify(Items.EGG, (components, context, item) -> components
                .set(DataComponents.FOOD, egg)
                .set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD));
        FoodProperties smallFood = new FoodProperties.Builder()
                .nutrition(1)
                .saturationModifier(0.0F)
                .build();
        for (var item : java.util.List.of(
                Items.WHEAT_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.MELON_SEEDS,
                Items.BEETROOT_SEEDS,
                Items.NETHER_WART,
                Items.SUGAR)) {
            event.modify(item, (components, context, ignored) -> components
                    .set(DataComponents.FOOD, smallFood)
                    .set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD));
        }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getPersistentData().getBoolean(INITIALIZED).orElse(false)) {
            player.setData(ModAttachments.SURVIVAL, R196SurvivalData.initial());
            player.getPersistentData().putBoolean(INITIALIZED, true);
            player.getFoodData().setFoodLevel((int) R196SurvivalRules.INITIAL_CAP);
            player.getFoodData().setSaturation((float) R196SurvivalRules.INITIAL_CAP);
        }
        recalculatePlayerLimits(player);
        mirrorFoodData(player, player.getData(ModAttachments.SURVIVAL));
    }

    private static void onClone(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().putBoolean(INITIALIZED, true);
        recalculatePlayerLimits(event.getEntity());
    }

    private static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        R196FoodProfile food = R196FoodProfiles.forStack(event.getItem());
        if (food == R196FoodProfile.EMPTY) return;
        R196SurvivalData updated = player.getData(ModAttachments.SURVIVAL)
                .eat(food, R196SurvivalRules.foodCap(player.experienceLevel));
        player.setData(ModAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        updateAirSpeed(player);
        if (player.tickCount % 10 != 0) return;

        R196SurvivalData current = player.getData(ModAttachments.SURVIVAL)
                .clamp(R196SurvivalRules.foodCap(player.experienceLevel));
        boolean moving = player.getDeltaMovement().horizontalDistanceSqr() > 0.0004D;
        boolean swimming = player.isSwimming() || player.isInWater() && moving;
        boolean jumping = !player.onGround() && player.getDeltaMovement().y > 0.08D;
        boolean wet = player.isInWaterOrRain();
        boolean cold = player.level().getBiome(player.blockPosition()).value().getBaseTemperature() < 0.4F;
        double cost = R196SurvivalRules.metabolism(
                        moving,
                        player.isSprinting(),
                        swimming,
                        jumping,
                        player.getVehicle() instanceof AbstractBoat,
                        wet,
                        cold,
                        current.isMalnourished())
                * 10.0D;
        int endurance = R196Enchantments.armorLevel(player, ModEnchantments.ENDURANCE);
        cost *= Math.max(0.5D, 1.0D - endurance * 0.05D);
        int nutrientDecay = Math.max(1, (int) Math.ceil(cost * 1_000.0D));
        R196SurvivalData updated = current.consume(
                cost, nutrientDecay, R196SurvivalRules.foodCap(player.experienceLevel));
        updated = applyRecovery(player, updated);
        player.setData(ModAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
        updateStatusEffects(player, updated);
        applyLethalPoison(player);
    }

    private static R196SurvivalData applyRecovery(ServerPlayer player, R196SurvivalData data) {
        if (!player.isHurt() || data.isEnergyEmpty()) {
            return data.withRecoveryProgress(0.0D);
        }
        double progress = data.recoveryProgress()
                + 10.0D * R196SurvivalRules.recoveryPerTick(
                        player.isSleeping(), data.isMalnourished(), regenerationLevel(player));
        if (progress < 1.0D) return data.withRecoveryProgress(progress);
        player.heal(1.0F);
        return data.withRecoveryProgress(progress - 1.0D)
                .consume(1.0D, 0, R196SurvivalRules.foodCap(player.experienceLevel));
    }

    private static int regenerationLevel(Player player) {
        int effect = player.hasEffect(MobEffects.REGENERATION)
                ? player.getEffect(MobEffects.REGENERATION).getAmplifier() + 1
                : 0;
        return effect + R196Enchantments.armorLevel(player, ModEnchantments.REGENERATION);
    }

    private static void updateStatusEffects(ServerPlayer player, R196SurvivalData data) {
        if (data.isMalnourished()) {
            player.addEffect(new MobEffectInstance(ModMobEffects.MALNUTRITION, 220, 0, true, false, true));
        } else {
            player.removeEffect(ModMobEffects.MALNUTRITION);
        }
        int insulin = data.insulinResistance().ordinal();
        if (insulin > 0) {
            player.addEffect(new MobEffectInstance(
                    ModMobEffects.INSULIN_RESISTANCE, 220, insulin - 1, true, false, true));
        } else {
            player.removeEffect(ModMobEffects.INSULIN_RESISTANCE);
        }
    }

    private static void applyLethalPoison(ServerPlayer player) {
        if (player.tickCount % 100 == 0 && player.hasEffect(MobEffects.POISON) && player.getHealth() <= 1.0F) {
            player.hurtServer(player.level(), player.damageSources().magic(), 1.0F);
        }
    }

    private static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) consumeAction(player, 0.025D);
    }

    private static void onBlockBroken(BreakBlockEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        float hardness = Math.max(0.0F, event.getState().getDestroySpeed(event.getLevel(), event.getPos()));
        consumeAction(player, 0.01D + hardness * 0.006D);
    }

    private static void consumeAction(ServerPlayer player, double amount) {
        R196SurvivalData updated = player.getData(ModAttachments.SURVIVAL)
                .consume(amount, Math.max(1, (int) Math.ceil(amount * 1_000.0D)),
                        R196SurvivalRules.foodCap(player.experienceLevel));
        player.setData(ModAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
    }

    private static void onContinueSleeping(CanContinueSleepingEvent event) {
        if (event.getEntity() instanceof Player player
                && (player.getData(ModAttachments.SURVIVAL).isEnergyEmpty()
                        || player.hasEffect(ModMobEffects.WITCH_CURSE))) {
            event.setContinueSleeping(false);
        }
    }

    public static void recalculatePlayerLimits(Player player) {
        var maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.setBaseValue(R196SurvivalRules.healthCap(player.experienceLevel));
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
        double foodCap = R196SurvivalRules.foodCap(player.experienceLevel);
        R196SurvivalData clamped = player.getData(ModAttachments.SURVIVAL).clamp(foodCap);
        player.setData(ModAttachments.SURVIVAL, clamped);
        if (player instanceof ServerPlayer serverPlayer) mirrorFoodData(serverPlayer, clamped);
    }

    private static void mirrorFoodData(ServerPlayer player, R196SurvivalData data) {
        player.getFoodData().setFoodLevel((int) Math.ceil(data.nutrition()));
        player.getFoodData().setSaturation((float) Math.min(data.satiation(), data.nutrition()));
    }

    private static void updateAirSpeed(ServerPlayer player) {
        var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        if (!player.onGround() && player.getData(ModAttachments.SURVIVAL).isEnergyEmpty()) {
            movement.addOrUpdateTransientModifier(new AttributeModifier(
                    EMPTY_AIR_SPEED, -0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            movement.removeModifier(EMPTY_AIR_SPEED);
        }
        if (player.getData(ModAttachments.SURVIVAL).isEnergyEmpty() && player.isSprinting()) {
            player.setSprinting(false);
        }
    }
}
