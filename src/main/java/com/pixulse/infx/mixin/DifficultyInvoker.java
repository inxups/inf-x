package com.pixulse.infx.mixin;

import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Constructor bridge used only while extending Minecraft's fixed difficulty enum. */
@Mixin(Difficulty.class)
public interface DifficultyInvoker {
    @Invoker("<init>")
    static Difficulty infx$create(String enumName, int ordinal, int id, String serializedName) {
        throw new AssertionError("Difficulty constructor invoker was not transformed");
    }
}
