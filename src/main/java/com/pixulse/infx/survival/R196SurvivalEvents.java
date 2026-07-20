package com.pixulse.infx.survival;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.enchantment.R196Enchantments;
import com.pixulse.infx.harvest.HarvestEvents;
import com.pixulse.infx.registry.ModAttachments;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.registry.ModMobEffects;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Applies player caps, metabolism, long-term nutrition and slow natural healing. */
public final class R196SurvivalEvents {
    private static final String INITIALIZED = "infx_r196_survival_initialized";
    private static final double STARVATION_PROGRESS_PER_TICK = 0.002D;
    private static final net.minecraft.resources.Identifier EMPTY_AIR_SPEED =
            InfiniteX.id("empty_air_speed");
    private static final Map<ServerPlayer, PlayerActivity> ACTIVITIES = new WeakHashMap<>();

    private R196SurvivalEvents() {}

    public static void register(IEventBus modBus, IEventBus gameBus) {
        modBus.addListener(R196SurvivalEvents::modifyVanillaFoodComponents);
        gameBus.addListener(R196SurvivalEvents::onLogin);
        gameBus.addListener(R196SurvivalEvents::onLogout);
        gameBus.addListener(R196SurvivalEvents::onClone);
        gameBus.addListener(R196SurvivalEvents::onFoodFinished);
        gameBus.addListener(R196SurvivalEvents::onPlayerTick);
        gameBus.addListener(R196SurvivalEvents::onJump);
        gameBus.addListener(EventPriority.LOWEST, R196SurvivalEvents::onAttack);
        gameBus.addListener(
                EventPriority.LOWEST,
                PlayerInteractEvent.LeftClickBlock.class,
                R196SurvivalEvents::onLeftClickBlock);
        gameBus.addListener(EventPriority.LOWEST, R196SurvivalEvents::onBlockBroken);
        gameBus.addListener(
                EventPriority.LOWEST,
                BlockEvent.EntityPlaceEvent.class,
                R196SurvivalEvents::onBlockPlaced);
        gameBus.addListener(EventPriority.LOWEST, R196SurvivalEvents::onToolModified);
        gameBus.addListener(R196SurvivalEvents::onDamaged);
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
        ACTIVITIES.put(player, new PlayerActivity(MovementStats.capture(player)));
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ACTIVITIES.remove(player);
    }

