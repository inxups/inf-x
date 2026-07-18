package com.pixulse.infx.equipment;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.item.R196ArrowItem;
import com.pixulse.infx.item.R196Catalog;
import com.pixulse.infx.item.R196EquipmentKey;
import com.pixulse.infx.item.R196EquipmentType;
import com.pixulse.infx.material.R196Material;
import com.pixulse.infx.material.R196Quality;
import com.pixulse.infx.registry.ModDataComponents;
import com.pixulse.infx.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class R196EquipmentBehaviors {
    private static final String RECOVERY_CHECKED = "infxArrowRecoveryChecked";

    private R196EquipmentBehaviors() {}

    public static void register(IEventBus ignored) {
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::applySilverBonus);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::onProjectileImpact);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::applyArmorDecay);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::addQualityTooltip);
    }

    static void applySilverBonus(LivingIncomingDamageEvent event) {
        if (!event.getEntity().getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)
                || !hasSilverAspect(event)) {
            return;
        }
        event.setAmount(event.getAmount() * 1.25F);
    }

    private static boolean hasSilverAspect(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getPickupItemStackOrigin().getItem() instanceof R196ArrowItem arrowItem) {
            return arrowItem.key().material() == R196Material.SILVER;
        }
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
            R196Catalog.EquipmentEntry entry = ModItems.catalog().equipment(attacker.getMainHandItem());
            return entry != null
                    && entry.key().material() == R196Material.SILVER
                    && entry.key().type() != R196EquipmentType.ARROW;
        }
        return false;
    }

    private static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            resolveArrowRecovery(arrow, event.getRayTraceResult());
        }
    }

    public static void resolveArrowRecovery(AbstractArrow arrow, HitResult hit) {
        if (!(arrow.level() instanceof net.minecraft.server.level.ServerLevel level)
                || !(arrow.getPickupItemStackOrigin().getItem() instanceof R196ArrowItem arrowItem)
                || arrow.pickup == AbstractArrow.Pickup.CREATIVE_ONLY
                || arrow.getPersistentData().getBooleanOr(RECOVERY_CHECKED, false)) {
            return;
        }
        arrow.getPersistentData().putBoolean(RECOVERY_CHECKED, true);
        boolean recovered = arrow.getRandom().nextFloat() < recoveryChance(arrowItem.key().material());
        if (hit.getType() == HitResult.Type.BLOCK) {
            arrow.pickup = recovered ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
        } else if (hit.getType() == HitResult.Type.ENTITY && recovered) {
            arrow.spawnAtLocation(level, arrow.getPickupItemStackOrigin().copyWithCount(1));
        }
    }

    public static float recoveryChance(R196Material material) {
        return switch (material) {
            case FLINT -> .30F;
            case OBSIDIAN -> .40F;
            case COPPER, SILVER -> .60F;
            case RUSTED_IRON, GOLD -> .50F;
            case IRON -> .70F;
            case ANCIENT_METAL, MITHRIL -> .80F;
            case ADAMANTIUM -> .90F;
            default -> 0.0F;
        };
    }

    static void applyArmorDecay(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        R196Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
        if (entry == null
                || (entry.key().type().armorForm() != R196EquipmentType.ArmorForm.PLATE
                        && entry.key().type().armorForm() != R196EquipmentType.ArmorForm.CHAIN)
                || !stack.isDamageableItem()) {
            return;
        }
        R196EquipmentKey key = entry.key();
        float factor = armorDurabilityFactor(stack.getDamageValue(), stack.getMaxDamage());
        var slot = key.type().armorType().orElseThrow().getSlot();
        event.replaceModifier(
                Attributes.ARMOR,
                new AttributeModifier(
                        InfiniteX.id("armor." + key.type().path()),
                        key.armorProtection() * factor,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(slot));
    }

    public static float armorDurabilityFactor(int damage, int maxDamage) {
        if (maxDamage <= 0) {
            return 1.0F;
        }
        float remaining = Math.clamp((maxDamage - damage) / (float) maxDamage, 0.0F, 1.0F);
        return Math.min(1.0F, remaining * 2.0F);
    }

    static void addQualityTooltip(ItemTooltipEvent event) {
        R196Quality quality = event.getItemStack().get(ModDataComponents.QUALITY.get());
        if (quality != null) {
            event.getToolTip().add(1, Component.translatable("quality.infx." + quality.getSerializedName())
                    .withStyle(quality.color()));
        }
    }
}
