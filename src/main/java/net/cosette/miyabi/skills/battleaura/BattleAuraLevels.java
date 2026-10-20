package net.cosette.miyabi.skills.battleaura;

import net.cosette.miyabi.config.ConfigValue;

/** Valeurs à atteindre pour chaque niveau de battle aura (réglables dans miyabi.toml). */
public final class BattleAuraLevels {
    private BattleAuraLevels() {}
    @ConfigValue(value = "battle_aura.max_level", comment = "Niveau maximal de la battle aura.", min = 1, max = 100)
    public static int MAX_LEVEL = 20;
    @ConfigValue(value = "battle_aura.required_conditions", comment = "Nombre de critères d'entraînement (sur 4) à remplir pour passer au niveau suivant.", min = 1, max = 4)
    public static int REQUIRED_CONDITIONS = 3;
    @ConfigValue(value = "battle_aura.threshold_exponent", comment = "Chaque seuil vaut base x niveau^exposant : plus l'exposant est grand, plus les hauts niveaux sont difficiles.", min = 0.1, max = 10)
    public static double EXPONENT = 1.8;
    @ConfigValue(value = "battle_aura.base_blocks_broken", comment = "Seuil de base (niveau 1) : blocs cassés.", min = 1)
    public static double BASE_BLOCKS_BROKEN = 100;
    @ConfigValue(value = "battle_aura.base_distance", comment = "Seuil de base (niveau 1) : distance parcourue à pied, en blocs.", min = 1)
    public static double BASE_DISTANCE = 1000;
    @ConfigValue(value = "battle_aura.base_jumps", comment = "Seuil de base (niveau 1) : sauts.", min = 1)
    public static double BASE_JUMPS = 100;
    @ConfigValue(value = "battle_aura.base_melee_kills", comment = "Seuil de base (niveau 1) : ennemis hostiles vaincus en mêlée.", min = 1)
    public static double BASE_MELEE_KILLS = 10;
    private static double base(AuraCriterion criterion) {
        return switch (criterion) {
            case BLOCKS_BROKEN -> BASE_BLOCKS_BROKEN;
            case DISTANCE -> BASE_DISTANCE;
            case JUMPS -> BASE_JUMPS;
            case MELEE_KILLS -> BASE_MELEE_KILLS;
        };
    }
    /** Valeur cumulée à atteindre pour obtenir le niveau donné (le niveau 1 est l'éveil). */
    public static double threshold(int level, AuraCriterion criterion) {
        return Math.round(base(criterion) * Math.pow(level, EXPONENT));
    }
}