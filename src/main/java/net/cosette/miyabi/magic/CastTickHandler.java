package net.cosette.miyabi.magic;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.cosette.miyabi.Miyabi;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class CastTickHandler {
    private CastTickHandler() {}
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CastData cast = player.getData(CastData.CAST);
        if (!cast.isCasting()) return;
        if (!cast.isSilentAtStart()) {
            player.connection.teleport(cast.getAnchorX(), cast.getAnchorY(), cast.getAnchorZ(), player.getYRot(), player.getXRot());
        }
        long now = player.level().getGameTime();
        if (now - cast.getLastInputTick() > CastManager.KEEPALIVE_TIMEOUT_TICKS) {
            CastManager.cancelCast(player, cast, "connexion perdue");
            return;
        }
        cast.addProgress(1);
        int duration = CastManager.effectiveDurationTicks(cast.getSlot(), cast.isSilentAtStart());
        if (cast.getProgressTicks() >= duration) {
            CastManager.finishCast(player, cast);
            return;
        }
        if (cast.getProgressTicks() % 20 == 0) {
            int percent = (int) ((cast.getProgressTicks() * 100L) / duration);
            if (percent > cast.getLastAnnouncedPercent()) {
                cast.setLastAnnouncedPercent(percent);
                player.sendSystemMessage(Component.literal("Incantation chargée à " + percent + "%"));
            }
        }
    }
}