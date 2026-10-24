package net.cosette.miyabi.client.screen;

import net.cosette.miyabi.client.hud.ClientIncantationState;
import net.cosette.miyabi.client.hud.IncantationHud;
import net.cosette.miyabi.config.ClientSettings;
import net.cosette.miyabi.network.payload.TrainerDataPayload;
import net.minecraft.client.Minecraft;
import net.cosette.miyabi.network.payload.MenuDataPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

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
    /**
     * Début, resynchronisation (texte non vide) ou fin (texte vide) de l'incantation. Si l'affichage à l'écran est
     * désactivé (miyabi-client.toml), le texte entier est écrit dans le chat au début du cast, dans la couleur de l'école.
     */
    public static void setIncantation(String text, int durationTicks, int progressTicks, String schools) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        double now = mc.level.getGameTime();

        if (text.isEmpty()) {
            ClientIncantationState.stop(now);
            return;
        }
        int color = IncantationHud.colorFor(schools);
        if (!ClientSettings.SHOW_INCANTATION_ON_SCREEN) {
            if (progressTicks == 0) {
                mc.gui.getChat().addMessage(Component.literal(text).withStyle(Style.EMPTY.withColor(color)));
            }
            return;
        }
        ClientIncantationState.start(text, color, durationTicks, progressTicks, now);
    }
    /** Nouvel état du menu de l'entraîneur : l'écran le lit à chaque image. */
    public static void updateTrainer(TrainerDataPayload data) {
        Minecraft mc = Minecraft.getInstance();
        ClientTrainerState.update(data, mc.level == null ? 0 : mc.level.getGameTime());
    }
}