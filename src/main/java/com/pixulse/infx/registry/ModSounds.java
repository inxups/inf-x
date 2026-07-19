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

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> record(String name) {
        return SOUNDS.register("record." + name, () -> SoundEvent.createVariableRangeEvent(InfiniteX.id("record." + name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
