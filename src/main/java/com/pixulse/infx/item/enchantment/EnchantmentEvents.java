package com.pixulse.infx.item.enchantment;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.tag.InfXItemTags;
import com.pixulse.infx.world.agriculture.AgricultureData;
import com.pixulse.infx.registry.InfXBlocks;
import com.pixulse.infx.registry.InfXEnchantments;
import com.pixulse.infx.registry.InfXMobEffects;

import java.util.List;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.Tags;
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
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class EnchantmentEvents {
    private static final net.minecraft.resources.Identifier SPEED = InfiniteX.id("enchantment_speed");
    private static final net.minecraft.resources.Identifier SLOWNESS_SPEED =
            net.minecraft.resources.Identifier.withDefaultNamespace("effect.slowness");
    private static final net.minecraft.resources.Identifier PARALYSIS_SPEED = InfiniteX.id("paralysis_speed");
    private static final String DISARMED_UNTIL = "infx_disarmed_until";
    private static final String RESTORE_PICKUP = "infx_disarmed_restore_pickup";
    private static boolean felling;

    private EnchantmentEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (event.getSource().getDirectEntity() != attacker) return;

        if (attacker instanceof Player player
                && player.hasEffect(MobEffects.STRENGTH)) {
            int strength = player.getEffect(MobEffects.STRENGTH).getAmplifier() + 1;
            float estimatedBase = Math.max(0.0F, event.getAmount() - strength * 3.0F);
            event.setAmount(estimatedBase * (1.0F + strength * 0.4F));
        }

        int slaughter = Enchantments.level(
                attacker.level(), attacker.getMainHandItem(), InfXEnchantments.SLAUGHTER);
        event.setAmount(event.getAmount() + EnchantmentRules.slaughterDamageBonus(slaughter));
    }

    @SubscribeEvent

    private static void onDamagePost(LivingDamageEvent.Post event) {
        if (event.getHealthDamage() <= 0.0F
                || event.getSource().is(net.minecraft.world.damagesource.DamageTypes.THORNS)) return;
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            applyArrowPoison(event.getEntity(), arrow);
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && event.getSource().getDirectEntity() == attacker) {
            applyMeleeEffects(attacker, event.getEntity(), event.getHealthDamage());
        }
        applyThorns(event);
    }

    /** MITE thorns retaliates against melee attackers and arrow shooters, wearing the cuirass. */
    private static void applyThorns(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        boolean directMelee = event.getSource().getDirectEntity() == attacker;
        if (!directMelee && !(event.getSource().getDirectEntity() instanceof AbstractArrow)) return;

        ItemStack thornsPiece = ItemStack.EMPTY;
        EquipmentSlot thornsSlot = null;
        int thorns = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = target.getItemBySlot(slot);
            int stackLevel = Enchantments.level(level, stack, InfXEnchantments.VANILLA_THORNS);
            if (stackLevel > thorns) {
                thorns = stackLevel;
                thornsPiece = stack;
                thornsSlot = slot;
            }
        }
        if (thorns <= 0) return;

        boolean triggered = target.getRandom().nextFloat() < EnchantmentRules.thornsChance(thorns);
        if (triggered) {
            attacker.hurtServer(level, target.damageSources().thorns(target),
                    EnchantmentRules.thornsDamage(thorns, target.getRandom()));
        }
        EquipmentSlot wornSlot = thornsSlot;
        thornsPiece.hurtAndBreak(
                EnchantmentRules.thornsArmorWear(triggered), level, target,
                item -> target.onEquippedItemBroken(item, wornSlot));
    }

    private static void applyArrowPoison(LivingEntity target, AbstractArrow arrow) {
        int poisoning = arrow.getPersistentData().getInt("infx_poisoning_enchantment").orElse(0);
        if (poisoning > 0 && arrow.getRandom().nextFloat() < EnchantmentRules.poisonChance(poisoning)) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    EnchantmentRules.poisonDuration(poisoning),
                    0), arrow);
        }
    }

    private static void applyMeleeEffects(LivingEntity attacker, LivingEntity target, float healthDamage) {
        ItemStack weapon = attacker.getMainHandItem();
        int stunning = Enchantments.level(attacker.level(), weapon, InfXEnchantments.STUNNING);
        if (stunning > 0 && attacker.getRandom().nextFloat() < EnchantmentRules.stunningChance(stunning)) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    EnchantmentRules.stunningDuration(stunning),
                    EnchantmentRules.stunningAmplifier(stunning)), attacker);
        }

        int disarming = Enchantments.level(attacker.level(), weapon, InfXEnchantments.DISARMING);
        if (attacker instanceof Player
                && target instanceof Mob mob
                && disarming > 0
                && attacker.getRandom().nextFloat() < EnchantmentRules.disarmingChance(disarming)) {
            disarm(mob);
        }

        int vampirism = Enchantments.level(attacker.level(), weapon, InfXEnchantments.VAMPIRISM);
        if (vampirism > 0
                && isBiologicallyAlive(target)
                && attacker.getRandom().nextFloat() < EnchantmentRules.vampirismChance(vampirism)) {
            attacker.heal(EnchantmentRules.vampirismHealing(healthDamage, attacker.getRandom().nextFloat()));
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

    @SubscribeEvent

    private static void onLivingDrops(LivingDropsEvent event) {
        if (!event.isRecentlyHit() || !(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        int level = Enchantments.level(attacker.level(), attacker.getMainHandItem(), InfXEnchantments.BUTCHERING);
        if (level <= 0) return;

        LivingEntity target = event.getEntity();
        if (target instanceof Cow) {
            addButcheringMeat(event, target, target.isOnFire() ? Items.COOKED_BEEF : Items.BEEF, level);
        } else if (target instanceof Pig) {
            addButcheringMeat(event, target, target.isOnFire() ? Items.COOKED_PORKCHOP : Items.PORKCHOP, level);
        } else if (target instanceof Sheep) {
            addButcheringMeat(event, target, target.isOnFire() ? Items.COOKED_MUTTON : Items.MUTTON, level);
        } else if (target instanceof Horse) {
            addEntityDrop(event, new ItemStack(
                    target.isOnFire() ? Items.COOKED_BEEF : Items.BEEF,
                    EnchantmentRules.horseButcheringBeefCount(level, target.getRandom())));
        } else if (target instanceof Spider
                && event.getDrops().stream().noneMatch(drop -> drop.getItem().is(Items.SPIDER_EYE))
                && EnchantmentRules.butcheringAddsSpiderEye(level, target.getRandom())) {
            addEntityDrop(event, new ItemStack(Items.SPIDER_EYE));
        }
    }

    @SubscribeEvent

    private static void onBlockDrops(BlockDropsEvent event) {
        addHarvestingDrops(event);
        addFortuneDrops(event);
    }

    private static void addButcheringMeat(LivingDropsEvent event, LivingEntity target, Item meat, int level) {
        int extra = EnchantmentRules.butcheringExtraCount(level, target.getRandom());
        if (extra > 0) {
            addEntityDrop(event, new ItemStack(meat, extra));
        }
    }

    private static void addEntityDrop(LivingDropsEvent event, ItemStack stack) {
        LivingEntity entity = event.getEntity();
        event.getDrops().add(new ItemEntity(
                entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
    }

    private static void addHarvestingDrops(BlockDropsEvent event) {
        Item crop = matureCropProduct(event.getState());
        if (crop == null) return;
        int harvesting = Enchantments.level(event.getLevel(), event.getTool(), InfXEnchantments.HARVESTING);
        if (harvesting <= 0 || !event.getTool().isCorrectToolForDrops(event.getState())) return;
        for (ItemEntity original : List.copyOf(event.getDrops())) {
            if (!original.getItem().is(crop)) continue;
            int extra = EnchantmentRules.harvestingBonusCount(
                    original.getItem().getCount(), harvesting, event.getLevel().getRandom());
            if (extra > 0) {
                event.getDrops().add(new ItemEntity(
                        original.level(), original.getX(), original.getY(), original.getZ(),
                        original.getItem().copyWithCount(extra)));
            }
        }
    }

    private static Item matureCropProduct(BlockState state) {
        if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return null;
        if (state.is(Blocks.WHEAT)) return Items.WHEAT;
        if (state.is(Blocks.CARROTS)) return Items.CARROT;
        if (state.is(Blocks.POTATOES)) return Items.POTATO;
        return state.is(Blocks.BEETROOTS) ? Items.BEETROOT : null;
    }

    private static void addFortuneDrops(BlockDropsEvent event) {
        int fortune = Enchantments.level(event.getLevel(), event.getTool(), InfXEnchantments.FORTUNE);
        if (fortune <= 0) return;

        BlockState state = event.getState();
        if (isFortuneOre(state)) {
            for (ItemEntity original : List.copyOf(event.getDrops())) {
                int extra = EnchantmentRules.fortuneOreBonusCount(
                        original.getItem().getCount(), fortune, event.getLevel().getRandom());
                if (extra > 0) {
                    event.getDrops().add(new ItemEntity(
                            original.level(), original.getX(), original.getY(), original.getZ(),
                            original.getItem().copyWithCount(extra)));
                }
            }
            return;
        }

        if (state.is(Blocks.NETHER_WART) && state.getValue(NetherWartBlock.AGE) == 3) {
            int extra = EnchantmentRules.netherWartFortuneBonus(fortune, event.getLevel().getRandom());
            if (extra > 0) {
                event.getDrops().add(new ItemEntity(
                        event.getLevel(),
                        event.getPos().getX() + 0.5D,
                        event.getPos().getY() + 0.5D,
                        event.getPos().getZ() + 0.5D,
                        new ItemStack(Items.NETHER_WART, extra)));
            }
        }
    }

    private static boolean isFortuneOre(BlockState state) {
        return state.is(Tags.Blocks.ORES)
                || state.is(InfXBlocks.SILVER_ORE.get())
                || state.is(InfXBlocks.MITHRIL_ORE.get())
                || state.is(InfXBlocks.ADAMANTIUM_ORE.get());
    }

    @SubscribeEvent

    private static void onBlockBroken(BreakBlockEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) return;
        fertilizeMatureCrop(event, level);
        if (felling || !event.getState().is(BlockTags.LOGS)) return;
        ItemStack tool = event.getPlayer().getMainHandItem();
        if (!tool.is(InfXItemTags.R196_TREE_FELLING_ENCHANTABLE)) return;
        int enchantment = Enchantments.level(level, tool, InfXEnchantments.TREE_FELLING);
        if (enchantment <= 0) return;
        felling = true;
        try {
            int remaining = EnchantmentRules.treeFellingExtraLogs(enchantment);
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
        int fertility = Enchantments.level(level, event.getPlayer().getMainHandItem(), InfXEnchantments.FERTILITY);
        if (fertility > 0 && level.getRandom().nextFloat() < EnchantmentRules.fertilityChance(fertility)) {
            AgricultureData.get(level).fertilize(event.getPos().below(), level.getGameTime());
        }
    }

    @SubscribeEvent

    private static void onToolModified(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()
                || event.getItemAbility() != ItemAbilities.HOE_TILL
                || !(event.getLevel() instanceof ServerLevel level)
                || event.getFinalState() == null
                || !event.getFinalState().is(Blocks.FARMLAND)) return;
        int fertility = Enchantments.level(level, event.getHeldItemStack(), InfXEnchantments.FERTILITY);
        if (fertility > 0 && level.getRandom().nextFloat() < EnchantmentRules.fertilityChance(fertility)) {
            AgricultureData.get(level).fertilize(event.getPos(), level.getGameTime());
        }
    }

    @SubscribeEvent

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
        MobEffectInstance paralysis = entity.getEffect(InfXMobEffects.PARALYSIS);
        if (slowness == null && paralysis == null) return;
        int freeMovement = Enchantments.maxArmorLevel(entity, InfXEnchantments.FREE_MOVEMENT);
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
        double adjusted = EnchantmentRules.freeMovementAdjustedImpairment(
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

    @SubscribeEvent

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        var movement = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        int speed = Enchantments.maxArmorLevel(player, InfXEnchantments.SPEED);
        if (speed > 0) {
            movement.addOrUpdateTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    SPEED, speed * 0.05D,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            movement.removeModifier(SPEED);
        }
    }
}
