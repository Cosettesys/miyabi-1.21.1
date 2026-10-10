package net.cosette.miyabi.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.progression.MeditationData;
import net.cosette.miyabi.progression.MeditationManager;

public final class MeditationPacketHandlers {
    private MeditationPacketHandlers() {}
    public static void onMeditationInput(MeditationInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MeditationData med = player.getData(MeditationData.MEDITATION);
            switch (payload.action()) {
                case MeditationInputPayload.ACTION_START -> MeditationManager.start(player, med);
                case MeditationInputPayload.ACTION_KEEPALIVE -> MeditationManager.keepAlive(player, med);
                case MeditationInputPayload.ACTION_STOP -> MeditationManager.stop(player, med, "arrêtée");
                default -> {}
            }
        });
    }
}