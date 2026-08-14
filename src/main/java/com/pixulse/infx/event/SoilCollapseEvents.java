package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import com.pixulse.infx.world.SoilCollapse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Schedules soil stability checks after world and entity interactions. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class SoilCollapseEvents {
    private SoilCollapseEvents() {}

    @SubscribeEvent
    public static void onBlockBroken(BreakBlockEvent event) {
        if (event.isCanceled() || event.getPlayer().getAbilities().instabuild
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        SoilCollapse.cancelPendingDelay(level, event.getPos());
        SoilCollapse.disturbAround(level, event.getPos());
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)
                || !event.getPlacedBlock().is(InfXBlockTags.GRAVITY_SOILS)) {
            return;
        }
        SoilCollapse.cancelPendingDelay(level, event.getPos());
        SoilCollapse.schedule(level, event.getPos());
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (BlockPos pos : event.getAffectedBlocks()) {
            SoilCollapse.cancelPendingDelay(level, pos);
            SoilCollapse.schedule(level, pos.above());
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)
                || entity.noPhysics
                || !entity.onGround()
                || entity instanceof FallingBlockEntity) {
            return;
        }
        SoilCollapse.schedule(level, BlockPos.containing(entity.getX(), entity.getY() - 0.2D, entity.getZ()));
    }
}
