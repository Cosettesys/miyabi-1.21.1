package net.cosette.miyabi.magic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;

import java.util.function.Supplier;

public final class SilentCastData {
    private boolean unlocked;
    private boolean enabled;
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        if (!unlocked) this.enabled = false;
    }
    public boolean isEnabled() { return enabled && unlocked; }
    public void setEnabled(boolean enabled) { this.enabled = enabled && unlocked; }
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("unlocked", unlocked);
        tag.putBoolean("enabled", enabled);
        return tag;
    }
    public static SilentCastData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        SilentCastData data = new SilentCastData();
        data.unlocked = tag.getBoolean("unlocked");
        data.enabled = tag.getBoolean("enabled");
        return data;
    }
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Miyabi.MODID);
    public static final Supplier<AttachmentType<SilentCastData>> SILENT_CAST =
            ATTACHMENT_TYPES.register("silent_cast", () -> AttachmentType.builder(holder -> new SilentCastData())
                    .serialize(new IAttachmentSerializer<CompoundTag, SilentCastData>() {
                        @Override
                        public SilentCastData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            return deserializeNBT(tag, provider);
                        }
                        @Override
                        public CompoundTag write(SilentCastData data, HolderLookup.Provider provider) {
                            return data.serializeNBT(provider);
                        }
                    })
                    .build());
}