package net.cosette.miyabi.client;

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
import net.cosette.miyabi.network.MenuRequestPayload;

@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class ClientMenuHandler {
    private ClientMenuHandler() {}
    public static final KeyMapping MENU = new KeyMapping(
            "key.miyabi.menu", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.KEY_K, "key.categories.miyabi");
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(MENU);
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        while (MENU.consumeClick()) {
            PacketDistributor.sendToServer(new MenuRequestPayload());
        }
    }
}