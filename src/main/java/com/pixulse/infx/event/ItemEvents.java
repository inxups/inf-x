package com.pixulse.infx.event;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.data.food.FoodIngestion;
import com.pixulse.infx.item.InfxBucketItem;
import com.pixulse.infx.item.MobBucketKind;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.network.Network;
import com.pixulse.infx.util.BucketHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Item-level behavior that has a public NeoForge event equivalent. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class ItemEvents {
    public static final int FLINT_AND_STEEL_DURABILITY = 16;
    public static final int DIAMOND_EXPERIENCE = 500;
    public static final int EMERALD_EXPERIENCE = 250;
    public static final int LAPIS_LAZULI_EXPERIENCE = 50;
    public static final int QUARTZ_EXPERIENCE = 25;

    private ItemEvents() {}

    /**
     * InfX flint and steel has 16 durability instead of the modern 64. Applied to the default
     * MAX_DAMAGE component on the mod event bus, so every stack reports the InfX value through
     * {@link ItemStack#getMaxDamage()}.
     */
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(Items.FLINT_AND_STEEL, (components, context, item) ->
                components.set(DataComponents.MAX_DAMAGE, FLINT_AND_STEEL_DURABILITY));
    }

    /**
     * Enforces InfX's player-specific pickup grace after a held INFX bucket melts.
     * {@link ItemEntityPickupEvent.Pre} fires at the head of {@code ItemEntity#playerTouch}
     * exactly where the old mixin cancelled, with the same server-only scope.
     */
    @SubscribeEvent
    public static void blockPickupAfterBucketMelt(ItemEntityPickupEvent.Pre event) {
        if (InfxBucketItem.isMeltPickupBlocked(event.getPlayer())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    /**
     * Gems and quartz can be redeemed for experience by right-clicking them in hand, consuming
     * one item per use, following the same pattern as {@link com.pixulse.infx.item.CoinItem}.
     * {@link PlayerInteractEvent.RightClickItem} fires right before {@code ItemStack#use} on both
     * the client prediction and the server, the same boundary the coin item uses for its own use.
     */
    @SubscribeEvent
    public static void redeemGemExperience(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand());
        int experience = gemExperience(held.getItem());
        if (experience <= 0) return;
        if (!event.getLevel().isClientSide()) {
            held.shrink(1);
            player.giveExperiencePoints(experience);
            playExperienceFeedback((ServerLevel) event.getLevel(), player, experience);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS.heldItemTransformedTo(held));
    }

    private static void playExperienceFeedback(ServerLevel level, Player player, int experience) {
        level.playSound(
                null,
                player.getX(),
                player.getY() + player.getEyeHeight() * 0.5D,
                player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                0.4F,
                1.0F);
        level.sendParticles(
                ParticleTypes.ENCHANT,
                player.getX(),
                player.getY() + player.getEyeHeight() * 0.5D,
                player.getZ(),
                Math.clamp(experience / 10, 8, 40),
                0.5D,
                0.4D,
                0.5D,
                0.05D);
    }

    public static int gemExperience(Item item) {
        if (item == Items.DIAMOND) return DIAMOND_EXPERIENCE;
        if (item == Items.EMERALD) return EMERALD_EXPERIENCE;
        if (item == Items.LAPIS_LAZULI) return LAPIS_LAZULI_EXPERIENCE;
        if (item == Items.QUARTZ) return QUARTZ_EXPERIENCE;
        return 0;
    }

    /**
     * EggItem hard-codes throwing; INFX gives eating priority while food is needed.
     * {@link PlayerInteractEvent.RightClickItem} fires right before {@code ItemStack#use} on both
     * the client prediction and the server, the same boundary the old mixin intercepted.
     */
    @SubscribeEvent
    public static void eatBeforeThrowingEgg(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack egg = player.getItemInHand(event.getHand());
        if (!(egg.getItem() instanceof EggItem)
                || player.getPersistentData().getBooleanOr(Network.FORCE_EGG_THROW, false)
                || !FoodIngestion.canIngest(player, egg)) {
            return;
        }
        Consumable consumable = egg.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            event.setCanceled(true);
            event.setCancellationResult(consumable.startConsuming(player, egg, event.getHand()));
        }
    }

    /**
     * Vanilla {@link Bucketable} only accepts {@code Items.WATER_BUCKET}. INFX water buckets must
     * also capture fish/axolotl/tadpole while preserving the bucket material.
     * {@link PlayerInteractEvent.EntityInteract} fires before {@code Entity#interact}, which is
     * where the vanilla bucket pickups normally run, so canceling there keeps the same result.
     */
    @SubscribeEvent
    public static void captureMobWithInfxWaterBucket(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand());
        if (!(held.getItem() instanceof InfxBucketItem bucket)
                || bucket.contents() != InfxBucketItem.Contents.WATER) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity pickupEntity)
                || !(pickupEntity instanceof Bucketable bucketable)
                || !pickupEntity.isAlive()) {
            return;
        }
        MobBucketKind kind = MobBucketKind.of(pickupEntity.getType());
        if (kind == null) {
            return;
        }
        InfxMaterial material = bucket.material();
        pickupEntity.playSound(bucketable.getPickupSound(), 1.0F, 1.0F);
        ItemStack filled = BucketHelper.mobBucket(material, kind);
        bucketable.saveToBucketTag(filled);
        ItemStack result = ItemUtils.createFilledResult(held, player, filled, false);
        player.setItemInHand(event.getHand(), result);
        Level level = pickupEntity.level();
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, filled);
        }
        if (pickupEntity instanceof Leashable leashable) {
            leashable.dropLeash();
        }
        pickupEntity.discard();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
