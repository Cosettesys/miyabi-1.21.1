package net.cosette.miyabi.skills.cast;

import net.cosette.miyabi.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.cosette.miyabi.Miyabi;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class CastDamageHandler {
    private CastDamageHandler() {}
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CastData cast = player.getData(ModAttachments.CAST);
        if (cast.isCasting()) {
            CastManager.interruptByDamage(player, cast);
        }
    }
}