package com.pixulse.infx.mixin;

import com.pixulse.infx.server.ExtremeDifficulty;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds the fifth, independently serialized Extreme difficulty to Minecraft's fixed enum. */
@Mixin(Difficulty.class)
public abstract class DifficultyMixin {
    @Inject(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/StringRepresentable;fromEnum(Ljava/util/function/Supplier;)Lnet/minecraft/util/StringRepresentable$EnumCodec;",
                    shift = At.Shift.BEFORE))
    private static void infx$createExtreme(CallbackInfo callback) {
        Difficulty hard = Difficulty.HARD;
        ExtremeDifficulty.infx$bootstrap(DifficultyInvoker.infx$create(
                "EXTREME", hard.ordinal() + 1, hard.getId() + 1, ExtremeDifficulty.NAME));
    }

    @Inject(method = "values", at = @At("RETURN"), cancellable = true)
    private static void infx$includeExtreme(CallbackInfoReturnable<Difficulty[]> callback) {
        Difficulty extreme = ExtremeDifficulty.infx$peek();
        Difficulty[] original = callback.getReturnValue();
        if (extreme == null || Arrays.asList(original).contains(extreme)) return;
        Difficulty[] extended = Arrays.copyOf(original, original.length + 1);
        extended[original.length] = extreme;
        callback.setReturnValue(extended);
    }

    @Inject(method = {"getDisplayName", "getInfo"}, at = @At("HEAD"), cancellable = true)
    private void infx$renderExtremeLiteralRed(CallbackInfoReturnable<Component> callback) {
        if (ExtremeDifficulty.isExtreme((Difficulty) (Object) this)) {
            callback.setReturnValue(Component.literal(ExtremeDifficulty.NAME).withStyle(ChatFormatting.RED));
        }
    }
}
