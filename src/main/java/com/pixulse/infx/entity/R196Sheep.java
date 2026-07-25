package com.pixulse.infx.entity;

import com.pixulse.infx.registry.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** R196 sheep: productive shearing, fire/acid wool strip, leather drop chance. */
public final class R196Sheep extends Sheep {
    public R196Sheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Sheep.createAttributes().add(Attributes.MAX_HEALTH, 8.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        R196Livestock.ensureGoals(this);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) {
            R196Livestock.serverTick(this);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            R196Livestock.onHurt(this, amount);
            if (source.is(DamageTypeTags.IS_FIRE)
                    || source.typeHolder().unwrapKey()
                            .map(key -> key.identifier().getPath().contains("acid"))
                            .orElse(false)) {
                setSheared(true);
            }
        }
        return hurt;
    }

    @Override
    public boolean canMate(Animal partner) {
        if (!(level() instanceof ServerLevel serverLevel)) return super.canMate(partner);
        return super.canMate(partner) && R196Livestock.canMateWith(serverLevel, this, partner);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        R196Livestock.markFedIfFood(this, stack);
        if (stack.is(Items.SHEARS) && !R196Livestock.isProductive(this)) {
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide() && getRandom().nextBoolean() && level() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(new ItemEntity(
                    serverLevel, getX(), getY(), getZ(), new ItemStack(Items.LEATHER)));
        }
        super.die(source);
    }

    @Override
    public @Nullable Sheep getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntityTypes.R196_SHEEP.get().create(level, EntitySpawnReason.BREEDING);
    }
}
