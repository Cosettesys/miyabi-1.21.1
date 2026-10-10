package net.cosette.miyabi.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.cosette.miyabi.Miyabi;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class MiyabiNetworking {
    private MiyabiNetworking() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(MeditationInputPayload.TYPE, MeditationInputPayload.STREAM_CODEC, MeditationPacketHandlers::onMeditationInput);
        registrar.playToServer(CastInputPayload.TYPE, CastInputPayload.STREAM_CODEC, CastPacketHandlers::onCastInput);
        registrar.playToServer(SilentCastTogglePayload.TYPE, SilentCastTogglePayload.STREAM_CODEC, CastPacketHandlers::onSilentCastToggle);
        registrar.playToServer(MenuRequestPayload.TYPE, MenuRequestPayload.STREAM_CODEC, MenuPacketHandlers::onMenuRequest);
        registrar.playToClient(MenuDataPayload.TYPE, MenuDataPayload.STREAM_CODEC, MenuPacketHandlers::onMenuData);
        registrar.playToServer(SlotAssignPayload.TYPE, SlotAssignPayload.STREAM_CODEC, MenuPacketHandlers::onSlotAssign);
    }
}