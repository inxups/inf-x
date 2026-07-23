package com.pixulse.infx.entity;

import com.pixulse.infx.equipment.R196CorrosionRules;
import com.pixulse.infx.equipment.R196CorrosionType;
import com.pixulse.infx.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/** Vanilla slime replacement and the four corrosive R196 gelatinous cubes. */
public final class R196Slime extends Slime implements R196Mob {
    private static final double MOVEMENT_SPEED = 0.70;

    public enum Variant {
        SLIME(1, R196CorrosionType.PEPSIN, 16.0),
        JELLY(2, R196CorrosionType.PEPSIN, 16.0),
        BLOB(3, R196CorrosionType.PEPSIN, 16.0),
        OOZE(3, R196CorrosionType.ACID, 32.0),
        PUDDING(4, R196CorrosionType.ACID, 16.0);

        private final int damageMultiplier;
        private final R196CorrosionType corrosionType;
        private final double followRange;

        Variant(int damageMultiplier, R196CorrosionType corrosionType, double followRange) {
            this.damageMultiplier = damageMultiplier;
            this.corrosionType = corrosionType;
            this.followRange = followRange;
        }

        public int damageMultiplier() {
            return damageMultiplier;
        }

        public R196CorrosionType corrosionType() {
            return corrosionType;
        }

        public double followRange() {
            return followRange;
        }
    }

    private final Map<BlockPos, Integer> dissolvingBlocks = new HashMap<>();

    public R196Slime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    public Variant variant() {
        return switch (R196EntityVariant.path(this)) {
            case "jelly" -> Variant.JELLY;
            case "blob" -> Variant.BLOB;
            case "ooze" -> Variant.OOZE;
            case "pudding" -> Variant.PUDDING;
            default -> Variant.SLIME;
        };
    }

    public static AttributeSupplier.Builder attributes() {
        return attributes(Variant.SLIME);
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, variant.followRange())
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    static double attackDamageForSize(Variant variant, int size) {
        return size * variant.damageMultiplier();
    }

    static int experienceForSize(Variant variant, int size) {
        return size * (variant.damageMultiplier() + (variant.corrosionType() == R196CorrosionType.ACID ? 1 : 0));
    }

    static double movementSpeedForSize(int size) {
        return MOVEMENT_SPEED;
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        if (variant() == Variant.OOZE) {
            size = Math.min(size, 2);
        }
        super.setSize(size, updateHealth);
        var movementSpeed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(movementSpeedForSize(getSize()));
        }
        var attackDamage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamageForSize(variant(), getSize()));
        }
        this.xpReward = experienceForSize(variant(), getSize());
    }

    @Override
    protected boolean isDealsDamage() {
        return isEffectiveAi();
    }

    @Override
    public boolean onClimbable() {
        return super.onClimbable() || (variant() == Variant.OOZE && horizontalCollision);
    }

    @Override
    protected void addTargetingGoals() {
        super.addTargetingGoals();
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        float health = target.getHealth();
        super.dealDamage(target);
        if (target.getHealth() >= health) {
            return;
        }
        Variant variant = variant();
        if (target instanceof net.minecraft.server.level.ServerPlayer player) {
            R196CorrosionRules.damageInventory(
                    player,
                    variant.corrosionType(),
                    0.05F * getSize(),
                    (float) attackDamageForSize(variant, getSize()));
        }
        if (variant == Variant.BLOB) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!acceptsDamage(level, source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    public Item gelatinousSphere() {
        return switch (variant()) {
            case SLIME -> ModItems.GREEN_GELATINOUS_SPHERE.get();
            case JELLY -> ModItems.OCHRE_GELATINOUS_SPHERE.get();
            case BLOB -> ModItems.CRIMSON_GELATINOUS_SPHERE.get();
            case OOZE -> ModItems.GRAY_GELATINOUS_SPHERE.get();
            case PUDDING -> ModItems.BLACK_GELATINOUS_SPHERE.get();
        };
    }

    boolean advanceDissolvingBlock(BlockPos pos, int period) {
        int progress = dissolvingBlocks.merge(pos.immutable(), getSize() * 20, Integer::sum);
        if (progress < period) {
            return false;
        }
        dissolvingBlocks.remove(pos);
        return true;
    }

    void clearDissolvingBlock(BlockPos pos) {
        dissolvingBlocks.remove(pos);
    }

    private boolean acceptsDamage(ServerLevel level, DamageSource source) {
        Variant variant = variant();
        if (variant != Variant.OOZE && variant != Variant.PUDDING) {
            return true;
        }
        if (source.is(DamageTypes.LAVA)
                || source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                || source.getDirectEntity() instanceof Snowball) {
            return true;
        }
        return variant == Variant.PUDDING && hasFireEnchantment(level, source.getWeaponItem());
    }

    private static boolean hasFireEnchantment(ServerLevel level, ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) {
            return false;
        }
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return weapon.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FIRE_ASPECT)) > 0
                || weapon.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FLAME)) > 0;
    }
}
