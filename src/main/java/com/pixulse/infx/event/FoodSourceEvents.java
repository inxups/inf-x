package com.pixulse.infx.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.registry.InfXEnchantments;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Survival sources for INFX foods that are not represented by modern vanilla crops. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class FoodSourceEvents {
    private FoodSourceEvents() {}

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player)) return;
        var level = event.getLevel();
        var state = event.getState();
        var random = level.getRandom();

        int fortune = Enchantments.level(level, event.getTool(), InfXEnchantments.FORTUNE);
        if (state.is(Blocks.GRASS_BLOCK)
                && level.getBiome(event.getPos()).value().getBaseTemperature() > 0.15F
                && random.nextInt(EnchantmentRules.grassWormDenominator(
                        fortune, level.isRainingAt(event.getPos().above()))) == 0) {
            event.getDrops().clear();
            addDrop(event, InfXItems.WORM.toStack());
            return;
        }
        if (!event.getDrops().isEmpty() || random.nextFloat() >= 0.005F) return;
        if (state.is(Blocks.JUNGLE_LEAVES)) {
            addDrop(event, InfXItems.BANANA.toStack());
        } else if (state.is(Blocks.OAK_LEAVES) && level.getBiome(event.getPos()).is(BiomeTags.IS_JUNGLE)) {
            addDrop(event, InfXItems.ORANGE.toStack());
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player)
                || !(event.getEntity() instanceof Zombie || event.getEntity() instanceof Witch)
                || event.getEntity().getRandom().nextInt(20) != 0) {
            return;
        }
        event.getDrops().add(new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                InfXItems.ONION.toStack()));
    }

    private static void addDrop(BlockDropsEvent event, ItemStack stack) {
        event.getDrops().add(new ItemEntity(
                event.getLevel(),
                event.getPos().getX() + 0.5D,
                event.getPos().getY() + 0.5D,
                event.getPos().getZ() + 0.5D,
                stack));
    }
}
