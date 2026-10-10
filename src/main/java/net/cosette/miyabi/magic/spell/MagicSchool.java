package net.cosette.miyabi.magic.spell;

public enum MagicSchool {
    FIRE(1.0f, 0xFF5500),
    WATER(1.0f, 0x9CC0FF),
    EARTH(1.0f, 0x531B00),
    WIND(1.0f, 0xF4F4F4),
    GRAVITY(1.0f, 0x23062E),
    SUMMONING(1.0f, 0xAC4BEB),
    BARRIER(1.0f, 0x0A6FAD),
    HEALING(1.0f, 0x20F565);
    private final float damageMultiplier;
    private final int particleColor;
    MagicSchool(float damageMultiplier, int particleColor) {
        this.damageMultiplier = damageMultiplier;
        this.particleColor = particleColor;
    }
    public float getDamageMultiplier() { return damageMultiplier; }
    public int getParticleColor() { return particleColor; }
}