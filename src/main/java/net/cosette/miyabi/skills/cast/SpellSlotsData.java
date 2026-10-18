package net.cosette.miyabi.skills.cast;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;

import java.util.function.Supplier;

public final class SpellSlotsData {

    private final String[] slots = { "", "", "", "", "" };

    /** Id du sort dans ce slot, ou "" si le slot est vide. */
    public String getSpellId(int slot) {
        return (slot >= 0 && slot < slots.length) ? slots[slot] : "";
    }

    /** Assigne un sort à un slot ; le retire de tout autre slot où il se trouvait. */
    public void assign(int slot, String spellId) {
        if (slot < 0 || slot >= slots.length) return;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].equals(spellId)) slots[i] = "";
        }
        slots[slot] = spellId;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < slots.length; i++) {
            tag.putString("slot" + i, slots[i]);
        }
        return tag;
    }

    public static SpellSlotsData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        SpellSlotsData data = new SpellSlotsData();
        for (int i = 0; i < data.slots.length; i++) {
            if (tag.contains("slot" + i)) {
                data.slots[i] = tag.getString("slot" + i);
            }
        }
        return data;
    }
}