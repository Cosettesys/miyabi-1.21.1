package net.cosette.miyabi.client.hud;

/** État de la battle aura reçu du serveur, lu par l'icône du HUD. */
public final class ClientAuraState {
    private ClientAuraState() {}
    public static final int STATE_INACTIVE = 0;
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_FATIGUE = 2;
    private static boolean awakened = false;
    private static int state = STATE_INACTIVE;
    public static void update(boolean newAwakened, int newState) {
        awakened = newAwakened;
        state = newState;
    }
    public static void reset() {
        update(false, STATE_INACTIVE);
    }
    public static boolean isAwakened() { return awakened; }
    public static int getState() { return state; }
}