package net.cosette.miyabi.client.screen;

import net.cosette.miyabi.client.hud.ClientIncantationState;
import net.minecraft.client.Minecraft;
import net.cosette.miyabi.network.payload.MenuDataPayload;

public final class ClientHooks {
    private ClientHooks() {}
    public static void openMenu(MenuDataPayload data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof MiyabiMenuScreen menu) {
            menu.updateData(data);
        } else {
            mc.setScreen(new MiyabiMenuScreen(data));
        }
    }
    /** Début, resynchronisation (texte non vide) ou fin (texte vide) de l'affichage de l'incantation. */
    public static void setIncantation(String text, int durationTicks, int progressTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        double now = mc.level.getGameTime();
        if (text.isEmpty()) {
            ClientIncantationState.stop(now);
        } else {
            ClientIncantationState.start(text, durationTicks, progressTicks, now);
        }
    }
}