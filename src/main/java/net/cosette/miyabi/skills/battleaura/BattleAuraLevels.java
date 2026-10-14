package net.cosette.miyabi.skills.battleaura;

/** Valeurs à atteindre pour chaque niveau de battle aura. Valeurs provisoires, destinées à passer en configuration. */
public final class BattleAuraLevels {
    private BattleAuraLevels() {}
    public static final int MAX_LEVEL = 20;
    /** Nombre de critères à remplir (sur 4) pour passer au niveau suivant. */
    public static final int REQUIRED_CONDITIONS = 3;
    private static final double EXPONENT = 1.8;
    private static double base(AuraCriterion criterion) {
        return switch (criterion) {
            case BLOCKS_BROKEN -> 100;
            case DISTANCE -> 1000;
            case JUMPS -> 100;
            case MELEE_KILLS -> 10;
        };
    }
    /** Valeur cumulée à atteindre pour obtenir le niveau donné (le niveau 1 est l'éveil). */
    public static double threshold(int level, AuraCriterion criterion) {
        return Math.round(base(criterion) * Math.pow(level, EXPONENT));
    }
}