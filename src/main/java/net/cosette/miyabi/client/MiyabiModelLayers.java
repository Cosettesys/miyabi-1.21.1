package net.cosette.miyabi.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.client.model.FireballSmallModel;

@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class MiyabiModelLayers {
    private MiyabiModelLayers() {}
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FireballSmallModel.LAYER_LOCATION, FireballSmallModel::createBodyLayer);
    }
}