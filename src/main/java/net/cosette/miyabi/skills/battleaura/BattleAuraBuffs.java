package net.cosette.miyabi.skills.battleaura;

import net.cosette.miyabi.config.ConfigValue;

/**
 * Réglages du renforcement du corps (réglables dans miyabi.toml). Chaque statistique vaut
 * (valeur au niveau 1 + gain par niveau), multipliée par un bonus qui passe de 1 au niveau de déblocage
 * à "high_level_boost" au niveau maximum.
 */
public final class BattleAuraBuffs {
    private BattleAuraBuffs() {}
    @ConfigValue(value = "battle_aura.unlock_level", comment = "Niveau de battle aura à partir duquel le renforcement du corps est possible.", min = 1, max = 100)
    public static int UNLOCK_LEVEL = 5;
    @ConfigValue(value = "battle_aura.high_level_boost", comment = "Multiplicateur des bonus au niveau maximum (1 au niveau de déblocage, progression linéaire entre les deux).", min = 1, max = 100)
    public static double HIGH_LEVEL_BOOST = 2.5;
    // Bonus en proportion (0,02 = +2 %)
    @ConfigValue(value = "battle_aura.move_speed_level_1", comment = "Bonus de vitesse de déplacement au niveau 1 (0,02 = +2 %).", min = 0)
    public static double MOVE_SPEED_LEVEL_1 = 0.02;
    @ConfigValue(value = "battle_aura.move_speed_per_level", comment = "Bonus de vitesse de déplacement gagné par niveau.", min = 0)
    public static double MOVE_SPEED_PER_LEVEL = 0.02;
    @ConfigValue(value = "battle_aura.attack_speed_level_1", comment = "Bonus de vitesse d'attaque au niveau 1.", min = 0)
    public static double ATTACK_SPEED_LEVEL_1 = 0.03;
    @ConfigValue(value = "battle_aura.attack_speed_per_level", comment = "Bonus de vitesse d'attaque gagné par niveau.", min = 0)
    public static double ATTACK_SPEED_PER_LEVEL = 0.03;
    @ConfigValue(value = "battle_aura.attack_damage_level_1", comment = "Bonus de dégâts d'attaque au niveau 1.", min = 0)
    public static double ATTACK_DAMAGE_LEVEL_1 = 0.04;
    @ConfigValue(value = "battle_aura.attack_damage_per_level", comment = "Bonus de dégâts d'attaque gagné par niveau.", min = 0)
    public static double ATTACK_DAMAGE_PER_LEVEL = 0.04;
    @ConfigValue(value = "battle_aura.mana_drain_level_1", comment = "Mana dépensé par seconde au niveau 1 pour maintenir le renforcement (non augmenté par high_level_boost).", min = 0)
    public static double MANA_DRAIN_LEVEL_1 = 0.2;
    @ConfigValue(value = "battle_aura.mana_drain_per_level", comment = "Mana par seconde gagné par niveau choisi.", min = 0)
    public static double MANA_DRAIN_PER_LEVEL = 0.2;
    @ConfigValue(value = "battle_aura.fatigue_step_percent", comment = "Par tranche de ce pourcentage du mana max dépensé pendant le renforcement, la fatigue monte d'un cran.", min = 1, max = 100)
    public static double FATIGUE_STEP_PERCENT = 10.0;
    @ConfigValue(value = "battle_aura.fatigue_max_seconds_at_unlock", comment = "Durée de la fatigue, en secondes, quand 100 % du mana max a été dépensé, au niveau de déblocage.", min = 0)
    public static double FATIGUE_MAX_SECONDS_AT_UNLOCK = 100.0;
    @ConfigValue(value = "battle_aura.fatigue_max_seconds_at_max_level", comment = "Même durée au niveau maximum (la durée baisse linéairement entre les deux).", min = 0)
    public static double FATIGUE_MAX_SECONDS_AT_MAX_LEVEL = 20.0;
    /** 0 jusqu'au niveau de déblocage, puis monte linéairement jusqu'à 1 au niveau maximum. */
    public static double highLevelProgress(int level) {
        int span = Math.max(1, BattleAuraLevels.MAX_LEVEL - UNLOCK_LEVEL);
        return Math.max(0.0, Math.min(1.0, (level - UNLOCK_LEVEL) / (double) span));
    }
    private static double curve(double atLevel1, double perLevel, double boost, int level) {
        double base = atLevel1 + perLevel * (level - 1);
        return base * (1.0 + (boost - 1.0) * highLevelProgress(level));
    }
    public static double moveSpeed(int level) { return curve(MOVE_SPEED_LEVEL_1, MOVE_SPEED_PER_LEVEL, HIGH_LEVEL_BOOST, level); }
    public static double attackSpeed(int level) { return curve(ATTACK_SPEED_LEVEL_1, ATTACK_SPEED_PER_LEVEL, HIGH_LEVEL_BOOST, level); }
    public static double attackDamage(int level) { return curve(ATTACK_DAMAGE_LEVEL_1, ATTACK_DAMAGE_PER_LEVEL, HIGH_LEVEL_BOOST, level); }
    public static double manaDrainPerSecond(int level) { return curve(MANA_DRAIN_LEVEL_1, MANA_DRAIN_PER_LEVEL, 1.0, level); }
    public static double maxFatigueSeconds(int level) {
        double t = highLevelProgress(level);
        return FATIGUE_MAX_SECONDS_AT_UNLOCK + (FATIGUE_MAX_SECONDS_AT_MAX_LEVEL - FATIGUE_MAX_SECONDS_AT_UNLOCK) * t;
    }
}