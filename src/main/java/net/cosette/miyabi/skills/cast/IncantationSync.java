package net.cosette.miyabi.skills.cast;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.cosette.miyabi.network.payload.IncantationPayload;
import net.cosette.miyabi.skills.magic.SpellDefinition;

/** Envoie au joueur de quoi afficher l'incantation de son sort pendant la charge. */
public final class IncantationSync {
    private IncantationSync() {}
    /** Début (ou resynchronisation) : rien n'est récité en silent cast ni pour un sort sans incantation. */
    public static void start(ServerPlayer player, SpellDefinition def, boolean silent, int durationTicks, int progressTicks) {
        if (silent || def.incantation().isBlank()) return;
        PacketDistributor.sendToPlayer(player, new IncantationPayload(def.incantation(), durationTicks, progressTicks));
    }
    public static void clear(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new IncantationPayload("", 0, 0));
    }
}