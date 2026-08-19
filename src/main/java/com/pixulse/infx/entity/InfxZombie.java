package com.pixulse.infx.entity;

import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.world.SpawnGate;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * MITE zombie, replacing the vanilla {@code Zombie} spawn with a dedicated InfX entity. The
 * behaviour previously layered onto the vanilla zombie by {@code ZombieEvents} (now removed) is
 * folded into overrides here: attribute alignment (5 melee, no armor), 1-in-8 smart zombies, no
 * baby zombies, the burning-zombie tree-ignition AI, raw-meat seeking, villager-conversion gating
 * and the rare metal-nugget drop. Leaders stay with vanilla {@code Zombie#handleAttributes}, and
 * the block-digging switch is driven from {@link MonsterTactics} like every other zombie-family
 * member.
 *
 * <p>Extends {@code Zombie} directly rather than {@link InfxZombieBase} so that the shared
 * tension-gear path ({@link MonsterTactics#equipForWorldAge}) and the rusted-iron arming path
 * ({@code RustedIronSources}) keep applying to it: both gate on {@code instanceof Zombie} and
 * exclude {@code InfxZombieBase}. The MITE zombie variants (ghoul, shadow, wight, revenant,
 * stalker) are the {@code InfxZombieBase} members that arm themselves or spawn bare.
 */
public final class InfxZombie extends Zombie implements InfxMob {
    public InfxZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.FOLLOW_RANGE, 35.0);
    }

    @Override
    public void setBaby(boolean baby) {
        // MITE has no baby zombies.
        super.setBaby(false);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            @NonNull ServerLevelAccessor level,
            @NonNull DifficultyInstance difficulty,
            @NonNull EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, new Zombie.ZombieGroupData(false, false));
        // MITE smart zombie: 1 in 8 spawns smart, unlocking bare-handed digging.
        if (getRandom().nextInt(8) == 0) {
            getPersistentData().putBoolean(MonsterTactics.SMART_KEY, true);
        }
        return data;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float amount) {
        // MITE: a zombie that is hit once by a player becomes permanently smart.
        if (source.isDirect() && source.getEntity() instanceof Player) {
            getPersistentData().putBoolean(MonsterTactics.SMART_KEY, true);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // MITE EntityAIMoveToFoodItem: zombies seek and eat dropped raw meat.
        goalSelector.addGoal(2, new MoveToFoodGoal(this));
    }

    @Override
    public boolean convertVillagerToZombieVillager(@NonNull ServerLevel level, @NonNull Villager villager) {
        // MITE: a zombie holding a digging tool refuses to turn a slain villager into a zombie villager.
        if (MonsterTactics.holdsDigTool(getMainHandItem())) {
            return false;
        }
        boolean converted = super.convertVillagerToZombieVillager(level, villager);
        if (converted) {
            // MITE: a villager conversion clears the killer zombie's five equipment slots.
            for (EquipmentSlot slot : List.of(
                    EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        return converted;
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel level, @NonNull DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        // MITE zombie rare drop: a player kill has a ~2.5% chance to drop one random metal nugget.
        if (!killedByPlayer) {
            return;
        }
        int looting = lootingLevel(level, source);
        if (getRandom().nextFloat() < rareDropChance(looting)) {
            spawnAtLocation(level, new ItemStack(randomNugget(getRandom())));
        }
    }

    @Override
    public void tick() {
        super.tick();
        // MITE burning-zombie tree ignition: every 40 ticks a burning zombie seeks the nearest log
        // with a player near its canopy, then sets fire to it on arrival. Modern burning spread is
        // not the old chance-based model, so the target log is ignited directly.
        if (level() instanceof ServerLevel serverLevel
                && isOnFire()
                && tickCount % 40 == 0
                && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
            igniteNearestTree(serverLevel, this);
        }
    }

    // ------------------------------------------------------------------ static MITE helpers

    /** MITE zombie rare-drop chance: (5 + looting×2) out of 200. */
    public static float rareDropChance(int looting) {
        return (5 + looting * 2) / 200.0F;
    }

    /** One of MITE's four zombie rare drops: copper, silver, gold or iron nugget. */
    public static Item randomNugget(RandomSource random) {
        Item[] nuggets = {
            Items.COPPER_NUGGET, InfXItems.SILVER_NUGGET.get(), Items.GOLD_NUGGET, Items.IRON_NUGGET
        };
        return nuggets[random.nextInt(nuggets.length)];
    }

    /** Ignites the nearest player-lit log once the zombie is within reach; returns whether fire was set. */
    public static boolean igniteNearestTree(ServerLevel level, Zombie zombie) {
        BlockPos log = nearestLog(level, zombie);
        if (log == null || !playerNearTree(level, log)) {
            return false;
        }
        if (zombie.distanceToSqr(Vec3.atCenterOf(log)) >= 9.0) {
            zombie.getNavigation().moveTo(log.getX() + 0.5, log.getY(), log.getZ() + 0.5, 1.0);
            return false;
        }
        BlockPos firePos = log.above();
        if (!level.isEmptyBlock(firePos) || !BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP)) {
            return false;
        }
        level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
        return true;
    }

    private static BlockPos nearestLog(ServerLevel level, Zombie zombie) {
        BlockPos origin = zombie.blockPosition();
        BlockPos nearest = null;
        double nearestDistance = 16.0 * 16.0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -16, -16), origin.offset(16, 16, 16))) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(net.minecraft.tags.BlockTags.LOGS)) {
                continue;
            }
            double distance = pos.distToCenterSqr(origin.getX(), origin.getY(), origin.getZ());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = pos.immutable();
            }
        }
        return nearest;
    }

    /** MITE requires a player standing at the tree's y+2..y+9 within 5.6 blocks horizontally. */
    private static boolean playerNearTree(ServerLevel level, BlockPos log) {
        AABB box = new AABB(log).inflate(5.6);
        return level.getEntitiesOfClass(
                        Player.class,
                        box,
                        player -> {
                            int py = player.getBlockY();
                            return py >= log.getY() + 2 && py <= log.getY() + 9;
                        })
                .stream()
                .findAny()
                .isPresent();
    }

    private static int lootingLevel(ServerLevel level, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity killer)) {
            return 0;
        }
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return killer.getMainHandItem().getEnchantmentLevel(enchantments.getOrThrow(Enchantments.LOOTING));
    }
}
