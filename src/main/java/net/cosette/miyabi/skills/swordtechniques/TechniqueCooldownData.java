package net.cosette.miyabi.skills.swordtechniques;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * Recharge de chaque technique d'épée d'un joueur. Sauvegardée : se déconnecter ne contourne pas une recharge.
 * Les dates sont en temps de jeu du monde, donc le temps passé hors ligne sur un serveur actif compte quand même.
 */
public final class TechniqueCooldownData {
    private final Map<String, Long> readyAtTick = new HashMap<>();
    public boolean isReady(String techniqueId, long now) {
        return now >= readyAtTick.getOrDefault(techniqueId, 0L);
    }
    public long remainingTicks(String techniqueId, long now) {
        return Math.max(0L, readyAtTick.getOrDefault(techniqueId, 0L) - now);
    }
    public void startCooldown(String techniqueId, long now, int ticks) {
        readyAtTick.values().removeIf(readyAt -> readyAt <= now); // oublie les recharges terminées
        readyAtTick.put(techniqueId, now + ticks);
    }
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Long> entry : readyAtTick.entrySet()) {
            tag.putLong(entry.getKey(), entry.getValue());
        }
        return tag;
    }
    public static TechniqueCooldownData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        TechniqueCooldownData data = new TechniqueCooldownData();
        for (String key : tag.getAllKeys()) {
            data.readyAtTick.put(key, tag.getLong(key));
        }
        return data;
    }
}