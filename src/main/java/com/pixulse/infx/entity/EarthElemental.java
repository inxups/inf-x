package com.pixulse.infx.entity;

import com.pixulse.infx.data.harvest.HarvestRequirements;
import com.pixulse.infx.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * INFX earth elemental with a material body selected from its spawn surface.
 *
 * <p>Stone, obsidian, netherrack and end-stone bodies each have normal and magma states. The
 * related {@link ClayGolem} uses the two clay states in the same synced form field.
 */
public class EarthElemental extends Monster implements InfxMob {
    private static final EntityDataAccessor<Byte> DATA_FORM =
            SynchedEntityData.defineId(EarthElemental.class, EntityDataSerializers.BYTE);

    static final String DIG_POS = "infx_earth_elemental_dig_pos";
    static final String DIG_PROGRESS = "infx_earth_elemental_dig_progress";
    static final String DIG_COOLOFF = "infx_earth_elemental_dig_cooloff";
    static final String DIG_PAUSE = "infx_earth_elemental_dig_pause";
    static final int INITIAL_DIG_COOLOFF = 40;
    private static final int MAGMA_THRESHOLD = 100;
    private static final int MAGMA_MAX_HEAT = 1000;

    private int heat;
    private int ticksUntilNextFizzSound;

    public EarthElemental(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 15;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_FORM, (byte) Form.STONE_NORMAL.id());
    }

    public static AttributeSupplier.Builder attributes() {
        return baseAttributes(12.0, 4.0);
    }

    @Override
    public boolean isWithinMeleeAttackRange(@NonNull LivingEntity target) {
        return AttackRanges.withinNewAiReach(this, target, AttackRanges.EARTH_ELEMENTAL_REACH);
    }

    protected static AttributeSupplier.Builder baseAttributes(double attackDamage, double armor) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    /** InfX elementals hunt players and villagers, break doors, and use normal hostile-mob AI. */
    @Override
    protected void registerGoals() {
        // Lets the ground navigator route through a closed wooden door so the dedicated InfX
        // breaking goal receives a path node instead of treating the door as an unreachable wall.
        getNavigation().setCanOpenDoors(true);
        goalSelector.addGoal(0, new InfxEarthFloatGoal(this));
        goalSelector.addGoal(1, new InfxEarthBreakDoorGoal(this));
        goalSelector.addGoal(1, new InfxEarthDigGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, false));
    }

    /** The four natural INFX mineral bodies. Clay is handled by the separate clay-golem class. */
    public enum Form {
        STONE_NORMAL(0, Blocks.STONE, 2, false, false),
        STONE_MAGMA(1, Blocks.STONE, 2, true, false),
        OBSIDIAN_NORMAL(2, Blocks.OBSIDIAN, 3, false, false),
        OBSIDIAN_MAGMA(3, Blocks.OBSIDIAN, 3, true, false),
        NETHERRACK_NORMAL(4, Blocks.NETHERRACK, 2, false, false),
        NETHERRACK_MAGMA(5, Blocks.NETHERRACK, 2, true, false),
        END_STONE_NORMAL(6, Blocks.END_STONE, 2, false, false),
        END_STONE_MAGMA(7, Blocks.END_STONE, 2, true, false),
        CLAY_NORMAL(8, Blocks.CLAY, 0, false, true),
        CLAY_HARDENED(9, Blocks.TERRACOTTA, 1, false, true);

        private final int id;
        private final Block block;
        private final int harvestLevel;
        private final boolean magma;
        private final boolean clay;

        Form(int id, Block block, int harvestLevel, boolean magma, boolean clay) {
            this.id = id;
            this.block = block;
            this.harvestLevel = harvestLevel;
            this.magma = magma;
            this.clay = clay;
        }

        public int id() {
            return id;
        }

        public Block block() {
            return block;
        }

        public int harvestLevel() {
            return harvestLevel;
        }

        public boolean isMagmaForm() {
            return magma;
        }

        public boolean clay() {
            return clay;
        }

        public Form normal() {
            return switch (this) {
                case STONE_MAGMA -> STONE_NORMAL;
                case OBSIDIAN_MAGMA -> OBSIDIAN_NORMAL;
                case NETHERRACK_MAGMA -> NETHERRACK_NORMAL;
                case END_STONE_MAGMA -> END_STONE_NORMAL;
                default -> this;
            };
        }

        public Form magmaForm() {
            return switch (this) {
                case STONE_NORMAL -> STONE_MAGMA;
                case OBSIDIAN_NORMAL -> OBSIDIAN_MAGMA;
                case NETHERRACK_NORMAL -> NETHERRACK_MAGMA;
                case END_STONE_NORMAL -> END_STONE_MAGMA;
                default -> this;
            };
        }

        public static Form fromId(int id) {
            for (Form form : values()) {
                if (form.id == id) {
                    return form;
                }
            }
            return STONE_NORMAL;
        }

        public static Form forGround(BlockState ground) {
            if (ground.is(Blocks.OBSIDIAN)) return OBSIDIAN_NORMAL;
            if (ground.is(Blocks.NETHERRACK)) return NETHERRACK_NORMAL;
            if (ground.is(Blocks.END_STONE)) return END_STONE_NORMAL;
            return STONE_NORMAL;
        }
    }

    public Form form() {
        return Form.fromId(entityData.get(DATA_FORM));
    }

    public void setForm(Form form) {
        if (!isClayGolem() && form.clay()) {
            form = Form.STONE_NORMAL;
        }
        if (isClayGolem() && !form.clay()) {
            form = Form.CLAY_NORMAL;
        }
        entityData.set(DATA_FORM, (byte) form.id());
        AttributeInstance armor = getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.setBaseValue(form == Form.CLAY_HARDENED ? 2.0 : form.clay() ? 0.0 : 4.0);
        }
    }

    protected boolean isClayGolem() {
        return false;
    }

    public boolean isClay() {
        return form().clay();
    }

    public boolean isNormalClay() {
        return form() == Form.CLAY_NORMAL;
    }

    public boolean isHardenedClay() {
        return form() == Form.CLAY_HARDENED;
    }

    public boolean isMagma() {
        return !isClayGolem() && (level().isClientSide() ? form().isMagmaForm() : heat >= MAGMA_THRESHOLD);
    }

    public int heat() {
        return heat;
    }

    public Block materialBlock() {
        return form().block();
    }

    public int blockHarvestLevel() {
        return form().harvestLevel();
    }

    public static boolean isValidGround(BlockState state) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.OBSIDIAN)
                || state.is(Blocks.NETHERRACK)
                || state.is(Blocks.END_STONE);
    }

    /** Applies the InfX spawn material after natural, egg, command, or dispenser creation. */
    public void initializeElementalForm() {
        BlockState ground = level().getBlockState(blockPosition().below());
        boolean heated = isInLava() || isClayGolem() && isStandingInFire();
        heat = 0;
        if (isClayGolem()) {
            setForm(heated ? Form.CLAY_HARDENED : Form.CLAY_NORMAL);
        } else {
            setForm(Form.forGround(ground));
            if (heated) {
                convertToMagma();
            }
        }
    }

    /** Enables deterministic GameTests and keeps block-to-form conversion in one place. */
    public void initializeElementalForm(BlockState ground, boolean heated) {
        heat = 0;
        if (isClayGolem()) {
            setForm(heated ? Form.CLAY_HARDENED : Form.CLAY_NORMAL);
        } else {
            setForm(Form.forGround(ground));
            if (heated) {
                convertToMagma();
            }
        }
    }

    /** InfX converts mineral bodies at 100 heat and keeps fully molten bodies at 1000 heat. */
    public void convertToMagma() {
        heat = MAGMA_MAX_HEAT;
        Form before = form();
        if (isClayGolem()) {
            setForm(Form.CLAY_HARDENED);
        } else {
            setForm(before.magmaForm());
        }
        if (before != form() && level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SMOKE, getX(), getY(0.5), getZ(), 8, 0.25, 0.25, 0.25, 0.0);
        }
    }

    /** Cools a molten mineral body; clay hardening is permanent in InfX. */
    public boolean convertToNormal(boolean steam) {
        Form before = form();
        // Cooling lowers heat before this method is called, so the server-side isMagma() check
        // may already be false even though the synchronized body is still magma-textured.
        if (isClayGolem() || !before.isMagmaForm()) {
            return false;
        }
        heat = 0;
        setForm(before.normal());
        if (steam && level() instanceof ServerLevel level) {
            emitQuenchEffect(level);
        }
        return true;
    }

    /** Quenching is an interaction, not water damage, for magma earth elementals. */
    public boolean quench(ServerLevel level) {
        return convertToNormal(true);
    }

    @Override
    public void thunderHit(@NonNull ServerLevel level, @NonNull LightningBolt lightningBolt) {
        // The InfX source calls super first, but its own immunity gate rejects lightning damage.
        // Applying only the material reaction prevents modern fire/lava side effects from leaking in.
        if (!isMagma() && !isHardenedClay()) {
            convertToMagma();
        } else if (heat < MAGMA_MAX_HEAT) {
            heat = MAGMA_MAX_HEAT;
        }
    }

    /** InfX elementals hit for a flat value and magma bodies sometimes ignite damaged targets. */
    @Override
    public boolean doHurtTarget(@NonNull ServerLevel level, Entity target) {
        swing(InteractionHand.MAIN_HAND);
        DamageSource source = damageSources().mobAttack(this);
        boolean hurt = target.hurtServer(level, source, (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            EnchantmentHelper.doPostAttackEffects(level, target, source);
            if (isMagma() && random.nextFloat() < 0.4F) {
                target.igniteForSeconds(1.0F + random.nextInt(8));
            }
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        }
        return hurt;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float damage) {
        // InfX snowballs deal their ordinary one point to normal clay, but mineral bodies only
        // use them as a quench trigger. Hardened clay still follows the tool-only damage gate.
        if (source.getDirectEntity() instanceof Snowball && !isNormalClay()) {
            quench(level);
            return false;
        }
        if (!MobDamageRules.earthElementalAccepts(this, source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void knockback(double power, double xd, double zd) {
        // InfX applies a separate 0.4 velocity multiplier after normal mob knockback handling.
        super.knockback(power * 0.4, xd, zd);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    /** InfX elementals use fire and lava for material reactions, never as direct damage sources. */
    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.is(MobEffects.REGENERATION) || effect.is(MobEffects.POISON) || effect.is(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    /** InfX's {@code mob.irongolem.hit} maps to the modern iron-golem hurt sound. */
    @Override
    protected @NonNull SoundEvent getHurtSound(@NonNull DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    /** InfX's {@code mob.irongolem.death} maps to the modern iron-golem death sound. */
    @Override
    protected @NonNull SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel level) {
            tickEnvironment(level);
        }
    }

    private void tickEnvironment(ServerLevel level) {
        if (isInWater()) {
            if (isMagma()) {
                convertToNormal(true);
            }
            heat = 0;
            return;
        }

        boolean heated = isInLava() || isClay() && isStandingInFire();
        if (heated) {
            if (heat < MAGMA_MAX_HEAT && ++heat == MAGMA_THRESHOLD) {
                convertToMagma();
            }
        } else if (heat > 0) {
            boolean wasMagma = isMagma();
            heat = Math.max(0, heat - coolingRate(level));
            if (wasMagma && !isMagma()) {
                convertToNormal(level.isRainingAt(blockPosition().above()));
            }
        }

        if (isMagma()) {
            spreadMagmaFire(level);
            if (level.isRainingAt(blockPosition().above())) {
                emitMagmaPrecipitation(level);
            }
        }
    }

    private int coolingRate(ServerLevel level) {
        int cooling = 1;
        BlockPos pos = blockPosition();
        if (level.isRainingAt(pos.above())) {
            cooling++;
        }
        if (level.getBiome(pos).value().getBaseTemperature() <= 0.15F) {
            cooling++;
        }
        if (isFreezingBlock(level.getBlockState(pos))) {
            cooling++;
        }
        if (isFreezingBlock(level.getBlockState(pos.below()))) {
            cooling++;
        }
        return cooling;
    }

    private static boolean isFreezingBlock(BlockState state) {
        return state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.FROSTED_ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.POWDER_SNOW);
    }

    private boolean isStandingInFire() {
        BlockPos feet = blockPosition();
        return level().getBlockState(feet).is(Blocks.FIRE) || level().getBlockState(feet.above()).is(Blocks.FIRE);
    }

    private void emitMagmaPrecipitation(ServerLevel level) {
        level.sendParticles(ParticleTypes.CLOUD, getX(), getY(0.6), getZ(), 1, 0.2, 0.2, 0.2, 0.0);
        if (--ticksUntilNextFizzSound <= 0) {
            level.playSound(
                    null,
                    blockPosition(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.HOSTILE,
                    0.7F,
                    1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
            ticksUntilNextFizzSound = random.nextInt(7) + 2;
        }
    }

    private void emitQuenchEffect(ServerLevel level) {
        level.playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.7F, 1.6F);
        level.sendParticles(ParticleTypes.CLOUD, getX(), getY(0.5), getZ(), 8, 0.25, 0.25, 0.25, 0.0);
    }

    private void spreadMagmaFire(ServerLevel level) {
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING) || random.nextFloat() >= 0.04F) {
            return;
        }
        BlockPos bodyPos = blockPosition();
        int bodyHeight = (int) getBbHeight() + 1;
        for (int offset = 0; offset < bodyHeight; offset++) {
            BlockPos pos = bodyPos.above(offset);
            if (level.isEmptyBlock(pos)) {
                BlockState fire = BaseFireBlock.getState(level, pos);
                if (random.nextInt(10) == 0 && fire.canSurvive(level, pos)) {
                    level.setBlockAndUpdate(pos, fire);
                } else if (offset == 0) {
                    meltBlock(level, getOnPos());
                }
            } else if (!tryToIgniteBlock(level, pos)) {
                meltBlock(level, pos);
            }
        }
    }

    /** Mirrors InfX's fire helper: one vertical body column can ignite or melt, never a random side block. */
    private boolean tryToIgniteBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (random.nextInt(100) >= state.getFlammability(level, pos, Direction.UP)) {
            return false;
        }
        state.onCaughtFire(level, pos, Direction.UP, this);
        if (random.nextInt(11) < 5 && !level.isRainingAt(pos)) {
            BlockState fire = BaseFireBlock.getState(level, pos);
            if (fire.canSurvive(level, pos)) {
                level.setBlock(pos, fire, 3);
                return true;
            }
        }
        level.removeBlock(pos, false);
        return true;
    }

    /** Modern equivalent of World#tryToMeltBlock for ice, layered snow and snow blocks. */
    private static boolean meltBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IceBlock) {
            level.setBlockAndUpdate(pos, IceBlock.meltsInto());
            return true;
        }
        if (state.is(Blocks.SNOW)) {
            int layers = state.getValue(SnowLayerBlock.LAYERS);
            if (layers == 1) {
                level.removeBlock(pos, false);
            } else {
                level.setBlockAndUpdate(pos, state.setValue(SnowLayerBlock.LAYERS, layers - 1));
            }
            return true;
        }
        if (state.is(Blocks.SNOW_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 7));
            return true;
        }
        if (state.is(Blocks.POWDER_SNOW)) {
            level.removeBlock(pos, false);
            return true;
        }
        return false;
    }

    boolean canDestroyBlock(ServerLevel level, BlockPos pos) {
        int footY = blockPosition().getY();
        if (pos.getY() < footY || pos.getY() > footY + 1) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()
                || !state.getFluidState().isEmpty()
                || state.getDestroySpeed(level, pos) < 0.0F
                || state.is(Blocks.BEDROCK)) {
            return false;
        }
        return HarvestRequirements.requiredLevel(state) <= blockHarvestLevel()
                && net.neoforged.neoforge.common.CommonHooks.canEntityDestroy(level, pos, this);
    }

    int blockDigCooloff(BlockState state, BlockPos pos) {
        float hardness = Math.max(0.0F, state.getDestroySpeed(level(), pos));
        int cooloff = (int) (300.0F * hardness);
        if (isBloodMoonFrenzied()) {
            cooloff /= 2;
        }
        return cooloff / (isNormalClay() ? 4 : isHardenedClay() ? 6 : 8);
    }

    boolean isBloodMoonFrenzied() {
        return level() instanceof ServerLevel level && MoonPhase.BLOOD.isActiveInOverworldAtNight(level);
    }

    @Nullable BlockPos diggingPosition() {
        long encoded = getPersistentData().getLong(DIG_POS).orElse(Long.MIN_VALUE);
        return encoded == Long.MIN_VALUE ? null : BlockPos.of(encoded);
    }

    void beginDigging(ServerLevel level, BlockPos pos, int cooloff, int pause) {
        BlockPos previous = diggingPosition();
        if (previous != null && !previous.equals(pos)) {
            level.destroyBlockProgress(getId(), previous, -1);
        }
        var data = getPersistentData();
        data.putLong(DIG_POS, pos.asLong());
        data.putInt(DIG_PROGRESS, -1);
        data.putInt(DIG_COOLOFF, Math.max(0, cooloff));
        data.putInt(DIG_PAUSE, Math.max(0, pause));
    }

    void stopDigging(ServerLevel level) {
        var data = getPersistentData();
        long encoded = data.getLong(DIG_POS).orElse(Long.MIN_VALUE);
        if (encoded != Long.MIN_VALUE) {
            level.destroyBlockProgress(getId(), BlockPos.of(encoded), -1);
        }
        data.remove(DIG_POS);
        data.remove(DIG_PROGRESS);
        data.remove(DIG_COOLOFF);
        data.remove(DIG_PAUSE);
    }

    public boolean isDigging() {
        return diggingPosition() != null;
    }

    public int doorBreakTicks(boolean woodenDoor) {
        int base = woodenDoor ? 1920 : 480;
        int divisor = isNormalClay() ? 4 : isHardenedClay() ? 6 : 8;
        int ticks = Math.max(1, base / divisor);
        return isBloodMoonFrenzied() ? Math.max(1, ticks / 2) : ticks;
    }

    /** InfX limits earth-elemental natural spawns to a single mob per cluster. */
    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    public ItemStack materialDrop() {
        Block block = materialBlock();
        if (block == Blocks.STONE) {
            return Blocks.COBBLESTONE.asItem().getDefaultInstance();
        }
        return block.asItem().getDefaultInstance();
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel level, @NonNull DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        spawnAtLocation(level, materialDrop());
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("R196EarthForm", form().id());
        output.putInt("R196EarthHeat", heat);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        setForm(Form.fromId(input.getIntOr("R196EarthForm", form().id())));
        heat = Math.max(0, input.getIntOr("R196EarthHeat", 0));
        if (!isClayGolem() && heat >= MAGMA_THRESHOLD && !form().isMagmaForm()) {
            setForm(form().magmaForm());
        }
    }
}
