package net.cosette.miyabi.client;

import net.minecraft.client.Minecraft;
import net.cosette.miyabi.network.MenuDataPayload;

public final class ClientHooks {
    private ClientHooks() {}
    public static void openMenu(MenuDataPayload data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof MiyabiMenuScreen menu) {
            menu.updateData(data);
        } else {
            mc.setScreen(new MiyabiMenuScreen(data));
        }
    }
}