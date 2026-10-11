package net.cosette.miyabi.network.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.skills.cast.CastData;
import net.cosette.miyabi.skills.cast.CastManager;
import net.cosette.miyabi.skills.cast.SilentCastData;
import net.cosette.miyabi.skills.cast.SpellSlots;
import net.cosette.miyabi.network.payload.CastInputPayload;
import net.cosette.miyabi.network.payload.SilentCastTogglePayload;

public final class CastPacketHandlers {
    private CastPacketHandlers() {}
    public static void onCastInput(CastInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (payload.slot() < 0 || payload.slot() >= SpellSlots.COUNT) return;
            CastData cast = player.getData(CastData.CAST);
            switch (payload.action()) {
                case CastInputPayload.ACTION_START -> CastManager.startCast(player, cast, payload.slot());
                case CastInputPayload.ACTION_KEEPALIVE -> CastManager.keepAlive(player, cast, payload.slot());
                case CastInputPayload.ACTION_STOP -> CastManager.release(player, cast);
                default -> {}
            }
        });
    }
    public static void onSilentCastToggle(SilentCastTogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            CastData cast = player.getData(CastData.CAST);
            if (cast.isCasting()) {
                player.sendSystemMessage(Component.literal("Impossible de changer le silent cast pendant une incantation."));
                return;
            }
            SilentCastData data = player.getData(SilentCastData.SILENT_CAST);
            if (!data.isUnlocked()) {
                player.sendSystemMessage(Component.literal("Silent cast non débloqué."));
                return;
            }
            data.setEnabled(!data.isEnabled());
            player.sendSystemMessage(Component.literal("Silent cast : " + (data.isEnabled() ? "activé" : "désactivé")));
        });
    }
}