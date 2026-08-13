package com.pixulse.infx.entity;

import com.pixulse.infx.data.nightwing.NightwingDimming;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.equipment.EquipmentBehaviors;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** INFX ambient bats. Vampire variants heal on contact; Nightwings dim player vision. */
public final class InfxBat extends Bat implements InfxMob {
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final int FEED_COOLDOWN_TICKS = 1_200;

    public enum Variant {
        NORMAL,
        VAMPIRE,
        NIGHTWING,
        GIANT_VAMPIRE
    }

    private int attackCooldown;
    private int feedCooldown;
    private @Nullable LivingEntity prey;

    public InfxBat(EntityType<? extends Bat> type, Level level) {
        super(type, level);
        xpReward = switch (variant()) {
            case NORMAL -> 0;
            case VAMPIRE -> 5;
            case NIGHTWING, GIANT_VAMPIRE -> 10;
        };
    }

    public Variant variant() {
        return switch (EntityVariant.path(this)) {
            case "infx_bat" -> Variant.NORMAL;
            case "nightwing" -> Variant.NIGHTWING;
            case "giant_vampire_bat" -> Variant.GIANT_VAMPIRE;
            case "vampire_bat" -> Variant.VAMPIRE;
            default -> Variant.NORMAL;
        };
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        AttributeSupplier.Builder attributes = Bat.createAttributes()
                .add(Attributes.MAX_HEALTH, variant == Variant.GIANT_VAMPIRE ? 6.0 : 3.0);
        if (variant == Variant.NORMAL) {
            return attributes;
        }
        return attributes
                .add(Attributes.ATTACK_DAMAGE, variant == Variant.GIANT_VAMPIRE ? 2.0 : 1.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    /** InfX nightwings only fall to silver, magic or sunlight. */
    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        if (variant() == Variant.NIGHTWING && !MobDamageRules.silverMagicGateAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected void customServerAiStep(@NonNull ServerLevel level) {
        super.customServerAiStep(level);
        Variant variant = variant();
        if (variant == Variant.NORMAL) {
            return;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (variant == Variant.VAMPIRE && feedCooldown > 0) {
            if (getHealth() < getMaxHealth()) {
                feedCooldown = 0;
            } else {
                feedCooldown--;
                if (prey != null && !isPrey(prey)) {
                    prey = null;
                }
            }
        }

        if (prey != null && (!prey.isAlive() || prey.isRemoved() || !isPrey(prey))) {
            prey = null;
        }
        if (tickCount % 20 == 0) {
            prey = findPrey(level);
        }
        LivingEntity target = prey;
        if (target != null) {
            setResting(false);
            Vec3 direction = target.getEyePosition().subtract(position());
            double distance = direction.length();
            if (distance > 0.001) {
                double speed = variant() == Variant.GIANT_VAMPIRE ? 0.22 : 0.28;
                setDeltaMovement(getDeltaMovement().scale(0.65).add(direction.normalize().scale(speed)));
            }

            if (hasAttackContact(target) && attackCooldown == 0) {
                float before = target.getHealth();
                if (doHurtTarget(level, target)) {
                    float dealt = Math.max(0.0F, before - target.getHealth());
                    if (variant == Variant.NIGHTWING) {
                        if (dealt > 0.0F && target instanceof ServerPlayer player) {
                            NightwingDimming.apply(player, nightwingDimmingAmount(player));
                        }
                    } else {
                        if (dealt > 0.0F) {
                            heal(dealt);
                            if (target instanceof Ocelot ocelot
                                    && ocelot.isAlive()
                                    && ocelot.getTarget() == null
                                    && MonsterEvents.withinFollowRange(ocelot, this)) {
                                ocelot.setTarget(this);
                            }
                            if (variant == Variant.VAMPIRE && getHealth() >= getMaxHealth()) {
                                feedCooldown = feedCooldownTicks();
                            }
                        }
                    }
                }
                attackCooldown = attackCooldownTicks();
            }
        }

        if (variant == Variant.NIGHTWING) {
            if (level.isBrightOutside() && level.canSeeSky(blockPosition()) && !level.isRaining()) {
                // InfX nightwings take 1000 sunlight damage: certain death.
                hurtServer(level, damageSources().genericKill(), 1000.0F);
            } else if (tickCount % 40 == 0) {
                int darknessHeal = (int) ((0.4F - getLightLevelDependentMagicValue()) * 10.0F);
                if (darknessHeal > 0) {
                    heal(darknessHeal);
                }
            }
        }
    }

    /** Shared INFX half-width contact predicate for all three hostile bat variants. */
    public boolean hasAttackContact(LivingEntity target) {
        return AttackRanges.scaledHorizontalContact(this, target, 0.5);
    }

    /** InfX shadow resistance is half of the worn silver armor coverage. */
    static float nightwingDimmingAmount(Player player) {
        float silverCoverage = 0.0F;
        for (EquipmentSlot slot : List.of(
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            var stack = player.getItemBySlot(slot);
            var equipment = InfXItems.catalog().equipment(stack);
            if (equipment == null || equipment.key().material() != InfxMaterial.SILVER) {
                continue;
            }
            EquipmentType type = equipment.key().type();
            float coverage = switch (type.armorForm()) {
                case PLATE -> type.durabilityComponents() / 24.0F;
                case CHAIN -> type.durabilityComponents() / 48.0F;
                default -> 0.0F;
            };
            silverCoverage += coverage
                    * EquipmentBehaviors.armorDurabilityFactor(stack.getDamageValue(), stack.getMaxDamage());
        }
        return nightwingDimmingAfterSilverCoverage(silverCoverage);
    }

    static float nightwingDimmingAfterSilverCoverage(float silverCoverage) {
        float resistance = Math.clamp(silverCoverage, 0.0F, 1.0F) * 0.5F;
        return 1.25F * (1.0F - resistance);
    }

    /** Hostile InfX bats use their runtime follow range for prey acquisition. */
    private @Nullable LivingEntity findPrey(ServerLevel level) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        double followRange = getAttributeValue(Attributes.FOLLOW_RANGE);
        double followRangeSqr = followRange * followRange;
        for (LivingEntity candidate : level.getEntitiesOfClass(
                LivingEntity.class,
                getBoundingBox().inflate(followRange),
                entity -> entity.isAlive() && isPrey(entity) && hasLineOfSight(entity))) {
            double distance = distanceToSqr(candidate);
            if (distance <= followRangeSqr && distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private boolean isPrey(LivingEntity entity) {
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        if (entity instanceof InfxWolf wolf && wolf.variant() == InfxWolf.Variant.HELLHOUND) {
            return false;
        }
        return !restrictsPreyToPlayers() && (entity instanceof Animal || entity instanceof Villager);
    }

    private boolean restrictsPreyToPlayers() {
        return variant() == Variant.VAMPIRE && feedCooldown > 0;
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (feedCooldown > 0) {
            output.putInt("R196VampireBatFeedCooldown", feedCooldown);
        }
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        feedCooldown = Math.max(0, input.getIntOr("R196VampireBatFeedCooldown", 0));
    }

    static int attackCooldownTicks() {
        return ATTACK_COOLDOWN_TICKS;
    }

    /** InfX vampire bats need a full minute of full health before resuming animal feeding. */
    static int feedCooldownTicks() {
        return FEED_COOLDOWN_TICKS;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return variant() == Variant.NORMAL ? 4 : 8;
    }
}
