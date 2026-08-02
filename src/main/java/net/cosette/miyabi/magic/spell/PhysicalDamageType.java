package net.cosette.miyabi.magic.spell;

public enum PhysicalDamageType {
    /** damageMultiplier, armorPenetrationPercent, armorDurabilityMultiplier */
    NONE(1.0f, 0f, 0f),
    BLUNT(1.3f, 0f, 2.0f),
    SLASHING(1.0f, 35f, 1.0f),
    PIERCING(0.7f, 70f, 0.3f);
    private final float damageMultiplier;
    private final float armorPenetrationPercent;
    private final float armorDurabilityMultiplier;
    PhysicalDamageType(float damageMultiplier, float armorPenetrationPercent, float armorDurabilityMultiplier) {
        this.damageMultiplier = damageMultiplier;
        this.armorPenetrationPercent = armorPenetrationPercent;
        this.armorDurabilityMultiplier = armorDurabilityMultiplier;
    }
    public float getDamageMultiplier() { return damageMultiplier; }
    public float getArmorPenetrationPercent() { return armorPenetrationPercent; }
    public float getArmorDurabilityMultiplier() { return armorDurabilityMultiplier; }
}