package net.cosette.miyabi.trainer;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;

import java.util.List;
/**
 * Un objectif possible, défini dans objectives.toml. Quand un entraîneur en tire un, la quantité est choisie au hasard
 * entre countMin et countMax.
 *
 * @param minRank rang visé minimal pour lequel l'objectif peut être tiré (pour passer Avancé -> Saint, le rang visé est Saint)
 * @param styles styles concernés ; liste vide = tous
 * @param description texte affiché ({count} = quantité), vide = texte automatique
 */
public record ObjectiveDefinition(String id, ObjectiveType type, String target, int countMin, int countMax,
                                  MagicRank minRank, MagicRank maxRank, List<SwordStyle> styles, String description) {
    /** L'objectif peut-il être tiré pour ce style et ce rang visé ? */
    public boolean isAvailableFor(SwordStyle style, MagicRank tier) {
        return tier.isAtLeast(minRank) && maxRank.isAtLeast(tier) && (styles.isEmpty() || styles.contains(style));
    }
}