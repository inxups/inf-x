package com.pixulse.infx.equipment;

import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.registry.ModItems;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Restores monster-held rusted iron and rusted arrows instead of inventing ambient iron corrosion. */
public final class R196RustedIronSources {
    private static final List<R196EquipmentType> ZOMBIE_WEAPONS = List.of(
            R196EquipmentType.SHOVEL,
            R196EquipmentType.HATCHET,
            R196EquipmentType.SHEARS,
            R196EquipmentType.SCYTHE,
            R196EquipmentType.HOE,
            R196EquipmentType.MATTOCK,
            R196EquipmentType.PICKAXE,
            R196EquipmentType.SWORD,
            R196EquipmentType.DAGGER);
    private static final List<R196EquipmentType> PLATE = R196EquipmentType.platePieces();

    private R196RustedIronSources() {}

    public static void register(IEventBus gameBus) {
        gameBus.addListener(R196RustedIronSources::onJoinLevel);
        gameBus.addListener(R196RustedIronSources::onLivingDrops);
    }

    private static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel)) {
            return;
        }
        if (event.getEntity() instanceof Zombie zombie) {
            equipZombie(zombie);
        }
    }

    private static void equipZombie(Zombie zombie) {
        if (zombie.getMainHandItem().isEmpty() && zombie.getRandom().nextFloat() < 0.05F) {
            R196EquipmentType type = ZOMBIE_WEAPONS.get(zombie.getRandom().nextInt(ZOMBIE_WEAPONS.size()));
            zombie.setItemSlot(EquipmentSlot.MAINHAND, equipment(type));
            zombie.setDropChance(EquipmentSlot.MAINHAND, 0.085F);
        }
        for (R196EquipmentType type : PLATE) {
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
                || !event.isRecentlyHit()
                || skeleton.getRandom().nextInt(3) != 0) {
            return;
        }
        ItemStack arrow = equipment(R196EquipmentType.ARROW);
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(), arrow));
    }

    private static ItemStack equipment(R196EquipmentType type) {
        return ModItems.catalog().equipment(R196Material.RUSTED_IRON, type).holder().toStack();
    }
}
