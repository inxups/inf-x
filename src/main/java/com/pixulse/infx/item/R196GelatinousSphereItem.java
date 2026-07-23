package com.pixulse.infx.item;

import com.pixulse.infx.equipment.R196CorrosionType;
import com.pixulse.infx.entity.R196GelatinousSphere;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

/** One of MITE's five throwable gelatinous spheres. */
public final class R196GelatinousSphereItem extends Item implements ProjectileItem {
    public enum Color {
        GREEN("green", R196CorrosionType.PEPSIN, 1),
        OCHRE("ochre", R196CorrosionType.PEPSIN, 2),
        CRIMSON("crimson", R196CorrosionType.PEPSIN, 3),
        GRAY("gray", R196CorrosionType.ACID, 3),
        BLACK("black", R196CorrosionType.ACID, 4);

        private final String path;
        private final R196CorrosionType corrosionType;
        private final int attackDamage;

        Color(String path, R196CorrosionType corrosionType, int attackDamage) {
            this.path = path;
            this.corrosionType = corrosionType;
            this.attackDamage = attackDamage;
        }

        public String path() {
            return path;
        }

        public R196CorrosionType corrosionType() {
            return corrosionType;
        }

        public int attackDamage() {
            return attackDamage;
        }
    }

    private final Color color;

    public R196GelatinousSphereItem(Color color, Properties properties) {
        super(properties);
        this.color = color;
    }

    public Color color() {
        return color;
    }

    public R196CorrosionType corrosionType() {
        return color.corrosionType();
    }

    public int attackDamage() {
        return color.attackDamage();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(
                    R196GelatinousSphere::new, serverLevel, stack, player, 0.0F, 1.5F, 1.0F);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        return new R196GelatinousSphere(level, position.x(), position.y(), position.z(), stack);
    }
}
