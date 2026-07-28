package com.pixulse.infx.food;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.harvest.HarvestEvents;
import com.pixulse.infx.registry.InfXAttachments;
import com.pixulse.infx.registry.InfXEnchantments;
import com.pixulse.infx.registry.InfXMobEffects;
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
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Applies player caps, metabolism, long-term nutrition and slow natural healing. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class SurvivalEvents {
    private static final String INITIALIZED = "infx_r196_survival_initialized";
    private static final double STARVATION_PROGRESS_PER_TICK = 0.002D;
    private static final net.minecraft.resources.Identifier EMPTY_AIR_SPEED =
            InfiniteX.id("empty_air_speed");
    private static final Map<ServerPlayer, PlayerActivity> ACTIVITIES = new WeakHashMap<>();

    private SurvivalEvents() {}

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

    @SubscribeEvent

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getPersistentData().getBoolean(INITIALIZED).orElse(false)) {
            player.setData(InfXAttachments.SURVIVAL, SurvivalData.initial());
            player.getPersistentData().putBoolean(INITIALIZED, true);
            player.getFoodData().setFoodLevel((int) SurvivalRules.INITIAL_CAP);
            player.getFoodData().setSaturation((float) SurvivalRules.INITIAL_CAP);
        }
        recalculatePlayerLimits(player);
        mirrorFoodData(player, player.getData(InfXAttachments.SURVIVAL));
        ACTIVITIES.put(player, new PlayerActivity(MovementStats.capture(player)));
    }

    @SubscribeEvent

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ACTIVITIES.remove(player);
    }

    @SubscribeEvent

    private static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original) ACTIVITIES.remove(original);
        event.getEntity().getPersistentData().putBoolean(INITIALIZED, true);
        recalculatePlayerLimits(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer player) {
            ACTIVITIES.put(player, new PlayerActivity(MovementStats.capture(player)));
        }
    }

    @SubscribeEvent

    private static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        applyFood(player, event.getItem());
    }

    /** Applies an R196 food profile, or re-mirrors FoodData when the item has no profile. */
    public static void applyFood(ServerPlayer player, ItemStack stack) {
        FoodProfile food = FoodProfiles.forStack(stack);
        if (food == FoodProfile.EMPTY) {
            // Vanilla FoodData.eat may have already run; discard that temporary change.
            mirrorFoodData(player, player.getData(InfXAttachments.SURVIVAL));
            return;
        }
        SurvivalData updated = player.getData(InfXAttachments.SURVIVAL)
                .eat(food, SurvivalRules.foodCap(player.experienceLevel));
        player.setData(InfXAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
    }

    public static void syncFoodData(ServerPlayer player) {
        mirrorFoodData(player, player.getData(InfXAttachments.SURVIVAL));
    }

    @SubscribeEvent

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
            int endurance = Enchantments.maxArmorLevel(player, InfXEnchantments.ENDURANCE);
            double enduranceActions = activity.miningMetabolism(player) + bowDrawMetabolism(player);
            double behaviorCost = movementCost
                    + rowingMetabolism(player)
                    + enduranceActions * SurvivalRules.enduranceModifier(endurance);
            consumeAction(player, behaviorCost);
        }
        if (player.tickCount % 10 != 0) return;

        tickMetabolism(player, 10, player.isSleeping());
        applyLethalPoison(player);
    }

    /**
     * Advances the player-only part of one R196 sleep tick without advancing the surrounding
     * world. Bed fast-forward calls this once per skipped clock tick so hunger, nutrient decay,
     * starvation and the fourfold bed recovery rate retain their normal ordering.
     *
     * @return whether the player still has either R196 food-energy layer available
     */
    public static boolean tickSleepingMetabolism(ServerPlayer player) {
        if (!hasActiveMetabolism(player)) return true;
        tickMetabolism(player, 1, true);
        return player.getData(InfXAttachments.SURVIVAL).hasFoodEnergy();
    }

    private static void tickMetabolism(ServerPlayer player, int elapsedTicks, boolean sleeping) {
        SurvivalData current = player.getData(InfXAttachments.SURVIVAL)
                .clamp(SurvivalRules.foodCap(player.experienceLevel));
        if (!hasActiveMetabolism(player)) {
            player.setData(InfXAttachments.SURVIVAL, current);
            mirrorFoodData(player, current);
            return;
        }
        boolean wet = player.isInWaterOrRain();
        boolean cold = player.level().getBiome(player.blockPosition()).value().getBaseTemperature() < 0.4F;
        double baselineCost = SurvivalRules.baselineMetabolism(wet, cold, current.isMalnourished());
        int hungerEffectLevel = player.hasEffect(MobEffects.HUNGER)
                ? player.getEffect(MobEffects.HUNGER).getAmplifier() + 1
                : 0;
        double cost = elapsedTicks * (baselineCost + SurvivalRules.hungerEffectMetabolism(hungerEffectLevel));
        SurvivalData updated = current.metabolize(
                cost,
                elapsedTicks * SurvivalRules.NUTRITION_METABOLISM_PER_TICK,
                elapsedTicks,
                SurvivalRules.foodCap(player.experienceLevel));
        updated = applyStarvation(player, updated, elapsedTicks);
        updated = applyRecovery(player, updated, elapsedTicks, sleeping);
        player.setData(InfXAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
        updateStatusEffects(player, updated);
    }

    private static SurvivalData applyRecovery(
            ServerPlayer player, SurvivalData data, int elapsedTicks, boolean sleeping) {
        boolean naturalRegeneration = player.level()
                .getGameRules()
                .get(GameRules.NATURAL_HEALTH_REGENERATION);
        if (!naturalRegeneration || !player.isHurt() || data.isStarving()) {
            return data.withRecoveryProgress(0.0D);
        }
        double progress = data.recoveryProgress()
                + elapsedTicks * SurvivalRules.recoveryPerTick(
                        data.nutrition(),
                        sleeping,
                        data.isMalnourished(),
                        regenerationLevel(player));
        if (progress < 1.0D) return data.withRecoveryProgress(progress);
        player.heal(1.0F);
        return data.withRecoveryProgress(progress - 1.0D)
                .metabolize(
                        SurvivalRules.HEALING_METABOLISM,
                        0.0D,
                        0,
                        SurvivalRules.foodCap(player.experienceLevel));
    }

    private static SurvivalData applyStarvation(ServerPlayer player, SurvivalData data, int elapsedTicks) {
        if (!data.isStarving()) return data.withStarvationProgress(0.0D);
        double progress = data.starvationProgress() + elapsedTicks * STARVATION_PROGRESS_PER_TICK;
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
        return Enchantments.maxArmorLevel(player, InfXEnchantments.REGENERATION);
    }

    private static void updateStatusEffects(ServerPlayer player, SurvivalData data) {
        if (data.isMalnourished()) {
            player.addEffect(new MobEffectInstance(InfXMobEffects.MALNUTRITION, 220, 0, true, false, true));
        } else {
            player.removeEffect(InfXMobEffects.MALNUTRITION);
        }
        int insulin = data.insulinResistance().ordinal();
        if (insulin > 0) {
            player.addEffect(new MobEffectInstance(
                    InfXMobEffects.INSULIN_RESISTANCE, 220, insulin - 1, true, false, true));
        } else {
            player.removeEffect(InfXMobEffects.INSULIN_RESISTANCE);
        }
    }

    private static void applyLethalPoison(ServerPlayer player) {
        if (player.tickCount % 100 == 0 && player.hasEffect(MobEffects.POISON) && player.getHealth() <= 1.0F) {
            player.hurtServer(player.level(), player.damageSources().magic(), 1.0F);
        }
    }

    @SubscribeEvent

    private static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            consumeAction(player, SurvivalRules.jumpMetabolism(player.isSprinting()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)

    private static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            consumeEnduranceAction(player, SurvivalRules.ATTACK_METABOLISM);
        }
    }

    @SubscribeEvent

    private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerActivity activity = ACTIVITIES.computeIfAbsent(
                player, ignored -> new PlayerActivity(MovementStats.capture(player)));
        switch (event.getAction()) {
            case START -> {
                if (!canStartMining(player, event.getPos())) return;
                activity.startMining(event.getPos(), player.getMainHandItem(), player.tickCount);
                consumeEnduranceAction(player, SurvivalRules.MINING_METABOLISM_PER_TICK);
            }
            case STOP -> activity.stopMining(event.getPos());
            case ABORT -> activity.stopMining();
            case CLIENT_HOLD -> {
                // The hold action is client-only; the server session is advanced from PlayerTickEvent.
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)

    private static void onBlockBroken(BreakBlockEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            PlayerActivity activity = ACTIVITIES.get(player);
            if (activity != null) activity.stopMining(event.getPos());
        }
    }

    @SubscribeEvent

    private static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        float hardness = event.getPlacedBlock().getDestroySpeed(event.getLevel(), event.getPos());
        consumeEnduranceAction(player, SurvivalRules.placementMetabolism(hardness));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)

    private static void onToolModified(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()
                || event.getItemAbility() != ItemAbilities.HOE_TILL
                || event.getFinalState().equals(event.getState())
                || !(event.getPlayer() instanceof ServerPlayer player)) return;
        float hardness = event.getState().getDestroySpeed(event.getLevel(), event.getPos());
        consumeEnduranceAction(player, SurvivalRules.tillingMetabolism(hardness));
    }

    @SubscribeEvent

    private static void onDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getHealthDamage() <= 0.0F
                || event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)
                || event.getSource().is(DamageTypeTags.IS_FIRE)) return;
        consumeAction(player, SurvivalRules.DAMAGE_METABOLISM);
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
        SurvivalData updated = player.getData(InfXAttachments.SURVIVAL)
                .metabolize(amount, 0.0D, 0,
                        SurvivalRules.foodCap(player.experienceLevel));
        player.setData(InfXAttachments.SURVIVAL, updated);
        mirrorFoodData(player, updated);
    }

    private static void consumeEnduranceAction(ServerPlayer player, double amount) {
        int endurance = Enchantments.maxArmorLevel(player, InfXEnchantments.ENDURANCE);
        consumeAction(player, amount * SurvivalRules.enduranceModifier(endurance));
    }

    public static void recalculatePlayerLimits(Player player) {
        var maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.setBaseValue(SurvivalRules.healthCap(player.experienceLevel));
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
        double foodCap = SurvivalRules.foodCap(player.experienceLevel);
        SurvivalData clamped = player.getData(InfXAttachments.SURVIVAL).clamp(foodCap);
        player.setData(InfXAttachments.SURVIVAL, clamped);
        if (player instanceof ServerPlayer serverPlayer) mirrorFoodData(serverPlayer, clamped);
    }

    private static void mirrorFoodData(ServerPlayer player, SurvivalData data) {
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
        return input.forward() != input.backward() ? SurvivalRules.ROW_METABOLISM_PER_TICK : 0.0D;
    }

    private static double bowDrawMetabolism(ServerPlayer player) {
        return player.isUsingItem() && player.getUseItem().getItem() instanceof BowItem
                ? SurvivalRules.BOW_DRAW_METABOLISM_PER_TICK
                : 0.0D;
    }

    private static void updateAirSpeed(ServerPlayer player, boolean activeMetabolism) {
        var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        if (activeMetabolism
                && !player.onGround()
                && player.getData(InfXAttachments.SURVIVAL).isEnergyEmpty()) {
            movement.addOrUpdateTransientModifier(new AttributeModifier(
                    EMPTY_AIR_SPEED, -0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            movement.removeModifier(EMPTY_AIR_SPEED);
        }
        if (activeMetabolism
                && player.getData(InfXAttachments.SURVIVAL).isEnergyEmpty()
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
            return SurvivalRules.movementMetabolism(
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
            return SurvivalRules.MINING_METABOLISM_PER_TICK;
        }

        private void stopMining(BlockPos pos) {
            if (pos.equals(miningPos)) stopMining();
        }

        private void stopMining() {
            miningPos = null;
            miningTool = ItemStack.EMPTY;
        }
    }
    @EventBusSubscriber(modid = InfiniteX.MOD_ID)
    private static final class ModEvents {
        @SubscribeEvent
        private static void modifyVanillaFoodComponents(ModifyDefaultComponentsEvent event) {
            SurvivalEvents.modifyVanillaFoodComponents(event);
        }
    }
}
