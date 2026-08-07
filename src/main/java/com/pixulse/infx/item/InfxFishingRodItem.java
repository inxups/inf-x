package com.pixulse.infx.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class InfxFishingRodItem extends FishingRodItem {
    private final EquipmentKey key;

    public InfxFishingRodItem(EquipmentKey key, Properties properties) {
        super(properties);
        this.key = key;
    }

    public EquipmentKey key() {
        return key;
    }

    /**
     * InfX only lets a player cast from a boat or horse, or on the ground while not standing in
     * liquid; reeling in an existing hook keeps the vanilla behavior.
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.fishing != null) {
            return super.use(level, player, hand);
        }
        if (!canCast(player)) {
            return InteractionResult.FAIL;
        }
        return super.use(level, player, hand);
    }

    private static boolean canCast(Player player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof Boat || vehicle instanceof AbstractHorse) {
            return true;
        }
        // InfX forbids casting while riding other mobs, airborne, or with the head under liquid.
        return vehicle == null
                && player.onGround()
                && !player.level().getFluidState(player.blockPosition().above()).isSource();
    }
}
