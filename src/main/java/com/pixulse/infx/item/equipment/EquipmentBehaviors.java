package com.pixulse.infx.item.equipment;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.entity.GelatinousCubeEvents;
import com.pixulse.infx.entity.InfxSlime;
import com.pixulse.infx.item.enchantment.Enchantments;
import com.pixulse.infx.item.enchantment.EnchantmentRules;
import com.pixulse.infx.item.*;
import com.pixulse.infx.item.material.InfxMaterial;
import com.pixulse.infx.item.material.Quality;
import com.pixulse.infx.registry.InfXDataComponents;
import com.pixulse.infx.registry.InfXEnchantments;
import com.pixulse.infx.registry.InfXItems;
import com.pixulse.infx.registry.tag.InfXItemTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import java.util.List;
import com.pixulse.infx.block.InfxFurnaceBlock;
import com.pixulse.infx.data.furnace.FurnaceHeatPolicy;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;

@EventBusSubscriber(modid = InfiniteX.MOD_ID)
public final class EquipmentBehaviors {
    private static final String RECOVERY_CHECKED = "infxArrowRecoveryChecked";

    private EquipmentBehaviors() {}

   @SubscribeEvent
    public static void applySilverBonus(LivingIncomingDamageEvent event) {
        if (!BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(event.getEntity().getType()).is(EntityTypeTags.UNDEAD)
                || !hasSilverAspect(event)) {
            return;
        }
        event.setAmount(event.getAmount() * 1.25F);
    }

