package net.cosette.miyabi.skills.battleaura;

/** Les quatre façons d'entraîner son corps. Au moins trois doivent être remplies pour passer au niveau suivant. */
public enum AuraCriterion {
    BLOCKS_BROKEN("Blocs cassés"),
    DISTANCE("Distance parcourue"),
    JUMPS("Sauts"),
    MELEE_KILLS("Ennemis vaincus en mêlée");
    private final String label;
    AuraCriterion(String label) {
        this.label = label;
    }
    public String getLabel() { return label; }
}