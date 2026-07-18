package com.pixulse.infx.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.Level;

/** The R196 magma cube only accepts mundane player damage from mining tools. */
public final class R196MagmaCube extends MagmaCube implements R196Mob {
    public R196MagmaCube(EntityType<? extends MagmaCube> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return MagmaCube.createAttributes();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player
                && !(source.getDirectEntity() instanceof Snowball)
                && !player.getMainHandItem().is(ItemTags.PICKAXES)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }
}
