package net.cosette.miyabi.magic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.magic.spell.SpellElement;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ElementResistanceData {

    private final Map<SpellElement, Float> resistancePercent = new EnumMap<>(SpellElement.class);

    public float getResistancePercent(SpellElement element) {
        return resistancePercent.getOrDefault(element, 0f);
    }

    public void setResistancePercent(SpellElement element, float percent) {
        resistancePercent.put(element, percent);
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (SpellElement element : SpellElement.values()) {
            tag.putFloat(element.name(), getResistancePercent(element));
        }
        return tag;
    }

    public static ElementResistanceData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        ElementResistanceData data = new ElementResistanceData();
        for (SpellElement element : SpellElement.values()) {
            if (tag.contains(element.name())) {
                data.setResistancePercent(element, tag.getFloat(element.name()));
            }
        }
        return data;
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Miyabi.MODID);

    public static final Supplier<AttachmentType<ElementResistanceData>> ELEMENT_RESISTANCE =
            ATTACHMENT_TYPES.register("element_resistance", () -> AttachmentType.builder(holder -> new ElementResistanceData())
                    .serialize(new IAttachmentSerializer<CompoundTag, ElementResistanceData>() {
                        @Override
                        public ElementResistanceData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            return deserializeNBT(tag, provider);
                        }
                        @Override
                        public CompoundTag write(ElementResistanceData data, HolderLookup.Provider provider) {
                            return data.serializeNBT(provider);
                        }
                    })
                    .build());
}