package net.cosette.miyabi.skills.magic;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.magic.effect.SpellEffect;

import java.util.List;

/**
 * @param secondarySchool deuxième école d'un sort hybride (par exemple la foudre : vent + eau), ou null
 * @param element élément des dégâts pour les réactions, ou null pour le déduire de l'école principale
 * @param description texte affiché dans le menu (peut être vide)
 * @param incantation formule récitée, affichée dans le menu (peut être vide)
 */
public record SpellDefinition(
        String id,
        MagicSchool school,
        MagicSchool secondarySchool,
        DamageElement element,
        MagicRank rank,
        SpellShape shape,
        SpellCategory category,
        SpellEffect effect,
        float baseSpeed,
        float baseSize,
        int baseCastDurationTicks,
        float manaCost,
        float maxIntensityPercent,
        String description,
        String incantation
) {
    /** Sort d'une seule école, sans description ni incantation. */
    public SpellDefinition(String id, MagicSchool school, MagicRank rank, SpellShape shape, SpellCategory category,
                           SpellEffect effect, float baseSpeed, float baseSize, int baseCastDurationTicks,
                           float manaCost, float maxIntensityPercent) {
        this(id, school, null, null, rank, shape, category, effect, baseSpeed, baseSize,
                baseCastDurationTicks, manaCost, maxIntensityPercent, "", "");
    }
    /** Une école, ou deux pour un sort hybride. */
    public List<MagicSchool> schools() {
        return secondarySchool == null ? List.of(school) : List.of(school, secondarySchool);
    }
    public boolean hasSchool(MagicSchool other) {
        return school == other || secondarySchool == other;
    }
    public DamageElement damageElement() {
        return element != null ? element : DamageElement.of(school);
    }
    /** Un sort qui n'agit pas sur une cible extérieure (aura autour du lanceur) ne peut pas se retourner contre lui. */
    public boolean canBackfire() {
        return category != SpellCategory.AURA;
    }
    public float computeSpeed() {
        return baseSpeed * shape.getSpeedMultiplier();
    }
    public float computeSize() {
        return baseSize * shape.getSizeMultiplier();
    }
}