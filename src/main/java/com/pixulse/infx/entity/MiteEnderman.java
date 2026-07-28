package com.pixulse.infx.entity;

import com.pixulse.infx.effect.curse.CurseManager;
import com.pixulse.infx.effect.curse.CurseType;
import java.util.EnumSet;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Enderman replacement with pearl awareness and projectile damage support. */
public final class MiteEnderman extends EnderMan implements MiteMob {
    private static final Identifier MITE_CHASE_SPEED_ID = Identifier.fromNamespaceAndPath("infx", "mite_chase_speed");
    private static final Identifier VANILLA_CHASE_SPEED_ID = Identifier.withDefaultNamespace("attacking");
    private static final double MITE_CHASE_SPEED_MULTIPLIER = 6.5 / 0.3;
    private static final double MITE_TARGET_RANGE = 64.0;
    private static final double VALUABLE_SEARCH_HORIZONTAL_RANGE = 16.0;
    private static final double VALUABLE_SEARCH_VERTICAL_RANGE = 8.0;
    private static final double VALUABLE_PICKUP_DISTANCE_SQUARED = 4.0;
    private static final int VALUABLE_PICKUP_COOLDOWN = 40;
    private static final int VALUABLE_TELEPORT_INTERVAL = 20;
    private static final int VALUABLE_TELEPORT_CHANCE = 10;
    private static final String STORED_PEARLS_KEY = "R196StoredEnderPearls";
    private static final String STORED_EYES_KEY = "R196StoredEnderEyes";
    private static final AttributeModifier MITE_CHASE_SPEED = new AttributeModifier(
            MITE_CHASE_SPEED_ID,
            MITE_CHASE_SPEED_MULTIPLIER - 1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    private int storedPearls;
    private int storedEyes;
    private boolean suppressDamageTeleport;

    public MiteEnderman(EntityType<? extends EnderMan> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return EnderMan.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.FOLLOW_RANGE, MITE_TARGET_RANGE)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return AttackRanges.withinOldAiReach(this, target, AttackRanges.OLD_AI_REACH);
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
        // MITE evaluates the nearest player every tick: the curse rolls first, held
        // valuables anger immediately, and inventory valuables each get a 1-in-2000 roll.
        // Priority zero prevents the delayed vanilla stare goal from masking these checks.
        targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                1,
                true,
                false,
                (target, level) -> target instanceof Player player && isAngeredByCurseOrPearls(player)));
        goalSelector.addGoal(6, new MiteValuableItemGoal(this));
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return hurtFromProjectile(level, source, damage);
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && isMiteIndirect(source)) {
            // EntityDamageSource#isIndirect in R196 clears aggression before blinking.
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
            // preserves the MITE rule that arrows and other projectiles actually hurt endermen.
            hurt = super.hurtServer(level, direct, damage);
        } finally {
            suppressDamageTeleport = false;
        }
        if (hurt && attacker instanceof LivingEntity living) {
            setTarget(living);
        }
        return hurt;
    }

    private static boolean isMiteIndirect(DamageSource source) {
        return source.getEntity() != null
                && source.getEntity() != source.getDirectEntity()
                && !source.is(DamageTypeTags.IS_PROJECTILE);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (tickCount % VALUABLE_TELEPORT_INTERVAL == 0 && random.nextInt(VALUABLE_TELEPORT_CHANCE) == 0) {
            teleportToValuableItem();
        }
        super.customServerAiStep(level);
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
        return item != null && randomTeleport(item.getX(), item.getY(), item.getZ(), true);
    }

    private @Nullable ItemEntity nearestValuableItem() {
        ItemEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ItemEntity item : level().getEntitiesOfClass(
                ItemEntity.class,
                getBoundingBox().inflate(VALUABLE_SEARCH_HORIZONTAL_RANGE, VALUABLE_SEARCH_VERTICAL_RANGE,
                        VALUABLE_SEARCH_HORIZONTAL_RANGE),
                this::isValuableItem)) {
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
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(STORED_PEARLS_KEY, storedPearls);
        output.putInt(STORED_EYES_KEY, storedEyes);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        storedPearls = input.getIntOr(STORED_PEARLS_KEY, 0);
        storedEyes = input.getIntOr(STORED_EYES_KEY, 0);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
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

    private static final class MiteValuableItemGoal extends Goal {
        private final MiteEnderman enderman;
        private @Nullable ItemEntity target;
        private int cooldown;

        private MiteValuableItemGoal(MiteEnderman enderman) {
            this.enderman = enderman;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (enderman.getTarget() != null || enderman.isInWaterOrRain() || enderman.isOnFire()) {
                return false;
            }
            target = enderman.nearestValuableItem();
            return target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && enderman.getTarget() == null && enderman.isValuableItem(target);
        }

        @Override
        public void start() {
            moveToTarget();
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }
            if (enderman.distanceToSqr(target) <= VALUABLE_PICKUP_DISTANCE_SQUARED) {
                enderman.collectValuable(target);
                cooldown = VALUABLE_PICKUP_COOLDOWN;
                target = null;
                enderman.getNavigation().stop();
                return;
            }
            moveToTarget();
        }

        @Override
        public void stop() {
            target = null;
            enderman.getNavigation().stop();
        }

        private void moveToTarget() {
            if (target != null) {
                enderman.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
            }
        }
    }
}
