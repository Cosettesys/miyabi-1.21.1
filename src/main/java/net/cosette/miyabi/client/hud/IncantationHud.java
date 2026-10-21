package net.cosette.miyabi.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.cosette.miyabi.Miyabi;

import java.util.ArrayList;
import java.util.List;

/**
 * Affiche l'incantation du sort pendant sa charge : les mots apparaissent un par un en haut au milieu de l'écran,
 * lignes centrées, avec retour à la ligne. Le premier mot apparaît au début de l'incantation et le dernier à sa fin,
 * donc le rythme dépend du nombre de mots et de la durée d'incantation.
 */
@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class IncantationHud {
    private IncantationHud() {}
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "incantation");
    private static final float SCALE = 1.6f;            // taille du texte
    private static final int TOP = 26;                   // distance du haut de l'écran, en pixels
    private static final float MAX_WIDTH_FRACTION = 0.7f; // largeur maximale du texte, en part de l'écran
    private static final int LINE_GAP = 3;
    private static final float WORD_FADE_IN_TICKS = 6f;  // apparition progressive de chaque mot
    private static final float FADE_OUT_TICKS = 12f;     // disparition à la fin de l'incantation
    private static final int COLOR = 0x3AA8C7;
    private record Word(String text, int line, int x) {}
    // Mise en page calculée une fois pour un texte et une largeur donnés : les mots ne bougent jamais en apparaissant
    private static String cachedText = null;
    private static int cachedMaxWidth = -1;
    private static List<Word> words = new ArrayList<>();
    private static List<Integer> lineWidths = new ArrayList<>();
    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID, IncantationHud::render);
    }
    private static void layout(Font font, String text, int maxWidth) {
        if (text.equals(cachedText) && maxWidth == cachedMaxWidth) return;
        cachedText = text;
        cachedMaxWidth = maxWidth;
        words = new ArrayList<>();
        lineWidths = new ArrayList<>();
        int spaceWidth = font.width(" ");
        int line = 0;
        int x = 0;
        for (String part : text.trim().split("\\s+")) {
            int width = font.width(part);
            if (x > 0 && x + spaceWidth + width > maxWidth) {
                lineWidths.add(x);
                line++;
                x = 0;
            }
            if (x > 0) x += spaceWidth;
            words.add(new Word(part, line, x));
            x += width;
        }
        lineWidths.add(x);
    }
    private static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        double now = mc.level.getGameTime() + deltaTracker.getGameTimeDeltaPartialTick(false);
        // Un joueur mort ne récite plus : on n'affiche pas l'ancienne incantation à sa réapparition
        if (mc.player.isDeadOrDying()) {
            ClientIncantationState.stop(now);
        }
        float fade = 1f;
        if (!ClientIncantationState.isActive()) {
            fade = 1f - (float) ((now - ClientIncantationState.getStoppedAt()) / FADE_OUT_TICKS);
            if (fade <= 0f || ClientIncantationState.getText().isEmpty()) return;
        }
        int maxWidth = (int) (mc.getWindow().getGuiScaledWidth() * MAX_WIDTH_FRACTION / SCALE);
        layout(mc.font, ClientIncantationState.getText(), maxWidth);
        if (words.isEmpty()) return;
        int duration = ClientIncantationState.getDurationTicks();
        double elapsed = now - ClientIncantationState.getStartGameTime();
        int count = words.size();
        g.pose().pushPose();
        g.pose().translate(mc.getWindow().getGuiScaledWidth() / 2f, TOP, 0f);
        g.pose().scale(SCALE, SCALE, 1f);
        for (int i = 0; i < count; i++) {
            // Premier mot au début, dernier mot à la fin de l'incantation (un mot seul apparaît à la fin)
            double appearsAt = count == 1 ? duration : duration * (i / (double) (count - 1));
            float age = (float) (elapsed - appearsAt);
            if (age < 0f) continue;
            float alpha = Math.min(1f, age / WORD_FADE_IN_TICKS) * fade;
            int a = (int) (alpha * 255f);
            if (a < 4) continue; // en dessous, le jeu traite la couleur comme opaque
            Word word = words.get(i);
            int x = -lineWidths.get(word.line()) / 2 + word.x();
            int y = word.line() * (mc.font.lineHeight + LINE_GAP);
            g.drawString(mc.font, word.text(), x, y, (a << 24) | COLOR, true);
        }
        g.pose().popPose();
    }
}