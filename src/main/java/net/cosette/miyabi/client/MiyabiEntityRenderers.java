package net.cosette.miyabi.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.registry.ModEntities;

@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class MiyabiEntityRenderers {
    private MiyabiEntityRenderers() {}
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
    }
}