package net.cosette.miyabi.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.client.model.FireballSmallModel;
import net.cosette.miyabi.magic.spell.SpellProjectile;

import java.util.HashMap;
import java.util.Map;

public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile> {
    private record Visual(EntityModel<SpellProjectile> model, ResourceLocation texture) {}
    private final Map<String, Visual> visuals = new HashMap<>();
    private final Visual fallback;
    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        Visual fireball = new Visual(
                new FireballSmallModel<>(context.bakeLayer(FireballSmallModel.LAYER_LOCATION)),
                ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "textures/entity/fireball_small.png")
        );
        visuals.put("fireball", fireball);
        fallback = fireball;
    }
    private Visual visualFor(SpellProjectile entity) {
        return visuals.getOrDefault(entity.getSpellId(), fallback);
    }
    @Override
    public ResourceLocation getTextureLocation(SpellProjectile entity) {
        return visualFor(entity).texture();
    }
    @Override
    public void render(SpellProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Visual visual = visualFor(entity);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getViewYRot(partialTicks)));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(partialTicks)));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0, -1.25, 0.0);
        var vertexConsumer = buffer.getBuffer(visual.model().renderType(visual.texture()));
        visual.model().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}