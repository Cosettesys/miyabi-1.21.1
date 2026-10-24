package net.cosette.miyabi.trainer;

import net.cosette.miyabi.config.ConfigValue;
import net.cosette.miyabi.progression.MagicRank;

/** Règles générales des entraîneurs (réglables dans miyabi.toml, section "trainer"). Les heures sont des heures de jeu : 1 h = 1000 ticks, 24 h = un jour de jeu. */
public final class TrainerRules {
    private TrainerRules() {}
    @ConfigValue(value = "trainer.trial_from_rank", comment = "À partir de ce rang (courant), monter au rang suivant dans un style exige l'épreuve d'un entraîneur : le joueur reste à un point du rang suivant jusqu'à l'avoir réussie.")
    public static String TRIAL_FROM_RANK = "ADVANCED";
    @ConfigValue(value = "trainer.abandon_cooldown_hours", comment = "Délai en heures de jeu avant de pouvoir commencer une nouvelle quête après en avoir abandonné une (évite de relancer les objectifs jusqu'à avoir ceux qui plaisent).", min = 0)
    public static float ABANDON_COOLDOWN_HOURS = 12f;
    @ConfigValue(value = "trainer.rankup_failure_cooldown_hours", comment = "Délai en heures de jeu avant de pouvoir retenter une épreuve de rang dont le temps est écoulé.", min = 0)
    public static float RANKUP_FAILURE_COOLDOWN_HOURS = 24f;
    public static long hoursToTicks(float hours) {
        return Math.round(hours * 1000.0);
    }
    /** Ce rang courant exige-t-il l'épreuve d'un entraîneur pour monter au suivant ? */
    public static boolean trialRequired(MagicRank currentRank) {
        return currentRank.isAtLeast(trialFromRank());
    }
    /** Rang courant à partir duquel une épreuve est exigée (ADVANCED si la valeur du fichier est invalide). */
    public static MagicRank trialFromRank() {
        try {
            return MagicRank.valueOf(TRIAL_FROM_RANK.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MagicRank.ADVANCED;
        }
    }
}