package net.cosette.miyabi.skills.swordtechniques;

import java.util.HashMap;
import java.util.Map;

/** Recharge de chaque technique d'épée d'un joueur (état de session, non sauvegardé). */
public final class TechniqueCooldownData {
    private final Map<String, Long> readyAtTick = new HashMap<>();
    public boolean isReady(String techniqueId, long now) {
        return now >= readyAtTick.getOrDefault(techniqueId, 0L);
    }
    public long remainingTicks(String techniqueId, long now) {
        return Math.max(0L, readyAtTick.getOrDefault(techniqueId, 0L) - now);
    }
    public void startCooldown(String techniqueId, long now, int ticks) {
        readyAtTick.put(techniqueId, now + ticks);
    }
}