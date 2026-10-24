package net.cosette.miyabi.client.screen;

import net.cosette.miyabi.network.payload.TrainerDataPayload;

/** Dernier état reçu pour le menu de l'entraîneur ouvert, et le temps de jeu de sa réception (pour décompter l'épreuve). */
public final class ClientTrainerState {
    private ClientTrainerState() {}
    private static TrainerDataPayload data = null;
    private static long receivedAtGameTime = 0;
    public static void update(TrainerDataPayload newData, long gameTime) {
        data = newData;
        receivedAtGameTime = gameTime;
    }
    public static TrainerDataPayload get() { return data; }
    public static long getReceivedAtGameTime() { return receivedAtGameTime; }
}