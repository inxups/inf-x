package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.ModEntityTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;

/** Restores the vanilla semantic tags that custom replacement entity types do not inherit from their Java class. */
final class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(EntityTypeTags.ZOMBIES).add(
                ModEntityTypes.R196_ZOMBIE.getKey(),
                ModEntityTypes.SHADOW.getKey(),
                ModEntityTypes.WIGHT.getKey(),
                ModEntityTypes.REVENANT.getKey(),
                ModEntityTypes.R196_ZOMBIFIED_PIGLIN.getKey());
        tag(EntityTypeTags.SKELETONS).add(
                ModEntityTypes.R196_SKELETON.getKey(),
                ModEntityTypes.LONGDEAD.getKey(),
                ModEntityTypes.BONE_LORD.getKey(),
                ModEntityTypes.ANCIENT_BONE_LORD.getKey());
        tag(EntityTypeTags.BURN_IN_DAYLIGHT).add(
                ModEntityTypes.R196_ZOMBIE.getKey(),
                ModEntityTypes.SHADOW.getKey(),
                ModEntityTypes.WIGHT.getKey(),
                ModEntityTypes.REVENANT.getKey(),
                ModEntityTypes.R196_SKELETON.getKey(),
                ModEntityTypes.LONGDEAD.getKey(),
                ModEntityTypes.BONE_LORD.getKey(),
                ModEntityTypes.ANCIENT_BONE_LORD.getKey());

        tag(EntityTypeTags.ARTHROPOD).add(
                ModEntityTypes.R196_SPIDER.getKey(),
                ModEntityTypes.R196_CAVE_SPIDER.getKey(),
                ModEntityTypes.BLACK_WIDOW_SPIDER.getKey(),
                ModEntityTypes.DEMON_SPIDER.getKey(),
                ModEntityTypes.WOOD_SPIDER.getKey(),
                ModEntityTypes.PHASE_SPIDER.getKey(),
                ModEntityTypes.NETHERSPAWN.getKey(),
                ModEntityTypes.COPPERSPINE.getKey(),
                ModEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(
                ModEntityTypes.NETHERSPAWN.getKey(),
                ModEntityTypes.COPPERSPINE.getKey(),
                ModEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.IMMUNE_TO_INFESTED).add(
                ModEntityTypes.NETHERSPAWN.getKey(),
                ModEntityTypes.COPPERSPINE.getKey(),
                ModEntityTypes.HOARY_SILVERFISH.getKey());
        tag(EntityTypeTags.DISMOUNTS_UNDERWATER).add(
                ModEntityTypes.R196_SPIDER.getKey(),
                ModEntityTypes.R196_CAVE_SPIDER.getKey(),
                ModEntityTypes.BLACK_WIDOW_SPIDER.getKey(),
                ModEntityTypes.DEMON_SPIDER.getKey(),
                ModEntityTypes.WOOD_SPIDER.getKey(),
                ModEntityTypes.PHASE_SPIDER.getKey());

        tag(EntityTypeTags.AQUATIC).add(ModEntityTypes.R196_SQUID.getKey());
        tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(ModEntityTypes.R196_SQUID.getKey());
        tag(EntityTypeTags.RAIDERS).add(ModEntityTypes.R196_WITCH.getKey());

        tag(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES).add(
                ModEntityTypes.R196_BLAZE.getKey(),
                ModEntityTypes.FIRE_ELEMENTAL.getKey(),
                ModEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(
                ModEntityTypes.R196_BLAZE.getKey(),
                ModEntityTypes.FIRE_ELEMENTAL.getKey(),
                ModEntityTypes.R196_GHAST.getKey(),
                ModEntityTypes.MAGMA_CUBE.getKey(),
                ModEntityTypes.VAMPIRE_BAT.getKey(),
                ModEntityTypes.NIGHTWING.getKey(),
                ModEntityTypes.GIANT_VAMPIRE_BAT.getKey());

        tag(EntityTypeTags.FROG_FOOD).add(
                ModEntityTypes.R196_SLIME.getKey(),
                ModEntityTypes.JELLY.getKey(),
                ModEntityTypes.BLOB.getKey(),
                ModEntityTypes.OOZE.getKey(),
                ModEntityTypes.PUDDING.getKey(),
                ModEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.NON_CONTROLLING_RIDER).add(
                ModEntityTypes.R196_SLIME.getKey(),
                ModEntityTypes.JELLY.getKey(),
                ModEntityTypes.BLOB.getKey(),
                ModEntityTypes.OOZE.getKey(),
                ModEntityTypes.PUDDING.getKey(),
                ModEntityTypes.MAGMA_CUBE.getKey());
        tag(EntityTypeTags.IMMUNE_TO_OOZING).add(
                ModEntityTypes.R196_SLIME.getKey(),
                ModEntityTypes.JELLY.getKey(),
                ModEntityTypes.BLOB.getKey(),
                ModEntityTypes.OOZE.getKey(),
                ModEntityTypes.PUDDING.getKey());
    }
}
