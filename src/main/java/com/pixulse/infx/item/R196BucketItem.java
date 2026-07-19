package com.pixulse.infx.item;

import com.pixulse.infx.material.R196Material;
import java.util.function.Supplier;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/** A material-preserving R196 bucket for empty, water, lava, milk and stone contents. */
public final class R196BucketItem extends BucketItem {
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
        InteractionResult result = super.use(level, player, hand);
        if (result instanceof InteractionResult.Success success) {
            ItemStack transformed = success.heldItemTransformedTo();
            if (transformed != null && transformed.is(Items.BUCKET)) {
                return success.heldItemTransformedTo(new ItemStack(emptyBucket.get(), transformed.getCount()));
            }
        }
        return result;
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
                : state.getFluidState().is(Fluids.LAVA) ? lavaBucket : null;
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
