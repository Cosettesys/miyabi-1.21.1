package net.cosette.miyabi.progression;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;

import java.util.function.Supplier;

public final class MeditationData {
    private boolean meditating = false;
    private long lastInputTick = 0;
    private int secondTicker = 0;
    private double anchorX, anchorY, anchorZ;

    public boolean isMeditating() { return meditating; }
    public long getLastInputTick() { return lastInputTick; }
    public double getAnchorX() { return anchorX; }
    public double getAnchorY() { return anchorY; }
    public double getAnchorZ() { return anchorZ; }

    public void start(long now, double x, double y, double z) {
        this.meditating = true;
        this.lastInputTick = now;
        this.secondTicker = 0;
        this.anchorX = x;
        this.anchorY = y;
        this.anchorZ = z;
    }

    public void refresh(long now) { this.lastInputTick = now; }

    public void stop() {
        this.meditating = false;
        this.secondTicker = 0;
    }

    /** @return true une fois par seconde (20 appels). */
    public boolean advanceSecondTicker() {
        secondTicker++;
        if (secondTicker >= 20) {
            secondTicker = 0;
            return true;
        }
        return false;
    }
}