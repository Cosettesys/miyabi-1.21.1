package net.cosette.miyabi.progression;

public final class MasteryEffects {
    private MasteryEffects() {}
    public static final int STEPS = 10;
    public static final float MAX_MANA_COST_REDUCTION = 0.5f;
    public static final float MAX_CAST_TIME_REDUCTION = 0.2f;
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