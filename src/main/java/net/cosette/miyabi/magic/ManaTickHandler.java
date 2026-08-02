package net.cosette.miyabi.magic;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.cosette.miyabi.Miyabi;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ManaTickHandler {
    private ManaTickHandler() {}
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int regenType = player.level().getGameRules().getInt(MiyabiGameRules.MANA_REGEN_TYPE);
        ManaData mana = player.getData(ManaData.MANA);
        mana.tick(regenType);
        if (player.getData(ManaDebugData.MANA_DEBUG).isEnabled() && player.level().getGameTime() % 20 == 0) {
            String status = mana.getTicksUntilNextRegen() > 0
                    ? "en cooldown (" + mana.getTicksUntilNextRegen() + " ticks)"
                    : "prêt à régénérer";
            player.sendSystemMessage(Component.literal(
                    "Mana : " + mana.getCurrent() + "/" + mana.getMax() + " — régén " + status));
        }
    }
}