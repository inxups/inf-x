package com.pixulse.infx.registry;

import com.pixulse.infx.InfiniteX;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, InfiniteX.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RECORD_UNDERWORLD = record("underworld");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECORD_DESCENT = record("descent");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECORD_WANDERER = record("wanderer");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECORD_LEGENDS = record("legends");
    public static final List<DeferredHolder<SoundEvent, SoundEvent>> RECORDS =
            List.of(RECORD_UNDERWORLD, RECORD_DESCENT, RECORD_WANDERER, RECORD_LEGENDS);

    public static final DeferredHolder<SoundEvent, SoundEvent> GHOUL_AMBIENT = mob("ghoul", "ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOUL_HURT = mob("ghoul", "hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GHOUL_DEATH = mob("ghoul", "death");

    public static final DeferredHolder<SoundEvent, SoundEvent> WIGHT_AMBIENT = mob("wight", "ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIGHT_HURT = mob("wight", "hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIGHT_DEATH = mob("wight", "death");

    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_AMBIENT = mob("shadow", "ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_HURT = mob("shadow", "hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHADOW_DEATH = mob("shadow", "death");

    public static final DeferredHolder<SoundEvent, SoundEvent> INVISIBLE_STALKER_AMBIENT =
            mob("invisible_stalker", "ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> INVISIBLE_STALKER_HURT =
            mob("invisible_stalker", "hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> INVISIBLE_STALKER_DEATH =
            mob("invisible_stalker", "death");

    public static final DeferredHolder<SoundEvent, SoundEvent> DEMON_SPIDER_AMBIENT = mob("demon_spider", "ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEMON_SPIDER_HURT = mob("demon_spider", "hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEMON_SPIDER_DEATH = mob("demon_spider", "death");

    public static final DeferredHolder<SoundEvent, SoundEvent> HELLHOUND_AMBIENT = mob("hellhound", "ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> HELLHOUND_HURT = mob("hellhound", "hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> HELLHOUND_DEATH = mob("hellhound", "death");
    public static final DeferredHolder<SoundEvent, SoundEvent> HELLHOUND_BREATH = mob("hellhound", "breath");

    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_AMBIENT = mob("witch", "ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_HURT = mob("witch", "hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WITCH_DEATH = mob("witch", "death");

    /** MITE imported sizzle for fire elemental. */
    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_ELEMENTAL_SIZZLE =
            SOUNDS.register(
                    "entity.fire_elemental.sizzle",
                    () -> SoundEvent.createVariableRangeEvent(InfiniteX.id("entity.fire_elemental.sizzle")));

    /** MITE imported sizzle played when an acid gelatinous cube scorches living ground. */
    public static final DeferredHolder<SoundEvent, SoundEvent> GELATINOUS_CUBE_CORROSION =
            SOUNDS.register(
                    "entity.gelatinous_cube.corrosion",
                    () -> SoundEvent.createVariableRangeEvent(InfiniteX.id("entity.gelatinous_cube.corrosion")));

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> record(String name) {
        return SOUNDS.register(
                "record." + name, () -> SoundEvent.createVariableRangeEvent(InfiniteX.id("record." + name)));
    }

    private static DeferredHolder<SoundEvent, SoundEvent> mob(String mob, String kind) {
        String path = "entity." + mob + "." + kind;
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(InfiniteX.id(path)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
