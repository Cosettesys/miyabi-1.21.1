package net.cosette.miyabi.skills.damage;

public enum PhysicalDamageType {
    /** damageMultiplier, armorPenetrationPercent, armorDurabilityMultiplier */
    NONE(1.0f, 0f, 0f),
    BLUNT(1.3f, 0f, 2.0f),
    SLASHING(1.0f, 35f, 1.0f),
    PIERCING(0.7f, 70f, 0.3f);
    private final float defaultDamageMultiplier;
    private final float defaultArmorPenetrationPercent;
    private final float defaultArmorDurabilityMultiplier;
    private float damageMultiplier;
    private float armorPenetrationPercent;
    private float armorDurabilityMultiplier;
    PhysicalDamageType(float damageMultiplier, float armorPenetrationPercent, float armorDurabilityMultiplier) {
        this.defaultDamageMultiplier = damageMultiplier;
        this.defaultArmorPenetrationPercent = armorPenetrationPercent;
        this.defaultArmorDurabilityMultiplier = armorDurabilityMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.armorPenetrationPercent = armorPenetrationPercent;
        this.armorDurabilityMultiplier = armorDurabilityMultiplier;
    }
    public float getDamageMultiplier() { return damageMultiplier; }
    public float getArmorPenetrationPercent() { return armorPenetrationPercent; }
    public float getArmorDurabilityMultiplier() { return armorDurabilityMultiplier; }
    // Valeurs du code, utilisées comme défauts du fichier de configuration
    public float getDefaultDamageMultiplier() { return defaultDamageMultiplier; }
    public float getDefaultArmorPenetrationPercent() { return defaultArmorPenetrationPercent; }
    public float getDefaultArmorDurabilityMultiplier() { return defaultArmorDurabilityMultiplier; }
    /** Appliqué par le chargement de la configuration. */
    public void configure(float damageMultiplier, float armorPenetrationPercent, float armorDurabilityMultiplier) {
        this.damageMultiplier = damageMultiplier;
        this.armorPenetrationPercent = armorPenetrationPercent;
        this.armorDurabilityMultiplier = armorDurabilityMultiplier;
    }
}