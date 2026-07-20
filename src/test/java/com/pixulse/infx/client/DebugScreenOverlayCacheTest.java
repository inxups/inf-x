package com.pixulse.infx.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class DebugScreenOverlayCacheTest {
    private static final Identifier GROUP = Identifier.fromNamespaceAndPath("infx", "cache_test");

    @Test
    void cachedDisplayRefreshesOnlyAfterItsInterval() {
        AtomicLong clock = new AtomicLong(1_000L);
        AtomicInteger delegateCalls = new AtomicInteger();
        DebugScreenEntry entry = (displayer, level, clientChunk, serverChunk) -> {
            int generation = delegateCalls.incrementAndGet();
            displayer.addPriorityLine("priority-" + generation);
            displayer.addLine("regular-" + generation);
            displayer.addToGroup(GROUP, List.of("group-a-" + generation, "group-b-" + generation));
            displayer.addToGroup(GROUP, "group-c-" + generation);
        };
        var cache = new CachedDebugDisplay(100L, clock::get);

        RecordingOutput first = new RecordingOutput();
        cache.display(entry, first, null, null, null);
        assertEquals(1, delegateCalls.get());

        clock.addAndGet(99L);
        RecordingOutput cached = new RecordingOutput();
        cache.display(entry, cached, null, null, null);
        assertEquals(1, delegateCalls.get());
        assertEquals(first.output, cached.output);

        clock.incrementAndGet();
        RecordingOutput refreshed = new RecordingOutput();
        cache.display(entry, refreshed, null, null, null);
        assertEquals(2, delegateCalls.get());
        assertEquals(List.of(
                "priority:priority-2",
                "regular:regular-2",
                "group:group-a-2",
                "group:group-b-2",
                "group:group-c-2"), refreshed.output);
    }

    @Test
    void cachedDisplayRejectsNonPositiveIntervals() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CachedDebugDisplay(0L, System::nanoTime));
    }

    private static final class RecordingOutput implements DebugScreenDisplayer {
        private final List<String> output = new ArrayList<>();

        @Override
        public void addPriorityLine(String line) {
            this.output.add("priority:" + line);
        }

        @Override
        public void addLine(String line) {
            this.output.add("regular:" + line);
        }

        @Override
        public void addToGroup(Identifier group, Collection<String> lines) {
            lines.forEach(line -> this.output.add("group:" + line));
        }

        @Override
        public void addToGroup(Identifier group, String line) {
            this.output.add("group:" + line);
        }
    }
}
