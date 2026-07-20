package com.pixulse.infx.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.LongSupplier;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

/** Records a debug entry's text and replays it until its next low-frequency refresh. */
public final class CachedDebugDisplay {
    private final long refreshIntervalNanos;
    private final LongSupplier clock;
    private List<RecordedCall> calls = List.of();
    private long lastRefreshNanos;
    private boolean initialized;

    public CachedDebugDisplay(long refreshIntervalNanos, LongSupplier clock) {
        if (refreshIntervalNanos <= 0L) {
            throw new IllegalArgumentException("refreshIntervalNanos must be positive");
        }
        this.refreshIntervalNanos = refreshIntervalNanos;
        this.clock = clock;
    }

    public void display(
            DebugScreenEntry entry,
            DebugScreenDisplayer displayer,
            @Nullable Level serverOrClientLevel,
            @Nullable LevelChunk clientChunk,
            @Nullable LevelChunk serverChunk) {
        long now = this.clock.getAsLong();
        if (!this.initialized || now - this.lastRefreshNanos >= this.refreshIntervalNanos) {
            RecordingDisplayer recording = new RecordingDisplayer();
            entry.display(recording, serverOrClientLevel, clientChunk, serverChunk);
            this.calls = List.copyOf(recording.calls);
            this.lastRefreshNanos = now;
            this.initialized = true;
        }

        for (int index = 0; index < this.calls.size(); index++) {
            this.calls.get(index).replay(displayer);
        }
    }

    private interface RecordedCall {
        void replay(DebugScreenDisplayer displayer);
    }

    private record PriorityLine(String line) implements RecordedCall {
        @Override
        public void replay(DebugScreenDisplayer displayer) {
            displayer.addPriorityLine(this.line);
        }
    }

    private record RegularLine(String line) implements RecordedCall {
        @Override
        public void replay(DebugScreenDisplayer displayer) {
            displayer.addLine(this.line);
        }
    }

    private record GroupLines(Identifier group, List<String> lines) implements RecordedCall {
        @Override
        public void replay(DebugScreenDisplayer displayer) {
            displayer.addToGroup(this.group, this.lines);
        }
    }

    private static final class RecordingDisplayer implements DebugScreenDisplayer {
        private final List<RecordedCall> calls = new ArrayList<>();

        @Override
        public void addPriorityLine(String line) {
            this.calls.add(new PriorityLine(line));
        }

        @Override
        public void addLine(String line) {
            this.calls.add(new RegularLine(line));
        }

        @Override
        public void addToGroup(Identifier group, Collection<String> lines) {
            this.calls.add(new GroupLines(group, List.copyOf(lines)));
        }

        @Override
        public void addToGroup(Identifier group, String line) {
            this.calls.add(new GroupLines(group, List.of(line)));
        }
    }
}
