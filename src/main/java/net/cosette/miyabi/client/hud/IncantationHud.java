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
import net.cosette.miyabi.config.ClientSettings;
import net.cosette.miyabi.skills.magic.MagicSchool;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Affiche l'incantation du sort pendant sa charge : les mots apparaissent un par un en haut au milieu de l'écran,
 * lignes centrées, avec retour à la ligne. Le premier mot apparaît au début de l'incantation et le dernier à sa fin,
 * donc le rythme dépend du nombre de mots et de la durée d'incantation. Une fois complète, elle s'éclaircit.
 * La taille, la position et les temps se règlent dans miyabi-client.toml ; les couleurs par école, ici.
 */
@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class IncantationHud {
    private IncantationHud() {}
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "incantation");
    // ---- COULEURS PAR ÉCOLE : à ajuster ici pour les tests (format 0xRRGGBB) ----
    /** Couleur quand l'école n'est pas connue. */
    private static final int DEFAULT_COLOR = 0x3AA8C7;
    private static final Map<MagicSchool, Integer> SCHOOL_COLORS = new EnumMap<>(MagicSchool.class);
    static {
        SCHOOL_COLORS.put(MagicSchool.FIRE, 0xFF6A2B);
        SCHOOL_COLORS.put(MagicSchool.WATER, 0x3AA8C7);
        SCHOOL_COLORS.put(MagicSchool.EARTH, 0xC9A24B);
        SCHOOL_COLORS.put(MagicSchool.WIND, 0xB6F0C8);
        SCHOOL_COLORS.put(MagicSchool.GRAVITY, 0xA45FE8);
        SCHOOL_COLORS.put(MagicSchool.SUMMONING, 0xE45FC9);
        SCHOOL_COLORS.put(MagicSchool.BARRIER, 0x6C8CF0);
        SCHOOL_COLORS.put(MagicSchool.HEALING, 0x5FE08E);
    }
    private static final int LINE_GAP = 3;
    /** Couleur d'un sort d'après ses écoles (séparées par des virgules) ; un sort hybride mélange les couleurs de ses écoles. */
    public static int colorFor(String schoolsCsv) {
        int r = 0;
        int g = 0;
        int b = 0;
        int count = 0;
        for (String name : schoolsCsv.split(",")) {
            try {
                int color = SCHOOL_COLORS.getOrDefault(MagicSchool.valueOf(name.trim()), DEFAULT_COLOR);
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
                count++;
            } catch (IllegalArgumentException ignored) {
                // école inconnue : ignorée
            }
        }
        if (count == 0) return DEFAULT_COLOR;
        return ((r / count) << 16) | ((g / count) << 8) | (b / count);
    }
    private static int lerpColor(int from, int to, float t) {
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (r << 16) | (g << 8) | b;
    }
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
        if (!ClientSettings.SHOW_INCANTATION_ON_SCREEN) return;
        double now = mc.level.getGameTime() + deltaTracker.getGameTimeDeltaPartialTick(false);
        // Un joueur mort ne récite plus : on n'affiche pas l'ancienne incantation à sa réapparition
        if (mc.player.isDeadOrDying()) {
            ClientIncantationState.stop(now);
        }
        float fade = 1f;
        if (!ClientIncantationState.isActive()) {
            fade = 1f - (float) ((now - ClientIncantationState.getStoppedAt()) / ClientSettings.INCANTATION_FADE_OUT_TICKS);
            if (fade <= 0f || ClientIncantationState.getText().isEmpty()) return;
        }
        float scale = ClientSettings.INCANTATION_SCALE;
        int maxWidth = (int) (mc.getWindow().getGuiScaledWidth() * ClientSettings.INCANTATION_MAX_WIDTH_FRACTION / scale);
        layout(mc.font, ClientIncantationState.getText(), maxWidth);
        if (words.isEmpty()) return;
        int duration = ClientIncantationState.getDurationTicks();
        double elapsed = now - ClientIncantationState.getStartGameTime();
        int count = words.size();
        // Une fois tous les mots écrits, la couleur s'éclaircit vers le blanc
        float glow = (float) Math.max(0.0, Math.min(1.0, (elapsed - duration) / ClientSettings.INCANTATION_COMPLETE_GLOW_TICKS));
        int rgb = lerpColor(ClientIncantationState.getColor(), 0xFFFFFF, glow * ClientSettings.INCANTATION_COMPLETE_BRIGHTNESS);
        g.pose().pushPose();
        g.pose().translate(mc.getWindow().getGuiScaledWidth() / 2f, ClientSettings.INCANTATION_TOP, 0f);
        g.pose().scale(scale, scale, 1f);
        for (int i = 0; i < count; i++) {
            // Premier mot au début, dernier mot à la fin de l'incantation (un mot seul apparaît à la fin)
            double appearsAt = count == 1 ? duration : duration * (i / (double) (count - 1));
            float age = (float) (elapsed - appearsAt);
            if (age < 0f) continue;
            float fadeIn = ClientSettings.INCANTATION_WORD_FADE_IN_TICKS;
            float alpha = (fadeIn <= 0f ? 1f : Math.min(1f, age / fadeIn)) * fade;
            int a = (int) (alpha * 255f);
            if (a < 4) continue; // en dessous, le jeu traite la couleur comme opaque
            Word word = words.get(i);
            int x = -lineWidths.get(word.line()) / 2 + word.x();
            int y = word.line() * (mc.font.lineHeight + LINE_GAP);
            g.drawString(mc.font, word.text(), x, y, (a << 24) | rgb, true);
        }
        g.pose().popPose();
    }
}