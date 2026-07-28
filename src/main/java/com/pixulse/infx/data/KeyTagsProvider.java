package com.pixulse.infx.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/** Restores the resource-key tag appender contract used by the 26.2 data providers. */
abstract class KeyTagsProvider<T> extends TagsProvider<T> {
    protected KeyTagsProvider(
            PackOutput output,
            ResourceKey<? extends Registry<T>> registryKey,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            String modId) {
        super(output, registryKey, lookupProvider, modId);
    }

    protected TagAppender<ResourceKey<T>, T> tag(TagKey<T> tag) {
        return TagAppender.forBuilder(getOrCreateRawBuilder(tag));
    }

    protected TagAppender<ResourceKey<T>, T> tag(TagKey<T> tag, boolean replace) {
        return tag(tag).replace(replace);
    }
}
