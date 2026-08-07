package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.entity.InfxBrickProjectile;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** MITE ItemBrick#onItemRightClick: brick, nether brick and resin brick throw on right-click. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ThrowEvents {
    private static final Set<Item> BRICKS = Set.of(Items.BRICK, Items.NETHER_BRICK, Items.RESIN_BRICK);

    private ThrowEvents() {}

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!BRICKS.contains(stack.getItem()) || player.isSpectator()) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        // MITE ItemBrick: bow sound at 0.5 volume, then a 1.5-speed throwable.
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        Projectile.spawnProjectileFromRotation(
                InfxBrickProjectile::new, level, stack, player, 0.0F, 1.5F, 1.0F);
        player.swing(event.getHand());
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (!player.hasInfiniteMaterials()) {
            stack.consume(1, player);
        }
    }
}
