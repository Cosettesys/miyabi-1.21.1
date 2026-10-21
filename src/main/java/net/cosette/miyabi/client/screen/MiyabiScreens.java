package net.cosette.miyabi.client.screen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.registry.ModMenus;

/** Associe les menus de conteneur à leurs écrans. */
@EventBusSubscriber(modid = Miyabi.MODID, value = Dist.CLIENT)
public final class MiyabiScreens {
    private MiyabiScreens() {}
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TRAINER.get(), TrainerScreen::new);
    }
}