package net.cosette.miyabi.magic;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;

import java.util.function.Supplier;

public final class DamageLogData {
    private boolean enabled = false;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Miyabi.MODID);

    public static final Supplier<AttachmentType<DamageLogData>> DAMAGE_LOG =
            ATTACHMENT_TYPES.register("damage_log", () -> AttachmentType.builder(holder -> new DamageLogData()).build());
}