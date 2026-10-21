package net.cosette.miyabi.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.npc.TrainerEntity;

/**
 * Modèle de base de l'entraîneur : un humanoïde. En attendant sa propre apparence, il utilise la texture de Steve ;
 * pour la changer, pointe TEXTURE vers un PNG au format joueur 64x64 (par exemple miyabi:textures/entity/trainer.png).
 */
public class TrainerRenderer extends HumanoidMobRenderer<TrainerEntity, HumanoidModel<TrainerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    public TrainerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }
    @Override
    public ResourceLocation getTextureLocation(TrainerEntity entity) {
        return TEXTURE;
    }
}