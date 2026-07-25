package com.pixulse.infx.item;

import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.network.R196Network;
import java.util.function.Supplier;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
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
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
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
import org.jspecify.annotations.Nullable;

/** A material-preserving R196 bucket for empty, water, lava, milk and stone contents. */
public final class R196BucketItem extends BucketItem {
    public static final int SOURCE_EXPERIENCE_COST = 100;
    public static final int LAVA_BURN_TIME = 3200;

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

        public String path(R196Material material) {
            return material.path() + "_" + pathPrefix + "bucket";
        }
    }

    private final R196Material material;
    private final Contents contents;
    private final Supplier<? extends Item> emptyBucket;
    private final Supplier<? extends Item> waterBucket;
    private final Supplier<? extends Item> lavaBucket;

    public R196BucketItem(
            R196Material material,
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
    }

    private static Fluid fluid(Contents contents) {
        return switch (contents) {
            case WATER -> Fluids.WATER;
            case LAVA -> Fluids.LAVA;
            case EMPTY, MILK, STONE -> Fluids.EMPTY;
        };
    }

    public R196Material material() {
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

    public static float lavaMeltChance(R196Material material) {
        return switch (material) {
            case COPPER, SILVER -> 0.16F;
            case GOLD -> 0.20F;
            case IRON -> 0.08F;
            case ANCIENT_METAL -> 0.04F;
            case MITHRIL -> 0.01F;
            case ADAMANTIUM -> 0.0F;
            default -> throw new IllegalArgumentException("No R196 bucket for " + material);
        };
    }

    public static boolean canPlaceAsSource(Player player, boolean forceSource) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        return forceSource && player.totalExperience >= SOURCE_EXPERIENCE_COST;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        return contents == Contents.LAVA ? LAVA_BURN_TIME : 0;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return switch (contents) {
            case EMPTY -> fill(level, player, hand);
            case WATER, LAVA -> empty(level, player, hand);
            case MILK -> consume(player, hand);
            case STONE -> InteractionResult.FAIL;
        };
    }

    private InteractionResult consume(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
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
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        BlockPos pos = hit.getBlockPos();
        Direction direction = hit.getDirection();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos.relative(direction), direction, held)) {
            return InteractionResult.FAIL;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) return InteractionResult.FAIL;
        Supplier<? extends Item> filled = state.getFluidState().is(Fluids.WATER)
                ? waterBucket
                : state.getFluidState().is(Fluids.LAVA)
                        ? lavaBucket
                        : state.is(Blocks.POWDER_SNOW)
                                ? () -> com.pixulse.infx.registry.ModItems.powderSnowBucket(material).value()
                                : null;
        if (filled == null) return InteractionResult.FAIL;
        ItemStack vanillaResult = pickup.pickupBlock(player, level, pos, state);
        if (vanillaResult.isEmpty()) return InteractionResult.FAIL;

        player.awardStat(Stats.ITEM_USED.get(this));
        pickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        if (!level.isClientSide() && vanillaResult.is(Items.LAVA_BUCKET)
                && level.getRandom().nextFloat() < lavaMeltChance()) {
            held.consume(1, player);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.7F);
            return InteractionResult.SUCCESS.heldItemTransformedTo(held);
        }

        ItemStack result = ItemUtils.createFilledResult(held, player, new ItemStack(filled.get()));
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, new ItemStack(filled.get()));
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(result);
    }

    @Override
    public boolean emptyContents(
            @Nullable LivingEntity user,
            Level level,
            BlockPos pos,
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

        FluidState fluidState = placeAsSource
                ? flowingFluid.getSource(false)
                : flowingFluid.getFlowing(1, false);
        if (!level.setBlock(pos, fluidState.createLegacyBlock(), 11) && !blockState.getFluidState().isSource()) {
            return false;
        }
        chargeSourceExperience(user, placeAsSource);
        this.playEmptySound(user, level, pos);
        return true;
    }

    private boolean shouldPlaceAsSource(@Nullable LivingEntity user) {
        if (!(user instanceof Player player)) {
            // Dispensers keep source placement so automated infinite sources stay gated by CreateFluidSourceEvent.
            return true;
        }
        boolean force = player.getPersistentData().getBooleanOr(R196Network.FORCE_PLACE_FLUID_SOURCE, false);
        return canPlaceAsSource(player, force);
    }

    private static void chargeSourceExperience(@Nullable LivingEntity user, boolean placedAsSource) {
        if (!placedAsSource
                || !(user instanceof Player player)
                || player.hasInfiniteMaterials()
                || player.level().isClientSide()) {
            return;
        }
        if (player.getPersistentData().getBooleanOr(R196Network.FORCE_PLACE_FLUID_SOURCE, false)) {
            player.giveExperiencePoints(-SOURCE_EXPERIENCE_COST);
        }
    }

    @Override
    protected void playEmptySound(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos) {
        SoundEvent soundEvent = this.content.getFluidType()
                .getSound(user, level, pos, net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY);
        if (soundEvent == null) {
            soundEvent = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }
        level.playSound(user, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(user, GameEvent.FLUID_PLACE, pos);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (contents == Contents.MILK && result.isEmpty() && !entity.hasInfiniteMaterials()) {
            return new ItemStack(emptyBucket.get());
        }
        return result;
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        return contents == Contents.EMPTY ? null : new ItemStackTemplate(emptyBucket.get());
    }
}
