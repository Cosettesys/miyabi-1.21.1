package net.cosette.miyabi.magic.spell;

public enum SpellShape {
    /** speedMultiplier, sizeMultiplier, damageMultiplier */
    SPHERE(1.0f, 1.0f, 1.0f);
    private final float speedMultiplier;
    private final float sizeMultiplier;
    private final float damageMultiplier;
    SpellShape(float speedMultiplier, float sizeMultiplier, float damageMultiplier) {
        this.speedMultiplier = speedMultiplier;
        this.sizeMultiplier = sizeMultiplier;
        this.damageMultiplier = damageMultiplier;
    }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public float getSizeMultiplier() { return sizeMultiplier; }
    public float getDamageMultiplier() { return damageMultiplier; }
}