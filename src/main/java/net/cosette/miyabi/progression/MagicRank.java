package net.cosette.miyabi.progression;

public enum MagicRank {
    // masteryToNext, damageMultiplier
    BEGINNER(20, 1.00f),
    INTERMEDIATE(50, 1.15f),
    ADVANCED(100, 1.30f),
    SAINT(200, 1.50f),
    KING(400, 1.75f),
    EMPEROR(800, 2.00f),
    GOD(0, 2.50f);
    private final int defaultMasteryToNext;
    private final float defaultDamageMultiplier;
    /** Points de maîtrise pour passer au rang suivant (0 pour le rang maximal). */
    private int masteryToNext;
    /** Multiplicateur de dégâts de tous les sorts de l'école à ce rang. */
    private float damageMultiplier;
    MagicRank(int masteryToNext, float damageMultiplier) {
        this.defaultMasteryToNext = masteryToNext;
        this.defaultDamageMultiplier = damageMultiplier;
        this.masteryToNext = masteryToNext;
        this.damageMultiplier = damageMultiplier;
    }
    public int getMasteryToNext() { return masteryToNext; }
    public float getDamageMultiplier() { return damageMultiplier; }
    // Valeurs du code, utilisées comme défauts du fichier de configuration
    public int getDefaultMasteryToNext() { return defaultMasteryToNext; }
    public float getDefaultDamageMultiplier() { return defaultDamageMultiplier; }
    /** Appliqué par le chargement de la configuration. */
    public void configure(int masteryToNext, float damageMultiplier) {
        this.masteryToNext = masteryToNext;
        this.damageMultiplier = damageMultiplier;
    }
    public MagicRank next() {
        return this == GOD ? null : values()[ordinal() + 1];
    }
    public boolean isAtLeast(MagicRank other) {
        return ordinal() >= other.ordinal();
    }
    public static MagicRank fromName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return BEGINNER;
        }
    }
}