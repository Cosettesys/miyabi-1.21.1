package net.cosette.miyabi.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.cosette.miyabi.Miyabi;

/**
 * Coin supérieur gauche : barre de mana avec sa valeur exacte en dessous, et à gauche de la barre
 * une icône d'épée qui change de couleur selon l'état de la battle aura. La liste des slots (SlotHud) vient dessous.
 */
@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class ManaHud {
    private ManaHud() {}
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "mana_bar");
    /** Image de l'icône : à placer dans src/main/resources/assets/miyabi/textures/gui/aura_sword.png */
    private static final ResourceLocation AURA_ICON =
            ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "textures/gui/aura_sword.png");
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 6;
    static final int MARGIN_LEFT = 8;
    static final int MARGIN_TOP = 8;
    /** Début de la liste des slots : sous la barre et sa ligne de texte. */
    static final int SLOT_LIST_TOP = MARGIN_TOP + BAR_HEIGHT + 3 + 9 + 4;
    private static final int ICON_SIZE = 16;
    private static final int ICON_TEXTURE_SIZE = 200; // taille en pixels du PNG (carré)
    private static final int ICON_GAP = 4;
    private static final int COLOR_BORDER = 0xFF000000;
    private static final int COLOR_BACKGROUND = 0xFF222222;
    private static final int COLOR_FILL = 0xFF3377FF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID, ManaHud::render);
    }
    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientManaState.reset();
        ClientAuraState.reset();
    }
    private static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        boolean auraAwakened = ClientAuraState.isAwakened();
        if (ClientAuraState.isAwakened()) {
            drawAuraIcon(g, MARGIN_LEFT + BAR_WIDTH + ICON_GAP, MARGIN_TOP);
        }
        if (!ClientManaState.isAwakened()) return;
        float current = ClientManaState.getCurrent();
        float max = ClientManaState.getMax();
        float ratio = max > 0f ? Mth.clamp(current / max, 0f, 1f) : 0f;
        int x = MARGIN_LEFT;
        int y = MARGIN_TOP;
        g.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, COLOR_BORDER);
        g.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BACKGROUND);
        int filled = Math.round(BAR_WIDTH * ratio);
        if (filled > 0) {
            g.fill(x, y, x + filled, y + BAR_HEIGHT, COLOR_FILL);
        }
        g.drawString(mc.font, format(current) + "/" + format(max), x, y + BAR_HEIGHT + 3, COLOR_TEXT);
    }
    private static void drawAuraIcon(GuiGraphics g, int x, int y) {
        float r;
        float gr;
        float b;
        switch (ClientAuraState.getState()) {
            case ClientAuraState.STATE_ACTIVE -> { r = 1.0f; gr = 0.85f; b = 0.2f; }
            case ClientAuraState.STATE_FATIGUE -> { r = 0.9f; gr = 0.3f; b = 0.3f; }
            default -> { r = 0.45f; gr = 0.45f; b = 0.45f; }
        }
        RenderSystem.enableBlend();
        g.setColor(r, gr, b, 1.0f);
        g.blit(AURA_ICON, x, y, ICON_SIZE, ICON_SIZE, 0.0f, 0.0f,
                ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
    /** Valeur exacte : entier si possible, sinon une décimale. */
    static String format(float value) {
        return Math.abs(value - Math.round(value)) < 0.05f
                ? String.valueOf(Math.round(value))
                : String.format("%.1f", value);
    }
}