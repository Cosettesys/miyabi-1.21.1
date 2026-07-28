package net.cosette.miyabi.magic;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import java.util.function.Supplier;

public final class CastData {
    public static final int NO_SLOT = -1;
    private int slot = NO_SLOT;
    private int progressTicks = 0;
    private long lastInputTick = 0;
    private int lastAnnouncedPercent = -1;
    private boolean silentAtStart = false;
    public int getSlot() { return slot; }
    public int getProgressTicks() { return progressTicks; }
    public long getLastInputTick() { return lastInputTick; }
    public boolean isCasting() { return slot != NO_SLOT; }
    public boolean isSilentAtStart() { return silentAtStart; }
    public int getLastAnnouncedPercent() { return lastAnnouncedPercent; }
    public void setLastAnnouncedPercent(int value) { this.lastAnnouncedPercent = value; }
    private double anchorX, anchorY, anchorZ;
    public double getAnchorX() { return anchorX; }
    public double getAnchorY() { return anchorY; }
    public double getAnchorZ() { return anchorZ; }
    public void start(int slot, long currentTick, boolean silent, double x, double y, double z) {
        this.slot = slot;
        this.progressTicks = 0;
        this.lastInputTick = currentTick;
        this.lastAnnouncedPercent = -1;
        this.silentAtStart = silent;
        this.anchorX = x;
        this.anchorY = y;
        this.anchorZ = z;
    }
    public void refreshInput(long currentTick) {
        this.lastInputTick = currentTick;
    }
    public void addProgress(int ticks) {
        this.progressTicks = Math.max(0, this.progressTicks + ticks);
    }
    public void reduceProgressByPercent(float percent) {
        this.progressTicks = Math.max(0, Math.round(this.progressTicks * (1f - percent / 100f)));
    }
    public void reset() {
        this.slot = NO_SLOT;
        this.progressTicks = 0;
        this.lastAnnouncedPercent = -1;
    }
    private long lastCastEndTick = Long.MIN_VALUE / 2; // pour ne pas bloquer le tout premier cast
    public long getLastCastEndTick() { return lastCastEndTick; }
    public void end(long currentTick) {
        reset();
        this.lastCastEndTick = currentTick;
    }
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Miyabi.MODID);
    public static final Supplier<AttachmentType<CastData>> CAST =
            ATTACHMENT_TYPES.register("cast", () -> AttachmentType.builder(holder -> new CastData()).build());
}