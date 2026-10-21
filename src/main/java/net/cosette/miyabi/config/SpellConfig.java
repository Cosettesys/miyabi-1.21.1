package net.cosette.miyabi.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;
import net.cosette.miyabi.skills.magic.effect.HealEffect;
import net.cosette.miyabi.skills.magic.effect.SpellEffect;

/** Lit (et complète) la section d'un sort dans le fichier de son école, et renvoie le sort avec ses valeurs. */
public final class SpellConfig {
    private SpellConfig() {}
    public static SpellDefinition apply(CommentedConfig config, SpellDefinition base, ConfigReport report) {
        String section = "spells." + base.id();
        String p = section + ".";
        String schools = base.secondarySchool() == null
                ? "école " + base.school().name()
                : "écoles " + base.school().name() + " + " + base.secondarySchool().name() + ", listé ici sous l'école principale";
        ConfigIO.sectionComment(config, section, "Sort " + base.id() + " (" + schools + ").");
        // Anciennes clés, retirées : la vitesse et la taille du projectile ne sont plus réglables
        config.remove(p + "projectile_speed");
        config.remove(p + "projectile_size");
        MagicRank rank = ConfigIO.getEnum(config, p + "rank", base.rank(),
                "Rang du sort : BEGINNER, INTERMEDIATE, ADVANCED, SAINT, KING, EMPEROR ou GOD.\nIl faut avoir ce rang dans l'école (dans chacune pour un sort hybride) pour lancer ou apprendre le sort.", report);
        float manaCost = ConfigIO.getFloat(config, p + "mana_cost", base.manaCost(),
                "Coût en mana à 100 % d'intensité, avant les réductions de maîtrise et de bâton.", 0, 1e9, report);
        float castSeconds = ConfigIO.getFloat(config, p + "cast_time_seconds", base.baseCastDurationTicks() / 20f,
                "Temps d'incantation de base en secondes, avant maîtrise, silent cast et bâton.", 0.05, 3600, report);
        float maxIntensity = ConfigIO.getFloat(config, p + "max_intensity_percent", base.maxIntensityPercent(),
                "Intensité maximale qu'on peut charger, en pourcentage (300 = trois fois le coût et l'effet).", 100, 100000, report);
        SpellEffect effect = base.effect();
        if (effect instanceof DamageEffect damage) {
            float value = ConfigIO.getFloat(config, p + "damage", damage.baseDamage(),
                    "Dégâts de base à 100 % d'intensité, avant les multiplicateurs de rang, de forme et d'école.", 0, 1e9, report);
            PhysicalDamageType type = ConfigIO.getEnum(config, p + "physical_type", damage.physicalType(),
                    "Type de dégâts physiques : NONE, BLUNT, SLASHING ou PIERCING (voir physical_types dans miyabi.toml).", report);
            int statusTicks = damage.statusTicks();
            if (damage.status() != null) {
                float seconds = ConfigIO.getFloat(config, p + "status_duration_seconds", damage.statusTicks() / 20f,
                        "Durée de l'état posé sur la cible (" + damage.status().name() + "), en secondes.", 0, 3600, report);
                statusTicks = Math.round(seconds * 20f);
            }
            effect = new DamageEffect(value, type, damage.status(), statusTicks);
        } else if (effect instanceof HealEffect heal) {
            float value = ConfigIO.getFloat(config, p + "heal", heal.baseHeal(),
                    "Soin de base à 100 % d'intensité.", 0, 1e9, report);
            effect = new HealEffect(value);
        }
        String description = ConfigIO.getString(config, p + "description", base.description(),
                "Description affichée dans le menu. Laisser vide pour ne rien afficher.", report);
        String incantation = ConfigIO.getString(config, p + "incantation", base.incantation(),
                "Incantation (la formule récitée) affichée dans le menu. Peut être longue pour un sort avancé. Laisser vide pour ne rien afficher.", report);
        return new SpellDefinition(base.id(), base.school(), base.secondarySchool(), base.element(), rank, base.shape(),
                base.category(), effect, base.baseSpeed(), base.baseSize(), Math.max(1, Math.round(castSeconds * 20f)),
                manaCost, maxIntensity, description, incantation);
    }
}