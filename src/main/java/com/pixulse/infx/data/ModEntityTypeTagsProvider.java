package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.InfXEntityTypes;
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
                InfXEntityTypes.R196_ZOMBIE.getKey(),
                InfXEntityTypes.SHADOW.getKey(),
                InfXEntityTypes.WIGHT.getKey(),
                InfXEntityTypes.REVENANT.getKey(),
                InfXEntityTypes.R196_ZOMBIFIED_PIGLIN.getKey());
        tag(EntityTypeTags.SKELETONS).add(
                InfXEntityTypes.R196_SKELETON.getKey(),
                InfXEntityTypes.LONGDEAD.getKey(),
                InfXEntityTypes.BONE_LORD.getKey(),
                InfXEntityTypes.ANCIENT_BONE_LORD.getKey());
        // MITE nightwings are undead bats; the zombie/skeleton families join UNDEAD through their family tags.
        tag(EntityTypeTags.UNDEAD).add(InfXEntityTypes.NIGHTWING.getKey());
        tag(EntityTypeTags.BURN_IN_DAYLIGHT).add(
                InfXEntityTypes.R196_ZOMBIE.getKey(),
                InfXEntityTypes.WIGHT.getKey(),
                InfXEntityTypes.REVENANT.getKey(),
                InfXEntityTypes.R196_SKELETON.getKey(),
                InfXEntityTypes.LONGDEAD.getKey(),
                InfXEntityTypes.BONE_LORD.getKey(),
                InfXEntityTypes.ANCIENT_BONE_LORD.getKey());

        tag(EntityTypeTags.ARTHROPOD).add(
                InfXEntityTypes.R196_SPIDER.getKey(),
                InfXEntityTypes.R196_CAVE_SPIDER.getKey(),
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
                InfXEntityTypes.R196_SPIDER.getKey(),
                InfXEntityTypes.R196_CAVE_SPIDER.getKey(),
                InfXEntityTypes.BLACK_WIDOW_SPIDER.getKey(),
                InfXEntityTypes.DEMON_SPIDER.getKey(),
                InfXEntityTypes.WOOD_SPIDER.getKey(),
                InfXEntityTypes.PHASE_SPIDER.getKey());

        tag(EntityTypeTags.AQUATIC).add(
                InfXEntityTypes.R196_SQUID.getKey(),
                InfXEntityTypes.R196_COD.getKey(),
                InfXEntityTypes.R196_SALMON.getKey(),
                InfXEntityTypes.R196_PUFFERFISH.getKey(),
                InfXEntityTypes.R196_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(
                InfXEntityTypes.R196_SQUID.getKey(),
                InfXEntityTypes.R196_COD.getKey(),
                InfXEntityTypes.R196_SALMON.getKey(),
                InfXEntityTypes.R196_PUFFERFISH.getKey(),
                InfXEntityTypes.R196_TROPICAL_FISH.getKey(),
                InfXEntityTypes.EARTH_ELEMENTAL.getKey(),
                InfXEntityTypes.CLAY_GOLEM.getKey());
        tag(EntityTypeTags.AXOLOTL_HUNT_TARGETS).add(
                InfXEntityTypes.R196_COD.getKey(),
                InfXEntityTypes.R196_SALMON.getKey(),
                InfXEntityTypes.R196_PUFFERFISH.getKey(),
                InfXEntityTypes.R196_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH).add(
                InfXEntityTypes.R196_COD.getKey(),
                InfXEntityTypes.R196_SALMON.getKey(),
                InfXEntityTypes.R196_PUFFERFISH.getKey(),
                InfXEntityTypes.R196_TROPICAL_FISH.getKey());
        tag(EntityTypeTags.RAIDERS).add(InfXEntityTypes.R196_WITCH.getKey());

        tag(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES).add(
                InfXEntityTypes.R196_BLAZE.getKey(),
                InfXEntityTypes.FIRE_ELEMENTAL.getKey(),
                InfXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(
                InfXEntityTypes.R196_BLAZE.getKey(),
                InfXEntityTypes.FIRE_ELEMENTAL.getKey(),
                InfXEntityTypes.R196_GHAST.getKey(),
                InfXEntityTypes.MAGMA_CUBE.getKey(),
                InfXEntityTypes.VAMPIRE_BAT.getKey(),
                InfXEntityTypes.NIGHTWING.getKey(),
                InfXEntityTypes.GIANT_VAMPIRE_BAT.getKey());

        tag(EntityTypeTags.FROG_FOOD).add(
                InfXEntityTypes.R196_SLIME.getKey(),
                InfXEntityTypes.JELLY.getKey(),
                InfXEntityTypes.BLOB.getKey(),
                InfXEntityTypes.OOZE.getKey(),
                InfXEntityTypes.PUDDING.getKey(),
                InfXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.NON_CONTROLLING_RIDER).add(
                InfXEntityTypes.R196_SLIME.getKey(),
                InfXEntityTypes.JELLY.getKey(),
                InfXEntityTypes.BLOB.getKey(),
                InfXEntityTypes.OOZE.getKey(),
                InfXEntityTypes.PUDDING.getKey(),
                InfXEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.IMMUNE_TO_OOZING).add(
                InfXEntityTypes.R196_SLIME.getKey(),
                InfXEntityTypes.JELLY.getKey(),
                InfXEntityTypes.BLOB.getKey(),
                InfXEntityTypes.OOZE.getKey(),
                InfXEntityTypes.PUDDING.getKey());
    }
}