    private static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original) ACTIVITIES.remove(original);
        event.getEntity().getPersistentData().putBoolean(INITIALIZED, true);
        recalculatePlayerLimits(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer player) {
            ACTIVITIES.put(player, new PlayerActivity(MovementStats.capture(player)));
        }
    }

    private static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        R196FoodProfile food = R196FoodProfiles.forStack(event.getItem());
        if (food == R196FoodProfile.EMPTY) return;
        R196SurvivalData updated = player.getData(ModAttachments.SURVIVAL)
                .eat(food, R196SurvivalRules.foodCap(player.experienceLevel));
        player.setData(ModAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerActivity activity = ACTIVITIES.computeIfAbsent(
                player, ignored -> new PlayerActivity(MovementStats.capture(player)));
        double movementCost = activity.sampleMovement(player);
        boolean activeMetabolism = hasActiveMetabolism(player);
        updateAirSpeed(player, activeMetabolism);
        if (!activeMetabolism) {
            activity.stopMining();
        } else {
            int endurance = R196Enchantments.maxArmorLevel(player, ModEnchantments.ENDURANCE);
            double enduranceActions = activity.miningMetabolism(player) + bowDrawMetabolism(player);
            double behaviorCost = movementCost
                    + rowingMetabolism(player)
                    + enduranceActions * R196SurvivalRules.enduranceModifier(endurance);
            consumeAction(player, behaviorCost);
        }
        if (player.tickCount % 10 != 0) return;

        R196SurvivalData current = player.getData(ModAttachments.SURVIVAL)
                .clamp(R196SurvivalRules.foodCap(player.experienceLevel));
        if (!activeMetabolism) {
            player.setData(ModAttachments.SURVIVAL, current);
            mirrorFoodData(player, current);
            return;
        }
        boolean wet = player.isInWaterOrRain();
        boolean cold = player.level().getBiome(player.blockPosition()).value().getBaseTemperature() < 0.4F;
        double baselineCost = R196SurvivalRules.baselineMetabolism(wet, cold, current.isMalnourished());
        int hungerEffectLevel = player.hasEffect(MobEffects.HUNGER)
                ? player.getEffect(MobEffects.HUNGER).getAmplifier() + 1
                : 0;
        double cost = 10.0D
                * (baselineCost + R196SurvivalRules.hungerEffectMetabolism(hungerEffectLevel));
        R196SurvivalData updated = current.metabolize(
                cost,
                10.0D * R196SurvivalRules.NUTRITION_METABOLISM_PER_TICK,
                10,
                R196SurvivalRules.foodCap(player.experienceLevel));
        updated = applyStarvation(player, updated);
        updated = applyRecovery(player, updated);
        player.setData(ModAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
        updateStatusEffects(player, updated);
        applyLethalPoison(player);
    }

    private static R196SurvivalData applyRecovery(ServerPlayer player, R196SurvivalData data) {
        boolean naturalRegeneration = player.level()
                .getGameRules()
                .get(GameRules.NATURAL_HEALTH_REGENERATION);
        if (!naturalRegeneration || !player.isHurt() || data.isStarving()) {
            return data.withRecoveryProgress(0.0D);
        }
        double progress = data.recoveryProgress()
                + 10.0D * R196SurvivalRules.recoveryPerTick(
                        data.nutrition(),
                        player.isSleeping(),
                        data.isMalnourished(),
                        regenerationLevel(player));
        if (progress < 1.0D) return data.withRecoveryProgress(progress);
        player.heal(1.0F);
        return data.withRecoveryProgress(progress - 1.0D)
                .metabolize(
                        R196SurvivalRules.HEALING_METABOLISM,
                        0.0D,
                        0,
                        R196SurvivalRules.foodCap(player.experienceLevel));
    }

    private static R196SurvivalData applyStarvation(ServerPlayer player, R196SurvivalData data) {
        if (!data.isStarving()) return data.withStarvationProgress(0.0D);
        double progress = data.starvationProgress() + 10.0D * STARVATION_PROGRESS_PER_TICK;
        if (progress < 1.0D) return data.withStarvationProgress(progress);
        int difficulty = player.level().getDifficulty().getId();
        if (player.getHealth() > 10.0F
                || difficulty >= 3
                || difficulty >= 2 && player.getHealth() > 1.0F) {
            player.hurtServer(player.level(), player.damageSources().starve(), 1.0F);
        }
        return data.withStarvationProgress(progress - 1.0D);
    }

    private static int regenerationLevel(Player player) {
        return R196Enchantments.maxArmorLevel(player, ModEnchantments.REGENERATION);
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

    private static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            consumeAction(player, R196SurvivalRules.jumpMetabolism(player.isSprinting()));
        }
    }

    private static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            consumeEnduranceAction(player, R196SurvivalRules.ATTACK_METABOLISM);
        }
    }

    private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerActivity activity = ACTIVITIES.computeIfAbsent(
                player, ignored -> new PlayerActivity(MovementStats.capture(player)));
        switch (event.getAction()) {
            case START -> {
                if (!canStartMining(player, event.getPos())) return;
                activity.startMining(event.getPos(), player.getMainHandItem(), player.tickCount);
                consumeEnduranceAction(player, R196SurvivalRules.MINING_METABOLISM_PER_TICK);
            }
            case STOP -> activity.stopMining(event.getPos());
            case ABORT -> activity.stopMining();
            case CLIENT_HOLD -> {
                // The hold action is client-only; the server session is advanced from PlayerTickEvent.
            }
        }
    }

    private static void onBlockBroken(BreakBlockEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            PlayerActivity activity = ACTIVITIES.get(player);
            if (activity != null) activity.stopMining(event.getPos());
        }
    }

    private static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        float hardness = event.getPlacedBlock().getDestroySpeed(event.getLevel(), event.getPos());
        consumeEnduranceAction(player, R196SurvivalRules.placementMetabolism(hardness));
    }

    private static void onToolModified(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()
                || event.getItemAbility() != ItemAbilities.HOE_TILL
                || event.getFinalState().equals(event.getState())
                || !(event.getPlayer() instanceof ServerPlayer player)) return;
        float hardness = event.getState().getDestroySpeed(event.getLevel(), event.getPos());
        consumeEnduranceAction(player, R196SurvivalRules.tillingMetabolism(hardness));
    }

    private static void onDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getHealthDamage() <= 0.0F
                || event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)
                || event.getSource().is(DamageTypeTags.IS_FIRE)) return;
        consumeAction(player, R196SurvivalRules.DAMAGE_METABOLISM);
    }

    private static boolean canStartMining(ServerPlayer player, BlockPos pos) {
        if (!hasActiveMetabolism(player)
                || !player.isWithinBlockInteractionRange(pos, 1.0D)
                || !player.level().mayInteract(player, pos)
                || player.level().getServer().isUnderSpawnProtection(player.level(), pos, player)
                || player.blockActionRestricted(player.level(), pos, player.gameMode.getGameModeForPlayer())) {
            return false;
        }
        var state = player.level().getBlockState(pos);
        return HarvestEvents.hasDestroyProgress(player, state, pos);
    }

    private static void consumeAction(ServerPlayer player, double amount) {
        if (!hasActiveMetabolism(player) || amount <= 0.0D) return;
        R196SurvivalData updated = player.getData(ModAttachments.SURVIVAL)
                .metabolize(amount, 0.0D, 0,
                        R196SurvivalRules.foodCap(player.experienceLevel));
        player.setData(ModAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
    }

    private static void consumeEnduranceAction(ServerPlayer player, double amount) {
        int endurance = R196Enchantments.maxArmorLevel(player, ModEnchantments.ENDURANCE);
        consumeAction(player, amount * R196SurvivalRules.enduranceModifier(endurance));
    }

    private static void onContinueSleeping(CanContinueSleepingEvent event) {
        if (event.getEntity() instanceof Player player
                && hasActiveMetabolism(player)
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
        player.getFoodData().setSaturation((float) data.satiation());
    }

    private static boolean hasActiveMetabolism(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    private static double rowingMetabolism(ServerPlayer player) {
        if (!(player.getVehicle() instanceof AbstractBoat boat)
                || boat.getControllingPassenger() != player) return 0.0D;
        var input = player.getLastClientInput();
        return input.forward() != input.backward() ? R196SurvivalRules.ROW_METABOLISM_PER_TICK : 0.0D;
    }

    private static double bowDrawMetabolism(ServerPlayer player) {
        return player.isUsingItem() && player.getUseItem().getItem() instanceof BowItem
                ? R196SurvivalRules.BOW_DRAW_METABOLISM_PER_TICK
                : 0.0D;
    }

    private static void updateAirSpeed(ServerPlayer player, boolean activeMetabolism) {
        var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        if (activeMetabolism
                && !player.onGround()
                && player.getData(ModAttachments.SURVIVAL).isEnergyEmpty()) {
            movement.addOrUpdateTransientModifier(new AttributeModifier(
                    EMPTY_AIR_SPEED, -0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            movement.removeModifier(EMPTY_AIR_SPEED);
        }
        if (activeMetabolism
                && player.getData(ModAttachments.SURVIVAL).isEnergyEmpty()
                && player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    private static int stat(ServerPlayer player, net.minecraft.resources.Identifier id) {
        return player.getStats().getValue(Stats.CUSTOM.get(id));
    }

    private static int positiveDelta(int current, int previous) {
        return current >= previous ? current - previous : 0;
    }

    private record MovementStats(
            int walk,
            int crouch,
            int sprint,
            int swim,
            int underwater,
            int onWater,
            int climb) {
        private static MovementStats capture(ServerPlayer player) {
            return new MovementStats(
                    stat(player, Stats.WALK_ONE_CM),
                    stat(player, Stats.CROUCH_ONE_CM),
                    stat(player, Stats.SPRINT_ONE_CM),
                    stat(player, Stats.SWIM_ONE_CM),
                    stat(player, Stats.WALK_UNDER_WATER_ONE_CM),
                    stat(player, Stats.WALK_ON_WATER_ONE_CM),
                    stat(player, Stats.CLIMB_ONE_CM));
        }

        private double metabolismSince(MovementStats previous) {
            return R196SurvivalRules.movementMetabolism(
                    positiveDelta(walk, previous.walk),
                    positiveDelta(crouch, previous.crouch),
                    positiveDelta(sprint, previous.sprint),
                    positiveDelta(swim, previous.swim),
                    positiveDelta(underwater, previous.underwater),
                    positiveDelta(onWater, previous.onWater),
                    positiveDelta(climb, previous.climb));
        }
    }

    private static final class PlayerActivity {
        private MovementStats movement;
        private BlockPos miningPos;
        private ItemStack miningTool = ItemStack.EMPTY;
        private int lastMiningChargeTick;

        private PlayerActivity(MovementStats movement) {
            this.movement = movement;
        }

        private double sampleMovement(ServerPlayer player) {
            MovementStats current = MovementStats.capture(player);
            double cost = current.metabolismSince(movement);
            movement = current;
            return cost;
        }

        private void startMining(BlockPos pos, ItemStack tool, int tick) {
            miningPos = pos.immutable();
            miningTool = tool.copy();
            lastMiningChargeTick = tick;
        }

        private double miningMetabolism(ServerPlayer player) {
            if (miningPos == null || lastMiningChargeTick >= player.tickCount) return 0.0D;
            if (!player.isWithinBlockInteractionRange(miningPos, 1.0D)
                    || player.level().getBlockState(miningPos).isAir()
                    || !ItemStack.isSameItemSameComponents(miningTool, player.getMainHandItem())) {
                stopMining();
                return 0.0D;
            }
            lastMiningChargeTick = player.tickCount;
            return R196SurvivalRules.MINING_METABOLISM_PER_TICK;
        }

        private void stopMining(BlockPos pos) {
            if (pos.equals(miningPos)) stopMining();
        }

        private void stopMining() {
            miningPos = null;
            miningTool = ItemStack.EMPTY;
        }
    }
}
