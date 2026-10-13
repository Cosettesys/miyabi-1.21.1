package net.cosette.miyabi.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.network.payload.SlotHudPayload;

import java.util.List;

@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class SlotHud {
    private SlotHud() {}
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "slot_list");
    private static final int LINE_HEIGHT = 10;
    private static final int GAP_ABOVE_BAR = 4;
    private static final int COLOR_SPELL = 0xFF77AAFF;      // bleu : coût en mana
    private static final int COLOR_TECHNIQUE = 0xFFFFDD55;  // jaune : recharge
    private static final int COLOR_EMPTY = 0xFF888888;
    private static final int COLOR_COOLDOWN = 0xFFFF7755;   // orange : en recharge
    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID, SlotHud::render);
    }
    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSlotHudState.reset();
    }
    private static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        List<SlotHudPayload.Entry> slots = ClientSlotHudState.getSlots();
        if (slots.stream().allMatch(e -> e.kind() == SlotHudPayload.KIND_EMPTY)) return;
        int x = ManaHud.MARGIN_LEFT;
        int barTop = mc.getWindow().getGuiScaledHeight() - ManaHud.MARGIN_BOTTOM;
        int top = barTop - GAP_ABOVE_BAR - slots.size() * LINE_HEIGHT;
        for (int i = 0; i < slots.size(); i++) {
            SlotHudPayload.Entry entry = slots.get(i);
            String text;
            int color;
            if (entry.kind() == SlotHudPayload.KIND_SPELL) {
                text = (i + 1) + " - " + ManaHud.format(entry.value());
                color = COLOR_SPELL;
            } else if (entry.kind() == SlotHudPayload.KIND_TECHNIQUE) {
                boolean cooling = entry.remaining() > 0;
                text = (i + 1) + " - " + (cooling ? entry.remaining() : Math.round(entry.value())) + "s";
                color = cooling ? COLOR_COOLDOWN : COLOR_TECHNIQUE;
            } else {
                text = (i + 1) + " -";
                color = COLOR_EMPTY;
            }
            g.drawString(mc.font, text, x, top + i * LINE_HEIGHT, color);
        }
    }
}