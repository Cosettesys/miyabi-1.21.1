package net.cosette.miyabi.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.cosette.miyabi.Miyabi;

@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class MiyabiClientKeyBindings {
    private MiyabiClientKeyBindings() {}
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        for (var key : MiyabiKeyMappings.SPELL_KEYS) {
            event.register(key);
        }
        event.register(MiyabiKeyMappings.SILENT_CAST_TOGGLE);
    }
}