    private static boolean hasSilverAspect(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getPickupItemStackOrigin().getItem() instanceof InfxArrowItem arrowItem) {
            return arrowItem.key().material() == InfxMaterial.SILVER;
        }
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
            Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(attacker.getMainHandItem());
            return entry != null
                    && entry.key().material() == InfxMaterial.SILVER
                    && entry.key().type() != EquipmentType.ARROW;
        }
        return false;
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            if (resolveArrowRecovery(arrow, event.getRayTraceResult())) {
                event.setCanceled(true);
            }
        }
    }

    /** Returns true when an acid slime consumed the arrow and the impact must be canceled. */
    public static boolean resolveArrowRecovery(AbstractArrow arrow, HitResult hit) {
        if (hit.getType() != HitResult.Type.ENTITY
                || !(arrow.level() instanceof net.minecraft.server.level.ServerLevel level)
                || arrow.getPersistentData().getBooleanOr(RECOVERY_CHECKED, false)) {
            return false;
        }
        ItemStack origin = arrow.getPickupItemStackOrigin();
        if (hit instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof InfxSlime slime
                && slime.variant().corrosionType() == CorrosionType.ACID
                && CorrosionRules.isHarmedBy(origin, CorrosionType.ACID)) {
            arrow.getPersistentData().putBoolean(RECOVERY_CHECKED, true);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            GelatinousCubeEvents.playCorrosionFizz(level, arrow, arrow.getRandom());
            arrow.discard();
            return true;
        }
        if (arrow.pickup == AbstractArrow.Pickup.CREATIVE_ONLY) {
            return false;
        }
        if (!(origin.getItem() instanceof InfxArrowItem arrowItem)) {
            return false;
        }
        arrow.getPersistentData().putBoolean(RECOVERY_CHECKED, true);
        int enchantment = arrow.getPersistentData().getInt("infx_recovery_enchantment").orElse(0);
        boolean recovered = arrow.getRandom().nextFloat()
                < recoveryChance(arrowItem.key().material(), enchantment);
        if (recovered) {
            // Drop the material arrow once; the projectile itself must not remain pickable.
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            arrow.spawnAtLocation(level, arrow.getPickupItemStackOrigin().copyWithCount(1));
        }
        return false;
    }

    public static float recoveryChance(InfxMaterial material) {
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

    public static float recoveryChance(InfxMaterial material, int recoveryEnchantmentLevel) {
        return EnchantmentRules.arrowRecoveryChance(
                recoveryChance(material), recoveryEnchantmentLevel);
    }

   @SubscribeEvent
    public static void applyArmorDecay(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
        if (entry == null
                || (entry.key().type().armorForm() != EquipmentType.ArmorForm.PLATE
                        && entry.key().type().armorForm() != EquipmentType.ArmorForm.CHAIN)
                || !stack.isDamageableItem()) {
            return;
        }
        EquipmentKey key = entry.key();
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

   @SubscribeEvent
    public static void applyFixedPointArmor(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        // MITE: fire and armor-bypassing damage skip mundane armor, but the typed protection
        // enchantments (fire/blast/projectile protection, feather falling) still contribute.
        boolean bypassesMundaneArmor = event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)
                || event.getSource().is(DamageTypeTags.IS_FIRE);
        float typed = typedProtectionPoints(player, event);
        float base = bypassesMundaneArmor
                ? 0.0F
                : (float) player.getAttributeValue(Attributes.ARMOR)
                        + protectionBonus(player)
                        - penetrationPoints(event);
        float armorPoints = Math.max(0.0F, base) + typed;
        if (armorPoints <= 0.0F && !bypassesMundaneArmor) {
            return;
        }
        event.getContainer().addModifier(
                DamageContainer.Reduction.ARMOR,
                (container, vanillaReduction) -> fixedArmorReduction(
                        container.getNewDamage(), armorPoints));
    }

    /** Sums MITE's typed protection points from the four armor pieces for a matching source. */
    private static float typedProtectionPoints(Player player, LivingIncomingDamageEvent event) {
        boolean fire = event.getSource().is(DamageTypeTags.IS_FIRE);
        boolean fall = event.getSource().is(DamageTypeTags.IS_FALL);
        boolean explosion = event.getSource().is(DamageTypeTags.IS_EXPLOSION);
        boolean projectile = event.getSource().is(DamageTypeTags.IS_PROJECTILE);
        if (!fire && !fall && !explosion && !projectile) {
            return 0.0F;
        }
        float total = 0.0F;
        for (EquipmentSlot slot : List.of(
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = player.getItemBySlot(slot);
            Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
            if (entry == null || entry.key().type().armorForm() == EquipmentType.ArmorForm.NONE) {
                continue;
            }
            float durabilityFactor = armorDurabilityFactor(stack.getDamageValue(), stack.getMaxDamage());
            if (fall) {
                total += EnchantmentRules.featherFallingPoints(
                        Enchantments.level(
                                player.level(), stack, InfXEnchantments.VANILLA_FEATHER_FALLING),
                        durabilityFactor);
                continue;
            }
            float pieceProtection = entry.key().armorProtection() * durabilityFactor;
            if (fire) {
                total += EnchantmentRules.typedProtectionPoints(pieceProtection,
                        Enchantments.level(
                                player.level(), stack, InfXEnchantments.VANILLA_FIRE_PROTECTION));
            }
            if (explosion) {
                total += EnchantmentRules.typedProtectionPoints(pieceProtection,
                        Enchantments.level(
                                player.level(), stack, InfXEnchantments.VANILLA_BLAST_PROTECTION));
            }
            if (projectile) {
                total += EnchantmentRules.typedProtectionPoints(pieceProtection,
                        Enchantments.level(
                                player.level(), stack, InfXEnchantments.VANILLA_PROJECTILE_PROTECTION));
            }
        }
        return total;
    }

    private static float protectionBonus(Player player) {
        float bonus = 0.0F;
        for (EquipmentSlot slot : List.of(
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = player.getItemBySlot(slot);
            Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
            if (entry == null
                    || (entry.key().type().armorForm() != EquipmentType.ArmorForm.PLATE
                            && entry.key().type().armorForm() != EquipmentType.ArmorForm.CHAIN)) {
                continue;
            }
            float currentProtection = entry.key().armorProtection()
                    * armorDurabilityFactor(stack.getDamageValue(), stack.getMaxDamage());
            int level = Enchantments.level(player.level(), stack, InfXEnchantments.PROTECTION);
            bonus += EnchantmentRules.protectionBonus(currentProtection, level);
        }
        return bonus;
    }

    private static float penetrationPoints(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)
                || event.getSource().getDirectEntity() != attacker) {
            return 0.0F;
        }
        ItemStack weapon = attacker.getMainHandItem();
        int penetration = Enchantments.level(attacker.level(), weapon, InfXEnchantments.PENETRATION);
        int cleaving = Enchantments.level(attacker.level(), weapon, InfXEnchantments.CLEAVING);
        return EnchantmentRules.penetrationPoints(Math.max(penetration, cleaving));
    }

    public static float fixedArmorReduction(float incomingDamage, float armorPoints) {
        if (incomingDamage <= 1.0F || armorPoints <= 0.0F) {
            return 0.0F;
        }
        return Math.min(armorPoints, incomingDamage - 1.0F);
    }

    /** Replaces modern percentage resistance with INFX's five fixed protection points per level. */
   @SubscribeEvent
    public static void applyFixedResistance(LivingIncomingDamageEvent event) {
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

   @SubscribeEvent
    public static void applyElementalCorrosion(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean lava = event.getSource().is(DamageTypes.LAVA);
        boolean fire = event.getSource().is(DamageTypeTags.IS_FIRE);
        if (!lava && !fire) {
            return;
        }

        for (EquipmentSlot slot : List.of(
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            damageForCorrosion(player, player.getItemBySlot(slot), slot, event.getAmount(), fire, lava);
        }
        if (lava) {
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (stack.isDamageableItem() && player.getRandom().nextInt(4) == 0) {
                    int damage = corrosionDamage(stack, event.getAmount(), fire, lava);
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
            boolean lava) {
        int wear = corrosionDamage(stack, damage, fire, lava);
        if (wear > 0) {
            stack.hurtAndBreak(wear, player, slot);
        }
    }

    public static int corrosionDamage(ItemStack stack, float incomingDamage, boolean fire, boolean lava) {
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
        if (entry == null || !stack.isDamageableItem()) {
            return 0;
        }
        return corrosionDamage(entry.key().material(), stack.getMaxDamage(), incomingDamage, fire, lava);
    }

    static int corrosionDamage(
            InfxMaterial material,
            int maxDamage,
            float incomingDamage,
            boolean fire,
            boolean lava) {
        if (material == InfxMaterial.ADAMANTIUM) {
            return 0;
        }
        if (material == InfxMaterial.LEATHER && (fire || lava)) {
            return maxDamage;
        }
        if (lava) {
            return Math.max(1, Math.round(incomingDamage * 10.0F));
        }
        return 0;
    }

   @SubscribeEvent
    public static void addQualityTooltip(ItemTooltipEvent event) {
        if (!shouldAddExtendedTooltips(InfiniteXTestMode.isEnabled())) return;

        ItemStack stack = event.getItemStack();
        Quality quality = event.getItemStack().get(InfXDataComponents.QUALITY.get());
        if (quality != null) {
            event.getToolTip().add(1, Component.translatable("quality.infx." + quality.getSerializedName())
                    .withStyle(quality.color()));
        }
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
        if (entry != null) {
            EquipmentKey key = entry.key();
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
                && blockItem.getBlock() instanceof InfxFurnaceBlock furnace) {
            event.getToolTip().add(Component.translatable("tooltip.infx.furnace_heat", furnace.maximumHeat()));
        }
    }

    static boolean shouldAddExtendedTooltips(boolean testMode) {
        return testMode;
    }

    private static int tooltipFuelHeat(ItemStack stack) {
        if (stack.is(Items.BLAZE_ROD)) return FurnaceHeatPolicy.HEAT_BLAZE;
        if (stack.is(Items.LAVA_BUCKET)
                || stack.getItem() instanceof InfxBucketItem bucket
                        && bucket.contents() == InfxBucketItem.Contents.LAVA) {
            return FurnaceHeatPolicy.HEAT_LAVA;
        }
        if (stack.is(InfXItemTags.FURNACE_FUELS_HEAT_2)) return FurnaceHeatPolicy.HEAT_COAL;
        if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.PLANKS)) return FurnaceHeatPolicy.HEAT_WOOD;
        return 0;
    }

   @SubscribeEvent
    public static void addBucketTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof InfxBucketItem bucket)) {
            return;
        }
        var player = event.getEntity();
        if (player != null
                && player.totalExperience >= InfxBucketItem.SOURCE_EXPERIENCE_COST
                && (bucket.contents() == InfxBucketItem.Contents.WATER
                        || bucket.contents() == InfxBucketItem.Contents.LAVA)) {
            event.getToolTip()
                    .add(net.minecraft.network.chat.Component.translatable("tooltip.infx.place_bucket_as_source")
                            .withStyle(
                                    bucket.contents() == InfxBucketItem.Contents.WATER
                                            ? net.minecraft.ChatFormatting.BLUE
                                            : net.minecraft.ChatFormatting.RED));
        }
        if (bucket.contents() == InfxBucketItem.Contents.LAVA) {
            int chance = Math.round(bucket.lavaMeltChance() * 100.0F);
            if (chance > 0) {
                event.getToolTip().add(net.minecraft.network.chat.Component.empty());
                event.getToolTip()
                        .add(net.minecraft.network.chat.Component.translatable("tooltip.infx.when_bucket_filled")
                                .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
                event.getToolTip()
                        .add(net.minecraft.network.chat.Component.translatable(
                                        "tooltip.infx.chance_of_bucket_melting", chance)
                                .withStyle(net.minecraft.ChatFormatting.RED));
            }
        }
    }
}
