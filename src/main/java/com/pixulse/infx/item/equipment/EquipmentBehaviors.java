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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import java.util.List;
import net.minecraft.world.phys.Vec3;
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

    /** InfX war hammers and cudgels add two damage against the skeleton family. */
    @SubscribeEvent
    public static void applySkeletonBane(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)
                || event.getSource().getDirectEntity() != attacker) {
            return;
        }
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(attacker.getMainHandItem());
        if (entry == null) {
            return;
        }
        EquipmentType type = entry.key().type();
        if (type != EquipmentType.WAR_HAMMER
                && type != EquipmentType.CUDGEL
                && type != EquipmentType.CLUB) {
            return;
        }
        if (!BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(event.getEntity().getType())
                .is(EntityTypeTags.SKELETONS)) {
            return;
        }
        event.setAmount(event.getAmount() + 2.0F);
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
        if (maxDamage > 1 && damage >= maxDamage - 1) {
            return 0.0F;
        }
        float remaining = Math.clamp((maxDamage - damage) / (float) maxDamage, 0.0F, 1.0F);
        return Math.min(1.0F, remaining * 2.0F);
    }

    @SubscribeEvent
    public static void applyFixedPointArmor(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        // InfX: fire and armor-bypassing damage skip mundane armor, but the typed protection
        // enchantments (fire/blast/projectile protection, feather falling) still contribute.
        boolean bypassesMundaneArmor = bypassesMundaneArmor(event);
        float typed = typedProtectionPoints(entity, event);
        float resistanceArmor = resistanceProtectionPoints(entity);
        float resistanceProtection = resistanceProtectionPoints(entity, event);
        float base = bypassesMundaneArmor
                ? 0.0F
                : mundaneArmorPoints(entity) - resistanceArmor
                        + protectionBonus(entity)
                        - penetrationPoints(event);
        float armorPoints = Math.max(0.0F, base) + typed;
        if (entity instanceof Player && !bypassesMundaneArmor) {
            armorPoints += resistanceProtection;
        }
        if (bypassesMundaneArmor) {
            float reduction = fixedArmorReduction(event.getAmount(), armorPoints);
            if (reduction > 0.0F) {
                event.setAmount(event.getAmount() - reduction);
            }
            // Fire and BYPASSES_ARMOR damage must not receive a second vanilla armor reduction.
            event.getContainer().addModifier(
                    DamageContainer.Reduction.ARMOR,
                    (container, vanillaReduction) -> 0.0F);
            return;
        }
        if (armorPoints <= 0.0F && resistanceArmor <= 0.0F) {
            return;
        }
        float fixedArmorPoints = armorPoints;
        event.getContainer().addModifier(
                DamageContainer.Reduction.ARMOR,
                (container, vanillaReduction) -> fixedArmorReduction(
                        container.getNewDamage(), fixedArmorPoints));
    }

    /** Sums InfX's typed protection points from the four armor pieces for a matching source. */
    private static float typedProtectionPoints(LivingEntity entity, LivingIncomingDamageEvent event) {
        boolean fire = isFireDamage(event);
        boolean fall = event.getSource().is(DamageTypeTags.IS_FALL);
        boolean explosion = event.getSource().is(DamageTypeTags.IS_EXPLOSION);
        boolean projectile = event.getSource().is(DamageTypeTags.IS_PROJECTILE);
        if (!fire && !fall && !explosion && !projectile) {
            return 0.0F;
        }
        float total = 0.0F;
        for (EquipmentSlot slot : List.of(
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = entity.getItemBySlot(slot);
            Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
            if (entry == null || entry.key().type().armorForm() == EquipmentType.ArmorForm.NONE) {
                continue;
            }
            float durabilityFactor = armorDamageFactor(entity, stack);
            if (fall) {
                total += EnchantmentRules.featherFallingPoints(
                        Enchantments.level(
                                entity.level(), stack, InfXEnchantments.VANILLA_FEATHER_FALLING),
                        durabilityFactor);
                continue;
            }
            float pieceProtection = entry.key().armorProtection() * durabilityFactor;
            if (fire) {
                total += EnchantmentRules.typedProtectionPoints(pieceProtection,
                        Enchantments.level(
                                entity.level(), stack, InfXEnchantments.VANILLA_FIRE_PROTECTION));
            }
            if (explosion) {
                total += EnchantmentRules.typedProtectionPoints(pieceProtection,
                        Enchantments.level(
                                entity.level(), stack, InfXEnchantments.VANILLA_BLAST_PROTECTION));
            }
            if (projectile) {
                total += EnchantmentRules.typedProtectionPoints(pieceProtection,
                        Enchantments.level(
                                entity.level(), stack, InfXEnchantments.VANILLA_PROJECTILE_PROTECTION));
            }
        }
        return total;
    }

    private static float mundaneArmorPoints(LivingEntity entity) {
        float armor = (float) entity.getAttributeValue(Attributes.ARMOR);
        if (entity instanceof Player) {
            return armor;
        }
        // InfX gives non-player armor a fixed 0.5 damage factor. The item attribute still uses
        // the player's durability curve, so replace that contribution before the fixed armor step.
        for (EquipmentSlot slot : List.of(
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = entity.getItemBySlot(slot);
            Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
            if (entry == null
                    || (entry.key().type().armorForm() != EquipmentType.ArmorForm.PLATE
                            && entry.key().type().armorForm() != EquipmentType.ArmorForm.CHAIN)) {
                continue;
            }
            armor += entry.key().armorProtection()
                    * (0.5F - armorDurabilityFactor(stack.getDamageValue(), stack.getMaxDamage()));
        }
        return armor;
    }

    private static float armorDamageFactor(LivingEntity entity, ItemStack stack) {
        return entity instanceof Player
                ? armorDurabilityFactor(stack.getDamageValue(), stack.getMaxDamage())
                : 0.5F;
    }

    private static float protectionBonus(LivingEntity entity) {
        float bonus = 0.0F;
        for (EquipmentSlot slot : List.of(
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = entity.getItemBySlot(slot);
            Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
            if (entry == null
                    || (entry.key().type().armorForm() != EquipmentType.ArmorForm.PLATE
                            && entry.key().type().armorForm() != EquipmentType.ArmorForm.CHAIN)) {
                continue;
            }
            float currentProtection = entry.key().armorProtection()
                    * armorDamageFactor(entity, stack);
            int level = Enchantments.level(entity.level(), stack, InfXEnchantments.PROTECTION);
            bonus += EnchantmentRules.protectionBonus(currentProtection, level);
        }
        return bonus;
    }

    private static boolean bypassesMundaneArmor(LivingIncomingDamageEvent event) {
        return event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)
                || event.getSource().is(DamageTypeTags.IS_FIRE)
                || event.getSource().is(DamageTypes.LAVA);
    }

    private static boolean isFireDamage(LivingIncomingDamageEvent event) {
        return event.getSource().is(DamageTypeTags.IS_FIRE)
                && !event.getSource().is(DamageTypes.LAVA);
    }

    @SubscribeEvent
    public static void applyBlastProtection(ExplosionKnockbackEvent event) {
        if (!(event.getAffectedEntity() instanceof LivingEntity entity)) {
            return;
        }
        int level = Enchantments.maxArmorLevel(entity, InfXEnchantments.VANILLA_BLAST_PROTECTION);
        if (level <= 0) {
            return;
        }
        Vec3 knockback = event.getKnockbackVelocity();
        double magnitude = knockback.length();
        if (magnitude <= 0.0D) {
            return;
        }
        double reducedMagnitude = EnchantmentRules.blastProtectionKnockback(magnitude, level);
        event.setKnockbackVelocity(knockback.scale(reducedMagnitude / magnitude));
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

    /**
     * InfX adds Resistance to total protection as fixed points. Players use the attribute in
     * the custom armor stage; other entities and armor-bypassing damage need a mob-effect-stage
     * fallback because vanilla does not run the armor reduction in those cases.
     */
    @SubscribeEvent
    public static void applyResistanceProtection(LivingIncomingDamageEvent event) {
        float protection = resistanceProtectionPoints(event.getEntity(), event);
        if (protection <= 0.0F) {
            return;
        }
        if (!(event.getEntity() instanceof Player)
                || bypassesMundaneArmor(event)) {
            event.getContainer().addModifier(
                    DamageContainer.Reduction.MOB_EFFECTS,
                    (container, vanillaReduction) -> fixedArmorReduction(
                            container.getNewDamage(), protection));
            return;
        }
        event.getContainer().addModifier(
                DamageContainer.Reduction.MOB_EFFECTS,
                (container, vanillaReduction) -> 0.0F);
    }

    private static float resistanceProtectionPoints(LivingEntity entity) {
        var resistance = entity.getEffect(MobEffects.RESISTANCE);
        return resistance == null ? 0.0F : (resistance.getAmplifier() + 1) * 5.0F;
    }

    private static float resistanceProtectionPoints(LivingEntity entity, LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                || event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            return 0.0F;
        }
        return resistanceProtectionPoints(entity);
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
        ItemStack stack = event.getItemStack();
        InfxMaterial hookMaterial = fishingRodHookMaterial(stack);
        if (hookMaterial != null) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.infx.fishing_rod_hook",
                    Component.translatable("material.infx." + hookMaterial.path())));
        }
        Catalog.EquipmentEntry entry = InfXItems.catalog().equipment(stack);
        if (entry != null) {
            EquipmentKey key = entry.key();
            if (key.material() == InfxMaterial.SILVER) {
                event.getToolTip()
                        .add(Component.translatable("tooltip.infx.silver_undead_bonus")
                                .withStyle(net.minecraft.ChatFormatting.GRAY));
                if (key.type().armorForm() != EquipmentType.ArmorForm.NONE) {
                    event.getToolTip()
                            .add(Component.translatable("tooltip.infx.silver_armor_resistance")
                                    .withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            }
            if (key.type() == EquipmentType.WAR_HAMMER
                    || key.type() == EquipmentType.CUDGEL
                    || key.type() == EquipmentType.CLUB) {
                event.getToolTip()
                        .add(Component.translatable("tooltip.infx.skeleton_bane")
                                .withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }
        if (!shouldAddExtendedTooltips(InfiniteXTestMode.isEnabled())) return;

        Quality quality = event.getItemStack().get(InfXDataComponents.QUALITY.get());
        if (quality != null) {
            event.getToolTip().add(1, Component.translatable("quality.infx." + quality.getSerializedName())
                    .withStyle(quality.color()));
        }
        if (entry != null) {
            EquipmentKey key = entry.key();
            event.getToolTip().add(Component.translatable(
                    "tooltip.infx.material", Component.translatable("material.infx." + key.material().path())));
            if (key.type().baseDamage() > 0.0F) {
                event.getToolTip().add(Component.translatable("tooltip.infx.damage", key.meleeDamage()));
                // INFX tools use the vanilla component-less attack reach like any other melee item.
                event.getToolTip().add(Component.translatable("tooltip.infx.reach", 1.5F));
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

    static InfxMaterial fishingRodHookMaterial(ItemStack stack) {
        return fishingRodHookMaterial(stack.getItem());
    }

    static InfxMaterial fishingRodHookMaterial(Item item) {
        if (item instanceof InfxFishingRodItem fishingRod) {
            return fishingRod.key().material();
        }
        if (item instanceof InfxCarrotOnAStickItem carrotOnAStick) {
            return carrotOnAStick.hookMaterial();
        }
        if (item instanceof InfxWarpedFungusOnAStickItem warpedOnAStick) {
            return warpedOnAStick.hookMaterial();
        }
        // InfX's vanilla fishing rod and carrot-on-a-stick use an iron hook; warped fungus
        // on a stick is the modern equivalent of the carrot item.
        if (item == Items.FISHING_ROD
                || item == Items.CARROT_ON_A_STICK
                || item == Items.WARPED_FUNGUS_ON_A_STICK) {
            return InfxMaterial.IRON;
        }
        return null;
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
