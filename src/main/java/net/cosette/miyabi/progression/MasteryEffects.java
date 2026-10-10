package net.cosette.miyabi.progression;

public final class MasteryEffects {
    private MasteryEffects() {}
    public static final int STEPS = 10;
    public static final float MAX_MANA_COST_REDUCTION = 0.5f;
    public static final float MAX_CAST_TIME_REDUCTION = 0.2f;
    /** Nombre de paliers atteints (0 à 10). */
    public static int masterySteps(int casts) {
        if (casts <= 0) return 0;
        return Math.min(STEPS, (int) ((long) casts * STEPS / MasteryManager.SPELL_MASTERY_CASTS));
    }
    public static float manaCostMultiplier(int casts) {
        return 1f - MAX_MANA_COST_REDUCTION * masterySteps(casts) / STEPS;
    }
    public static float castTimeMultiplier(int casts) {
        return 1f - MAX_CAST_TIME_REDUCTION * masterySteps(casts) / STEPS;
    }
}