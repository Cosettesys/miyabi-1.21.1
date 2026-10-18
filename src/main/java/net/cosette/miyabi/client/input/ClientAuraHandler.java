package net.cosette.miyabi.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.network.payload.AuraInputPayload;

/**
 * Touche de la battle aura. Un appui court active ou désactive le renforcement ; maintenue, la molette choisit le niveau
 * (et l'appui ne bascule alors pas le renforcement au relâchement).
 */
@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class ClientAuraHandler {
    private ClientAuraHandler() {}
    public static final KeyMapping AURA = new KeyMapping(
            "key.miyabi.aura", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.KEY_B, "key.categories.miyabi");
    private static boolean wasDown = false;
    private static boolean scrolledWhileHeld = false;
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(AURA);
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            wasDown = false;
            return;
        }
        boolean down = AURA.isDown();
        if (down && !wasDown) {
            scrolledWhileHeld = false;
        }
        if (!down && wasDown && !scrolledWhileHeld) {
            PacketDistributor.sendToServer(new AuraInputPayload(AuraInputPayload.ACTION_TOGGLE, 0));
        }
        wasDown = down;
    }
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || !AURA.isDown()) return;

        int delta = (int) Math.signum(event.getScrollDeltaY());
        if (delta != 0) {
            scrolledWhileHeld = true;
            PacketDistributor.sendToServer(new AuraInputPayload(AuraInputPayload.ACTION_SCROLL, delta));
        }
        event.setCanceled(true); // la molette ne change pas l'objet en main pendant que la touche est maintenue
    }
}