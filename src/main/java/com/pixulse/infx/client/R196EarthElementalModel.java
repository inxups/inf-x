package com.pixulse.infx.client;

import com.pixulse.infx.InfiniteX;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

/**
 * R196's 64x32 ModelInvisibleStalker geometry and its permanently raised-arm animation.
 *
 * <p>The old model was otherwise the normal biped layout, so the modern humanoid mesh keeps the
 * authored MITE UV placement while this override reproduces its distinctive arm pose.
 */
final class R196EarthElementalModel extends HumanoidModel<R196EarthElementalRenderState> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(InfiniteX.id("earth_elemental"), "main");

    R196EarthElementalModel(ModelPart root) {
        super(root);
    }

    static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32);
    }

    @Override
    public void setupAnim(R196EarthElementalRenderState state) {
        super.setupAnim(state);
        float attack = Mth.sin(state.attackTime * (float) Math.PI);
        float recovery = Mth.sin((1.0F - (1.0F - state.attackTime) * (1.0F - state.attackTime)) * (float) Math.PI);

        rightArm.zRot = 0.0F;
        leftArm.zRot = 0.0F;
        rightArm.yRot = -(0.1F - attack * 0.6F);
        leftArm.yRot = 0.1F - attack * 0.6F;
        rightArm.xRot = -(float) Math.PI / 2.0F - (attack * 1.2F - recovery * 0.4F);
        leftArm.xRot = -(float) Math.PI / 2.0F - (attack * 1.2F - recovery * 0.4F);
        rightArm.zRot += Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F;
        leftArm.zRot -= Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F;
        rightArm.xRot += Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
        leftArm.xRot -= Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
    }
}
