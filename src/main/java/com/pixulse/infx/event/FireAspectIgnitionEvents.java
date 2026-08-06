package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.registry.InfXEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * A sword enchanted with fire aspect can light TNT and unlit (non-waterlogged)
 * campfires by right-click, at the cost of one durability point, mirroring
 * flint and steel. The ignition itself reuses the public NeoForge
 * {@code IBlockStateExtension#onCaughtFire} API for TNT so the {@code
 * TNT_EXPLODES} gamerule is respected.
 */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class FireAspectIgnitionEvents {
    private static final int IGNITION_DURABILITY_COST = 1;

    private FireAspectIgnitionEvents() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.isSpectator()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !hasFireAspect(player, stack)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!ignite(level, pos, state, player, event.getFace())) {
            return;
        }
        stack.hurtAndBreak(IGNITION_DURABILITY_COST, player, event.getHand().asEquipmentSlot());
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        player.swing(event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static boolean hasFireAspect(Player player, ItemStack stack) {
        return Enchantments.level(player.level(), stack, InfXEnchantments.VANILLA_FIRE_ASPECT) > 0;
    }

    private static boolean ignite(
            ServerLevel level, BlockPos pos, BlockState state, Player player, Direction face) {
        if (state.is(Blocks.TNT)) {
            if (!state.onCaughtFire(level, pos, face, player)) {
                return false;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            return true;
        }
        if (state.getBlock() instanceof CampfireBlock
                && !state.getValue(CampfireBlock.LIT)
                && !state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.setBlock(pos, state.setValue(CampfireBlock.LIT, true), 11);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.FLINTANDSTEEL_USE,
                    SoundSource.BLOCKS,
                    1.0F,
                    level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }
}
