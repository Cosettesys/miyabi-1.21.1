package net.cosette.miyabi.network.handler;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.client.hud.ClientSlotHudState;
import net.cosette.miyabi.network.payload.SlotHudPayload;

public final class SlotHudHandlers {
    private SlotHudHandlers() {}

    // Côté client. ClientSlotHudState n'utilise aucune classe Minecraft client : sûr même sur un serveur dédié.
    public static void onSlotHud(SlotHudPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientSlotHudState.update(payload.slots()));
    }
}