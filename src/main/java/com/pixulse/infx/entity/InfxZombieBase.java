package com.pixulse.infx.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Shared zombie-family infrastructure for the InfX MITE mobs that live as separate entity types:
 * no babies, no vanilla reinforcement spawns, and the tuned melee reach. Water conversion back to
 * vanilla behavior is inherited (submerged InfX zombies still become drowned).
 */
public abstract class InfxZombieBase extends Zombie implements InfxMob {
    protected InfxZombieBase(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        setCanBreakDoors(breaksDoors());
        setCanPickUpLoot(picksUpLoot());
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinNewAiReach(this, target);
    }

    @Override
    public final @Nullable SpawnGroupData finalizeSpawn(
            @NonNull ServerLevelAccessor level,
            @NonNull DifficultyInstance difficulty,
            @NonNull EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, new ZombieGroupData(false, false));
        setBaby(false);
        AttributeInstance reinforcements = getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (reinforcements != null) {
            reinforcements.removeModifiers();
            reinforcements.setBaseValue(0.0);
        }
        setCanBreakDoors(breaksDoors());
        setCanPickUpLoot(picksUpLoot());
        afterFinalizeSpawn(level.getLevel());
        return data;
    }

    /** Extension point for subclasses that arm themselves at spawn (e.g. the revenant). */
    protected void afterFinalizeSpawn(ServerLevel level) {}

    @Override
    public final void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        if (targetsAnimals()) {
            targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        }
    }

    /** InfX stalkers and shadows move silently. */
    @Override
    protected float getSoundVolume() {
        return isSilentType() ? 0.2F : super.getSoundVolume();
    }

    @Override
    protected void playStepSound(@NonNull BlockPos pos, @NonNull BlockState state) {
        if (isSilentType()) {
            return;
        }
        super.playStepSound(pos, state);
    }

    /** Shadows and invisible stalkers extinguish nearby torches once per tick. */
    protected void disableNearbyLight(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 3, 1))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(net.minecraft.world.level.block.Blocks.TORCH)
                    || state.is(net.minecraft.world.level.block.Blocks.WALL_TORCH)
                    || state.is(net.minecraft.world.level.block.Blocks.REDSTONE_TORCH)
                    || state.is(net.minecraft.world.level.block.Blocks.REDSTONE_WALL_TORCH)) {
                level.destroyBlock(pos, true, this);
                return;
            }
            if (state.is(net.minecraft.world.level.block.Blocks.JACK_O_LANTERN)) {
                BlockState pumpkin = net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN.defaultBlockState();
                if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                    pumpkin = pumpkin.setValue(
                            HorizontalDirectionalBlock.FACING, state.getValue(HorizontalDirectionalBlock.FACING));
                }
                level.setBlockAndUpdate(pos, pumpkin);
                spawnAtLocation(level, new ItemStack(Items.TORCH));
                return;
            }
        }
    }

    protected boolean isSilentType() {
        return false;
    }

    /** Revenants are the only InfX mob that digs bare-handed from birth. */
    protected boolean digsBareHanded() {
        return false;
    }

    protected abstract boolean breaksDoors();

    protected abstract boolean picksUpLoot();

    protected abstract boolean targetsAnimals();

    /** Whether this zombie-family mob converts slain villagers; stalkers opt out. */
    protected boolean zombifiesVillagers() {
        return true;
    }

    @Override
    public boolean convertVillagerToZombieVillager(ServerLevel level, Villager villager) {
        return zombifiesVillagers() && super.convertVillagerToZombieVillager(level, villager);
    }
}
