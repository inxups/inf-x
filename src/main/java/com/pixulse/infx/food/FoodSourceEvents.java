package com.pixulse.infx.food;

import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.registry.InfinityXEnchantments;
import com.pixulse.infx.registry.InfinityXItems;
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
public final class FoodSourceEvents {
    private FoodSourceEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(FoodSourceEvents::onBlockDrops);
        gameBus.addListener(FoodSourceEvents::onLivingDrops);
    }

    private static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player)) return;
        var level = event.getLevel();
        var state = event.getState();
        var random = level.getRandom();

        int fortune = Enchantments.level(level, event.getTool(), InfinityXEnchantments.FORTUNE);
        if (state.is(Blocks.GRASS_BLOCK)
                && level.getBiome(event.getPos()).value().getBaseTemperature() > 0.15F
                && random.nextInt(EnchantmentRules.grassWormDenominator(
                        fortune, level.isRainingAt(event.getPos().above()))) == 0) {
            event.getDrops().clear();
            addDrop(event, InfinityXItems.WORM.toStack());
            return;
        }
        if (state.is(Blocks.SWEET_BERRY_BUSH)
                && state.getValue(SweetBerryBushBlock.AGE) >= 2) {
            addDrop(event, InfinityXItems.BLUEBERRIES.toStack(1 + random.nextInt(2)));
            return;
        }
        if (!event.getDrops().isEmpty() || random.nextFloat() >= 0.005F) return;
        if (state.is(Blocks.JUNGLE_LEAVES)) {
            addDrop(event, InfinityXItems.BANANA.toStack());
        } else if (state.is(Blocks.OAK_LEAVES) && level.getBiome(event.getPos()).is(BiomeTags.IS_JUNGLE)) {
            addDrop(event, InfinityXItems.ORANGE.toStack());
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
                InfinityXItems.ONION.toStack()));
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
