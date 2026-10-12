package net.cosette.miyabi.client.hud;

/** Dernières valeurs de mana reçues du serveur, lues par la barre de mana. */
public final class ClientManaState {
    private ClientManaState() {}
    private static float current = 0f;
    private static float max = 0f;
    private static boolean awakened = false;
    public static void update(float newCurrent, float newMax, boolean newAwakened) {
        current = newCurrent;
        max = newMax;
        awakened = newAwakened;
    }
    /** Appelée à la déconnexion, pour ne pas afficher la barre d'une session précédente. */
    public static void reset() {
        update(0f, 0f, false);
    }
    public static float getCurrent() { return current; }
    public static float getMax() { return max; }
    public static boolean isAwakened() { return awakened; }
}