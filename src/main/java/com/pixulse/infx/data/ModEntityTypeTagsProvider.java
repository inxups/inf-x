package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfinityXEntityTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

/** Restores the vanilla semantic tags that custom replacement entity types do not inherit from their Java class. */
final class ModEntityTypeTagsProvider extends KeyTagsProvider<EntityType<?>> {
    ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(EntityTypeTags.ZOMBIES).add(
                InfinityXEntityTypes.R196_ZOMBIE.getKey(),
                InfinityXEntityTypes.SHADOW.getKey(),
                InfinityXEntityTypes.WIGHT.getKey(),
                InfinityXEntityTypes.REVENANT.getKey(),
                InfinityXEntityTypes.R196_ZOMBIFIED_PIGLIN.getKey());
        tag(EntityTypeTags.SKELETONS).add(
                InfinityXEntityTypes.R196_SKELETON.getKey(),
                InfinityXEntityTypes.LONGDEAD.getKey(),
                InfinityXEntityTypes.BONE_LORD.getKey(),
                InfinityXEntityTypes.ANCIENT_BONE_LORD.getKey());
        // MITE nightwings are undead bats; the zombie/skeleton families join UNDEAD through their family tags.
        tag(EntityTypeTags.UNDEAD).add(InfinityXEntityTypes.NIGHTWING.getKey());
        tag(EntityTypeTags.BURN_IN_DAYLIGHT).add(
                InfinityXEntityTypes.R196_ZOMBIE.getKey(),
                InfinityXEntityTypes.WIGHT.getKey(),
                InfinityXEntityTypes.REVENANT.getKey(),
                InfinityXEntityTypes.R196_SKELETON.getKey(),
                InfinityXEntityTypes.LONGDEAD.getKey(),
                InfinityXEntityTypes.BONE_LORD.getKey(),
                InfinityXEntityTypes.ANCIENT_BONE_LORD.getKey());

        tag(EntityTypeTags.ARTHROPOD).add(
                InfinityXEntityTypes.R196_SPIDER.getKey(),
                InfinityXEntityTypes.R196_CAVE_SPIDER.getKey(),
                InfinityXEntityTypes.BLACK_WIDOW_SPIDER.getKey(),
                InfinityXEntityTypes.DEMON_SPIDER.getKey(),
                InfinityXEntityTypes.WOOD_SPIDER.getKey(),
                InfinityXEntityTypes.PHASE_SPIDER.getKey(),
                InfinityXEntityTypes.NETHERSPAWN.getKey(),
                InfinityXEntityTypes.COPPERSPINE.getKey(),
                InfinityXEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(
                InfinityXEntityTypes.NETHERSPAWN.getKey(),
                InfinityXEntityTypes.COPPERSPINE.getKey(),
                InfinityXEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.IMMUNE_TO_INFESTED).add(
                InfinityXEntityTypes.NETHERSPAWN.getKey(),
                InfinityXEntityTypes.COPPERSPINE.getKey(),
                InfinityXEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.DISMOUNTS_UNDERWATER).add(
                InfinityXEntityTypes.R196_SPIDER.getKey(),
                InfinityXEntityTypes.R196_CAVE_SPIDER.getKey(),
                InfinityXEntityTypes.BLACK_WIDOW_SPIDER.getKey(),
                InfinityXEntityTypes.DEMON_SPIDER.getKey(),
                InfinityXEntityTypes.WOOD_SPIDER.getKey(),
                InfinityXEntityTypes.PHASE_SPIDER.getKey());

        tag(EntityTypeTags.AQUATIC).add(
                InfinityXEntityTypes.R196_SQUID.getKey(),
                InfinityXEntityTypes.R196_COD.getKey(),
                InfinityXEntityTypes.R196_SALMON.getKey(),
                InfinityXEntityTypes.R196_PUFFERFISH.getKey(),
                InfinityXEntityTypes.R196_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(
                InfinityXEntityTypes.R196_SQUID.getKey(),
                InfinityXEntityTypes.R196_COD.getKey(),
                InfinityXEntityTypes.R196_SALMON.getKey(),
                InfinityXEntityTypes.R196_PUFFERFISH.getKey(),
                InfinityXEntityTypes.R196_TROPICAL_FISH.getKey(),
                InfinityXEntityTypes.EARTH_ELEMENTAL.getKey(),
                InfinityXEntityTypes.CLAY_GOLEM.getKey());
        tag(EntityTypeTags.AXOLOTL_HUNT_TARGETS).add(
                InfinityXEntityTypes.R196_COD.getKey(),
                InfinityXEntityTypes.R196_SALMON.getKey(),
                InfinityXEntityTypes.R196_PUFFERFISH.getKey(),
                InfinityXEntityTypes.R196_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH).add(
                InfinityXEntityTypes.R196_COD.getKey(),
                InfinityXEntityTypes.R196_SALMON.getKey(),
                InfinityXEntityTypes.R196_PUFFERFISH.getKey(),
                InfinityXEntityTypes.R196_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.RAIDERS).add(InfinityXEntityTypes.R196_WITCH.getKey());

        tag(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES).add(
                InfinityXEntityTypes.R196_BLAZE.getKey(),
                InfinityXEntityTypes.FIRE_ELEMENTAL.getKey(),
                InfinityXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(
                InfinityXEntityTypes.R196_BLAZE.getKey(),
                InfinityXEntityTypes.FIRE_ELEMENTAL.getKey(),
                InfinityXEntityTypes.R196_GHAST.getKey(),
                InfinityXEntityTypes.MAGMA_CUBE.getKey(),
                InfinityXEntityTypes.VAMPIRE_BAT.getKey(),
                InfinityXEntityTypes.NIGHTWING.getKey(),
                InfinityXEntityTypes.GIANT_VAMPIRE_BAT.getKey());

        tag(EntityTypeTags.FROG_FOOD).add(
                InfinityXEntityTypes.R196_SLIME.getKey(),
                InfinityXEntityTypes.JELLY.getKey(),
                InfinityXEntityTypes.BLOB.getKey(),
                InfinityXEntityTypes.OOZE.getKey(),
                InfinityXEntityTypes.PUDDING.getKey(),
                InfinityXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.NON_CONTROLLING_RIDER).add(
                InfinityXEntityTypes.R196_SLIME.getKey(),
                InfinityXEntityTypes.JELLY.getKey(),
                InfinityXEntityTypes.BLOB.getKey(),
                InfinityXEntityTypes.OOZE.getKey(),
                InfinityXEntityTypes.PUDDING.getKey(),
                InfinityXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.IMMUNE_TO_OOZING).add(
                InfinityXEntityTypes.R196_SLIME.getKey(),
                InfinityXEntityTypes.JELLY.getKey(),
                InfinityXEntityTypes.BLOB.getKey(),
                InfinityXEntityTypes.OOZE.getKey(),
                InfinityXEntityTypes.PUDDING.getKey());
    }
}
