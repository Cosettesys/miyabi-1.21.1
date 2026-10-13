package net.cosette.miyabi.client.hud;

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

/** Barre de mana en bas à gauche de l'écran, avec la valeur exacte en dessous. */
@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class ManaHud {
    private ManaHud() {}
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "mana_bar");
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 6;
    static final int MARGIN_LEFT = 8;
    static final int MARGIN_BOTTOM = 28; // distance entre le haut de la barre et le bas de l'écran
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
    }
    private static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !ClientManaState.isAwakened()) return;
        float current = ClientManaState.getCurrent();
        float max = ClientManaState.getMax();
        float ratio = max > 0f ? Mth.clamp(current / max, 0f, 1f) : 0f;
        int x = MARGIN_LEFT;
        int y = mc.getWindow().getGuiScaledHeight() - MARGIN_BOTTOM;
        g.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, COLOR_BORDER);
        g.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BACKGROUND);
        int filled = Math.round(BAR_WIDTH * ratio);
        if (filled > 0) {
            g.fill(x, y, x + filled, y + BAR_HEIGHT, COLOR_FILL);
        }
        g.drawString(mc.font, format(current) + "/" + format(max), x, y + BAR_HEIGHT + 3, COLOR_TEXT);
    }
    /** Valeur exacte : entier si possible, sinon une décimale. */
    static String format(float value) {
        return Math.abs(value - Math.round(value)) < 0.05f
                ? String.valueOf(Math.round(value))
                : String.format("%.1f", value);
    }
}