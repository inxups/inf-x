package com.pixulse.infx.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Loose terrain, explosion conversion, falling impact and R196 fluid restrictions. */
public final class R196PhysicsEvents {
    private static boolean updatingGravity;

    private R196PhysicsEvents() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196PhysicsEvents::onNeighborUpdate);
        gameBus.addListener(R196PhysicsEvents::onBlockPlaced);
        gameBus.addListener(R196PhysicsEvents::onEntityTick);
        gameBus.addListener(R196PhysicsEvents::onExplosion);
        gameBus.addListener(R196PhysicsEvents::onProjectileImpact);
        gameBus.addListener(R196PhysicsEvents::onLivingFall);
        gameBus.addListener(R196PhysicsEvents::coverFragileBlock);
        gameBus.addListener(R196PhysicsEvents::restrictFluidSources);
        gameBus.addListener(R196PhysicsEvents::meltLavaBucket);
        gameBus.addListener(R196PhysicsEvents::wetInventory);
    }

    private static void onNeighborUpdate(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || updatingGravity) return;
        tryFall(level, event.getPos());
        for (Direction direction : event.getNotifiedSides()) tryFall(level, event.getPos().relative(direction));
    }

    private static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) tryFall(level, event.getPos());
    }

    private static void onEntityTick(EntityTickEvent.Post event) {
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

    private static void tryFall(ServerLevel level, BlockPos pos) {
        if (updatingGravity || !level.isLoaded(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!R196PhysicsRules.isLoose(state) || !FallingBlock.isFree(level.getBlockState(pos.below()))) return;
        updatingGravity = true;
        try {
            FallingBlockEntity entity = FallingBlockEntity.fall(level, pos, state);
            entity.setHurtsEntities(1.5F, 40);
        } finally {
            updatingGravity = false;
        }
    }

    private static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Vec3 center = event.getExplosion().center();
        float radius = event.getExplosion().radius();
        boolean tnt = event.getExplosion().getDirectSourceEntity() instanceof PrimedTnt;
        if (tnt) {
            event.getAffectedBlocks().removeIf(pos -> {
                if (!level.getBlockState(pos).is(Blocks.COBBLESTONE)
                        || Vec3.atCenterOf(pos).distanceTo(center) > Math.min(3.0F, radius)) return false;
                level.setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
                return true;
            });
            for (ItemEntity item : level.getEntitiesOfClass(
                    ItemEntity.class, new AABB(center, center).inflate(Math.min(3.0F, radius)))) {
                if (item.getItem().is(Items.COBBLESTONE)) {
                    item.setItem(new ItemStack(Items.GRAVEL, item.getItem().getCount()));
                }
            }
        }
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(center, center).inflate(radius))) {
            damageEquipment(item, center, radius);
        }
        level.getServer().execute(() -> {
            for (BlockPos affected : event.getAffectedBlocks()) {
                for (Direction direction : Direction.values()) tryFall(level, affected.relative(direction));
            }
        });
    }

    private static void damageEquipment(ItemEntity entity, Vec3 center, float radius) {
        ItemStack stack = entity.getItem();
        if (!stack.isDamageableItem() || stack.is(Items.NETHER_STAR)) return;
        int wear = R196PhysicsRules.explosionWear(entity.position().distanceTo(center), radius);
        if (wear <= 0) return;
        int damage = stack.getDamageValue() + wear;
        if (damage >= stack.getMaxDamage()) stack.shrink(1);
        else stack.setDamageValue(damage);
    }

    private static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow)
                || !(event.getRayTraceResult() instanceof BlockHitResult hit)
                || !(event.getProjectile().level() instanceof ServerLevel level)) return;
        BlockState state = level.getBlockState(hit.getBlockPos());
        if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("glass_pane")) {
            level.destroyBlock(hit.getBlockPos(), true, event.getProjectile());
        }
    }

    private static void onLivingFall(LivingFallEvent event) {
        BlockPos landing = event.getEntity().blockPosition().below();
        BlockState state = event.getEntity().level().getBlockState(landing);
        float multiplier = R196PhysicsRules.fallDamageMultiplier(state)
                * R196PhysicsRules.snowLayerMultiplier(state);
        event.setDamageMultiplier(event.getDamageMultiplier() * multiplier);
    }

    private static void coverFragileBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !isFragile(level.getBlockState(event.getPos()))
                || !(event.getItemStack().getItem() instanceof BlockItem blockItem)) return;
        level.setBlockAndUpdate(event.getPos(), blockItem.getBlock().defaultBlockState());
        if (!event.getEntity().hasInfiniteMaterials()) event.getItemStack().shrink(1);
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        event.setCanceled(true);
    }

    private static void restrictFluidSources(CreateFluidSourceEvent event) {
        boolean dispenser = false;
        for (Direction direction : Direction.values()) {
            if (event.getLevel().getBlockState(event.getPos().relative(direction)).getBlock() instanceof DispenserBlock) {
                dispenser = true;
                break;
            }
        }
        event.setCanConvert(dispenser);
    }

    private static void meltLavaBucket(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !event.getItemStack().is(Items.LAVA_BUCKET)
                || level.getRandom().nextFloat() >= 0.08F) return;
        event.getItemStack().consume(1, event.getEntity());
        level.playSound(null, event.getPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.7F);
        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
    }

    private static void wetInventory(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                || !player.isInWater()
                || player.tickCount % 20 != 0) return;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.LAVA_BUCKET)) {
                player.getInventory().setItem(slot, new ItemStack(Items.OBSIDIAN, stack.getCount()));
            } else if (stack.is(Items.MILK_BUCKET)) {
                player.getInventory().setItem(slot, new ItemStack(Items.BUCKET, stack.getCount()));
            }
        }
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
