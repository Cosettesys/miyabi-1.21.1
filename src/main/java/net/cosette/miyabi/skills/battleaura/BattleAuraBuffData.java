package net.cosette.miyabi.skills.battleaura;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * État du renforcement du corps. Sauvegardés : le niveau choisi et la fatigue (se déconnecter ne l'esquive pas).
 * Non sauvegardés : le renforcement actif, le mana dépensé, et l'état déjà envoyé au client.
 */
public final class BattleAuraBuffData {
    // --- sauvegardé ---
    private int selectedLevel = 0; // 0 = le plus haut niveau débloqué
    private long fatigueUntilTick = 0;
    // --- session ---
    private boolean active = false;
    private double totalSpentAtStart = 0.0;
    private boolean syncedAwakened = false;
    private int syncedState = -1;
    public int getSelectedLevel() { return selectedLevel; }
    public void setSelectedLevel(int level) { this.selectedLevel = level; }
    /** Niveau réellement utilisé : le niveau choisi, sans dépasser le niveau débloqué. */
    public int effectiveLevel(int unlockedLevel) {
        return selectedLevel <= 0 || selectedLevel > unlockedLevel ? unlockedLevel : selectedLevel;
    }
    public long remainingFatigue(long now) { return Math.max(0L, fatigueUntilTick - now); }
    public void startFatigue(long now, long ticks) { this.fatigueUntilTick = now + ticks; }
    public boolean isActive() { return active; }
    public void activate(double totalManaSpentNow) {
        this.active = true;
        this.totalSpentAtStart = totalManaSpentNow;
    }
    public void deactivate() {
        this.active = false;
    }
    /** Mana dépensé depuis l'activation, tous usages confondus (maintien de l'aura, sorts...). */
    public double spentSinceActivation(double totalManaSpentNow) {
        return Math.max(0.0, totalManaSpentNow - totalSpentAtStart);
    }
    public boolean needsSync(boolean awakened, int state) {
        return syncedAwakened != awakened || syncedState != state;
    }
    public void markSynced(boolean awakened, int state) {
        this.syncedAwakened = awakened;
        this.syncedState = state;
    }
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("selectedLevel", selectedLevel);
        tag.putLong("fatigueUntil", fatigueUntilTick);
        return tag;
    }
    public static BattleAuraBuffData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        BattleAuraBuffData data = new BattleAuraBuffData();
        data.selectedLevel = tag.getInt("selectedLevel");
        data.fatigueUntilTick = tag.getLong("fatigueUntil");
        return data;
    }
}