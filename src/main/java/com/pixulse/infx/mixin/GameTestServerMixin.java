package com.pixulse.infx.mixin;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Keeps Minecraft's hard-coded GameTest grid inside worlds with a raised minimum Y. */
@Mixin(GameTestServer.class)
public abstract class GameTestServerMixin {
    @ModifyConstant(method = "startTests", constant = @Constant(intValue = -59))
    private int infx$placeTestsAboveWorldBottom(int originalY, ServerLevel level) {
        return level.getMinY() + 5;
    }
}
