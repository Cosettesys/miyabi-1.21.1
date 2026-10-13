package net.cosette.miyabi.progression;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;

/**
 * Progression par l'usage. Aucune valeur chiffrée n'est montrée au joueur : seuls des messages au niveau de la hotbar
 */
public final class MasteryManager {
    private MasteryManager() {}
    public static final int MASTERY_PER_CAST = 1;
    public static final int SPELL_MASTERY_CASTS = 200;
    public static final int TECHNIQUE_MASTERY_USES = 200;
    public static void onSpellCast(ServerPlayer player, SpellDefinition def) {
        PlayerProgressionData data = player.getData(ModAttachments.PROGRESSION);
        int casts = data.incrementSpellCasts(def.id());
        if (MasteryEffects.masterySteps(casts) > MasteryEffects.masterySteps(casts - 1)) {
            player.sendSystemMessage(Component.literal("Je me sens plus à l'aise avec " + def.id() + "."));
        }
        if (casts == SPELL_MASTERY_CASTS) {
            player.sendSystemMessage(Component.literal("Je maîtrise désormais " + def.id() + "."));
        }
        MagicSchool school = def.school();
        MagicRank current = data.getSchoolRank(school);
        MagicRank next = current.next();
        if (def.rank() != current || next == null) {
            return;
        }
        int points = data.getSchoolMastery(school) + MASTERY_PER_CAST;
        int required = current.getMasteryToNext();
        if (points >= required) {
            data.setSchoolRank(school, next);
            data.setSchoolMastery(school, points - required);
            player.sendSystemMessage(Component.literal(
                    "Rang " + school.name() + " : " + current.name() + " -> " + next.name() + " !"));
        } else {
            data.setSchoolMastery(school, points);
        }
    }
    public static void onTechniqueUse(ServerPlayer player, SwordTechniqueDefinition technique) {
        PlayerProgressionData data = player.getData(ModAttachments.PROGRESSION);
        int uses = data.incrementTechniqueUses(technique.id());
        if (MasteryEffects.masterySteps(uses, TECHNIQUE_MASTERY_USES)
                > MasteryEffects.masterySteps(uses - 1, TECHNIQUE_MASTERY_USES)) {
            player.sendSystemMessage(Component.literal("Mes gestes sont plus fluides avec " + technique.id() + "."));
        }
        if (uses == TECHNIQUE_MASTERY_USES) {
            player.sendSystemMessage(Component.literal("Je maîtrise désormais " + technique.id() + "."));
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
            if (!canRankUp(data, next)) {
                // Bloqué juste avant le palier, sans dire pourquoi précisément.
                data.setStyleMastery(style, required - 1);
                player.displayClientMessage(Component.literal("Je dois renforcer mon corps pour progresser dans ce style."), true);
                return;
            }
            data.setStyleRank(style, next);
            data.setStyleMastery(style, points - required);
            player.sendSystemMessage(Component.literal(
                    "Rang " + style.name() + " : " + current.name() + " -> " + next.name() + " !"));
        } else {
            data.setStyleMastery(style, points);
        }
    }
    private static boolean canRankUp(PlayerProgressionData data, MagicRank next) {
        return next != MagicRank.ADVANCED || data.isBattleAuraAwakened();
    }
}