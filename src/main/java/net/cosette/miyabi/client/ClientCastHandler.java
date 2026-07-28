package net.cosette.miyabi.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.network.CastInputPayload;
import net.cosette.miyabi.network.SilentCastTogglePayload;

@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class ClientCastHandler {
    private ClientCastHandler() {}
    private static final int KEEPALIVE_INTERVAL_TICKS = 10; // 0.5s
    private static int activeSlot = -1;
    private static int ticksSinceLastKeepAlive = 0;
    private static boolean silentTogglePrevDown = false;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        handleSpellKeys();
        handleSilentToggleKey();
    }
    private static void handleSpellKeys() {
        KeyMapping[] keys = MiyabiKeyMappings.SPELL_KEYS;
        int pressedSlot = -1;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].isDown()) { pressedSlot = i; break; }
        }
        if (pressedSlot == -1) {
            if (activeSlot != -1) {
                PacketDistributor.sendToServer(new CastInputPayload(activeSlot, CastInputPayload.ACTION_STOP));
                activeSlot = -1;
                ticksSinceLastKeepAlive = 0;
            }
            return;
        }
        if (pressedSlot != activeSlot) {
            activeSlot = pressedSlot;
            ticksSinceLastKeepAlive = 0;
            PacketDistributor.sendToServer(new CastInputPayload(activeSlot, CastInputPayload.ACTION_START));
            return;
        }
        ticksSinceLastKeepAlive++;
        if (ticksSinceLastKeepAlive >= KEEPALIVE_INTERVAL_TICKS) {
            ticksSinceLastKeepAlive = 0;
            PacketDistributor.sendToServer(new CastInputPayload(activeSlot, CastInputPayload.ACTION_KEEPALIVE));
        }
    }
    private static void handleSilentToggleKey() {
        boolean down = MiyabiKeyMappings.SILENT_CAST_TOGGLE.isDown();
        if (down && !silentTogglePrevDown) {
            PacketDistributor.sendToServer(new SilentCastTogglePayload());
        }
        silentTogglePrevDown = down;
    }
}