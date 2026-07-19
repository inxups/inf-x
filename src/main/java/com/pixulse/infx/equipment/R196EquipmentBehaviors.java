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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffects;
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
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import com.pixulse.infx.entity.R196Slime;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import java.util.List;
import com.pixulse.infx.block.R196FurnaceBlock;
import com.pixulse.infx.furnace.FurnaceHeatPolicy;
import com.pixulse.infx.tag.ModTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;

public final class R196EquipmentBehaviors {
    private static final String RECOVERY_CHECKED = "infxArrowRecoveryChecked";

    private R196EquipmentBehaviors() {}

    public static void register(IEventBus ignored) {
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::applySilverBonus);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::onProjectileImpact);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::applyArmorDecay);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::applyFixedPointArmor);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::applyFixedResistance);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::applyElementalCorrosion);
        NeoForge.EVENT_BUS.addListener(R196EquipmentBehaviors::addQualityTooltip);
    }

    static void applySilverBonus(LivingIncomingDamageEvent event) {
        if (!BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(event.getEntity().getType()).is(EntityTypeTags.UNDEAD)
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
        int enchantment = arrow.getPersistentData().getInt("infx_recovery_enchantment").orElse(0);
        boolean recovered = arrow.getRandom().nextFloat()
                < Math.min(1.0F, recoveryChance(arrowItem.key().material()) + enchantment * 0.1F);
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

    static void applyFixedPointArmor(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)
                || event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        float armorPoints = (float) player.getAttributeValue(Attributes.ARMOR);
        if (armorPoints <= 0.0F) {
            return;
        }
        boolean fire = event.getSource().is(DamageTypeTags.IS_FIRE);
        event.getContainer().addModifier(
                DamageContainer.Reduction.ARMOR,
                (container, vanillaReduction) -> r196ArmorReduction(
                        container.getNewDamage(), armorPoints, fire));
    }

    static float r196ArmorReduction(float incomingDamage, float armorPoints, boolean fire) {
        return fire ? 0.0F : fixedArmorReduction(incomingDamage, armorPoints);
    }

    public static float fixedArmorReduction(float incomingDamage, float armorPoints) {
        if (incomingDamage <= 1.0F || armorPoints <= 0.0F) {
            return 0.0F;
        }
        return Math.min(armorPoints, incomingDamage - 1.0F);
    }

    /** Replaces modern percentage resistance with R196's five fixed protection points per level. */
    static void applyFixedResistance(LivingIncomingDamageEvent event) {
        var resistance = event.getEntity().getEffect(MobEffects.RESISTANCE);
        if (resistance == null
                || event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                || event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            return;
        }
        float protection = (resistance.getAmplifier() + 1) * 5.0F;
        event.getContainer().addModifier(
                DamageContainer.Reduction.MOB_EFFECTS,
                (container, vanillaReduction) -> fixedArmorReduction(container.getNewDamage(), protection));
    }

    static void applyElementalCorrosion(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean lava = event.getSource().is(DamageTypes.LAVA);
        boolean fire = event.getSource().is(DamageTypeTags.IS_FIRE);
        boolean acid = event.getSource().getEntity() instanceof R196Slime slime
                && (slime.variant() == R196Slime.Variant.OOZE
                        || slime.variant() == R196Slime.Variant.PUDDING);
        if (!lava && !fire && !acid) {
            return;
        }

        for (EquipmentSlot slot : List.of(
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            damageForCorrosion(player, player.getItemBySlot(slot), slot, event.getAmount(), fire, lava, acid);
        }
        if (lava || acid) {
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (stack.isDamageableItem() && player.getRandom().nextInt(4) == 0) {
                    int damage = corrosionDamage(stack, event.getAmount(), fire, lava, acid);
                    if (damage > 0) {
                        stack.hurtAndBreak(damage, player.level(), player, ignored -> {});
                    }
                }
            }
        }
    }

    private static void damageForCorrosion(
            ServerPlayer player,
            ItemStack stack,
            EquipmentSlot slot,
            float damage,
            boolean fire,
            boolean lava,
            boolean acid) {
        int wear = corrosionDamage(stack, damage, fire, lava, acid);
        if (wear > 0) {
            stack.hurtAndBreak(wear, player, slot);
        }
    }

    public static int corrosionDamage(
            ItemStack stack, float incomingDamage, boolean fire, boolean lava, boolean acid) {
        R196Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
        if (entry == null || !stack.isDamageableItem()) {
            return 0;
        }
        return corrosionDamage(
                entry.key().material(), stack.getMaxDamage(), incomingDamage, fire, lava, acid);
    }

    static int corrosionDamage(
            R196Material material,
            int maxDamage,
            float incomingDamage,
            boolean fire,
            boolean lava,
            boolean acid) {
        if (material == R196Material.ADAMANTIUM) {
            return 0;
        }
        if (material == R196Material.LEATHER && (fire || lava || acid)) {
            return maxDamage;
        }
        if (lava) {
            return Math.max(1, Math.round(incomingDamage * 10.0F));
        }
        if (acid) {
            return Math.max(1, Math.round(incomingDamage * 4.0F));
        }
        return 0;
    }

    static void addQualityTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        R196Quality quality = event.getItemStack().get(ModDataComponents.QUALITY.get());
        if (quality != null) {
            event.getToolTip().add(1, Component.translatable("quality.infx." + quality.getSerializedName())
                    .withStyle(quality.color()));
        }
        R196Catalog.EquipmentEntry entry = ModItems.catalog().equipment(stack);
        if (entry != null) {
            R196EquipmentKey key = entry.key();
            event.getToolTip().add(Component.translatable(
                    "tooltip.infx.material", Component.translatable("material.infx." + key.material().path())));
            if (key.type().baseDamage() > 0.0F) {
                event.getToolTip().add(Component.translatable("tooltip.infx.damage", key.meleeDamage()));
                event.getToolTip().add(Component.translatable(
                        "tooltip.infx.reach", 1.5F + key.type().reachBonus()));
            }
            if (key.armorProtection() > 0.0F) {
                event.getToolTip().add(Component.translatable("tooltip.infx.protection", key.armorProtection()));
            }
            if (stack.isDamageableItem()) {
                event.getToolTip().add(Component.translatable(
                        "tooltip.infx.repair", Component.translatable("material.infx." + key.material().path())));
            }
        }

        int fuelHeat = tooltipFuelHeat(stack);
        if (fuelHeat > 0) event.getToolTip().add(Component.translatable("tooltip.infx.fuel_heat", fuelHeat));
        int recipeHeat = FurnaceHeatPolicy.requiredHeat(stack);
        if (recipeHeat > 1) event.getToolTip().add(Component.translatable("tooltip.infx.recipe_heat", recipeHeat));
        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof R196FurnaceBlock furnace) {
            event.getToolTip().add(Component.translatable("tooltip.infx.furnace_heat", furnace.maximumHeat()));
        }
    }

    private static int tooltipFuelHeat(ItemStack stack) {
        if (stack.is(Items.BLAZE_ROD)) return FurnaceHeatPolicy.HEAT_BLAZE;
        if (stack.is(Items.LAVA_BUCKET)) return FurnaceHeatPolicy.HEAT_LAVA;
        if (stack.is(ModTags.Items.FURNACE_FUELS_HEAT_2)) return FurnaceHeatPolicy.HEAT_COAL;
        if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.PLANKS)) return FurnaceHeatPolicy.HEAT_WOOD;
        return 0;
    }
}
