package net.cosette.miyabi.client.particle;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.registry.ModParticles;

@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class MiyabiParticleProviders {
    private MiyabiParticleProviders() {}
    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SLASH.get(), SlashParticle.Provider::new);
    }
}