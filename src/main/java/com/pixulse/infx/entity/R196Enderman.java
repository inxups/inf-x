package com.pixulse.infx.entity;

import com.pixulse.infx.curse.R196CurseManager;
import com.pixulse.infx.curse.R196CurseType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Enderman replacement with pearl awareness and projectile damage support. */
public final class R196Enderman extends EnderMan implements R196Mob {
    private static final Identifier MITE_CHASE_SPEED_ID = Identifier.fromNamespaceAndPath("infx", "mite_chase_speed");
    private static final Identifier VANILLA_CHASE_SPEED_ID = Identifier.withDefaultNamespace("attacking");
    private static final double MITE_CHASE_SPEED_MULTIPLIER = 6.5 / 0.3;
    private static final AttributeModifier MITE_CHASE_SPEED = new AttributeModifier(
            MITE_CHASE_SPEED_ID,
            MITE_CHASE_SPEED_MULTIPLIER - 1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    public R196Enderman(EntityType<? extends EnderMan> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return EnderMan.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        AttributeInstance movementSpeed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        // The old modifier was an additive +6.2 on MITE's 0.3 base (6.5 total).
        // Express that as a multiplier so it remains tied to the registered modern base
        // and replaces 26.2's unrelated +0.15 attacking modifier.
        movementSpeed.removeModifier(VANILLA_CHASE_SPEED_ID);
        boolean chasing = getTarget() != null;
        if (chasing && !movementSpeed.hasModifier(MITE_CHASE_SPEED_ID)) {
            movementSpeed.addTransientModifier(MITE_CHASE_SPEED);
        } else if (!chasing && movementSpeed.hasModifier(MITE_CHASE_SPEED_ID)) {
            movementSpeed.removeModifier(MITE_CHASE_SPEED_ID);
        }
    }

    static double chasingMovementSpeed(double baseMovementSpeed) {
        return baseMovementSpeed * MITE_CHASE_SPEED_MULTIPLIER;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // MITE checks the nearest player every tick: held pearls anger immediately, while
        // inventory pearls draw attention at one roll in 2000 per pearl per tick.
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                0,
                true,
                false,
                (target, level) -> {
                    if (!(target instanceof Player player)) {
                        return false;
                    }
                    if (random.nextInt(3) == 0
                            && R196CurseManager.hasCurse(player, R196CurseType.ENDERMEN_AGGRO)) {
                        R196CurseManager.reveal(player, R196CurseType.ENDERMEN_AGGRO);
                        return true;
                    }
                    if (isPearlLike(player.getMainHandItem()) || isPearlLike(player.getOffhandItem())) {
                        return true;
                    }
                    int pearls = player.getInventory().countItem(Items.ENDER_PEARL)
                            + player.getInventory().countItem(Items.ENDER_EYE);
                    return pearls > 0 && random.nextInt(2000) < pearls;
                }));
    }

    private static boolean isPearlLike(net.minecraft.world.item.ItemStack stack) {
        return stack.is(Items.ENDER_PEARL) || stack.is(Items.ENDER_EYE);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getEntity() instanceof LivingEntity attacker) {
            DamageSource direct = attacker instanceof Player player
                    ? level.damageSources().playerAttack(player)
                    : level.damageSources().mobAttack(attacker);
            boolean hurt = super.hurtServer(level, direct, damage);
            if (hurt) {
                // MITE endermen take the projectile hit, then drop aggression and blink away.
                setTarget(null);
                for (int attempt = 0; attempt < 64 && !teleport(); attempt++) {}
            }
            return hurt;
        }
        return super.hurtServer(level, source, damage);
    }
}
