package com.pixulse.infx.mixin.world.level.levelgen.feature;

import com.pixulse.infx.config.InfXConfig;
import com.pixulse.infx.world.SpawnGate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE dungeon depth layering. Vanilla {@link MonsterRoomFeature} picks a uniform mob from the
 * {@code monster_room_mobs} datamap; MITE instead tiers the mob by the room's depth
 * ({@code WorldGenDungeons.pickMobSpawner}). The current y is captured at {@code place} entry and
 * applied when {@code randomEntityId} rolls the type. Generation runs single-threaded during chunk
 * baking, so the thread-local y cannot leak across rooms.
 */
@Mixin(MonsterRoomFeature.class)
public abstract class MonsterRoomFeatureMixin {
    @Unique
    private static final ThreadLocal<Integer> infx$currentY = new ThreadLocal<>();

    @Inject(method = "place", at = @At("HEAD"))
    private void infx$captureY(
            FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        infx$currentY.set(context.origin().getY());
    }

    @Inject(method = "randomEntityId", at = @At("HEAD"), cancellable = true)
    private void infx$depthLayeredEntity(RandomSource random, CallbackInfoReturnable<EntityType<?>> cir) {
        Integer y = infx$currentY.get();
        if (y != null && SpawnGate.enabled() && InfXConfig.INSTANCE.mobs.spawnerDepthLayering.getValue()) {
            cir.setReturnValue(SpawnGate.spawnerDepthType(random, y));
        }
    }
}
