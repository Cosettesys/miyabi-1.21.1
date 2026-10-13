package net.cosette.miyabi.skills.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;

import java.util.EnumSet;

public final class PositionLock {
    private PositionLock() {}
    /** Écart toléré avant de ramener le joueur (0,02 bloc au carré). */
    private static final double TOLERANCE_SQR = 0.0004;
    public static void hold(ServerPlayer player, double x, double y, double z) {
        if (player.distanceToSqr(x, y, z) < TOLERANCE_SQR) return;
        // La rotation est marquée "relative" : le client garde sa propre orientation au lieu de revenir
        // à celle que le serveur connaît, qui a un tick de retard.
        player.connection.teleport(x, y, z, player.getYRot(), player.getXRot(),
                EnumSet.of(RelativeMovement.X_ROT, RelativeMovement.Y_ROT));
    }
}