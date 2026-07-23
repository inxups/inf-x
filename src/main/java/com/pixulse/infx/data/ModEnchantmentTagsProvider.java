package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.registry.ModEnchantments;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

final class ModEnchantmentTagsProvider extends TagsProvider<Enchantment> {
    ModEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENCHANTMENT, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        for (TagKey<Enchantment> source : List.of(
                EnchantmentTags.IN_ENCHANTING_TABLE,
                EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT,
                EnchantmentTags.ON_TRADED_EQUIPMENT,
                EnchantmentTags.ON_RANDOM_LOOT,
                EnchantmentTags.TRADEABLE,
                EnchantmentTags.TRADES_DESERT_COMMON,
                EnchantmentTags.TRADES_JUNGLE_COMMON,
                EnchantmentTags.TRADES_PLAINS_COMMON,
                EnchantmentTags.TRADES_SAVANNA_COMMON,
                EnchantmentTags.TRADES_SNOW_COMMON,
                EnchantmentTags.TRADES_SWAMP_COMMON,
                EnchantmentTags.TRADES_TAIGA_COMMON)) {
            tag(source, true).add(ModEnchantments.R196.toArray(net.minecraft.resources.ResourceKey[]::new));
        }
    }
}
