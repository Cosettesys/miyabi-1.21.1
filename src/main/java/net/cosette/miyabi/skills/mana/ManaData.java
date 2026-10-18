package net.cosette.miyabi.skills.mana;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import java.util.function.Supplier;

public final class ManaData {
    public static final int REGEN_INTERVAL_TICKS = 100;
    private float current;
    private float max;
    private int regenFlat;
    private float regenScaling; // pourcentage, 0-100
    private int ticksUntilNextRegen;
    public int getTicksUntilNextRegen() { return ticksUntilNextRegen; }
    public ManaData(float max, int regenFlat, float regenScaling) {
        this.max = max;
        this.current = max;
        this.regenFlat = regenFlat;
        this.regenScaling = clampScaling(regenScaling);
        this.ticksUntilNextRegen = REGEN_INTERVAL_TICKS;
    }
    public float getCurrent() { return current; }
    public float getMax() { return max; }
    public int getRegenFlat() { return regenFlat; }
    public float getRegenScaling() { return regenScaling; }
    public void setCurrent(float value) {
        this.current = Math.max(0f, Math.min(max, value));
    }
    public void setMax(float value) {
        this.max = Math.max(0f, value);
        this.current = Math.min(this.current, this.max);
    }
    public void setRegenFlat(int value) {
        this.regenFlat = Math.max(0, value);
    }
    public void setRegenScaling(float value) {
        this.regenScaling = clampScaling(value);
    }
    private static float clampScaling(float value) {
        return Math.max(0f, Math.min(100f, value));
    }
    /** Total de mana dépensé depuis le chargement (non sauvegardé) : sert à mesurer la dépense pendant un renforcement. */
    private double totalSpent = 0.0;
    public double getTotalSpent() { return totalSpent; }
    public boolean spend(float amount) {
        if (amount <= 0f) return true;
        if (current < amount) return false;
        current -= amount;
        totalSpent += amount;
        ticksUntilNextRegen = REGEN_INTERVAL_TICKS; // repousse le prochain gain, sans dépasser 5s
        return true;
    }
    public void tick(int regenType) {
        if (current < 0f) {
            current = 0f;
        } else if (current > max) {
            current = max;
        }
        if (current >= max) {
            ticksUntilNextRegen = REGEN_INTERVAL_TICKS;
            return;
        }
        if (ticksUntilNextRegen > 0) {
            ticksUntilNextRegen--;
            return;
        }
        float gain = switch (regenType) {
            case 1 -> regenFlat;
            case 2 -> (regenScaling / 100f) * max;
            default -> regenFlat + (regenScaling / 100f) * max;
        };
        current = Math.min(max, current + gain);
        ticksUntilNextRegen = REGEN_INTERVAL_TICKS;
    }
    // --- Synchronisation vers le client (état du dernier envoi, non sauvegardé) ---
    private float syncedCurrent = Float.NaN;
    private float syncedMax = Float.NaN;
    private boolean syncedAwakened = false;

    public boolean needsSync(boolean awakened) {
        return Float.isNaN(syncedCurrent) || syncedCurrent != current || syncedMax != max || syncedAwakened != awakened;
    }

    public void markSynced(boolean awakened) {
        this.syncedCurrent = current;
        this.syncedMax = max;
        this.syncedAwakened = awakened;
    }
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("current", current);
        tag.putFloat("max", max);
        tag.putInt("regenFlat", regenFlat);
        tag.putFloat("regenScaling", regenScaling);
        tag.putInt("ticksUntilNextRegen", ticksUntilNextRegen);
        return tag;
    }
    public static ManaData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        ManaData data = new ManaData(
                tag.getFloat("max"),
                tag.getInt("regenFlat"),
                tag.getFloat("regenScaling")
        );
        data.current = tag.getFloat("current");
        data.ticksUntilNextRegen = tag.contains("ticksUntilNextRegen")
                ? tag.getInt("ticksUntilNextRegen")
                : REGEN_INTERVAL_TICKS;
        return data;
    }
}