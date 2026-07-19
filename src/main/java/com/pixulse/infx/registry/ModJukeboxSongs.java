package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

public final class ModJukeboxSongs {
    public static final ResourceKey<JukeboxSong> UNDERWORLD = key("underworld");
    public static final ResourceKey<JukeboxSong> DESCENT = key("descent");
    public static final ResourceKey<JukeboxSong> WANDERER = key("wanderer");
    public static final ResourceKey<JukeboxSong> LEGENDS = key("legends");

    private ModJukeboxSongs() {}

    private static ResourceKey<JukeboxSong> key(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, InfiniteX.id(name));
    }

    public static void bootstrap(BootstrapContext<JukeboxSong> context) {
        HolderGetter<SoundEvent> sounds = context.lookup(Registries.SOUND_EVENT);
        context.register(UNDERWORLD, song(sounds, ModSounds.RECORD_UNDERWORLD.getKey(), "underworld", 122.906F, 11));
        context.register(DESCENT, song(sounds, ModSounds.RECORD_DESCENT.getKey(), "descent", 119.380F, 12));
        context.register(WANDERER, song(sounds, ModSounds.RECORD_WANDERER.getKey(), "wanderer", 123.768F, 13));
        context.register(LEGENDS, song(sounds, ModSounds.RECORD_LEGENDS.getKey(), "legends", 62.563F, 14));
    }

    private static JukeboxSong song(
            HolderGetter<SoundEvent> sounds,
            ResourceKey<SoundEvent> sound,
            String name,
            float seconds,
            int comparatorOutput) {
        return new JukeboxSong(
                sounds.getOrThrow(sound),
                Component.translatable("jukebox_song.infx." + name),
                seconds,
                comparatorOutput);
    }
}
