package net.cosette.miyabi.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.magic.CastData;
import net.cosette.miyabi.magic.CastManager;
import net.cosette.miyabi.magic.SilentCastData;
import net.cosette.miyabi.magic.SpellSlots;

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
                case CastInputPayload.ACTION_STOP -> CastManager.cancelCast(player, cast, "relâchée");
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