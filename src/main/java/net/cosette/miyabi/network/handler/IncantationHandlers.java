package net.cosette.miyabi.network.handler;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.client.screen.ClientHooks;
import net.cosette.miyabi.network.payload.IncantationPayload;

public final class IncantationHandlers {
    private IncantationHandlers() {}
    // Client. Passe par ClientHooks pour ne pas charger de classes client sur un serveur dédié.
    public static void onIncantation(IncantationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHooks.setIncantation(payload.text(), payload.durationTicks(), payload.progressTicks()));
    }
}