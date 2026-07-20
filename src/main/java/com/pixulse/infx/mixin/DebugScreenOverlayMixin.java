package com.pixulse.infx.mixin;

import com.pixulse.infx.InfiniteXTestMode;
import com.pixulse.infx.client.CachedDebugDisplay;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reduces allocation spikes from the full vanilla F3 text profile in test mode.
 *
 * <p>NeoForge's public debug-entry event intentionally does not allow replacing vanilla entries,
 * so this redirects only their display call and leaves profile selection and rendering untouched.
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {
    @Unique
    private static final long INFX$DEBUG_REFRESH_NANOS = TimeUnit.MILLISECONDS.toNanos(100L);

    @Unique
    private Map<DebugScreenEntry, CachedDebugDisplay> infx$testModeDebugCache;

    @Redirect(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntry;display(Lnet/minecraft/client/gui/components/debug/DebugScreenDisplayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void infx$cacheTestModeDebugText(
            DebugScreenEntry entry,
            DebugScreenDisplayer displayer,
            @Nullable Level serverOrClientLevel,
            @Nullable LevelChunk clientChunk,
            @Nullable LevelChunk serverChunk) {
        if (!InfiniteXTestMode.isEnabled()
                || entry == DebugScreenEntries.getEntry(DebugScreenEntries.FPS)
                || !entry.category().equals(DebugEntryCategory.SCREEN_TEXT)) {
            entry.display(displayer, serverOrClientLevel, clientChunk, serverChunk);
            return;
        }

        if (this.infx$testModeDebugCache == null) {
            this.infx$testModeDebugCache = new IdentityHashMap<>();
        }
        this.infx$testModeDebugCache
                .computeIfAbsent(entry, ignored -> new CachedDebugDisplay(INFX$DEBUG_REFRESH_NANOS, System::nanoTime))
                .display(entry, displayer, serverOrClientLevel, clientChunk, serverChunk);
    }
}
