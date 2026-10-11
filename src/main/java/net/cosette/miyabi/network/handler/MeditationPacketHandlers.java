package net.cosette.miyabi.network.handler;

import net.cosette.miyabi.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.progression.MeditationData;
import net.cosette.miyabi.progression.MeditationManager;
import net.cosette.miyabi.network.payload.MeditationInputPayload;

public final class MeditationPacketHandlers {
    private MeditationPacketHandlers() {}
    public static void onMeditationInput(MeditationInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MeditationData med = player.getData(ModAttachments.MEDITATION);
            switch (payload.action()) {
                case MeditationInputPayload.ACTION_START -> MeditationManager.start(player, med);
                case MeditationInputPayload.ACTION_KEEPALIVE -> MeditationManager.keepAlive(player, med);
                case MeditationInputPayload.ACTION_STOP -> MeditationManager.stop(player, med, "arrêtée");
                default -> {}
            }
        });
    }
}