package net.cosette.miyabi.skills.cast;

import net.cosette.miyabi.registry.ModAttachments;
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
        CastData cast = player.getData(ModAttachments.CAST);
        if (!cast.isCasting()) return;
        if (!cast.isSilentAtStart()) {
            PositionLock.hold(player, cast.getAnchorX(), cast.getAnchorY(), cast.getAnchorZ());
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
                ManaData mana = player.getData(ModAttachments.MANA);
                manaLimitedIntensity = (mana.getCurrent() / cost) * 100f;
            }
        }
        float achievableIntensity = Math.max(100f, Math.min(maxIntensity, manaLimitedIntensity));
        int maxProgressTicks = Math.round(duration * (achievableIntensity / 100f));

        // Au-delà de 100 %, la charge ne monte que si le joueur est accroupi : on peut ainsi lancer à 100 % pile.
        int limit = player.isShiftKeyDown() ? maxProgressTicks : Math.min(maxProgressTicks, duration);
        if (cast.getProgressTicks() < limit) {
            cast.addProgress(1);
        }
        // Message au-dessus de la hotbar, rafraîchi quatre fois par seconde pour qu'il reste affiché pendant la pause à 100 %.
        if (now % 5 == 0) {
            int percent = (int) ((cast.getProgressTicks() * 100L) / duration);
            player.displayClientMessage(Component.literal("Incantation chargée à " + percent + "%"), true);
        }
    }
}