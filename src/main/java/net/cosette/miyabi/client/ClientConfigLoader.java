package net.cosette.miyabi.client;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.config.ClientConfig;
import net.cosette.miyabi.config.ConfigReport;
import net.cosette.miyabi.util.Chat;

/** Charge la configuration client au démarrage et ajoute /miyabiclient reload. */
@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class ClientConfigLoader {
    private ClientConfigLoader() {}
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientConfig::reload);
    }
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("miyabiclient")
                .then(Commands.literal("reload").executes(ctx -> {
                    ConfigReport report = ClientConfig.reload();
                    String message = Chat.title("Configuration client rechargée")
                            + (report.warnings().isEmpty() ? "" : " " + Chat.bad(report.warnings().size() + " avertissement(s)") + " : " + report.warnings().get(0));
                    ctx.getSource().sendSuccess(() -> Component.literal(message), false);
                    return 1;
                })));
    }
}