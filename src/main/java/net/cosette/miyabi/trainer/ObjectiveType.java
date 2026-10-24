package net.cosette.miyabi.trainer;

/** Types d'objectifs qu'un entraîneur peut donner. */
public enum ObjectiveType {
    /** Vaincre N mobs d'un type précis (target = minecraft:zombie). */
    KILL_ENTITY,
    /** Vaincre N mobs d'une classe, un tag de types d'entités (target = minecraft:undead). */
    KILL_TAG,
    /** Vaincre N monstres hostiles. */
    KILL_HOSTILE,
    /** Vaincre N monstres hostiles à mains nues. */
    KILL_HOSTILE_UNARMED,
    /** Maîtriser une technique (target = id de la technique). */
    MASTER_TECHNIQUE,
    /** Avoir appris une technique (target = id de la technique). */
    LEARN_TECHNIQUE,
    /** Atteindre un niveau de battle aura (count = niveau). */
    AURA_LEVEL;
    public boolean isKill() {
        return this == KILL_ENTITY || this == KILL_TAG || this == KILL_HOSTILE || this == KILL_HOSTILE_UNARMED;
    }
}