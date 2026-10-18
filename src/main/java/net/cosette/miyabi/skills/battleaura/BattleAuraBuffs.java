package net.cosette.miyabi.skills.battleaura;

/**
 * Réglages du renforcement du corps. Valeurs provisoires, regroupées ici pour passer plus tard en fichier de configuration.
 */
public final class BattleAuraBuffs {
    private BattleAuraBuffs() {}
    /** Niveau de battle aura à partir duquel le renforcement est possible. */
    public static final int UNLOCK_LEVEL = 5;
    public static final int MAX_LEVEL = BattleAuraLevels.MAX_LEVEL;
    /**
     * Valeur d'une statistique selon le niveau : (valeur au niveau 1 + gain par niveau), multipliée par un bonus
     * qui passe de 1 au niveau de déblocage à boostAtMaxLevel au niveau maximum. Les hauts niveaux deviennent ainsi
     * beaucoup plus puissants que ne le ferait une simple progression linéaire.
     */
    public record StatCurve(double atLevel1, double perLevel, double boostAtMaxLevel) {
        public double at(int level) {
            double base = atLevel1 + perLevel * (level - 1);
            return base * (1.0 + (boostAtMaxLevel - 1.0) * highLevelProgress(level));
        }
    }
    /** 0 jusqu'au niveau de déblocage, puis monte linéairement jusqu'à 1 au niveau maximum. */
    public static double highLevelProgress(int level) {
        return Math.max(0.0, Math.min(1.0, (level - UNLOCK_LEVEL) / (double) (MAX_LEVEL - UNLOCK_LEVEL)));
    }
    // Bonus en proportion (0,10 = +10 %). Niveau 5 : +10 % / +15 % / +20 %. Niveau 10 : +30 % / +45 % / +60 %.
    // Niveau 20 : +100 % / +150 % / +200 %.
    private static final double HIGH_LEVEL_BOOST = 2.5;
    public static final StatCurve MOVE_SPEED = new StatCurve(0.02, 0.02, HIGH_LEVEL_BOOST);
    public static final StatCurve ATTACK_SPEED = new StatCurve(0.03, 0.03, HIGH_LEVEL_BOOST);
    public static final StatCurve ATTACK_DAMAGE = new StatCurve(0.04, 0.04, HIGH_LEVEL_BOOST);

    /** Mana dépensé par seconde pour maintenir le renforcement (non renforcé à haut niveau). Niveau 5 : 1 par seconde. */
    public static final StatCurve MANA_DRAIN_PER_SECOND = new StatCurve(0.2, 0.2, 1.0);

    // --- Fatigue : proportionnelle à la part du mana max dépensée pendant le renforcement ---
    /** Par tranche de 10 % du mana max dépensés, la fatigue monte d'un dixième de sa durée maximale. */
    public static final double FATIGUE_STEP_PERCENT = 10.0;
    /** Durée de la fatigue quand 100 % du mana max a été dépensé : 100 s au niveau 5, 20 s au niveau 20. */
    public static final double FATIGUE_MAX_SECONDS_AT_UNLOCK = 100.0;
    public static final double FATIGUE_MAX_SECONDS_AT_MAX_LEVEL = 20.0;
    public static double maxFatigueSeconds(int level) {
        double t = highLevelProgress(level);
        return FATIGUE_MAX_SECONDS_AT_UNLOCK + (FATIGUE_MAX_SECONDS_AT_MAX_LEVEL - FATIGUE_MAX_SECONDS_AT_UNLOCK) * t;
    }
}