package net.cosette.miyabi.skills.cast;

import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.mana.ManaData;

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
        int duration = CastManager.effectiveDurationTicks(player, cast.getSlot(), cast.isSilentAtStart());
        SpellDefinition def = CastManager.spellInSlot(player, cast.getSlot());
        float maxIntensity = (def != null) ? def.maxIntensityPercent() : 100f;
        float manaLimitedIntensity = maxIntensity;
        if (def != null) {
            float cost = CastManager.effectiveManaCost(player, def);
            if (cost > 0f) {
                ManaData mana = player.getData(ManaData.MANA);
                manaLimitedIntensity = (mana.getCurrent() / cost) * 100f;
            }
        }
        float achievableIntensity = Math.max(100f, Math.min(maxIntensity, manaLimitedIntensity));
        int maxProgressTicks = Math.round(duration * (achievableIntensity / 100f));

        if (cast.getProgressTicks() < maxProgressTicks) {
            cast.addProgress(1);
        }
        if (cast.getProgressTicks() % 20 == 0) {
            int percent = (int) ((cast.getProgressTicks() * 100L) / duration);
            if (percent != cast.getLastAnnouncedPercent()) {
                cast.setLastAnnouncedPercent(percent);
                player.sendSystemMessage(Component.literal("Incantation chargée à " + percent + "%"));
            }
        }
    }
}