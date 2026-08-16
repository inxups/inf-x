package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXSounds;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * MITE white ghoul: drains experience from players it wounds and is immune to everything except
 * fire, lava, silver and magic.
 */
public final class Wight extends InfxZombieBase {
    public Wight(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes().add(Attributes.ARMOR, 0.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected boolean breaksDoors() {
        return false;
    }

    @Override
    protected boolean picksUpLoot() {
        return false;
    }

    @Override
    protected boolean targetsAnimals() {
        return true;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float amount) {
        if (!MobDamageRules.wightAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean doHurtTarget(@NonNull ServerLevel level, @NonNull Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof Player player && random.nextFloat() < 0.4F) {
            player.giveExperiencePoints(-Math.max(20, (player.experienceLevel + 1) * 10));
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel level, @NonNull DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (!killedByPlayer) {
            return;
        }
        int looting = lootingLevel(level, source);
        if (random.nextFloat() < 0.10F + Math.max(0, looting) * 0.04F) {
            spawnAtLocation(level, rareDrop());
        }
    }

    private ItemStack rareDrop() {
        Item[] drops = {
            Items.COPPER_NUGGET, InfXItems.SILVER_NUGGET.get(), Items.GOLD_NUGGET, Items.IRON_NUGGET
        };
        return drops[random.nextInt(drops.length)].getDefaultInstance();
    }

    private static int lootingLevel(ServerLevel level, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity killer)) {
            return 0;
        }
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return EnchantmentHelper.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.LOOTING), killer);
    }

    @Override
    protected @NonNull SoundEvent getAmbientSound() {
        return InfXSounds.WIGHT_AMBIENT.get();
    }

    @Override
    protected @NonNull SoundEvent getHurtSound(@NonNull DamageSource source) {
        return InfXSounds.WIGHT_HURT.get();
    }

    @Override
    protected @NonNull SoundEvent getDeathSound() {
        return InfXSounds.WIGHT_DEATH.get();
    }
}
