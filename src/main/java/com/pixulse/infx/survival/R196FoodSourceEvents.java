package com.pixulse.infx.survival;

import com.pixulse.infx.enchantment.R196Enchantments;
import com.pixulse.infx.enchantment.R196EnchantmentRules;
import com.pixulse.infx.registry.ModEnchantments;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Survival sources for R196 foods that are not represented by modern vanilla crops. */
public final class R196FoodSourceEvents {
    private R196FoodSourceEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196FoodSourceEvents::onBlockDrops);
        gameBus.addListener(R196FoodSourceEvents::onLivingDrops);
    }

    private static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player)) return;
        var level = event.getLevel();
        var state = event.getState();
        var random = level.getRandom();

        int fortune = R196Enchantments.level(level, event.getTool(), ModEnchantments.FORTUNE);
        if (state.is(Blocks.GRASS_BLOCK)
                && level.getBiome(event.getPos()).value().getBaseTemperature() > 0.15F
                && random.nextInt(R196EnchantmentRules.grassWormDenominator(
                        fortune, level.isRainingAt(event.getPos().above()))) == 0) {
            event.getDrops().clear();
            addDrop(event, ModItems.WORM.toStack());
            return;
        }
        if (state.is(Blocks.SWEET_BERRY_BUSH)
                && state.getValue(SweetBerryBushBlock.AGE) >= 2) {
            addDrop(event, ModItems.BLUEBERRIES.toStack(1 + random.nextInt(2)));
            return;
        }
        if (!event.getDrops().isEmpty() || random.nextFloat() >= 0.005F) return;
        if (state.is(Blocks.JUNGLE_LEAVES)) {
            addDrop(event, ModItems.BANANA.toStack());
        } else if (state.is(Blocks.OAK_LEAVES) && level.getBiome(event.getPos()).is(BiomeTags.IS_JUNGLE)) {
            addDrop(event, ModItems.ORANGE.toStack());
        }
    }

    private static void onLivingDrops(LivingDropsEvent event) {
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
                ModItems.ONION.toStack()));
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
