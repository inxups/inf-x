package com.pixulse.infx.entity;

import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXEntityTypes;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.InfXSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Zombie-shaped INFX mobs, including the replacement zombie and five new variants. */
public final class InfxZombie extends Zombie implements InfxMob {
    private static final String VILLAGER_ZOMBIE_KEY = "R196VillagerZombie";
    public enum Variant {
        ZOMBIE,
        INVISIBLE_STALKER,
        GHOUL,
        SHADOW,
        WIGHT,
        REVENANT
    }

    private boolean villagerZombie;

    public InfxZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        setCanBreakDoors(breaksDoors(variant()));
        setCanPickUpLoot(variant() == Variant.ZOMBIE);
        xpReward = switch (variant()) {
            case ZOMBIE -> xpReward;
            case INVISIBLE_STALKER, GHOUL, SHADOW, WIGHT -> 10;
            case REVENANT -> 15;
        };
    }

    public Variant variant() {
        return switch (EntityVariant.path(this)) {
            case "invisible_stalker" -> Variant.INVISIBLE_STALKER;
            case "ghoul" -> Variant.GHOUL;
            case "shadow" -> Variant.SHADOW;
            case "wight" -> Variant.WIGHT;
            case "revenant" -> Variant.REVENANT;
            default -> Variant.ZOMBIE;
        };
    }

    /** MITE stalkers, zombies, and ghouls can force a path through closed doors. */
    static boolean breaksDoors(Variant variant) {
        return variant == Variant.ZOMBIE || variant == Variant.INVISIBLE_STALKER || variant == Variant.GHOUL;
    }

    static boolean burnsInSunlight(Variant variant) {
        return variant != Variant.INVISIBLE_STALKER;
    }

    static boolean zombifiesVillagers(Variant variant) {
        return variant != Variant.INVISIBLE_STALKER;
    }

    static boolean targetsAnimals(Variant variant) {
        return variant != Variant.INVISIBLE_STALKER;
    }

    /** MITE stores villager zombies as a flagged normal zombie, not a separate modern entity type. */
    public boolean isVillagerZombie() {
        return villagerZombie;
    }

    private void setVillagerZombie(boolean villagerZombie) {
        this.villagerZombie = villagerZombie;
    }

    public static AttributeSupplier.Builder attributes(Variant variant) {
        AttributeSupplier.Builder builder = Zombie.createAttributes().add(Attributes.ARMOR, 0.0);
        return switch (variant) {
            case ZOMBIE, SHADOW -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.23)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case INVISIBLE_STALKER -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.23)
                    .add(Attributes.ATTACK_DAMAGE, 4.0);
            case GHOUL -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.28)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case WIGHT -> builder
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.25)
                    .add(Attributes.ATTACK_DAMAGE, 5.0);
            case REVENANT -> builder
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FOLLOW_RANGE, 40.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.26)
                    .add(Attributes.ATTACK_DAMAGE, 7.0);
        };
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinNewAiReach(this, target);
    }

    @Override
    protected void addBehaviourGoals() {
        if (variant() == Variant.INVISIBLE_STALKER) {
            // The original stalker is an EntityMob, not a zombie: it pursues only players and villagers.
            goalSelector.addGoal(3, new ZombieAttackGoal(this, 1.0, false));
            goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
            goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
            targetSelector.addGoal(1, new HurtByTargetGoal(this));
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
            targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
            return;
        }
        super.addBehaviourGoals();
        if (targetsAnimals(variant())) {
            targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            @NonNull ServerLevelAccessor level,
            @NonNull DifficultyInstance difficulty,
            @NonNull EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        // MITE zombies never spawn as babies, so chicken jockeys cannot appear either.
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, new ZombieGroupData(false, false));
        setBaby(false);
        AttributeInstance reinforcements = getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (reinforcements != null) {
            reinforcements.removeModifiers();
            reinforcements.setBaseValue(0.0);
        }
        setCanBreakDoors(breaksDoors(variant()));
        setCanPickUpLoot(variant() == Variant.ZOMBIE);
        if (variant() == Variant.REVENANT) {
            equipRevenantKit(level.getLevel());
        }
        return data;
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected boolean isSunSensitive() {
        return burnsInSunlight(variant()) && super.isSunSensitive();
    }

    @Override
    public boolean convertVillagerToZombieVillager(@NonNull ServerLevel level, @NonNull Villager villager) {
        if (!zombifiesVillagers(variant()) || getMainHandItem().has(DataComponents.TOOL)) {
            return false;
        }
        InfxZombie converted = villager.convertTo(
                InfXEntityTypes.INFX_ZOMBIE.get(),
                ConversionParams.single(villager, true, true),
                zombie -> {
                    zombie.setVillagerZombie(true);
                    zombie.finalizeSpawn(
                            level,
                            level.getCurrentDifficultyAt(zombie.blockPosition()),
                            EntitySpawnReason.CONVERSION,
                            new ZombieGroupData(false, false));
                    net.neoforged.neoforge.event.EventHooks.onLivingConvert(villager, zombie);
                    if (!isSilent()) {
                        level.levelEvent(null, 1016, blockPosition(), 0);
                    }
                });
        return converted != null;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        // MITE zombies never spawn with vanilla weapons or armor. The plain zombie instead
        // receives MITE world-age gear through MonsterTactics, and the revenant its fixed kit.
    }

    /** MITE revenants always spawn in full rusted-iron armor with a weighted rusted weapon. */
    private void equipRevenantKit(ServerLevel level) {
        long day = MonsterTactics.survivalDay(level);
        int bound = 2 + (day >= 10L ? 1 : 0) + (day >= 20L ? 1 : 0);
        int roll = random.nextInt(bound);
        EquipmentType weapon = roll <= 1
                ? EquipmentType.SWORD
                : roll == 2 && day >= 10L ? EquipmentType.BATTLE_AXE : EquipmentType.WAR_HAMMER;
        MonsterTactics.equip(level, this, EquipmentSlot.MAINHAND, InfxMaterial.RUSTED_IRON, weapon, day);
        MonsterTactics.equip(level, this, EquipmentSlot.HEAD, InfxMaterial.RUSTED_IRON, EquipmentType.HELMET, day);
        MonsterTactics.equip(
                level, this, EquipmentSlot.CHEST, InfxMaterial.RUSTED_IRON, EquipmentType.CHESTPLATE, day);
        MonsterTactics.equip(
                level, this, EquipmentSlot.LEGS, InfxMaterial.RUSTED_IRON, EquipmentType.LEGGINGS, day);
        MonsterTactics.equip(level, this, EquipmentSlot.FEET, InfxMaterial.RUSTED_IRON, EquipmentType.BOOTS, day);
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float amount) {
        if (variant() == Variant.SHADOW && !MobDamageRules.silverMagicGateAccepts(source)) {
            return false;
        }
        if (variant() == Variant.WIGHT && !MobDamageRules.wightAccepts(source)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected @NonNull SoundEvent getAmbientSound() {
        return switch (variant()) {
            case GHOUL -> InfXSounds.GHOUL_AMBIENT.get();
            case SHADOW -> InfXSounds.SHADOW_AMBIENT.get();
            case WIGHT -> InfXSounds.WIGHT_AMBIENT.get();
            case INVISIBLE_STALKER -> InfXSounds.INVISIBLE_STALKER_AMBIENT.get();
            case REVENANT, ZOMBIE -> super.getAmbientSound();
        };
    }

    @Override
    protected @NonNull SoundEvent getHurtSound(@NonNull DamageSource source) {
        return switch (variant()) {
            case GHOUL -> InfXSounds.GHOUL_HURT.get();
            case SHADOW -> InfXSounds.SHADOW_HURT.get();
            case WIGHT -> InfXSounds.WIGHT_HURT.get();
            case INVISIBLE_STALKER -> InfXSounds.INVISIBLE_STALKER_HURT.get();
            case REVENANT, ZOMBIE -> super.getHurtSound(source);
        };
    }

    @Override
    protected @NonNull SoundEvent getDeathSound() {
        return switch (variant()) {
            case GHOUL -> InfXSounds.GHOUL_DEATH.get();
            case SHADOW -> InfXSounds.SHADOW_DEATH.get();
            case WIGHT -> InfXSounds.WIGHT_DEATH.get();
            case INVISIBLE_STALKER -> InfXSounds.INVISIBLE_STALKER_DEATH.get();
            case REVENANT, ZOMBIE -> super.getDeathSound();
        };
    }

    /** MITE stalkers and shadows move silently. */
    @Override
    protected float getSoundVolume() {
        return variant() == Variant.INVISIBLE_STALKER || variant() == Variant.SHADOW ? 0.2F : super.getSoundVolume();
    }

    @Override
    protected void playStepSound(@NonNull BlockPos pos, @NonNull BlockState state) {
        if (variant() == Variant.INVISIBLE_STALKER || variant() == Variant.SHADOW) {
            return;
        }
        super.playStepSound(pos, state);
    }

    @Override
    public boolean doHurtTarget(@NonNull ServerLevel level, @NonNull Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (!hurt || !(target instanceof LivingEntity living)) {
            return hurt;
        }

        switch (variant()) {
            case GHOUL -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5), this);
            case SHADOW -> {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0), this);
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0), this);
            }
            case WIGHT -> {
                if (living instanceof Player player && random.nextFloat() < 0.4F) {
                    player.giveExperiencePoints(-Math.max(20, (player.experienceLevel + 1) * 10));
                }
            }
            case INVISIBLE_STALKER, REVENANT, ZOMBIE -> {
            }
        }
        // MITE ghouls are the only family member that heals from feeding on flesh.
        if (variant() == Variant.GHOUL && living instanceof Animal && !living.isAlive()) {
            heal(getMaxHealth() * 0.5F);
        }
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel level, @NonNull DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (!killedByPlayer || (variant() != Variant.ZOMBIE && variant() != Variant.WIGHT && variant() != Variant.REVENANT)) {
            return;
        }
        int looting = lootingLevel(level, source);
        if (random.nextFloat() < rareDropChance(variant(), villagerZombie, looting)) {
            spawnAtLocation(level, rareDrop());
        }
    }

    static float rareDropChance(Variant variant, boolean villagerZombie, int lootingLevel) {
        float base = variant == Variant.REVENANT || villagerZombie ? 0.10F : 0.025F;
        float bonus = variant == Variant.REVENANT || villagerZombie ? 0.04F : 0.01F;
        return base + Math.max(0, lootingLevel) * bonus;
    }

    private ItemStack rareDrop() {
        Item[] drops = villagerZombie
                ? new Item[] {Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.CARROT, Items.POTATO, InfXItems.ONION.get()}
                : new Item[] {Items.COPPER_NUGGET, InfXItems.SILVER_NUGGET.get(), Items.GOLD_NUGGET, Items.IRON_NUGGET};
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
    public void aiStep() {
        super.aiStep();
        if (variant() == Variant.INVISIBLE_STALKER && isInvisible()) {
            // Older saves used vanilla invisibility. MITE instead renders a visible 5% silhouette.
            setInvisible(false);
        }
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        if (variant() == Variant.SHADOW) {
            if (level.isBrightOutside() && level.canSeeSky(blockPosition()) && !level.isRaining()) {
                // MITE shadows take 1000 sunlight damage: certain death, no helmet protection.
                hurtServer(level, damageSources().genericKill(), 1000.0F);
            } else if (tickCount % 40 == 0) {
                int darknessHeal = (int) ((0.4F - getLightLevelDependentMagicValue()) * 10.0F);
                if (darknessHeal > 0) {
                    heal(darknessHeal);
                }
            }
        }
        if ((variant() == Variant.INVISIBLE_STALKER || variant() == Variant.SHADOW)
                && (getLastHurtByMob() == null || tickCount - getLastHurtByMobTimestamp() > 100)
                && level.getNearestPlayer(this, 4.0) == null) {
            disableNearbyLight(level);
        }
        if (variant() != Variant.INVISIBLE_STALKER && tickCount % 20 == 0 && isOnFire() && random.nextFloat() < 0.15F) {
            igniteNearbyBlock(level);
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(VILLAGER_ZOMBIE_KEY, villagerZombie);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        villagerZombie = input.getBooleanOr(VILLAGER_ZOMBIE_KEY, false);
    }

    private void disableNearbyLight(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos origin = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 3, 1))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.TORCH)
                    || state.is(Blocks.WALL_TORCH)
                    || state.is(Blocks.REDSTONE_TORCH)
                    || state.is(Blocks.REDSTONE_WALL_TORCH)) {
                level.destroyBlock(pos, true, this);
                return;
            }
            if (state.is(Blocks.JACK_O_LANTERN)) {
                BlockState pumpkin = Blocks.CARVED_PUMPKIN.defaultBlockState();
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

    private void igniteNearbyBlock(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        BlockPos pos = blockPosition().relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        if (!level.isEmptyBlock(pos)) {
            return;
        }
        BlockState fire = BaseFireBlock.getState(level, pos);
        if (fire.canSurvive(level, pos)) {
            level.setBlockAndUpdate(pos, fire);
        }
    }
}
