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
        // MITE only arms plain zombies this way: the zombie variants spawn bare (the revenant
        // brings its fixed kit) and pig zombies carry their golden weapon instead.
        if (event.getEntity() instanceof Zombie zombie
                && !(zombie instanceof com.pixulse.infx.entity.R196ZombifiedPiglin)
                && !(zombie instanceof com.pixulse.infx.entity.R196Zombie r196
                        && r196.variant() != com.pixulse.infx.entity.R196Zombie.Variant.ZOMBIE)) {
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
                || !event.isRecentlyHit()) {
            return;
        }
        // MITE: skeletons shed nextInt(2) rusted arrows; longdead shed an ancient-metal arrow
        // one time in six.
        R196Material material = R196Material.RUSTED_IRON;
        int count;
        if (skeleton instanceof com.pixulse.infx.entity.R196Skeleton r196
                && r196.variant() == com.pixulse.infx.entity.R196Skeleton.Variant.LONGDEAD) {
            material = R196Material.ANCIENT_METAL;
            count = skeleton.getRandom().nextInt(6) == 0 ? 1 : 0;
        } else {
            count = skeleton.getRandom().nextInt(2);
        }
        if (count <= 0) {
            return;
        }
        ItemStack arrow = ModItems.catalog().equipment(material, R196EquipmentType.ARROW).holder().toStack();
        arrow.setCount(count);
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(), arrow));
    }

    private static ItemStack equipment(R196EquipmentType type) {
        return ModItems.catalog().equipment(R196Material.RUSTED_IRON, type).holder().toStack();
    }
}
