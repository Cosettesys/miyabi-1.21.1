package net.cosette.miyabi.network.handler;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.client.hud.ClientManaState;
import net.cosette.miyabi.network.payload.ManaSyncPayload;

public final class ManaSyncHandlers {
    private ManaSyncHandlers() {}
    // Côté client. ClientManaState n'utilise aucune classe Minecraft client : sûr même sur un serveur dédié.
    public static void onManaSync(ManaSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientManaState.update(payload.current(), payload.max(), payload.awakened()));
    }
}