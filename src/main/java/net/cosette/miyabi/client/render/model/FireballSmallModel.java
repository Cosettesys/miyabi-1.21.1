package net.cosette.miyabi.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.cosette.miyabi.Miyabi;

public class FireballSmallModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "fireball_small"), "main");
    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart shell;
    private final ModelPart spikes;
    public FireballSmallModel(ModelPart root) {
        this.root = root;
        this.core = root.getChild("core");
        this.shell = root.getChild("shell");
        this.spikes = root.getChild("spikes");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose pose = PartPose.offset(0.0F, 18.0F, 0.0F); // centre de la boule
        root.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), pose);
        root.addOrReplaceChild("shell", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F), pose);
        root.addOrReplaceChild("spikes", CubeListBuilder.create()
                .texOffs(0, 40).addBox(6.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(16, 40).addBox(-10.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(32, 40).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(48, 40).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(0, 48).addBox(-2.0F, -2.0F, 6.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(16, 48).addBox(-2.0F, -2.0F, -10.0F, 4.0F, 4.0F, 4.0F), pose);
        return LayerDefinition.create(mesh, 64, 64); // la texture est en 64x64 maintenant
    }
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        spikes.yRot = ageInTicks * 0.15F;          // les pointes tournent
        shell.yRot  = -ageInTicks * 0.08F;         // la coque tourne en sens inverse, plus lentement
        float pulse = 1.0F + 0.08F * Mth.sin(ageInTicks * 0.4F);
        core.xScale = pulse; core.yScale = pulse; core.zScale = pulse; // le noyau pulse
    }
    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        root.render(ps, vc, light, overlay, color); // dessine les 3 bones d'un coup
    }
}