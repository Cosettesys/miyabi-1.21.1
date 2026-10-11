package net.cosette.miyabi.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.network.payload.MeditationInputPayload;

@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class ClientMeditationHandler {
    private ClientMeditationHandler() {}
    public static final KeyMapping MEDITATE = new KeyMapping(
            "key.miyabi.meditate", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.KEY_N, "key.categories.miyabi");
    private static final int KEEPALIVE_INTERVAL_TICKS = 10;
    private static boolean wasDown = false;
    private static int ticksSinceKeepAlive = 0;
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(MEDITATE);
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        boolean down = MEDITATE.isDown();
        if (down && !wasDown) {
            ticksSinceKeepAlive = 0;
            PacketDistributor.sendToServer(new MeditationInputPayload(MeditationInputPayload.ACTION_START));
        } else if (down) {
            ticksSinceKeepAlive++;
            if (ticksSinceKeepAlive >= KEEPALIVE_INTERVAL_TICKS) {
                ticksSinceKeepAlive = 0;
                PacketDistributor.sendToServer(new MeditationInputPayload(MeditationInputPayload.ACTION_KEEPALIVE));
            }
        } else if (wasDown) {
            PacketDistributor.sendToServer(new MeditationInputPayload(MeditationInputPayload.ACTION_STOP));
        }
        wasDown = down;
    }
}