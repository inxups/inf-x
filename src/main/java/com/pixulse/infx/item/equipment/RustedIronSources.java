package com.pixulse.infx.item.equipment;

import com.pixulse.infx.entity.MiteSkeleton;
import com.pixulse.infx.entity.MiteZombie;
import com.pixulse.infx.entity.MiteZombifiedPiglin;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.MiteMaterial;
import com.pixulse.infx.registry.InfinityXItems;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Restores monster-held rusted iron and rusted arrows instead of inventing ambient iron corrosion. */
public final class RustedIronSources {
    private static final List<EquipmentType> ZOMBIE_WEAPONS = List.of(
            EquipmentType.SHOVEL,
            EquipmentType.HATCHET,
            EquipmentType.SHEARS,
            EquipmentType.SCYTHE,
            EquipmentType.HOE,
            EquipmentType.MATTOCK,
            EquipmentType.PICKAXE,
            EquipmentType.SWORD,
            EquipmentType.DAGGER);
    private static final List<EquipmentType> PLATE = EquipmentType.platePieces();

    private RustedIronSources() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(RustedIronSources::onJoinLevel);
        gameBus.addListener(RustedIronSources::onLivingDrops);
    }

    private static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel)) {
            return;
        }
        // MITE only arms plain zombies this way: the zombie variants spawn bare (the revenant
        // brings its fixed kit) and pig zombies carry their golden weapon instead.
        if (event.getEntity() instanceof Zombie zombie
                && !(zombie instanceof MiteZombifiedPiglin)
                && !(zombie instanceof MiteZombie r196
                        && r196.variant() != MiteZombie.Variant.ZOMBIE)) {
            equipZombie(zombie);
        }
    }

    private static void equipZombie(Zombie zombie) {
        if (zombie.getMainHandItem().isEmpty() && zombie.getRandom().nextFloat() < 0.05F) {
            EquipmentType type = ZOMBIE_WEAPONS.get(zombie.getRandom().nextInt(ZOMBIE_WEAPONS.size()));
            zombie.setItemSlot(EquipmentSlot.MAINHAND, equipment(type));
            zombie.setDropChance(EquipmentSlot.MAINHAND, 0.085F);
        }
        for (EquipmentType type : PLATE) {
            EquipmentSlot slot = type.armorType().orElseThrow().getSlot();
            if (zombie.getItemBySlot(slot).isEmpty() && zombie.getRandom().nextFloat() < 0.025F) {
                zombie.setItemSlot(slot, equipment(type));
                zombie.setDropChance(slot, 0.085F);
            }
        }
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof AbstractSkeleton skeleton)
                || skeleton instanceof WitherSkeleton
                || !event.isRecentlyHit()) {
            return;
        }
        // MITE: skeletons shed nextInt(2) rusted arrows; longdead shed an ancient-metal arrow
        // one time in six.
        MiteMaterial material = MiteMaterial.RUSTED_IRON;
        int count;
        if (skeleton instanceof MiteSkeleton r196
                && r196.variant() == MiteSkeleton.Variant.LONGDEAD) {
            material = MiteMaterial.ANCIENT_METAL;
            count = skeleton.getRandom().nextInt(6) == 0 ? 1 : 0;
        } else {
            count = skeleton.getRandom().nextInt(2);
        }
        if (count <= 0) {
            return;
        }
        ItemStack arrow = InfinityXItems.catalog().equipment(material, EquipmentType.ARROW).holder().toStack();
        arrow.setCount(count);
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(), arrow));
    }

    private static ItemStack equipment(EquipmentType type) {
        return InfinityXItems.catalog().equipment(MiteMaterial.RUSTED_IRON, type).holder().toStack();
    }
}
