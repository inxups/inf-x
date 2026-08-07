package com.pixulse.infx.event;

import com.pixulse.infx.data.harvest.HarvestPolicy;
import com.pixulse.infx.data.harvest.HarvestRequirements;
import com.pixulse.infx.data.harvest.HarvestSpeedRules;
import com.pixulse.infx.data.harvest.HarvestTier;
import com.pixulse.infx.data.harvest.InfxMiningRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.Catalog;
import com.pixulse.infx.item.MiningFamily;

import java.util.Optional;

import com.pixulse.infx.block.MetalAnvilBlock;
import com.pixulse.infx.block.SafeBlock;
import com.pixulse.infx.block.entity.SafeBlockEntity;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXBlockTags;
import com.pixulse.infx.registry.tag.InfXItemTags;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class HarvestEvents {
    private HarvestEvents() {}

    /** Rejects server mining starts that cannot make any block-destroy progress. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void rejectServerMiningWithoutProgress(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()
                || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
            return;
        }
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        if (!hasDestroyProgress(player, event.getLevel().getBlockState(pos), pos)) {
            event.setCanceled(true);
        }
    }

    /** Metal anvils are portable MITE blocks: hand recovery must produce the anvil item. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void allowMetalAnvilHandHarvest(PlayerEvent.HarvestCheck event) {
        if (event.getTargetBlock().getBlock() instanceof MetalAnvilBlock) {
            event.setCanHarvest(true);
        }
    }

    /**
     * MITE furnaces drop their block when broken by hand, so every furnace
     * (infx and vanilla) must pass the harvest check regardless of tool.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void allowFurnaceHandHarvest(PlayerEvent.HarvestCheck event) {
        if (event.getTargetBlock().getBlock() instanceof AbstractFurnaceBlock) {
            event.setCanHarvest(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyBreakSpeedRules(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        BlockPos pos = event.getPosition().orElse(null);
        float hardness = pos == null ? -1.0F : state.getDestroySpeed(player.level(), pos);
        boolean portable = isPortable(player, state, pos);
        float speed = HarvestSpeedRules.adjustedBreakSpeed(
                player,
                event.getNewSpeed(),
                hardness,
                portable);
        // SafeEvents grants owners harvest capability so vanilla already uses its /30 divisor.
        if (portable && !(state.getBlock() instanceof SafeBlock)) {
            speed = HarvestSpeedRules.compensatePortableHandSpeed(
                    speed,
                    state.requiresCorrectToolForDrops(),
                    hasHarvestCapability(player, state, pos));
        }
        event.setNewSpeed(speed);
    }

    public static boolean hasDestroyProgress(Player player, BlockState state, BlockPos pos) {
        return !state.isAir()
                && isAllowed(player, state, pos)
                && (player.getAbilities().instabuild
                        || state.getDestroyProgress(player, player.level(), pos) > 0.0F);
    }

    private static boolean isAllowed(Player player, BlockState state, @Nullable BlockPos pos) {
        ItemStack tool = player.getMainHandItem();
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(tool);
        if (entry != null
                && entry.key().type().miningFamily() == MiningFamily.SWORD
                && !InfxMiningRules.isEffective(entry.key(), state)) {
            // MITE swords cut plants and webs but cannot mine ordinary blocks.
            return false;
        }
        return HarvestPolicy.allows(
                player.getAbilities().instabuild,
                isPortable(player, state, pos),
                tool.isCorrectToolForDrops(state),
                highestToolTier(tool).map(HarvestTier::level).orElse(0),
                HarvestRequirements.requiredLevel(state));
    }

    private static boolean hasHarvestCapability(Player player, BlockState state, @Nullable BlockPos pos) {
        return pos == null
                ? player.hasCorrectToolForDrops(state)
                : player.hasCorrectToolForDrops(state, player.level(), pos);
    }

    private static Optional<HarvestTier> highestToolTier(ItemStack tool) {
        for (int index = HarvestTier.values().length - 1; index >= 0; index--) {
            HarvestTier tier = HarvestTier.values()[index];
            if (tool.is(InfXItemTags.toolTier(tier))) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

    private static boolean isPortable(Player player, BlockState state, @Nullable BlockPos pos) {
        if (!state.is(InfXBlockTags.PORTABLE_HAND_HARVEST)) {
            return false;
        }
        if (!(state.getBlock() instanceof SafeBlock)) {
            return true;
        }
        return pos != null
                && player.level().getBlockEntity(pos) instanceof SafeBlockEntity safe
                && safe.isPortableTo(player);
    }

}
