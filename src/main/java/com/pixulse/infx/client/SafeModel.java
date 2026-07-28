package com.pixulse.infx.client;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Chest-like safe geometry without the vanilla 1px body/lid side overlap.
 * Vanilla lid sits at y=9 while the body reaches y=10, so side faces z-fight
 * and metal textures flash a mid-body seam. Body ends at y=9; lid starts there.
 */
final class SafeModel extends Model<Float> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(com.pixulse.infx.InfiniteX.id("safe"), "main");

    private final ModelPart lid;
    private final ModelPart lock;

    SafeModel(ModelPart root) {
        super(root, RenderTypes::entityCutoutCull);
        this.lid = root.getChild("lid");
        this.lock = root.getChild("lock");
    }

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Body top meets lid bottom at y=9 — no shared side faces.
        root.addOrReplaceChild(
                "bottom",
                CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 9.0F, 14.0F),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "lid",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F),
                PartPose.offset(0.0F, 9.0F, 1.0F));
        root.addOrReplaceChild(
                "lock",
                CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 14.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 9.0F, 1.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Float open) {
        super.setupAnim(open);
        this.lid.xRot = -(open * (float) (Math.PI / 2));
        this.lock.xRot = this.lid.xRot;
    }
}
