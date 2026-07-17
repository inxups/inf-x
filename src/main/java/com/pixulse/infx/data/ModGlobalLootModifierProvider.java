package com.pixulse.infx.data;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.loot.GravelLootModifier;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

final class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    ModGlobalLootModifierProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, InfiniteX.MOD_ID);
    }

    @Override
    protected void start() {
        add("gravel", new GravelLootModifier(new LootItemCondition[0], 2000));
    }
}
