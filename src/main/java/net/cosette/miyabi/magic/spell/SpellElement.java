package net.cosette.miyabi.magic.spell;

public enum SpellElement {
    FIRE(1.0f, 0xFF5500),
    WATER(1.0f, 0x3377FF),
    EARTH(1.0f, 0x531B00),
    AIR(1.0f, 0xF4F4F4);
    private final float damageMultiplier;
    private final int particleColor;
    SpellElement(float damageMultiplier, int particleColor) {
        this.damageMultiplier = damageMultiplier;
        this.particleColor = particleColor;
    }
    public float getDamageMultiplier() { return damageMultiplier; }
    public int getParticleColor() { return particleColor; }
}