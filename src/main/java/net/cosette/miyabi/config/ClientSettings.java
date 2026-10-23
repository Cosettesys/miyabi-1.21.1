package net.cosette.miyabi.config;

/** Réglages côté client, lus dans config/miyabi/miyabi-client.toml. Ne concernent que l'affichage chez le joueur. */
public final class ClientSettings {
    private ClientSettings() {}
    @ConfigValue(value = "incantation.show_incantation_on_screen", comment = "true : l'incantation s'écrit à l'écran pendant le cast. false : elle apparaît dans le chat, pour toi seulement, dans la couleur de l'école.")
    public static boolean SHOW_INCANTATION_ON_SCREEN = true;
    @ConfigValue(value = "incantation.scale", comment = "Taille du texte de l'incantation à l'écran.", min = 0.5, max = 5)
    public static float INCANTATION_SCALE = 1.6f;
    @ConfigValue(value = "incantation.top", comment = "Distance entre le haut de l'écran et la première ligne de l'incantation, en pixels.", min = 0, max = 1000)
    public static int INCANTATION_TOP = 26;
    @ConfigValue(value = "incantation.max_width_fraction", comment = "Largeur maximale de l'incantation avant le retour à la ligne, en part de la largeur de l'écran (0,7 = 70 %).", min = 0.1, max = 1)
    public static float INCANTATION_MAX_WIDTH_FRACTION = 0.7f;
    @ConfigValue(value = "incantation.word_fade_in_ticks", comment = "Durée d'apparition progressive de chaque mot, en ticks (20 = 1 s).", min = 0, max = 200)
    public static float INCANTATION_WORD_FADE_IN_TICKS = 6f;
    @ConfigValue(value = "incantation.fade_out_ticks", comment = "Durée de disparition de l'incantation à la fin du cast, en ticks.", min = 1, max = 200)
    public static float INCANTATION_FADE_OUT_TICKS = 12f;
    @ConfigValue(value = "incantation.complete_brightness", comment = "Éclaircissement de l'incantation une fois complète (0 = aucun, 1 = blanc).", min = 0, max = 1)
    public static float INCANTATION_COMPLETE_BRIGHTNESS = 0.3f;
    @ConfigValue(value = "incantation.complete_glow_ticks", comment = "Durée de la montée en luminosité une fois l'incantation complète, en ticks.", min = 1, max = 200)
    public static float INCANTATION_COMPLETE_GLOW_TICKS = 20f;
}