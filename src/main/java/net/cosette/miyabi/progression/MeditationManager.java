package net.cosette.miyabi.progression;

import net.cosette.miyabi.config.ConfigValue;
import net.cosette.miyabi.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.skills.cast.CastData;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.util.Chat;

public final class MeditationManager {
    private MeditationManager() {}
    /** Chance par seconde de progresser. */
    @ConfigValue(value = "meditation.success_chance", comment = "Chance par seconde de progresser dans l'éveil du mana en méditant (0 à 1).", min = 0, max = 1)
    public static float SUCCESS_CHANCE = 0.10f;
    // Avant l'éveil
    @ConfigValue(value = "meditation.awakening_threshold", comment = "Progression d'éveil à atteindre pour éveiller le mana.", min = 1)
    public static float AWAKENING_THRESHOLD = 100f;
    @ConfigValue(value = "meditation.awakening_progress_per_success", comment = "Progression gagnée à chaque réussite.", min = 0)
    public static float AWAKENING_PROGRESS_PER_SUCCESS = 5f;
    @ConfigValue(value = "meditation.initial_max_mana", comment = "Mana maximum obtenu à l'éveil.", min = 1)
    public static float INITIAL_MAX_MANA = 20f;
    // Après l'éveil
    @ConfigValue(value = "meditation.mana_cost_per_second", comment = "Mana dépensé par seconde de méditation (une fois éveillé).", min = 0)
    public static float MANA_COST_PER_SECOND = 2f;
    @ConfigValue(value = "meditation.max_mana_gain_per_success", comment = "Mana maximum gagné à chaque réussite de méditation (une fois éveillé).", min = 0)
    public static float MAX_MANA_GAIN_PER_SUCCESS = 1f;
    @ConfigValue(value = "mana.hard_cap", comment = "Plafond absolu du mana maximum, que la méditation et les dépenses de mana ne peuvent pas dépasser.", min = 1)
    public static float MANA_HARD_CAP = 500000f;
    public static final int KEEPALIVE_TIMEOUT_TICKS = 15;
    public static void start(ServerPlayer player, MeditationData med) {
        if (med.isMeditating()) return;
        if (player.getData(ModAttachments.CAST).isCasting()) return;
        if (!player.onGround()) {
            player.sendSystemMessage(Component.literal("Tu dois être " + Chat.bad("au sol") + " pour méditer."));
            return;
        }
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        if (progression.isManaAwakened()
                && player.getData(ModAttachments.MANA).getCurrent() < MANA_COST_PER_SECOND) {
            player.sendSystemMessage(Component.literal("Pas assez de " + Chat.mana("mana") + " pour méditer."));
            return;
        }
        var pos = player.position();
        med.start(player.level().getGameTime(), pos.x, pos.y, pos.z);
        player.sendSystemMessage(Component.literal("Tu commences à " + Chat.mana("méditer") + "."));
    }
    public static void keepAlive(ServerPlayer player, MeditationData med) {
        if (!med.isMeditating()) return;
        med.refresh(player.level().getGameTime());
    }
    public static void stop(ServerPlayer player, MeditationData med, String reason) {
        if (!med.isMeditating()) return;
        med.stop();
        player.sendSystemMessage(Component.literal("Méditation " + Chat.dim("terminée") + " (" + reason + ")."));
    }
    /** Appelée une fois par seconde pendant une méditation. */
    public static void secondTick(ServerPlayer player, MeditationData med) {
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        ManaData mana = player.getData(ModAttachments.MANA);
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
        player.sendSystemMessage(Component.literal("Ton " + Chat.mana("mana") + " s'" + Chat.good("éveille") + " !"));
    }
}