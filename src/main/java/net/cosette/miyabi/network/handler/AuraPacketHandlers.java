package net.cosette.miyabi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.client.hud.ClientAuraState;
import net.cosette.miyabi.network.payload.AuraInputPayload;
import net.cosette.miyabi.network.payload.AuraStatePayload;
import net.cosette.miyabi.skills.battleaura.BattleAuraBuffManager;

public final class AuraPacketHandlers {
    private AuraPacketHandlers() {}
    // Serveur
    public static void onAuraInput(AuraInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            switch (payload.action()) {
                case AuraInputPayload.ACTION_TOGGLE -> BattleAuraBuffManager.toggle(player);
                case AuraInputPayload.ACTION_SCROLL -> BattleAuraBuffManager.scroll(player, Integer.signum(payload.value()));
                default -> {}
            }
        });
    }
    // Client. ClientAuraState n'utilise aucune classe Minecraft client : sûr même sur un serveur dédié.
    public static void onAuraState(AuraStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientAuraState.update(payload.awakened(), payload.state()));
    }
}