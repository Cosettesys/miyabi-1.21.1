package net.cosette.miyabi.client.hud;

import net.cosette.miyabi.network.payload.SlotHudPayload;

import java.util.List;

/** Derniers contenus de slots reçus du serveur, lus par le HUD des slots. */
public final class ClientSlotHudState {
    private ClientSlotHudState() {}

    private static List<SlotHudPayload.Entry> slots = List.of();

    public static void update(List<SlotHudPayload.Entry> newSlots) {
        slots = List.copyOf(newSlots);
    }

    public static void reset() {
        slots = List.of();
    }

    public static List<SlotHudPayload.Entry> getSlots() { return slots; }
}