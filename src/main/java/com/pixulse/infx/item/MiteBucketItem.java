package com.pixulse.infx.item;

import com.pixulse.infx.entity.FireElemental;
import com.pixulse.infx.entity.EarthElemental;
import com.pixulse.infx.entity.Livestock;
import com.pixulse.infx.entity.MiteSilverfish;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.network.Network;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.world.FluidDecayData;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** A material-preserving R196 bucket for empty, water, lava, milk and stone contents. */
public final class MiteBucketItem extends BucketItem {
    public static final int SOURCE_EXPERIENCE_COST = 100;
    public static final int LAVA_BURN_TIME = 3200;
    /** MITE scheduleBlockChange delay before a placed water cell degrades to flowing. */
    public static final int WATER_DECAY_DELAY = 16;
    /** MITE scheduleBlockChange delay before a placed lava cell degrades to flowing. */
    public static final int LAVA_DECAY_DELAY = 48;
    /** MITE prevent_item_pickup_due_to_held_item_breaking_until: 1.5s in ticks. */
    public static final int MELT_PICKUP_DELAY = 30;
    /** Persistent key holding the game time until which melt-induced pickup stays suppressed. */
    public static final String MELT_PICKUP_BLOCK = "infx_bucket_melt_pickup_block";
    /** MITE ItemVessel water damage to fire elementals. */
    public static final float FIRE_ELEMENTAL_QUENCH_DAMAGE = 20.0F;
    /** MITE ItemVessel water damage to netherspawn. */
    public static final float NETHERSPAWN_QUENCH_DAMAGE = 8.0F;

    public enum Contents {
        EMPTY(""),
        WATER("water_"),
        LAVA("lava_"),
        MILK("milk_"),
        STONE("stone_");

        private final String pathPrefix;

        Contents(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }

        public String path(MiteMaterial material) {
            return material.path() + "_" + pathPrefix + "bucket";
        }
    }

    private final MiteMaterial material;
    private final Contents contents;
    private final Supplier<? extends Item> emptyBucket;
    private final Supplier<? extends Item> waterBucket;
    private final Supplier<? extends Item> lavaBucket;

    public MiteBucketItem(
            MiteMaterial material,
            Contents contents,
            Supplier<? extends Item> emptyBucket,
            Supplier<? extends Item> waterBucket,
            Supplier<? extends Item> lavaBucket,
            Item.Properties properties) {
        super(fluid(contents), properties);
        this.material = material;
        this.contents = contents;
        this.emptyBucket = emptyBucket;
        this.waterBucket = waterBucket;
        this.lavaBucket = lavaBucket;
        registerDispenserBehavior();
    }

    private void registerDispenserBehavior() {
        switch (contents) {
            case EMPTY -> DispenserBlock.registerBehavior(this, emptyDispenserBehavior());
            case WATER, LAVA -> registerFilledDispenserBehavior(this, emptyBucket);
            case MILK, STONE -> {
                // These contents cannot be placed, so the dispenser keeps its default drop behavior.
            }
        }
    }

