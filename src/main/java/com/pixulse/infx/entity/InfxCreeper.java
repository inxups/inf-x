package com.pixulse.infx.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;

/** Creeper replacement and the terrain-breaking Infernal Creeper. */
public final class InfxCreeper extends Creeper implements InfxMob {
    private static final int CACTUS_FUSE_WINDOW_TICKS = 120;

    public enum Variant {
        CREEPER,
        INFERNAL
    }

    public InfxCreeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
        if (variant() == Variant.INFERNAL) {
            // InfX infernal creepers are worth triple the base experience.
            xpReward = 15;
        }
    }

    private boolean amplifyingExplosion;
    private int cactusFuseTicks;

    public Variant variant() {
        return EntityVariant.path(this).equals("infernal_creeper") ? Variant.INFERNAL : Variant.CREEPER;
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        return Creeper.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, variant == Variant.INFERNAL ? 2.0 : 0.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof SwellGoal);
        goalSelector.addGoal(2, new InfxCreeperSwellGoal(this));
    }

    @Override
    public void tick() {
        if (cactusFuseTicks > 0) {
            cactusFuseTicks--;
        }
        super.tick();
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel level, @NonNull DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (variant() != Variant.INFERNAL) {
            return;
        }

        int looting = lootingLevel(level, source);
        int firstRoll = random.nextInt(4);
        int fallbackRoll = firstRoll == 0 ? random.nextInt(3) : 0;
        int lootingRoll = looting > 0 ? random.nextInt(looting + 1) : 0;
        int provisionalCount = firstRoll == 0 ? fallbackRoll : firstRoll;
        provisionalCount += lootingRoll;
        int nonPlayerReduction = !killedByPlayer && provisionalCount > 0
                ? random.nextInt(provisionalCount + 1)
                : 0;
        int count = infernalPowderDropCount(
                firstRoll, fallbackRoll, lootingRoll, killedByPlayer, nonPlayerReduction);
        for (int index = 0; index < count; index++) {
            int nonPlayerItemRoll = killedByPlayer ? 0 : random.nextInt(3);
            if (shouldDropInfernalPowder(killedByPlayer, nonPlayerItemRoll)) {
                spawnAtLocation(level, new ItemStack(Items.GUNPOWDER));
            }
        }
    }

    /** InfX's first {@code nextInt(4)} roll with its zero fallback and non-player reduction. */
    static int infernalPowderDropCount(
            int firstRoll, int fallbackRoll, int lootingRoll, boolean killedByPlayer, int nonPlayerReduction) {
        int count = firstRoll == 0 ? fallbackRoll : firstRoll;
        count += lootingRoll;
        return !killedByPlayer && count > 0 ? count - nonPlayerReduction : count;
    }

    /** Player kills always keep powder; other kills retain InfX's one-in-three item roll. */
    static boolean shouldDropInfernalPowder(boolean killedByPlayer, int nonPlayerItemRoll) {
        return killedByPlayer || nonPlayerItemRoll == 0;
    }

    /** InfX varies ignition distance by path state and doubles it for infernal creepers. */
    static double swellStartDistanceSqr(Variant variant, boolean navigationDone, float healthFraction) {
        double ordinary = navigationDone ? 16.0 : healthFraction < 1.0F ? 9.0 : 4.5;
        return variant == Variant.INFERNAL ? ordinary * 2.0 : ordinary;
    }

    /** InfX preserves the swell while any visible player is within this health-scaled squared range. */
    static double swellContinueDistanceSqr(Variant variant, float healthFraction) {
        double base = variant == Variant.INFERNAL ? 36.0 : 16.0;
        double clampedHealth = healthFraction <= 0.4F ? 0.4 : Math.min(healthFraction, 1.0F);
        return base / clampedHealth;
    }

    float healthFraction() {
        return getHealth() / getMaxHealth();
    }

    void armCactusFuse() {
        cactusFuseTicks = CACTUS_FUSE_WINDOW_TICKS;
    }

    boolean hasCactusFuseTrigger() {
        if (cactusFuseTicks <= 0) {
            return false;
        }
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            if (level().getBlockState(pos).is(Blocks.CACTUS)) {
                return true;
            }
        }
        return false;
    }

    private static int lootingLevel(ServerLevel level, DamageSource source) {
        if (!(source.getEntity() instanceof net.minecraft.world.entity.LivingEntity killer)) {
            return 0;
        }
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return EnchantmentHelper.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.LOOTING), killer);
    }

    boolean isAmplifyingExplosion() {
        return amplifyingExplosion;
    }

    void setAmplifyingExplosion(boolean amplifyingExplosion) {
        this.amplifyingExplosion = amplifyingExplosion;
    }
}
