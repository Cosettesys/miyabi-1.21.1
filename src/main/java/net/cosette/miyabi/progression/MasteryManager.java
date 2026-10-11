package net.cosette.miyabi.progression;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellDefinition;

public final class MasteryManager {
    private MasteryManager() {}
    public static final int MASTERY_PER_CAST = 1;
    public static final int SPELL_MASTERY_CASTS = 200;
    /** Appelée une fois par sort réellement lancé. */
    public static void onSpellCast(ServerPlayer player, SpellDefinition def) {
        PlayerProgressionData data = player.getData(PlayerProgressionData.PROGRESSION);
        // Maîtrise du sort lui-même : compte tous les lancers, quel que soit le rang
        int casts = data.incrementSpellCasts(def.id());
        int stepsNow = MasteryEffects.masterySteps(casts);
        if (stepsNow > MasteryEffects.masterySteps(casts - 1)) {
            player.sendSystemMessage(Component.literal("Maîtrise de " + def.id() + " : palier "
                    + stepsNow + "/" + MasteryEffects.STEPS + " (coût en mana et incantation réduits)."));
        }
        if (casts == SPELL_MASTERY_CASTS) {
            player.sendSystemMessage(Component.literal("Tu maîtrises désormais le sort " + def.id() + " !"));
        }
        // Maîtrise de rang : uniquement si le sort est du rang actuel dans son école
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
            player.displayClientMessage(Component.literal(
                    "Maîtrise " + school.name() + " : " + points + "/" + required), true);
        }
    }
}