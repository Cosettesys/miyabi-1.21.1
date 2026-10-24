package net.cosette.miyabi.trainer;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.cosette.miyabi.Miyabi;

/** Branche le suivi des quêtes sur les événements du jeu. */
@EventBusSubscriber(modid = Miyabi.MODID)
public final class QuestEvents {
    private QuestEvents() {}
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            QuestManager.onKill(player, event.getEntity(), event.getSource());
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestManager.tick(player);
        }
    }
}