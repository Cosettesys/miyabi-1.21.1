package net.cosette.miyabi.progression;

import net.cosette.miyabi.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.cast.CastData;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class MeditationTickHandler {
    private MeditationTickHandler() {}
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MeditationData med = player.getData(ModAttachments.MEDITATION);
        if (!med.isMeditating()) return;
        if (player.getData(ModAttachments.CAST).isCasting()) {
            MeditationManager.stop(player, med, "incantation");
            return;
        }
        player.connection.teleport(med.getAnchorX(), med.getAnchorY(), med.getAnchorZ(),
                player.getYRot(), player.getXRot());
        long now = player.level().getGameTime();
        if (now - med.getLastInputTick() > MeditationManager.KEEPALIVE_TIMEOUT_TICKS) {
            MeditationManager.stop(player, med, "connexion perdue");
            return;
        }
        if (med.advanceSecondTicker()) {
            MeditationManager.secondTick(player, med);
        }
    }
}