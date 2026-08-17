package com.pixulse.infx.entity;

import com.pixulse.infx.data.curse.CurseManager;
import com.pixulse.infx.data.curse.CurseType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Enderman replacement with pearl awareness and projectile damage support. */
public final class InfxEnderman extends EnderMan implements InfxMob {
    private static final Identifier INFX_CHASE_SPEED_ID = Identifier.fromNamespaceAndPath("infx", "infx_chase_speed");
    private static final Identifier VANILLA_CHASE_SPEED_ID = Identifier.withDefaultNamespace("attacking");
    private static final double MODERN_CHASE_SPEED_BONUS = 0.15;
    private static final double INFX_TARGET_RANGE = 64.0;
    private static final double VALUABLE_SEARCH_HORIZONTAL_RANGE = 16.0;
    private static final double VALUABLE_SEARCH_VERTICAL_RANGE = 8.0;
    private static final int VALUABLE_TELEPORT_INTERVAL = 20;
    private static final int VALUABLE_TELEPORT_CHANCE = 10;
    private static final String STORED_PEARLS_KEY = "infx.stored_ender_pearls";
    private static final String STORED_EYES_KEY = "infx.stored_ender_eyes";
    private static final AttributeModifier INFX_CHASE_SPEED = new AttributeModifier(
            INFX_CHASE_SPEED_ID,
            MODERN_CHASE_SPEED_BONUS,
            AttributeModifier.Operation.ADD_VALUE);

    private int storedPearls;
    private int storedEyes;
    private boolean suppressDamageTeleport;

