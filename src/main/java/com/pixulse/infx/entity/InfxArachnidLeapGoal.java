package com.pixulse.infx.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** INFX arachnid pounce: two-to-six blocks, attempted once in ten while grounded. */
final class InfxArachnidLeapGoal extends Goal {
    private final InfxSpider spider;
    private LivingEntity target;

    InfxArachnidLeapGoal(InfxSpider spider) {
        this.spider = spider;
        setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        target = spider.getTarget();
        return target != null
                && !spider.hasControllingPassenger()
                && AttackRanges.isArachnidLeapDistance(spider.distanceToSqr(target))
                && spider.onGround()
                && spider.getRandom().nextInt(10) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !spider.onGround();
    }

    @Override
    public void start() {
        Vec3 movement = spider.getDeltaMovement();
        Vec3 direction = new Vec3(target.getX() - spider.getX(), 0.0, target.getZ() - spider.getZ());
        if (direction.lengthSqr() > 1.0E-7) {
            direction = direction.normalize().scale(0.4).add(movement.scale(0.2));
        }
        spider.setDeltaMovement(direction.x, 0.4, direction.z);
    }
}
