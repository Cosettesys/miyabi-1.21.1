package net.cosette.miyabi.progression;

import net.cosette.miyabi.config.ConfigValue;
import net.cosette.miyabi.trainer.TrainerRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.util.Chat;

/**
 * Progression par l'usage. Aucune valeur chiffrée n'est montrée au joueur : seuls des messages au niveau de la hotbar
 */
public final class MasteryManager {
    private MasteryManager() {}
    @ConfigValue(value = "mastery.rank_points_per_cast", comment = "Points de maîtrise de rang gagnés par sort ou technique lancé au rang actuel.", min = 0)
    public static int MASTERY_PER_CAST = 1;
    @ConfigValue(value = "mastery.spell_casts_to_master", comment = "Nombre de lancers pour maîtriser un sort (coût et temps d'incantation au minimum).", min = 1)
    public static int SPELL_MASTERY_CASTS = 200;
    @ConfigValue(value = "mastery.technique_uses_to_master", comment = "Nombre d'utilisations pour maîtriser une technique (recharge au minimum).", min = 1)
    public static int TECHNIQUE_MASTERY_USES = 200;
    /** Appelée une fois par sort réellement lancé. */
    public static void onSpellCast(ServerPlayer player, SpellDefinition def) {
        PlayerProgressionData data = player.getData(ModAttachments.PROGRESSION);
        // Maîtrise du sort lui-même : compte tous les lancers, quel que soit le rang
        int casts = data.incrementSpellCasts(def.id());
        int castsToMaster = def.masteryCastsRequired(); // propre à chaque sort
        if (MasteryEffects.masterySteps(casts, castsToMaster) > MasteryEffects.masterySteps(casts - 1, castsToMaster)) {
            player.sendSystemMessage(Component.literal("Je me sens plus à l'aise avec " + Chat.schools(def.schools()) + " : " + Chat.good(def.id()) + "."));
        }
        if (casts == castsToMaster) {
            player.sendSystemMessage(Component.literal("Je " + Chat.good("maîtrise") + " désormais " + def.id() + "."));
        }
        // Maîtrise de rang : pour chaque école du sort où il est du rang actuel du joueur (un sort hybride fait progresser les deux)
        for (MagicSchool school : def.schools()) {
            MagicRank current = data.getSchoolRank(school);
            MagicRank next = current.next();
            if (def.rank() != current || next == null) continue;
            int points = data.getSchoolMastery(school) + MASTERY_PER_CAST;
            int required = current.getMasteryToNext();
            if (points >= required) {
                data.setSchoolRank(school, next);
                data.setSchoolMastery(school, points - required);
                player.sendSystemMessage(Component.literal(
                        "Rang " + Chat.school(school) + " : " + Chat.rank(current) + " -> " + Chat.rank(next) + " !"));
            } else {
                data.setSchoolMastery(school, points);
            }
        }
    }
    public static void onTechniqueUse(ServerPlayer player, SwordTechniqueDefinition technique) {
        PlayerProgressionData data = player.getData(ModAttachments.PROGRESSION);
        int uses = data.incrementTechniqueUses(technique.id());
        if (MasteryEffects.masterySteps(uses, TECHNIQUE_MASTERY_USES)
                > MasteryEffects.masterySteps(uses - 1, TECHNIQUE_MASTERY_USES)) {
            player.sendSystemMessage(Component.literal("Mes gestes sont " + Chat.good("plus fluides") + " avec " + Chat.style(technique.style()) + " : " + technique.id() + "."));
        }
        if (uses == TECHNIQUE_MASTERY_USES) {
            player.sendSystemMessage(Component.literal("Je " + Chat.good("maîtrise") + " désormais " + technique.id() + "."));
        }
        var style = technique.style();
        MagicRank current = data.getStyleRank(style);
        MagicRank next = current.next();
        if (technique.rank() != current || next == null) {
            return;
        }
        int points = data.getStyleMastery(style) + MASTERY_PER_CAST;
        int required = current.getMasteryToNext();
        if (points >= required) {
            if (TrainerRules.trialRequired(current)) {
                // À partir du rang Avancé, il faut réussir l'épreuve d'un entraîneur : on reste à un point du rang suivant.
                data.setStyleMastery(style, required - 1);
                player.displayClientMessage(Component.literal("Je dois passer une épreuve auprès d'un maître pour progresser dans ce style."), true);
                return;
            }
            if (!canRankUp(data, next)) {
                // Bloqué juste avant le palier, sans dire pourquoi précisément.
                data.setStyleMastery(style, required - 1);
                player.displayClientMessage(Component.literal("Je dois renforcer mon corps pour progresser dans ce style."), true);
                return;
            }
            data.setStyleRank(style, next);
            data.setStyleMastery(style, points - required);
            player.sendSystemMessage(Component.literal(
                    "Rang " + Chat.style(style) + " : " + Chat.rank(current) + " -> " + Chat.rank(next) + " !"));
        } else {
            data.setStyleMastery(style, points);
        }
    }
    private static boolean canRankUp(PlayerProgressionData data, MagicRank next) {
        return next != MagicRank.ADVANCED || data.isBattleAuraAwakened();
    }
}