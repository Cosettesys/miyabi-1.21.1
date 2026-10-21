package net.cosette.miyabi.skills.magic;

/** Élément des dégâts d'un sort, utilisé par les réactions (par exemple la foudre sur une cible mouillée). */
public enum DamageElement {
    FIRE, WATER, EARTH, WIND, GRAVITY, LIGHTNING, NEUTRAL;
    /** Élément d'un sort d'une seule école. */
    public static DamageElement of(MagicSchool school) {
        return switch (school) {
            case FIRE -> FIRE;
            case WATER -> WATER;
            case EARTH -> EARTH;
            case WIND -> WIND;
            case GRAVITY -> GRAVITY;
            default -> NEUTRAL;
        };
    }
}