package com.pixulse.infx.enchantment;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.agriculture.R196AgricultureData;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.registry.ModMobEffects;
import com.pixulse.infx.tag.ModTags;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Runtime effects for R196's data-driven enchantment registrations. */
public final class R196EnchantmentEvents {
    private static final net.minecraft.resources.Identifier SPEED = InfiniteX.id("enchantment_speed");
    private static final net.minecraft.resources.Identifier SLOWNESS_SPEED =
            net.minecraft.resources.Identifier.withDefaultNamespace("effect.slowness");
    private static final net.minecraft.resources.Identifier PARALYSIS_SPEED = InfiniteX.id("paralysis_speed");
    private static final String DISARMED_UNTIL = "infx_disarmed_until";
    private static final String RESTORE_PICKUP = "infx_disarmed_restore_pickup";
    private static boolean felling;

    private R196EnchantmentEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(EventPriority.HIGH, R196EnchantmentEvents::onIncomingDamage);
        gameBus.addListener(R196EnchantmentEvents::onDamagePost);
        gameBus.addListener(R196EnchantmentEvents::onLivingDrops);
        gameBus.addListener(R196EnchantmentEvents::onBlockDrops);
        gameBus.addListener(R196EnchantmentEvents::onBlockBroken);
        gameBus.addListener(R196EnchantmentEvents::onToolModified);
        gameBus.addListener(R196EnchantmentEvents::onEntityTick);
        gameBus.addListener(R196EnchantmentEvents::onPlayerTick);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (event.getSource().getDirectEntity() != attacker) return;

        if (attacker instanceof Player player
                && player.hasEffect(MobEffects.STRENGTH)) {
            int strength = player.getEffect(MobEffects.STRENGTH).getAmplifier() + 1;
            float estimatedBase = Math.max(0.0F, event.getAmount() - strength * 3.0F);
            event.setAmount(estimatedBase * (1.0F + strength * 0.4F));
        }

