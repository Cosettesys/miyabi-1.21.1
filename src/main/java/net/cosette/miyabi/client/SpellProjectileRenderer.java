package net.cosette.miyabi.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.client.model.FireballSmallModel;
import net.cosette.miyabi.magic.spell.SpellProjectile;

public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "textures/entity/fireball_small.png");
    private final FireballSmallModel<SpellProjectile> model;
    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FireballSmallModel<>(context.bakeLayer(FireballSmallModel.LAYER_LOCATION));
    }
    @Override
    public ResourceLocation getTextureLocation(SpellProjectile entity) {
        return TEXTURE;
    }
    @Override
    public void render(SpellProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(-0.0, -1.25, 0.0);
        var vertexConsumer = buffer.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}