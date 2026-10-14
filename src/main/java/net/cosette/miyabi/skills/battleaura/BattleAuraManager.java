package net.cosette.miyabi.skills.battleaura;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;

import java.util.ArrayList;
import java.util.List;

public final class BattleAuraManager {
    private BattleAuraManager() {}
    /** Avancement de 0 à 1 vers le niveau suivant, pour chaque critère (dans l'ordre de AuraCriterion). */
    public static List<Float> progress(PlayerProgressionData data) {
        List<Float> result = new ArrayList<>();
        int level = data.getBattleAuraLevel();
        for (AuraCriterion criterion : AuraCriterion.values()) {
            if (level >= BattleAuraLevels.MAX_LEVEL) {
                result.add(1f);
                continue;
            }
            double needed = BattleAuraLevels.threshold(level + 1, criterion);
            result.add((float) Math.min(1.0, data.getAuraCounter(criterion) / needed));
        }
        return result;
    }
    public static int conditionsMet(PlayerProgressionData data, int targetLevel) {
        int met = 0;
        for (AuraCriterion criterion : AuraCriterion.values()) {
            if (data.getAuraCounter(criterion) >= BattleAuraLevels.threshold(targetLevel, criterion)) {
                met++;
            }
        }
        return met;
    }
    /** Vérifie si le joueur passe au niveau suivant. Sans mana éveillé, la battle aura ne peut pas progresser. */
    public static void checkLevelUp(ServerPlayer player) {
        PlayerProgressionData data = player.getData(ModAttachments.PROGRESSION);
        if (!data.isManaAwakened()) return;

        int level = data.getBattleAuraLevel();
        if (level >= BattleAuraLevels.MAX_LEVEL) return;

        int target = level + 1;
        if (conditionsMet(data, target) >= BattleAuraLevels.REQUIRED_CONDITIONS) {
            data.setBattleAuraLevel(target);
            player.sendSystemMessage(Component.literal(
                    target == 1 ? "Je sens mon corps plus fort maintenant." : "Mon corps se renforce."));
        }
    }
}