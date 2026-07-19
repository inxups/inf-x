package com.pixulse.infx.enchantment;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.agriculture.R196AgricultureData;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.registry.ModMobEffects;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Runtime effects for R196's data-driven enchantment registrations. */
public final class R196EnchantmentEvents {
    private static final net.minecraft.resources.Identifier SPEED = InfiniteX.id("enchantment_speed");
    private static boolean felling;

    private R196EnchantmentEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(EventPriority.HIGH, R196EnchantmentEvents::onIncomingDamage);
        gameBus.addListener(R196EnchantmentEvents::onLivingDrops);
        gameBus.addListener(R196EnchantmentEvents::onBlockDrops);
        gameBus.addListener(R196EnchantmentEvents::onBlockBroken);
        gameBus.addListener(R196EnchantmentEvents::onRightClickBlock);
        gameBus.addListener(R196EnchantmentEvents::onPlayerTick);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            int poisoning = arrow.getPersistentData().getInt("infx_poisoning_enchantment").orElse(0);
            if (poisoning > 0) {
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 100 * poisoning, poisoning - 1));
            }
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            int cleaving = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.CLEAVING);
            int penetration = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.PENETRATION);
            int slaughter = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.SLAUGHTER);
            float multiplier = 1.0F + cleaving * 0.12F + penetration * 0.08F;
            if (event.getEntity() instanceof Animal) multiplier += slaughter * 0.2F;
            event.setAmount(event.getAmount() * multiplier);

            int poisoning = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.POISONING);
            if (poisoning > 0) event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 100 * poisoning, poisoning - 1), attacker);
            int stunning = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.STUNNING);
            if (stunning > 0 && attacker.getRandom().nextFloat() < 0.12F * stunning) {
                event.getEntity().addEffect(new MobEffectInstance(ModMobEffects.PARALYSIS, 20 + stunning * 20, stunning - 1), attacker);
            }
            int disarming = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.DISARMING);
            if (disarming > 0 && attacker.level() instanceof ServerLevel level
                    && attacker.getRandom().nextFloat() < 0.1F * disarming) {
                ItemStack held = event.getEntity().getMainHandItem();
                if (!held.isEmpty()) {
                    event.getEntity().spawnAtLocation(level, held.copy());
                    event.getEntity().setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                }
            }
            int vampirism = R196Enchantments.level(attacker.level(), weapon, ModEnchantments.VAMPIRISM);
            if (vampirism > 0 && attacker.getHealth() < attacker.getMaxHealth()) {
                attacker.heal(event.getAmount() * 0.05F * vampirism);
            }

            if (attacker instanceof Player player && player.hasEffect(MobEffects.STRENGTH)
                    && event.getSource().getDirectEntity() == player) {
                int strength = player.getEffect(MobEffects.STRENGTH).getAmplifier() + 1;
                float estimatedBase = Math.max(0.0F, event.getAmount() - strength * 3.0F);
                event.setAmount(estimatedBase * (1.0F + strength * 0.4F));
            }
        }
        int protection = R196Enchantments.armorLevel(event.getEntity(), ModEnchantments.PROTECTION);
        if (protection > 0) event.setAmount(Math.max(1.0F, event.getAmount() - protection * 0.5F));
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || event.getDrops().isEmpty()) return;
        int level = R196Enchantments.level(attacker.level(), attacker.getMainHandItem(), ModEnchantments.BUTCHERING);
        if (level <= 0 || !(event.getEntity() instanceof Animal)) return;
        for (ItemEntity original : new ArrayList<>(event.getDrops())) {
            if (attacker.getRandom().nextFloat() < 0.25F * level) {
                event.getDrops().add(new ItemEntity(
                        original.level(), original.getX(), original.getY(), original.getZ(), original.getItem().copy()));
            }
        }
    }

    private static void onBlockDrops(BlockDropsEvent event) {
        int harvesting = R196Enchantments.level(event.getLevel(), event.getTool(), ModEnchantments.HARVESTING);
        int fortune = R196Enchantments.level(event.getLevel(), event.getTool(), ModEnchantments.FORTUNE);
        int level = event.getState().is(BlockTags.CROPS) ? harvesting : fortune;
        if (level <= 0 || event.getDrops().isEmpty() || event.getLevel().getRandom().nextFloat() >= 0.2F * level) return;
        ItemEntity original = event.getDrops().getFirst();
        event.getDrops().add(new ItemEntity(
                event.getLevel(), original.getX(), original.getY(), original.getZ(), original.getItem().copy()));
    }

    private static void onBlockBroken(BreakBlockEvent event) {
        if (felling || !(event.getLevel() instanceof ServerLevel level) || !event.getState().is(BlockTags.LOGS)) return;
        int enchantment = R196Enchantments.level(level, event.getPlayer().getMainHandItem(), ModEnchantments.TREE_FELLING);
        if (enchantment <= 0) return;
        felling = true;
        try {
            int remaining = enchantment * 4;
            for (int y = 1; y <= 16 && remaining > 0; y++) {
                BlockPos pos = event.getPos().above(y);
                if (!level.getBlockState(pos).is(BlockTags.LOGS)) break;
                level.destroyBlock(pos, true, event.getPlayer());
                remaining--;
            }
        } finally {
            felling = false;
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.getBlockState(event.getPos()).is(Blocks.FARMLAND)) return;
        int fertility = R196Enchantments.level(level, event.getItemStack(), ModEnchantments.FERTILITY);
        if (fertility > 0) R196AgricultureData.get(level).fertilize(event.getPos(), level.getGameTime());
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement == null) return;
        int speed = R196Enchantments.armorLevel(player, ModEnchantments.SPEED);
        if (speed > 0) {
            movement.addOrUpdateTransientModifier(new AttributeModifier(
                    SPEED, speed * 0.05D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            movement.removeModifier(SPEED);
        }
    }
}