    public InfxEnderman(EntityType<? extends EnderMan> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return EnderMan.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.FOLLOW_RANGE, INFX_TARGET_RANGE)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinOldAiReach(this, target, AttackRanges.OLD_AI_REACH);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        AttributeInstance movementSpeed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        // 26.1's Enderman uses the modern +0.15 attacking modifier. Keep a namespaced
        // equivalent so the replacement does not depend on the parent implementation's
        // modifier lifecycle, while preserving the vanilla 0.30 -> 0.45 chase speed.
        movementSpeed.removeModifier(VANILLA_CHASE_SPEED_ID);
        boolean chasing = getTarget() != null;
        if (chasing && !movementSpeed.hasModifier(INFX_CHASE_SPEED_ID)) {
            movementSpeed.addTransientModifier(INFX_CHASE_SPEED);
        } else if (!chasing && movementSpeed.hasModifier(INFX_CHASE_SPEED_ID)) {
            movementSpeed.removeModifier(INFX_CHASE_SPEED_ID);
        }
    }

    static double chasingMovementSpeed(double baseMovementSpeed) {
        return baseMovementSpeed + MODERN_CHASE_SPEED_BONUS;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // InfX evaluates the nearest player every tick: the curse rolls first, held
        // valuables anger immediately, and inventory valuables each get a 1-in-2000 roll.
        // Priority zero prevents the delayed vanilla stare goal from masking these checks.
        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                1,
                true,
                false,
                (target, level) -> target instanceof Player player && isAngeredByCurseOrPearls(player)));
    }

    private boolean isAngeredByCurseOrPearls(Player player) {
        if (random.nextInt(3) == 0
                && CurseManager.hasCurse(player, CurseType.ENDERMEN_AGGRO)) {
            CurseManager.reveal(player, CurseType.ENDERMEN_AGGRO);
            return true;
        }
        return isAngeredByPearls(player);
    }

    private boolean isAngeredByPearls(Player player) {
        if (isPearlLike(player.getMainHandItem()) || isPearlLike(player.getOffhandItem())) {
            return true;
        }
        int valuables = player.getInventory().countItem(Items.ENDER_PEARL)
                + player.getInventory().countItem(Items.ENDER_EYE);
        return valuables > 0 && random.nextInt(2000) < valuables;
    }

    static boolean isPearlLike(ItemStack stack) {
        return isPearlLike(stack.getItem());
    }

    static boolean isPearlLike(Item item) {
        return item == Items.ENDER_PEARL || item == Items.ENDER_EYE;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return hurtFromProjectile(level, source, damage);
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && isIndirect(source)) {
            // EntityDamageSource#isIndirect in INFX clears aggression before blinking.
            // Clear the retaliation memory too, otherwise the inherited HurtByTargetGoal
            // reacquires the indirect attacker on its next tick.
            setTarget(null);
            setLastHurtByMob(null);
            for (int attempt = 0; attempt < 64 && !teleport(); attempt++) {}
        }
        return hurt;
    }

    private boolean hurtFromProjectile(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        DamageSource direct = attacker instanceof Player player
                ? level.damageSources().playerAttack(player)
                : attacker instanceof LivingEntity living ? level.damageSources().mobAttack(living) : level.damageSources().generic();
        boolean hurt;
        suppressDamageTeleport = true;
        try {
            // The modern parent short-circuits projectile damage. Feeding it a direct source
            // preserves the InfX rule that arrows and other projectiles actually hurt endermen.
            hurt = super.hurtServer(level, direct, damage);
        } finally {
            suppressDamageTeleport = false;
        }
        if (hurt && attacker instanceof LivingEntity living && MonsterEvents.withinFollowRange(this, living)) {
            setTarget(living);
        }
        return hurt;
    }

    private static boolean isIndirect(DamageSource source) {
        return source.getEntity() != null
                && source.getEntity() != source.getDirectEntity()
                && !source.is(DamageTypeTags.IS_PROJECTILE);
    }

    @Override
    protected void customServerAiStep(@NonNull ServerLevel level) {
        super.customServerAiStep(level);
        if (tickCount % VALUABLE_TELEPORT_INTERVAL == 0 && random.nextInt(VALUABLE_TELEPORT_CHANCE) == 0) {
            teleportToValuableItem();
        }
    }

    @Override
    protected boolean teleport() {
        if (suppressDamageTeleport) {
            return false;
        }
        return teleportToValuableItem() || super.teleport();
    }

    private boolean teleportToValuableItem() {
        if (isInWaterOrRain() || isOnFire()) {
            return false;
        }
        ItemEntity item = nearestValuableItem();
        if (item == null) {
            return false;
        }
        BlockPos destination = item.blockPosition();
        Vec3 origin = position();
        if (!randomTeleport(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D, true)) {
            return false;
        }
        level().gameEvent(GameEvent.TELEPORT, origin, GameEvent.Context.of(this));
        if (!isSilent()) {
            level().playSound(
                    null,
                    origin.x,
                    origin.y,
                    origin.z,
                    net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                    getSoundSource(),
                    1.0F,
                    1.0F);
            playSound(net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
        if (item.isAlive()) {
            collectValuable(item);
        }
        return true;
    }

    private @Nullable ItemEntity nearestValuableItem() {
        ItemEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ItemEntity item : level().getEntitiesOfClass(
                ItemEntity.class,
                getBoundingBox().inflate(VALUABLE_SEARCH_HORIZONTAL_RANGE, VALUABLE_SEARCH_VERTICAL_RANGE,
                        VALUABLE_SEARCH_HORIZONTAL_RANGE),
                item -> isValuableItem(item) && hasValuableHeadroom(item.blockPosition()))) {
            double distance = distanceToSqr(item);
            if (distance < nearestDistance) {
                nearest = item;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private boolean isValuableItem(ItemEntity item) {
        return item.isAlive() && !item.isInWater() && !item.isOnFire() && isPearlLike(item.getItem());
    }

    /** MITE only considers valuables with the two clear head blocks an enderman needs to arrive. */
    private boolean hasValuableHeadroom(BlockPos itemPos) {
        return isPassable(itemPos.above()) && isPassable(itemPos.above(2));
    }

    private boolean isPassable(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        return state.getCollisionShape(level(), pos).isEmpty() && state.getFluidState().isEmpty();
    }

    private void collectValuable(ItemEntity item) {
        ItemStack stack = item.getItem();
        if (!isPearlLike(stack)) {
            return;
        }
        if (stack.is(Items.ENDER_PEARL)) {
            storedPearls++;
        } else {
            storedEyes++;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            item.discard();
        } else {
            item.setItem(stack);
        }
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(STORED_PEARLS_KEY, storedPearls);
        output.putInt(STORED_EYES_KEY, storedEyes);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        storedPearls = input.getIntOr(STORED_PEARLS_KEY, 0);
        storedEyes = input.getIntOr(STORED_EYES_KEY, 0);
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel level, @NonNull DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        dropStoredValuables(level, Items.ENDER_PEARL, storedPearls);
        dropStoredValuables(level, Items.ENDER_EYE, storedEyes);
        storedPearls = 0;
        storedEyes = 0;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || storedPearls > 0 || storedEyes > 0;
    }

    private void dropStoredValuables(ServerLevel level, Item item, int count) {
        while (count > 0) {
            int stackSize = Math.min(count, item.getDefaultMaxStackSize());
            spawnAtLocation(level, new ItemStack(item, stackSize));
            count -= stackSize;
        }
    }

}
