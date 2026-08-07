package com.pixulse.infx.item.equipment;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;

import com.pixulse.infx.entity.InfxSkeleton;
import com.pixulse.infx.entity.InfxZombie;
import com.pixulse.infx.entity.InfxZombifiedPiglin;
import com.pixulse.infx.item.EquipmentType;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.registry.InfXItems;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Restores monster-held rusted iron and rusted arrows instead of inventing ambient iron corrosion. */
@EventBusSubscriber(modid = InfiniteX.MOD_ID)
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

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel)) {
            return;
        }
        // InfX only arms plain zombies this way: the zombie variants spawn bare (the revenant
        // brings its fixed kit) and pig zombies carry their golden weapon instead.
        if (event.getEntity() instanceof Zombie zombie
                && !(zombie instanceof InfxZombifiedPiglin)
                && !(zombie instanceof InfxZombie r196
                        && r196.variant() != InfxZombie.Variant.ZOMBIE)) {
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

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof AbstractSkeleton skeleton)) {
            return;
        }
        // Vanilla skeleton variants use the modern arrow and tipped-arrow loot tables. InfX
        // skeletons use material arrows instead, so strip only the two vanilla item types before
        // the material-arrow roll below; unrelated drops and INFX arrows remain untouched.
        event.getDrops().removeIf(RustedIronSources::isVanillaArrow);
        if (skeleton instanceof WitherSkeleton || !event.isRecentlyHit()) {
            return;
        }
        // InfX: skeletons shed nextInt(2) rusted arrows; longdead shed an ancient-metal arrow
        // one time in six.
        InfxMaterial material = InfxMaterial.RUSTED_IRON;
        int count;
        if (skeleton instanceof InfxSkeleton r196
                && (r196.variant() == InfxSkeleton.Variant.LONGDEAD
                        || r196.variant() == InfxSkeleton.Variant.LONGDEAD_GUARDIAN)) {
            material = InfxMaterial.ANCIENT_METAL;
            count = skeleton.getRandom().nextInt(6) == 0 ? 1 : 0;
        } else {
            count = skeleton.getRandom().nextInt(2);
        }
        if (count <= 0) {
            return;
        }
        ItemStack arrow = InfXItems.catalog().equipment(material, EquipmentType.ARROW).holder().toStack();
        arrow.setCount(count);
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                skeleton.level(), skeleton.getX(), skeleton.getY(), skeleton.getZ(), arrow));
    }

    private static boolean isVanillaArrow(net.minecraft.world.entity.item.ItemEntity drop) {
        return drop.getItem().is(Items.ARROW) || drop.getItem().is(Items.TIPPED_ARROW);
    }

    private static ItemStack equipment(EquipmentType type) {
        return InfXItems.catalog().equipment(InfxMaterial.RUSTED_IRON, type).holder().toStack();
    }
}