        int slaughter = R196Enchantments.level(
                attacker.level(), attacker.getMainHandItem(), ModEnchantments.SLAUGHTER);
        event.setAmount(event.getAmount() + R196EnchantmentRules.slaughterDamageBonus(slaughter));
    }

    private static void onDamagePost(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() <= 0.0F) return;
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            applyArrowPoison(event.getEntity(), arrow);
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && event.getSource().getDirectEntity() == attacker) {
            applyMeleeEffects(attacker, event.getEntity(), event.getHealthDamage());
        }
    }

    private static void applyArrowPoison(LivingEntity target, AbstractArrow arrow) {
        int poisoning = arrow.getPersistentData().getInt("infx_poisoning_enchantment").orElse(0);
        if (poisoning > 0 && arrow.getRandom().nextFloat() < R196EnchantmentRules.poisonChance(poisoning)) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    R196EnchantmentRules.poisonDuration(poisoning),
                    0), arrow);
        }
    }

    private static void applyMeleeEffects(LivingEntity attacker, LivingEntity target, float healthDamage) {
        ItemStack weapon = attacker.getMainHandItem();
        int stunning = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.STUNNING);
        if (stunning > 0 && attacker.getRandom().nextFloat() < R196EnchantmentRules.stunningChance(stunning)) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    R196EnchantmentRules.stunningDuration(stunning),
                    R196EnchantmentRules.stunningAmplifier(stunning)), attacker);
        }

        int disarming = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.DISARMING);
        if (attacker instanceof Player
                && target instanceof Mob mob
                && disarming > 0
                && attacker.getRandom().nextFloat() < R196EnchantmentRules.disarmingChance(disarming)) {
            disarm(mob);
        }

        int vampirism = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.VAMPIRISM);
        if (vampirism > 0
                && isBiologicallyAlive(target)
                && attacker.getRandom().nextFloat() < R196EnchantmentRules.vampirismChance(vampirism)) {
            attacker.heal(R196EnchantmentRules.vampirismHealing(healthDamage, attacker.getRandom().nextFloat()));
        }
    }

    private static void disarm(Mob target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        ItemStack held = target.getMainHandItem();
        if (held.isEmpty()) return;
        target.spawnAtLocation(level, held.copy());
        target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        target.getPersistentData().putLong(DISARMED_UNTIL, level.getGameTime() + 40L);
        target.getPersistentData().putBoolean(RESTORE_PICKUP, target.canPickUpLoot());
        target.setCanPickUpLoot(false);
    }

    private static boolean isBiologicallyAlive(LivingEntity entity) {
        return !BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(EntityTypeTags.UNDEAD);
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)
                || !(event.getEntity() instanceof Animal)) return;
        int level = R196Enchantments.level(attacker.level(), attacker.getMainHandItem(), ModEnchantments.BUTCHERING);
        int extra = level <= 0 ? 0 : attacker.getRandom().nextInt(level + 1);
        if (extra <= 0) return;
        ItemEntity foodDrop = null;
        for (ItemEntity original : event.getDrops()) {
            if (original.getItem().has(DataComponents.FOOD)) {
                foodDrop = original;
                break;
            }
        }
        if (foodDrop == null) return;
        ItemStack bonus = foodDrop.getItem().copyWithCount(extra);
        event.getDrops().add(new ItemEntity(
                foodDrop.level(), foodDrop.getX(), foodDrop.getY(), foodDrop.getZ(), bonus));
    }

    private static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getState().getBlock() instanceof CropBlock crop) || !crop.isMaxAge(event.getState())) return;
        int harvesting = R196Enchantments.level(event.getLevel(), event.getTool(), ModEnchantments.HARVESTING);
        if (harvesting <= 0 || !event.getTool().isCorrectToolForDrops(event.getState())) return;
        float chance = R196EnchantmentRules.harvestingBonusChance(harvesting);
        for (ItemEntity original : List.copyOf(event.getDrops())) {
            int extra = 0;
            for (int unit = 0; unit < original.getItem().getCount(); unit++) {
                if (event.getLevel().getRandom().nextFloat() < chance) extra++;
            }
            if (extra > 0) {
                event.getDrops().add(new ItemEntity(
                        original.level(), original.getX(), original.getY(), original.getZ(),
                        original.getItem().copyWithCount(extra)));
            }
        }
    }

    private static void onBlockBroken(BreakBlockEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) return;
        fertilizeMatureCrop(event, level);
        if (felling || !event.getState().is(BlockTags.LOGS)) return;
        ItemStack tool = event.getPlayer().getMainHandItem();
        if (!tool.is(ModTags.Items.R196_TREE_FELLING_ENCHANTABLE)) return;
        int enchantment = R196Enchantments.level(level, tool, ModEnchantments.TREE_FELLING);
        if (enchantment <= 0) return;
        felling = true;
        try {
            int remaining = R196EnchantmentRules.treeFellingExtraLogs(enchantment);
            for (int y = 1; y <= remaining; y++) {
                BlockPos pos = event.getPos().above(y);
                if (!level.getBlockState(pos).is(BlockTags.LOGS)) break;
                level.destroyBlock(pos, true, event.getPlayer());
            }
        } finally {
            felling = false;
        }
    }

    private static void fertilizeMatureCrop(BreakBlockEvent event, ServerLevel level) {
        if (!(event.getState().getBlock() instanceof CropBlock crop) || !crop.isMaxAge(event.getState())) return;
        int fertility = R196Enchantments.level(level, event.getPlayer().getMainHandItem(), ModEnchantments.FERTILITY);
        if (fertility > 0 && level.getRandom().nextFloat() < R196EnchantmentRules.fertilityChance(fertility)) {
            R196AgricultureData.get(level).fertilize(event.getPos().below(), level.getGameTime());
        }
    }

    private static void onToolModified(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()
                || event.getItemAbility() != ItemAbilities.HOE_TILL
                || !(event.getLevel() instanceof ServerLevel level)
                || event.getFinalState() == null
                || !event.getFinalState().is(Blocks.FARMLAND)) return;
        int fertility = R196Enchantments.level(level, event.getHeldItemStack(), ModEnchantments.FERTILITY);
        if (fertility > 0 && level.getRandom().nextFloat() < R196EnchantmentRules.fertilityChance(fertility)) {
            R196AgricultureData.get(level).fertilize(event.getPos(), level.getGameTime());
        }
    }

    private static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.level().isClientSide()) return;
        applyFreeMovementResistance(living);
        if (!(living instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        long until = mob.getPersistentData().getLongOr(DISARMED_UNTIL, 0L);
        if (until == 0L) return;
        if (level.getGameTime() < until) {
            mob.setCanPickUpLoot(false);
            return;
        }
        if (mob.getPersistentData().getBooleanOr(RESTORE_PICKUP, false)) {
            mob.setCanPickUpLoot(true);
        }
        mob.getPersistentData().remove(DISARMED_UNTIL);
        mob.getPersistentData().remove(RESTORE_PICKUP);
    }

    private static void applyFreeMovementResistance(LivingEntity entity) {
        MobEffectInstance slowness = entity.getEffect(MobEffects.SLOWNESS);
        MobEffectInstance paralysis = entity.getEffect(ModMobEffects.PARALYSIS);
        if (slowness == null && paralysis == null) return;
        int freeMovement = R196Enchantments.maxArmorLevel(entity, ModEnchantments.FREE_MOVEMENT);
        AttributeInstance movement = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        replaceImpairmentModifier(
                movement,
                slowness,
                SLOWNESS_SPEED,
                -0.15D,
                freeMovement);
        replaceImpairmentModifier(
                movement,
                paralysis,
                PARALYSIS_SPEED,
                -1.0D,
                freeMovement);
    }

    private static void replaceImpairmentModifier(
            AttributeInstance movement,
            MobEffectInstance effect,
            net.minecraft.resources.Identifier modifierId,
            double baseAmount,
            int freeMovement) {
        if (effect == null) return;
        double adjusted = R196EnchantmentRules.freeMovementAdjustedImpairment(
                baseAmount * (effect.getAmplifier() + 1), freeMovement);
        AttributeModifier current = movement.getModifier(modifierId);
        if (current != null
                && current.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                && Math.abs(current.amount() - adjusted) < 1.0E-9D) {
            return;
        }
        movement.addOrReplacePermanentModifier(new AttributeModifier(
                modifierId, adjusted, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        var movement = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        int speed = R196Enchantments.armorLevel(player, ModEnchantments.SPEED);
        if (speed > 0) {
            movement.addOrUpdateTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    SPEED, speed * 0.05D,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            movement.removeModifier(SPEED);
        }
    }
}