    static void registerFilledDispenserBehavior(Item filled, Supplier<? extends Item> emptyBucket) {
        DispenserBlock.registerBehavior(filled, new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior fallback = new DefaultDispenseItemBehavior();

            @Override
            public @NonNull ItemStack execute(net.minecraft.core.dispenser.@NonNull BlockSource source, @NonNull ItemStack dispensed) {
                DispensibleContainerItem container = (DispensibleContainerItem) dispensed.getItem();
                BlockPos target = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                Level level = source.level();
                if (!container.emptyContents(null, level, target, null, dispensed)) {
                    return fallback.dispense(source, dispensed);
                }
                container.checkExtraContent(null, level, dispensed, target);
                return consumeWithRemainder(source, dispensed, new ItemStack(emptyBucket.get()));
            }
        });
    }

    private DefaultDispenseItemBehavior emptyDispenserBehavior() {
        return new DefaultDispenseItemBehavior() {
            @Override
            public @NonNull ItemStack execute(net.minecraft.core.dispenser.@NonNull BlockSource source, @NonNull ItemStack dispensed) {
                LevelAccessor level = source.level();
                BlockPos target = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                BlockState state = level.getBlockState(target);
                if (!(state.getBlock() instanceof BucketPickup pickup)) return super.execute(source, dispensed);
                ItemStack filledStack;
                if (state.getFluidState().is(Fluids.WATER)) {
                    filledStack = new ItemStack(waterBucket.get());
                } else if (state.getFluidState().is(Fluids.LAVA)) {
                    filledStack = new ItemStack(lavaBucket.get());
                } else if (state.is(Blocks.POWDER_SNOW)) {
                    filledStack = InfXItems.powderSnowBucket(material).toStack();
                } else {
                    return super.execute(source, dispensed);
                }
                ItemStack vanilla = pickup.pickupBlock(null, level, target, state);
                if (vanilla.isEmpty()) return super.execute(source, dispensed);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, target);
                if (state.getFluidState().is(Fluids.LAVA)
                        && source.level().getRandom().nextFloat() < lavaMeltChance(material)) {
                    dispensed.shrink(1);
                    source.level().playSound(
                            null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.7F);
                    return dispensed;
                }
                return consumeWithRemainder(source, dispensed, filledStack);
            }
        };
    }

    private static Fluid fluid(Contents contents) {
        return switch (contents) {
            case WATER -> Fluids.WATER;
            case LAVA -> Fluids.LAVA;
            case EMPTY, MILK, STONE -> Fluids.EMPTY;
        };
    }

    public MiteMaterial material() {
        return material;
    }

    public Contents contents() {
        return contents;
    }

    public Item emptyBucket() {
        return emptyBucket.get();
    }

    public float lavaMeltChance() {
        return lavaMeltChance(material);
    }

    /**
     * MITE ItemBucket#getChanceOfMeltingWhenFilledWithLava: adamantium is lava safe, gold is a flat
     * 20%, everything else scales inversely with material durability against mithril's 1% baseline.
     */
    public static float lavaMeltChance(MiteMaterial material) {
        return switch (material) {
            case ADAMANTIUM -> 0.0F;
            case GOLD -> 0.20F;
            case COPPER, SILVER, IRON, ANCIENT_METAL, MITHRIL ->
                0.01F * (MiteMaterial.MITHRIL.durabilityMultiplier() / material.durabilityMultiplier());
            default -> throw new IllegalArgumentException("No R196 bucket for " + material);
        };
    }

    public static boolean canPlaceAsSource(Player player, boolean forceSource) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        return forceSource && player.totalExperience >= SOURCE_EXPERIENCE_COST;
    }

    /** Replays a Ctrl-modified use through the server game mode so its returned hand item is applied. */
    public InteractionResult useWithCtrl(ServerPlayer player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(this)
                || contents == Contents.MILK
                || contents == Contents.STONE
                || contents != Contents.EMPTY && !canPlaceAsSource(player, true)) {
            return InteractionResult.FAIL;
        }
        player.getPersistentData().putBoolean(Network.CTRL_USE, true);
        try {
            return player.gameMode.useItem(player, player.level(), held, hand);
        } finally {
            player.getPersistentData().remove(Network.CTRL_USE);
        }
    }

    /** Returns whether a lava-melted bucket is still suppressing this player's item pickup. */
    public static boolean isMeltPickupBlocked(Player player) {
        long until = player.getPersistentData().getLong(MELT_PICKUP_BLOCK).orElse(0L);
        if (until <= 0L) {
            return false;
        }
        if (player.level().getGameTime() < until) {
            return true;
        }
        player.getPersistentData().remove(MELT_PICKUP_BLOCK);
        return false;
    }

    @Override
    public int getBurnTime(@NonNull ItemStack itemStack, @Nullable RecipeType<?> recipeType, @NonNull FuelValues fuelValues) {
        return contents == Contents.LAVA ? LAVA_BURN_TIME : 0;
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        return switch (contents) {
            case EMPTY -> fill(level, player, hand);
            case WATER, LAVA -> empty(level, player, hand);
            case MILK -> consume(player, hand);
            case STONE -> InteractionResult.FAIL;
        };
    }

    private InteractionResult consume(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // MITE ItemBucketMilk#onItemRightClick: milk douses fire before it can be drunk.
        Level level = player.level();
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos firePos = fireTarget(level, hit);
            if (firePos != null) {
                return douse(level, player, hand, firePos);
            }
        }
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        return consumable == null ? InteractionResult.FAIL : consumable.startConsuming(player, stack, hand);
    }

    private InteractionResult empty(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() == HitResult.Type.BLOCK && contents == Contents.WATER) {
            BlockPos farmlandPos = farmlandTarget(level, hit);
            if (farmlandPos != null && moistenFarmland(level, player, farmlandPos)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                ItemStack emptied = ItemUtils.createFilledResult(held, player, new ItemStack(emptyBucket.get()));
                return InteractionResult.SUCCESS.heldItemTransformedTo(emptied);
            }
        }
        // MITE ItemBucket#onItemRightClick: pouring a liquid back into itself only spends the bucket.
        if (hit.getType() == HitResult.Type.BLOCK && !player.hasInfiniteMaterials() && sameLiquidAt(level, hit)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            ItemStack emptied = ItemUtils.createFilledResult(held, player, new ItemStack(emptyBucket.get()));
            return InteractionResult.SUCCESS.heldItemTransformedTo(emptied);
        }
        // Vanilla use() hands emptyContents the neighbour of the clicked cell, so a fire hit would place
        // water beside the flame instead of dousing it. Intercept here; the dispenser path is guarded
        // inside emptyContents.
        if (hit.getType() == HitResult.Type.BLOCK && canDouseFire()) {
            BlockPos firePos = fireTarget(level, hit);
            if (firePos != null) {
                return douse(level, player, hand, firePos);
            }
        }

        InteractionResult result = super.use(level, player, hand);
        if (result instanceof InteractionResult.Success success) {
            ItemStack transformed = success.heldItemTransformedTo();
            if (transformed != null && transformed.is(Items.BUCKET)) {
                return success.heldItemTransformedTo(new ItemStack(emptyBucket.get(), transformed.getCount()));
            }
        }
        return result;
    }

    private static @Nullable BlockPos farmlandTarget(Level level, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.FARMLAND) && hit.getDirection() == Direction.UP) {
            return pos;
        }
        BlockPos below = pos.below();
        if (level.getBlockState(below).is(Blocks.FARMLAND) && hit.getDirection() == Direction.UP) {
            return below;
        }
        return null;
    }

    /**
     * MITE ItemBucket#onItemRightClick: the contents already occupy the hit cell or the cell beyond
     * the hit face, so emptying changes nothing. Matches both source and flowing states.
     */
    private boolean sameLiquidAt(Level level, BlockHitResult hit) {
        if (content == Fluids.EMPTY) {
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        return level.getFluidState(pos).is(content)
                || level.getFluidState(pos.relative(hit.getDirection())).is(content);
    }

    /** MITE World#douseFire target: the fire cell hit directly, or the one beyond the hit face. */
    private static @Nullable BlockPos fireTarget(Level level, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        if (level.getBlockState(pos).getBlock() instanceof BaseFireBlock) {
            return pos;
        }
        BlockPos neighbor = pos.relative(hit.getDirection());
        return level.getBlockState(neighbor).getBlock() instanceof BaseFireBlock ? neighbor : null;
    }

    /** MITE World#douseFire: extinguish without placing the contents, still spending the vessel. */
    private InteractionResult douse(Level level, Player player, InteractionHand hand, BlockPos pos) {
        ItemStack held = player.getItemInHand(hand);
        douseFire(level, pos);
        player.awardStat(Stats.ITEM_USED.get(this));
        ItemStack emptied = ItemUtils.createFilledResult(held, player, new ItemStack(emptyBucket.get()));
        return InteractionResult.SUCCESS.heldItemTransformedTo(emptied);
    }

    /** Moistens a 3×3 farmland patch (MITE water-bucket fertilize). */
    public static boolean moistenFarmland(Level level, @Nullable Player player, BlockPos center) {
        if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, center)) {
            if (!level.isClientSide()) {
                level.playSound(
                        null,
                        center,
                        SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.5F,
                        2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
            }
            return true;
        }
        boolean moistened = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                BlockState state = level.getBlockState(pos);
                if (!state.is(Blocks.FARMLAND) || !state.hasProperty(FarmlandBlock.MOISTURE)) {
                    continue;
                }
                if (state.getValue(FarmlandBlock.MOISTURE) < 7) {
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(pos, state.setValue(FarmlandBlock.MOISTURE, 7));
                    }
                    moistened = true;
                }
            }
        }
        return moistened;
    }

    private InteractionResult fill(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        BlockPos pos = hit.getBlockPos();
        Direction direction = hit.getDirection();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos.relative(direction), direction, held)) {
            return InteractionResult.FAIL;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) return InteractionResult.FAIL;
        FluidState fluidState = state.getFluidState();
        Supplier<? extends Item> filled = fluidState.is(FluidTags.WATER)
                ? waterBucket
                : fluidState.is(FluidTags.LAVA)
                        ? lavaBucket
                        : state.is(Blocks.POWDER_SNOW)
                                ? () -> InfXItems.powderSnowBucket(material).value()
                                : null;
        if (filled == null) return InteractionResult.FAIL;

        // MITE ItemBucket#onItemRightClick: scooping source or flowing liquid leaves its cell in place.
        // The cell is only consumed in creative or with Ctrl held, MITE's "take this cell" modifier.
        // Waterlogged blocks are not liquid cells, so they keep vanilla pickup.
        boolean liquidCell = state.getBlock() instanceof LiquidBlock;
        if (!liquidCell) {
            if (pickup.pickupBlock(player, level, pos, state).isEmpty()) return InteractionResult.FAIL;
        } else if (shouldTakeLiquidCell(player)) {
            if (fluidState.isSource()) {
                if (pickup.pickupBlock(player, level, pos, state).isEmpty()) return InteractionResult.FAIL;
            } else if (!level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11)) {
                return InteractionResult.FAIL;
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        pickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        if (!level.isClientSide() && fluidState.is(FluidTags.LAVA)
                && level.getRandom().nextFloat() < lavaMeltChance()) {
            return meltInLava(level, player, hand, pos);
        }

        ItemStack result = ItemUtils.createFilledResult(held, player, new ItemStack(filled.get()));
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, new ItemStack(filled.get()));
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(result);
    }

    /** MITE ctrl_is_down while filling: consume the liquid cell instead of leaving it behind. */
    private static boolean shouldTakeLiquidCell(Player player) {
        return player.hasInfiniteMaterials()
                || player.getPersistentData().getBooleanOr(Network.CTRL_USE, false);
    }

    /**
     * MITE ItemBucket#onItemRightClick melt branch: the vessel is destroyed outright because every
     * bucket metal except adamantium is harmed by lava, and adamantium never melts. Pickup is
     * suppressed briefly so the empty hand does not immediately grab a nearby drop.
     */
    private InteractionResult meltInLava(Level level, Player player, InteractionHand hand, BlockPos pos) {
        ItemStack held = player.getItemInHand(hand);
        player.awardStat(Stats.ITEM_BROKEN.get(this));
        held.consume(1, player);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.7F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    8,
                    0.25,
                    0.25,
                    0.25,
                    0.0);
        }
        if (player.getItemInHand(hand).isEmpty()) {
            player.getPersistentData()
                    .putLong(MELT_PICKUP_BLOCK, player.level().getGameTime() + MELT_PICKUP_DELAY);
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(player.getItemInHand(hand));
    }

    @Override
    public boolean emptyContents(
            @Nullable LivingEntity user,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @Nullable BlockHitResult hitResult,
            @Nullable ItemStack containerItem) {
        if (!(this.content instanceof FlowingFluid flowingFluid)) {
            return false;
        }
        BlockState blockState = level.getBlockState(pos);
        boolean mayReplace = blockState.canBeReplaced(this.content);
        boolean shiftKeyDown = user != null && user.isShiftKeyDown();
        boolean placeLiquid = mayReplace
                || blockState.getBlock() instanceof LiquidBlockContainer container
                        && container.canPlaceLiquid(user, level, pos, blockState, this.content);
        boolean canPlaceFluidInsideBlock = blockState.isAir() || placeLiquid && (!shiftKeyDown || hitResult == null);
        if (!canPlaceFluidInsideBlock) {
            return hitResult != null
                    && this.emptyContents(
                            user, level, hitResult.getBlockPos().relative(hitResult.getDirection()), null, containerItem);
        }

        // MITE tryPlaceContainedLiquid: a dousing liquid aimed at fire only extinguishes it.
        if (canDouseFire() && blockState.getBlock() instanceof BaseFireBlock) {
            douseFire(level, pos);
            return true;
        }

        if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)
                && this.content.is(FluidTags.WATER)) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            RandomSource random = level.getRandom();
            level.playSound(
                    user,
                    pos,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.5F,
                    2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
            for (int i = 0; i < 8; i++) {
                level.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        x + random.nextFloat(),
                        y + random.nextFloat(),
                        z + random.nextFloat(),
                        0.0,
                        0.0,
                        0.0);
            }
            return true;
        }

        boolean placeAsSource = shouldPlaceAsSource(user);
        if (blockState.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(user, level, pos, blockState, content)) {
            // Waterloggable blocks always take a source fluid cell.
            container.placeLiquid(level, pos, blockState, flowingFluid.getSource(false));
            chargeSourceExperience(user, placeAsSource);
            this.playEmptySound(user, level, pos);
            return true;
        }

        if (!level.isClientSide() && mayReplace && !blockState.liquid()) {
            level.destroyBlock(pos, true);
        }

        // MITE tryConvertLavaToCobblestoneOrObsidian / tryConvertWaterToCobblestone: contact between the
        // two liquids sets stone instead of leaving a fluid cell.
        if (convertOnContact(level, pos)) {
            return true;
        }

        // MITE always writes a source cell and schedules the degrade separately, so the pour spreads
        // once before settling to flowing unless the player paid for a permanent source.
        if (!level.setBlock(pos, flowingFluid.getSource(false).createLegacyBlock(), 11)
                && !blockState.getFluidState().isSource()) {
            return false;
        }
        if (placeAsSource) {
            chargeSourceExperience(user, true);
            // A paid source must not be demoted by a degrade an earlier free pour left queued here.
            cancelSourceDecay(level, pos);
        } else {
            scheduleSourceDecay(level, pos);
        }
        this.playEmptySound(user, level, pos);
        return true;
    }

    /** MITE water↔lava contact conversion. Returns true when stone replaced the fluid cell. */
    private boolean convertOnContact(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }
        FluidState target = level.getFluidState(pos);
        if (content.is(FluidTags.WATER) && target.is(FluidTags.LAVA)) {
            // A full lava source becomes obsidian; anything shallower becomes cobblestone.
            boolean source = target.isSource();
            level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
            level.setBlockAndUpdate(pos, source ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
            return true;
        }
        if (content.is(FluidTags.LAVA) && target.is(FluidTags.WATER)) {
            level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
            level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
            return true;
        }
        return false;
    }

    /** Queues the MITE scheduleBlockChange that degrades an unpaid source cell to flowing. */
    private void scheduleSourceDecay(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int delay = content.is(FluidTags.LAVA) ? LAVA_DECAY_DELAY : WATER_DECAY_DELAY;
        FluidDecayData.get(serverLevel).schedule(pos, content.is(FluidTags.LAVA), serverLevel.getGameTime() + delay);
        serverLevel.scheduleTick(pos, content, delay);
    }

    /** Drops any queued degrade for a cell, so a paid source stays permanent. */
    private static void cancelSourceDecay(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            FluidDecayData.get(serverLevel).cancel(pos);
        }
    }

    private boolean canDouseFire() {
        return content.is(FluidTags.WATER) || contents == Contents.MILK;
    }

    /** MITE World#douseFire: smoke and steam, then the fire cell clears. */
    static void douseFire(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
        serverLevel.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                8,
                0.25,
                0.25,
                0.25,
                0.0);
        serverLevel.removeBlock(pos, false);
    }

    private boolean shouldPlaceAsSource(@Nullable LivingEntity user) {
        if (!(user instanceof Player player)) {
            // Dispensers keep source placement so automated infinite sources stay gated by CreateFluidSourceEvent.
            return true;
        }
        boolean force = player.getPersistentData().getBooleanOr(Network.CTRL_USE, false);
        return canPlaceAsSource(player, force);
    }

    private static void chargeSourceExperience(@Nullable LivingEntity user, boolean placedAsSource) {
        if (!placedAsSource
                || !(user instanceof Player player)
                || player.hasInfiniteMaterials()
                || player.level().isClientSide()) {
            return;
        }
        if (player.getPersistentData().getBooleanOr(Network.CTRL_USE, false)) {
            player.giveExperiencePoints(-SOURCE_EXPERIENCE_COST);
        }
    }

    @Override
    protected void playEmptySound(@Nullable LivingEntity user, @NonNull LevelAccessor level, @NonNull BlockPos pos) {
        SoundEvent soundEvent = this.content.getFluidType()
                .getSound(user, level, pos, net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY);
        if (soundEvent == null) {
            soundEvent = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }
        // Ctrl use is canceled client-side and replayed only on the server, so the placing player
        // needs the server broadcast that ordinary bucket use replaces with a local sound.
        LivingEntity excludedPlayer = user;
        if (user instanceof ServerPlayer player
                && player.getPersistentData().getBooleanOr(Network.CTRL_USE, false)) {
            excludedPlayer = null;
        }
        level.playSound(excludedPlayer, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(user, GameEvent.FLUID_PLACE, pos);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (contents == Contents.MILK && result.isEmpty() && !entity.hasInfiniteMaterials()) {
            return new ItemStack(emptyBucket.get());
        }
        return result;
    }

    /**
     * MITE ItemVessel#tryEntityInteraction: water satisfies thirsty livestock and quenches
     * fire-aligned mobs. Thirst lives in server-side persistent data, so the client defers.
     */
    @Override
    public @NonNull InteractionResult interactLivingEntity(
            @NonNull ItemStack stack, @NonNull Player player, @NonNull LivingEntity target, @NonNull InteractionHand hand) {
        if (contents != Contents.WATER || !(player.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        if (target instanceof Animal animal
                && Livestock.isLivestock(animal)
                && Livestock.isThirsty(animal, serverLevel.getGameTime())) {
            Livestock.markWatered(animal, serverLevel.getGameTime());
            serverLevel.playSound(
                    null, target.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.NEUTRAL, 1.0F, 1.0F);
            return spendOnEntity(player, hand);
        }
        if (target instanceof EarthElemental elemental && elemental.quench(serverLevel)) {
            return spendOnEntity(player, hand);
        }
        float damage = quenchDamage(target);
        if (damage <= 0.0F) {
            return InteractionResult.PASS;
        }
        target.hurtServer(serverLevel, serverLevel.damageSources().drown(), damage);
        target.clearFire();
        serverLevel.playSound(
                null, target.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.7F, 1.6F);
        serverLevel.sendParticles(
                ParticleTypes.LARGE_SMOKE, target.getX(), target.getY(0.5), target.getZ(), 8, 0.25, 0.25, 0.25, 0.0);
        return spendOnEntity(player, hand);
    }

    /**
     * MITE water damage tiers: 20 against fire elementals, 8 against netherspawn. Deliberately not
     * keyed on {@code isSensitiveToWater}, which also covers endermen, snow golems and striders that
     * MITE's vessel interaction never touched.
     */
    private static float quenchDamage(LivingEntity target) {
        if (target instanceof FireElemental) {
            return FIRE_ELEMENTAL_QUENCH_DAMAGE;
        }
        if (target instanceof MiteSilverfish silverfish
                && silverfish.variant() == MiteSilverfish.Variant.NETHERSPAWN) {
            return NETHERSPAWN_QUENCH_DAMAGE;
        }
        return 0.0F;
    }

    private InteractionResult spendOnEntity(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(emptyBucket.get())));
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(@NonNull ItemInstance instance) {
        return contents == Contents.EMPTY ? null : new ItemStackTemplate(emptyBucket.get());
    }
}
