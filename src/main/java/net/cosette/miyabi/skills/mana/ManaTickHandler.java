package net.cosette.miyabi.skills.mana;

import net.cosette.miyabi.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.cosette.miyabi.Miyabi;
import net.neoforged.neoforge.network.PacketDistributor;
import net.cosette.miyabi.network.payload.ManaSyncPayload;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ManaTickHandler {
    private ManaTickHandler() {}
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int regenType = player.level().getGameRules().getInt(MiyabiGameRules.MANA_REGEN_TYPE);
        ManaData mana = player.getData(ModAttachments.MANA);
        mana.tick(regenType);
        // Synchronise la barre de mana du client dès que quelque chose change (valeur, max ou éveil).
        boolean awakened = player.getData(ModAttachments.PROGRESSION).isManaAwakened();
        if (mana.needsSync(awakened)) {
            PacketDistributor.sendToPlayer(player, new ManaSyncPayload(mana.getCurrent(), mana.getMax(), awakened));
            mana.markSynced(awakened);
        }
        if (player.getData(ModAttachments.MANA_DEBUG).isEnabled() && player.level().getGameTime() % 20 == 0) {
            String status = mana.getTicksUntilNextRegen() > 0
                    ? "en cooldown (" + mana.getTicksUntilNextRegen() + " ticks)"
                    : "prêt à régénérer";
            player.sendSystemMessage(Component.literal(
                    "Mana : " + mana.getCurrent() + "/" + mana.getMax() + " — régén " + status));
        }
    }
}