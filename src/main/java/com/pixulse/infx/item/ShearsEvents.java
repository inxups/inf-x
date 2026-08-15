package com.pixulse.infx.item;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** Material-shears block cutting: right-click shears, left-click cuts effective blocks. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ShearsEvents {
    private ShearsEvents() {}

    @SubscribeEvent
    public static void preventLeftClickBlockBreaking(BreakBlockEvent event) {
        if (!(event.getPlayer().getMainHandItem().getItem() instanceof InfxShearsItem)
                || InfxShearsItem.isRightClickShearing(event.getLevel(), event.getPos())) {
            return;
        }
        BlockState state = event.getState();
        if (!state.is(InfXBlockTags.effectiveWith(MiningFamily.SHEARS))) {
            event.setCanceled(true);
            event.setNotifyClient(true);
        }
    }

    /**
     * InfX shears harvest one piece of string from cobwebs instead of the modern cobweb block.
     * Every other tool (including the hand) destroys cobwebs without dropping anything.
     * Right-click shearing is exempt: it drops the cobweb block itself via the shears loot table.
     */
    @SubscribeEvent
    public static void onCobwebDrops(BlockDropsEvent event) {
        if (!event.getState().is(Blocks.COBWEB)) {
            return;
        }
        if (InfxShearsItem.isRightClickShearing(event.getLevel(), event.getPos())) {
            return;
        }
        ItemStack tool = event.getTool();
        boolean shears = tool.getItem() instanceof InfxShearsItem || tool.is(Items.SHEARS);
        event.getDrops().clear();
        if (shears) {
            event.getDrops().add(new ItemEntity(
                    event.getLevel(),
                    event.getPos().getX() + 0.5D,
                    event.getPos().getY() + 0.5D,
                    event.getPos().getZ() + 0.5D,
                    new ItemStack(Items.STRING)));
        }
    }
}
