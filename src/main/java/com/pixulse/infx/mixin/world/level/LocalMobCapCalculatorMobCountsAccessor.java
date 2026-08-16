package com.pixulse.infx.mixin.world.level;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the per-player category counts so the depth-spawn cap can read them. */
@Mixin(LocalMobCapCalculator.MobCounts.class)
public interface LocalMobCapCalculatorMobCountsAccessor {
    @Accessor("counts")
    Object2IntMap<MobCategory> counts();
}
