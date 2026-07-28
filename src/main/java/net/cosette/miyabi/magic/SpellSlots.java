package net.cosette.miyabi.magic;

public final class SpellSlots {
    private SpellSlots() {}
    public static final int COUNT = 5;
    public static final int[] CAST_DURATION_TICKS = { 40, 100, 200, 300, 400 };
    public static int durationTicks(int slot) {
        return CAST_DURATION_TICKS[slot];
    }
}