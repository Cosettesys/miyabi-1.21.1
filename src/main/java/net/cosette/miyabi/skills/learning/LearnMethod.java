package net.cosette.miyabi.skills.learning;

/** Par quel objet on peut apprendre un sort (en plus de le voir lancer ou de passer par un entraîneur). */
public enum LearnMethod {
    /** Aucun objet n'enseigne ce sort. */
    NONE("aucun objet"),
    /** Un parchemin (spell_scroll). */
    SCROLL("parchemin"),
    /** Un grimoire (grimoire), pour les sorts les plus puissants. */
    GRIMOIRE("grimoire");
    private final String itemName;
    LearnMethod(String itemName) {
        this.itemName = itemName;
    }
    /** Nom de l'objet, pour les messages. */
    public String itemName() { return itemName; }
}