package net.cosette.miyabi.progression;

import net.cosette.miyabi.config.ConfigValue;

public final class MasteryEffects {
    private MasteryEffects() {}
    @ConfigValue(value = "mastery.steps", comment = "Nombre de paliers entre un sort ou une technique non maîtrisé et maîtrisé.", min = 1, max = 1000)
    public static int STEPS = 10;
    @ConfigValue(value = "mastery.max_mana_cost_reduction", comment = "Réduction maximale du coût en mana d'un sort maîtrisé (0,5 = moitié moins).", min = 0, max = 0.95)
    public static float MAX_MANA_COST_REDUCTION = 0.5f;
    @ConfigValue(value = "mastery.max_cast_time_reduction", comment = "Réduction maximale du temps d'incantation d'un sort maîtrisé (0,2 = 20 % plus rapide).", min = 0, max = 0.95)
    public static float MAX_CAST_TIME_REDUCTION = 0.2f;
    /** Nombre de paliers atteints (0 à 10) pour un sort. */
    public static int masterySteps(int casts) {
        return masterySteps(casts, MasteryManager.SPELL_MASTERY_CASTS);
    }
    /** Nombre de paliers atteints (0 à 10) : count utilisations sur masteryCount nécessaires pour maîtriser. */
    public static int masterySteps(int count, int masteryCount) {
        if (count <= 0) return 0;
        return Math.min(STEPS, (int) ((long) count * STEPS / masteryCount));
    }
    public static float manaCostMultiplier(int casts) {
        return 1f - MAX_MANA_COST_REDUCTION * masterySteps(casts) / STEPS;
    }
    public static float castTimeMultiplier(int casts) {
        return 1f - MAX_CAST_TIME_REDUCTION * masterySteps(casts) / STEPS;
    }
    /** Recharge d'une technique : de baseTicks (non maîtrisée) à masteredTicks (maîtrisée), par paliers. */
    public static int techniqueCooldownTicks(int baseTicks, int masteredTicks, int uses) {
        int steps = masterySteps(uses, MasteryManager.TECHNIQUE_MASTERY_USES);
        return Math.round(baseTicks - (baseTicks - masteredTicks) * steps / (float) STEPS);
    }
}