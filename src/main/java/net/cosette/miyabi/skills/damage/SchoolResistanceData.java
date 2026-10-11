package net.cosette.miyabi.skills.damage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.magic.MagicSchool;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SchoolResistanceData {
    private final Map<MagicSchool, Float> resistancePercent = new EnumMap<>(MagicSchool.class);
    public float getResistancePercent(MagicSchool school) {
        return resistancePercent.getOrDefault(school, 0f);
    }
    public void setResistancePercent(MagicSchool school, float percent) {
        resistancePercent.put(school, percent);
    }
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (MagicSchool school : MagicSchool.values()) {
            tag.putFloat(school.name(), getResistancePercent(school));
        }
        return tag;
    }
    public static SchoolResistanceData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        SchoolResistanceData data = new SchoolResistanceData();
        for (MagicSchool school : MagicSchool.values()) {
            if (tag.contains(school.name())) {
                data.setResistancePercent(school, tag.getFloat(school.name()));
            }
        }
        return data;
    }
}