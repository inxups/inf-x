package com.pixulse.infx.client;

import com.pixulse.infx.entity.EarthElemental;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** Client render data for the synchronized INFX earth-elemental body material. */
final class EarthElementalRenderState extends HumanoidRenderState {
    EarthElemental.Form form = EarthElemental.Form.STONE_NORMAL;
}
