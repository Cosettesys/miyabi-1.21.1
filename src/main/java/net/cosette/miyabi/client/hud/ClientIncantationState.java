package net.cosette.miyabi.client.hud;

/** Incantation en cours d'affichage (état reçu du serveur). Les temps sont en ticks de jeu du client. */
public final class ClientIncantationState {
    private ClientIncantationState() {}
    private static String text = "";
    private static int color = 0xFFFFFF;
    private static int durationTicks = 0;
    private static double startGameTime = 0;
    private static boolean active = false;
    private static double stoppedAt = 0;
    /** Démarre ou resynchronise : le point de départ est reculé de progressTicks. */
    public static void start(String newText, int newColor, int duration, int progressTicks, double nowGameTime) {
        text = newText;
        color = newColor;
        durationTicks = Math.max(1, duration);
        startGameTime = nowGameTime - progressTicks;
        active = true;
    }
    public static void stop(double nowGameTime) {
        if (active) {
            active = false;
            stoppedAt = nowGameTime;
        }
    }
    public static boolean isActive() { return active; }
    public static String getText() { return text; }
    public static int getColor() { return color; }
    public static int getDurationTicks() { return durationTicks; }
    public static double getStartGameTime() { return startGameTime; }
    public static double getStoppedAt() { return stoppedAt; }
}