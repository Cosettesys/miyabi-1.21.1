package net.cosette.miyabi.skills.cast;

import net.cosette.miyabi.network.payload.SlotHudPayload;

/** Dernier contenu de slots envoyé au client (état de session, non sauvegardé). */
public final class SlotHudSyncState {
    private SlotHudPayload lastSent = null;

    public SlotHudPayload getLastSent() { return lastSent; }
    public void setLastSent(SlotHudPayload payload) { this.lastSent = payload; }
}