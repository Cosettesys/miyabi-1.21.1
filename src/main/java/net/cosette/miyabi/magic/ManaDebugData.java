package net.cosette.miyabi.magic;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import java.util.function.Supplier;

public final class ManaDebugData {
    private boolean enabled = false;
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Miyabi.MODID);
    public static final Supplier<AttachmentType<ManaDebugData>> MANA_DEBUG =
            ATTACHMENT_TYPES.register("mana_debug", () -> AttachmentType.builder(holder -> new ManaDebugData()).build());
}