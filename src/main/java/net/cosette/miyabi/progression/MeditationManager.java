package net.cosette.miyabi.progression;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.magic.CastData;
import net.cosette.miyabi.magic.ManaData;

public final class MeditationManager {
    private MeditationManager() {}
    /** Chance par seconde de progresser. */
    public static final float SUCCESS_CHANCE = 0.10f;
    // Avant l'éveil
    public static final float AWAKENING_THRESHOLD = 100f;
    public static final float AWAKENING_PROGRESS_PER_SUCCESS = 5f;
    public static final float INITIAL_MAX_MANA = 20f;
    // Après l'éveil
    public static final float MANA_COST_PER_SECOND = 2f;
    public static final float MAX_MANA_GAIN_PER_SUCCESS = 1f;
    /** Plafond provisoire ; dépendra de plusieurs facteurs plus tard. */
    public static final float MANA_HARD_CAP = 500000f;
    public static final int KEEPALIVE_TIMEOUT_TICKS = 15;
    public static void start(ServerPlayer player, MeditationData med) {
        if (med.isMeditating()) return;
        if (player.getData(CastData.CAST).isCasting()) return;
        if (!player.onGround()) {
            player.sendSystemMessage(Component.literal("Tu dois être au sol pour méditer."));
            return;
        }
        PlayerProgressionData progression = player.getData(PlayerProgressionData.PROGRESSION);
        if (progression.isManaAwakened()
                && player.getData(ManaData.MANA).getCurrent() < MANA_COST_PER_SECOND) {
            player.sendSystemMessage(Component.literal("Pas assez de mana pour méditer."));
            return;
        }
        var pos = player.position();
        med.start(player.level().getGameTime(), pos.x, pos.y, pos.z);
        player.sendSystemMessage(Component.literal("Tu commences à méditer."));
    }
    public static void keepAlive(ServerPlayer player, MeditationData med) {
        if (!med.isMeditating()) return;
        med.refresh(player.level().getGameTime());
    }
    public static void stop(ServerPlayer player, MeditationData med, String reason) {
        if (!med.isMeditating()) return;
        med.stop();
        player.sendSystemMessage(Component.literal("Méditation terminée (" + reason + ")."));
    }
    /** Appelée une fois par seconde pendant une méditation. */
    public static void secondTick(ServerPlayer player, MeditationData med) {
        PlayerProgressionData progression = player.getData(PlayerProgressionData.PROGRESSION);
        ManaData mana = player.getData(ManaData.MANA);
        boolean success = player.getRandom().nextFloat() < SUCCESS_CHANCE;
        if (!progression.isManaAwakened()) {
            if (success) {
                progression.setAwakeningProgress(progression.getAwakeningProgress() + AWAKENING_PROGRESS_PER_SUCCESS);
            }
            if (progression.getAwakeningProgress() >= AWAKENING_THRESHOLD) {
                awaken(player, progression, mana);
                return;
            }
            int percent = (int) (progression.getAwakeningProgress() * 100f / AWAKENING_THRESHOLD);
            player.displayClientMessage(Component.literal("Méditation : " + percent + "%"), true);
            return;
        }
        if (mana.getCurrent() < MANA_COST_PER_SECOND) {
            stop(player, med, "mana épuisé");
            return;
        }
        mana.spend(MANA_COST_PER_SECOND); // remet aussi le cooldown de régén à zéro

        if (success && mana.getMax() < MANA_HARD_CAP) {
            mana.setMax(Math.min(MANA_HARD_CAP, mana.getMax() + MAX_MANA_GAIN_PER_SUCCESS));
        }
        player.displayClientMessage(Component.literal(
                "Méditation : mana " + String.format("%.0f/%.0f", mana.getCurrent(), mana.getMax())), true);
    }
    private static void awaken(ServerPlayer player, PlayerProgressionData progression, ManaData mana) {
        progression.setManaAwakened(true);
        mana.setMax(INITIAL_MAX_MANA);
        mana.setCurrent(INITIAL_MAX_MANA);
        player.sendSystemMessage(Component.literal("Ton mana s'éveille !"));
    }
}