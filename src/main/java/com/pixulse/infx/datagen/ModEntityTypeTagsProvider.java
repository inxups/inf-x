package com.pixulse.infx.datagen;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXEntityTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.NonNull;

/** Restores the vanilla semantic tags that custom replacement entity types do not inherit from their Java class. */
final class ModEntityTypeTagsProvider extends KeyTagsProvider<EntityType<?>> {
    ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        tag(EntityTypeTags.ZOMBIES).add(
                InfXEntityTypes.SHADOW.getKey(),
                InfXEntityTypes.WIGHT.getKey(),
                InfXEntityTypes.REVENANT.getKey(),
                InfXEntityTypes.INFX_ZOMBIFIED_PIGLIN.getKey());
        tag(EntityTypeTags.SKELETONS).add(
                InfXEntityTypes.INFX_SKELETON.getKey(),
                InfXEntityTypes.INFX_WITHER_SKELETON.getKey(),
                InfXEntityTypes.LONGDEAD.getKey(),
                InfXEntityTypes.LONGDEAD_GUARDIAN.getKey(),
                InfXEntityTypes.BONE_LORD.getKey(),
                InfXEntityTypes.ANCIENT_BONE_LORD.getKey());
        // InfX nightwings are undead bats; the zombie/skeleton families join UNDEAD through their family tags.
        tag(EntityTypeTags.UNDEAD).add(InfXEntityTypes.NIGHTWING.getKey());
        tag(EntityTypeTags.BURN_IN_DAYLIGHT).add(
                InfXEntityTypes.GHOUL.getKey(),
                InfXEntityTypes.WIGHT.getKey(),
                InfXEntityTypes.REVENANT.getKey(),
                InfXEntityTypes.INFX_SKELETON.getKey(),
                InfXEntityTypes.LONGDEAD.getKey(),
                InfXEntityTypes.LONGDEAD_GUARDIAN.getKey(),
                InfXEntityTypes.BONE_LORD.getKey(),
                InfXEntityTypes.ANCIENT_BONE_LORD.getKey());

        tag(EntityTypeTags.ARTHROPOD).add(
                InfXEntityTypes.INFX_SPIDER.getKey(),
                InfXEntityTypes.INFX_CAVE_SPIDER.getKey(),
                InfXEntityTypes.BLACK_WIDOW_SPIDER.getKey(),
                InfXEntityTypes.DEMON_SPIDER.getKey(),
                InfXEntityTypes.WOOD_SPIDER.getKey(),
                InfXEntityTypes.PHASE_SPIDER.getKey(),
                InfXEntityTypes.NETHERSPAWN.getKey(),
                InfXEntityTypes.COPPERSPINE.getKey(),
                InfXEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(
                InfXEntityTypes.NETHERSPAWN.getKey(),
                InfXEntityTypes.COPPERSPINE.getKey(),
                InfXEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.IMMUNE_TO_INFESTED).add(
                InfXEntityTypes.NETHERSPAWN.getKey(),
                InfXEntityTypes.COPPERSPINE.getKey(),
                InfXEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.DISMOUNTS_UNDERWATER).add(
                InfXEntityTypes.INFX_SPIDER.getKey(),
                InfXEntityTypes.INFX_CAVE_SPIDER.getKey(),
                InfXEntityTypes.BLACK_WIDOW_SPIDER.getKey(),
                InfXEntityTypes.DEMON_SPIDER.getKey(),
                InfXEntityTypes.WOOD_SPIDER.getKey(),
                InfXEntityTypes.PHASE_SPIDER.getKey());

        tag(EntityTypeTags.AQUATIC).add(
                InfXEntityTypes.INFX_SQUID.getKey(),
                InfXEntityTypes.INFX_COD.getKey(),
                InfXEntityTypes.INFX_SALMON.getKey(),
                InfXEntityTypes.INFX_PUFFERFISH.getKey(),
                InfXEntityTypes.INFX_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(
                InfXEntityTypes.INFX_SQUID.getKey(),
                InfXEntityTypes.INFX_COD.getKey(),
                InfXEntityTypes.INFX_SALMON.getKey(),
                InfXEntityTypes.INFX_PUFFERFISH.getKey(),
                InfXEntityTypes.INFX_TROPICAL_FISH.getKey(),
                InfXEntityTypes.EARTH_ELEMENTAL.getKey(),
                InfXEntityTypes.CLAY_GOLEM.getKey());
        tag(EntityTypeTags.AXOLOTL_HUNT_TARGETS).add(
                InfXEntityTypes.INFX_COD.getKey(),
                InfXEntityTypes.INFX_SALMON.getKey(),
                InfXEntityTypes.INFX_PUFFERFISH.getKey(),
                InfXEntityTypes.INFX_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH).add(
                InfXEntityTypes.INFX_COD.getKey(),
                InfXEntityTypes.INFX_SALMON.getKey(),
                InfXEntityTypes.INFX_PUFFERFISH.getKey(),
                InfXEntityTypes.INFX_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.RAIDERS).add(InfXEntityTypes.INFX_WITCH.getKey());

        tag(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES).add(
                InfXEntityTypes.INFX_BLAZE.getKey(),
                InfXEntityTypes.FIRE_ELEMENTAL.getKey(),
                InfXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(
                InfXEntityTypes.INFX_BLAZE.getKey(),
                InfXEntityTypes.FIRE_ELEMENTAL.getKey(),
                InfXEntityTypes.INFX_GHAST.getKey(),
                InfXEntityTypes.MAGMA_CUBE.getKey(),
                InfXEntityTypes.INFX_BAT.getKey(),
                InfXEntityTypes.VAMPIRE_BAT.getKey(),
                InfXEntityTypes.NIGHTWING.getKey(),
                InfXEntityTypes.GIANT_VAMPIRE_BAT.getKey());

        // 26.1 equips saddles and horse armor only for entity types listed in these tags;
        // the INFX replacements must inherit the vanilla equipping privileges.
        tag(EntityTypeTags.CAN_EQUIP_SADDLE).add(
                InfXEntityTypes.INFX_PIG.getKey(),
                InfXEntityTypes.INFX_HORSE.getKey());
        tag(EntityTypeTags.CAN_WEAR_HORSE_ARMOR).add(InfXEntityTypes.INFX_HORSE.getKey());

        tag(EntityTypeTags.FROG_FOOD).add(
                InfXEntityTypes.INFX_SLIME.getKey(),
                InfXEntityTypes.JELLY.getKey(),
                InfXEntityTypes.BLOB.getKey(),
                InfXEntityTypes.OOZE.getKey(),
                InfXEntityTypes.PUDDING.getKey(),
                InfXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.NON_CONTROLLING_RIDER).add(
                InfXEntityTypes.INFX_SLIME.getKey(),
                InfXEntityTypes.JELLY.getKey(),
                InfXEntityTypes.BLOB.getKey(),
                InfXEntityTypes.OOZE.getKey(),
                InfXEntityTypes.PUDDING.getKey(),
                InfXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.IMMUNE_TO_OOZING).add(
                InfXEntityTypes.INFX_SLIME.getKey(),
                InfXEntityTypes.JELLY.getKey(),
                InfXEntityTypes.BLOB.getKey(),
                InfXEntityTypes.OOZE.getKey(),
                InfXEntityTypes.PUDDING.getKey());
    }
}
