package com.pixulse.infx.event;

import com.pixulse.infx.world.PhysicsRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.registry.InfXItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Loose terrain, explosion conversion, falling impact and INFX fluid restrictions. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class PhysicsEvents {
    private PhysicsEvents() {}

    @SubscribeEvent
    public static void onNeighborUpdate(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        tryFall(level, event.getPos());
        for (Direction direction : event.getNotifiedSides()) tryFall(level, event.getPos().relative(direction));
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) tryFall(level, event.getPos());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            wetInventory(player);
        }
        if (event.getEntity() instanceof ItemEntity item
                && item.level() instanceof ServerLevel
                && item.isInWater()) {
            wetDroppedBucket(item);
        }
        if (event.getEntity() instanceof FallingBlockEntity falling
                && falling.level() instanceof ServerLevel level) {
            BlockState occupied = level.getBlockState(falling.blockPosition());
            if (isFragile(occupied)) {
                level.destroyBlock(falling.blockPosition(), true, falling);
                Vec3 velocity = falling.getDeltaMovement();
                falling.setDeltaMovement(velocity.x, Math.min(-0.08D, velocity.y), velocity.z);
            }
            return;
        }
        if (event.getEntity().level() instanceof ServerLevel level
                && event.getEntity().onGround()
                && event.getEntity().tickCount % 5 == 0) {
            tryFall(level, event.getEntity().blockPosition().below());
        }
    }

    /** InfX EntityItem.spentTickInWater: lava solidifies; other vessels fill with water. */
    private static void wetDroppedBucket(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        if (stack.getItem() instanceof InfxBucketItem bucket) {
            if (bucket.contents() == InfxBucketItem.Contents.LAVA) {
                entity.setItem(InfXItems.bucket(bucket.material(), InfxBucketItem.Contents.STONE)
                        .toStack(stack.getCount()));
                entity.level()
                        .playSound(
                                null,
                                entity.blockPosition(),
                                SoundEvents.FIRE_EXTINGUISH,
                                SoundSource.BLOCKS,
                                0.5F,
                                1.0F);
            } else if (bucket.contents() != InfxBucketItem.Contents.STONE
                    && bucket.contents() != InfxBucketItem.Contents.WATER) {
                entity.setItem(InfXItems.bucket(bucket.material(), InfxBucketItem.Contents.WATER)
                        .toStack(stack.getCount()));
            }
            return;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            entity.setItem(new ItemStack(Items.OBSIDIAN, stack.getCount()));
        } else if (stack.is(Items.BUCKET) || stack.is(Items.MILK_BUCKET)) {
            entity.setItem(new ItemStack(Items.WATER_BUCKET, stack.getCount()));
        } else if (stack.is(Items.BOWL)) {
            entity.setItem(InfXItems.WATER_BOWL.toStack(
                    Math.min(stack.getCount(), InfXItems.WATER_BOWL.get().getDefaultMaxStackSize())));
        }
    }

    private static void tryFall(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!PhysicsRules.isLoose(state) || !FallingBlock.isFree(level.getBlockState(pos.below()))) return;
        // FallingBlockEntity.fall removes the block (flag 3) and synchronously fires neighbor
        // updates, so the loose block above falls in the same cascade; each fall consumes its
        // own block, so the chain terminates.
        FallingBlockEntity.fall(level, pos, state);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Vec3 center = event.getExplosion().center();
        float radius = event.getExplosion().radius();
        // InfX Block#dropBlockAsEntityItem: blocks destroyed by any explosion drop their exploded
        // forms instead of themselves (wool -> string, wood -> sticks, stone -> cobblestone, ...).
        event.getAffectedBlocks().removeIf(pos -> convertExplodedBlock(level, pos));
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(center, center).inflate(radius))) {
            damageEquipment(item, center, radius);
        }
        level.getServer().execute(() -> {
            for (BlockPos affected : event.getAffectedBlocks()) {
                for (Direction direction : Direction.values()) tryFall(level, affected.relative(direction));
            }
        });
    }

    /**
     * InfX Block#dropBlockAsEntityItem exploded forms: wool->string, wood->sticks, brick->1 brick,
     * lapis block->9 blue dye at 50%, stone/end stone->cobblestone, coal block->9 coal at 50%,
     * terracotta and netherrack->nothing. Destroys the block and pops the replacement. Returns
     * true when the explosion entry was consumed.
     */
    private static boolean convertExplodedBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.BRICKS)) {
            return explodeInto(level, pos, new ItemStack(Items.BRICK));
        }
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)) {
            return explodeInto(level, pos, new ItemStack(Items.GRAVEL));
        }
        if (state.is(Blocks.LAPIS_BLOCK)) {
            return explodeInto(level, pos, dropQuantity(level, Items.LAPIS_LAZULI, 9, 0.5F));
        }
        if (state.is(BlockTags.WOOL)) {
            return explodeInto(level, pos, new ItemStack(Items.STRING));
        }
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) {
            return explodeInto(level, pos, new ItemStack(Items.STICK));
        }
        if (state.is(BlockTags.TERRACOTTA) || state.is(Blocks.NETHERRACK)) {
            return explodeInto(level, pos, ItemStack.EMPTY);
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.END_STONE)) {
            return explodeInto(level, pos, new ItemStack(Blocks.COBBLESTONE));
        }
        if (state.is(Blocks.COAL_BLOCK)) {
            return explodeInto(level, pos, dropQuantity(level, Items.COAL, 9, 0.5F));
        }
        return false;
    }

    /** InfX quantity/chance roll: each of {@code quantity} units drops with {@code chance}. */
    private static ItemStack dropQuantity(ServerLevel level, net.minecraft.world.item.Item item, int quantity, float chance) {
        int count = 0;
        for (int i = 0; i < quantity; i++) {
            if (level.getRandom().nextFloat() < chance) {
                count++;
            }
        }
        return new ItemStack(item, count);
    }

    /** Destroys the block without drops and pops the replacement item, if any. */
    private static boolean explodeInto(ServerLevel level, BlockPos pos, ItemStack drop) {
        level.destroyBlock(pos, false);
        if (!drop.isEmpty()) {
            Block.popResource(level, pos, drop);
        }
        return true;
    }

    private static void damageEquipment(ItemEntity entity, Vec3 center, float radius) {
        ItemStack stack = entity.getItem();
        if (!stack.isDamageableItem() || stack.is(Items.NETHER_STAR)) return;
        int wear = PhysicsRules.explosionWear(entity.position().distanceTo(center), radius);
        if (wear <= 0) return;
        int damage = stack.getDamageValue() + wear;
        if (damage >= stack.getMaxDamage()) stack.shrink(1);
        else stack.setDamageValue(damage);
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow)
                || !(event.getRayTraceResult() instanceof BlockHitResult hit)
                || !(event.getProjectile().level() instanceof ServerLevel level)) return;
        BlockState state = level.getBlockState(hit.getBlockPos());
        if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("glass_pane")) {
            level.destroyBlock(hit.getBlockPos(), true, event.getProjectile());
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        BlockPos landing = event.getEntity().blockPosition().below();
        BlockState state = event.getEntity().level().getBlockState(landing);
        float multiplier = PhysicsRules.fallDamageMultiplier(state)
                * PhysicsRules.snowLayerMultiplier(state);
        event.setDamageMultiplier(event.getDamageMultiplier() * multiplier);
    }

    @SubscribeEvent
    public static void coverFragileBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !isFragile(level.getBlockState(event.getPos()))
                || !(event.getItemStack().getItem() instanceof BlockItem blockItem)) return;
        level.setBlockAndUpdate(event.getPos(), blockItem.getBlock().defaultBlockState());
        if (!event.getEntity().hasInfiniteMaterials()) event.getItemStack().shrink(1);
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void restrictFluidSources(CreateFluidSourceEvent event) {
        boolean dispenser = false;
        for (Direction direction : Direction.values()) {
            if (event.getLevel().getBlockState(event.getPos().relative(direction)).getBlock() instanceof DispenserBlock) {
                dispenser = true;
                break;
            }
        }
        event.setCanConvert(dispenser);
    }

    @SubscribeEvent
    public static void meltLavaBucket(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !event.getItemStack().is(Items.LAVA_BUCKET)
                || level.getRandom().nextFloat() >= 0.08F) return;
        event.getItemStack().consume(1, event.getEntity());
        level.playSound(null, event.getPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.7F);
        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
    }

    private static void wetInventory(net.minecraft.server.level.ServerPlayer player) {
        if (!eyesInWater(player) || player.tickCount % 20 != 0) return;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof InfxBucketItem bucket
                    && bucket.contents() == InfxBucketItem.Contents.LAVA) {
                player.getInventory().setItem(
                        slot,
                        InfXItems.bucket(bucket.material(), InfxBucketItem.Contents.STONE)
                                .toStack(stack.getCount()));
            } else if (stack.getItem() instanceof InfxBucketItem bucket
                    && bucket.contents() == InfxBucketItem.Contents.MILK) {
                player.getInventory().setItem(
                        slot,
                        InfXItems.bucket(bucket.material(), InfxBucketItem.Contents.EMPTY)
                                .toStack(stack.getCount()));
            } else if (stack.is(Items.LAVA_BUCKET)) {
                player.getInventory().setItem(slot, new ItemStack(Items.OBSIDIAN, stack.getCount()));
            } else if (stack.is(Items.MILK_BUCKET)) {
                player.getInventory().setItem(slot, new ItemStack(Items.BUCKET, stack.getCount()));
            }
        }
    }

    private static boolean eyesInWater(net.minecraft.server.level.ServerPlayer player) {
        BlockPos eye = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        var fluid = player.level().getFluidState(eye);
        return fluid.is(FluidTags.WATER)
                && player.getEyeY() <= eye.getY() + fluid.getHeight(player.level(), eye);
    }

    private static boolean isFragile(BlockState state) {
        return state.is(BlockTags.BUTTONS)
                || state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.SOUL_TORCH)
                || state.is(Blocks.SOUL_WALL_TORCH)
                || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.REDSTONE_WALL_TORCH)
                || state.is(Blocks.REDSTONE_WIRE)
                || state.is(Blocks.TRIPWIRE)
                || state.is(Blocks.TRIPWIRE_HOOK)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.COBWEB);
    }
}
