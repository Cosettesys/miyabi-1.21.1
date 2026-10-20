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
        ConfigIO.sectionComment(config, section, "Sort " + base.id() + " (école " + base.school().name() + ").");
        MagicRank rank = ConfigIO.getEnum(config, p + "rank", base.rank(),
                "Rang du sort : BEGINNER, INTERMEDIATE, ADVANCED, SAINT, KING, EMPEROR ou GOD.\nIl faut avoir ce rang dans l'école pour lancer ou apprendre le sort.", report);
        float manaCost = ConfigIO.getFloat(config, p + "mana_cost", base.manaCost(),
                "Coût en mana à 100 % d'intensité, avant les réductions de maîtrise et de bâton.", 0, 1e9, report);
        float castSeconds = ConfigIO.getFloat(config, p + "cast_time_seconds", base.baseCastDurationTicks() / 20f,
                "Temps d'incantation de base en secondes, avant maîtrise, silent cast et bâton.", 0.05, 3600, report);
        float maxIntensity = ConfigIO.getFloat(config, p + "max_intensity_percent", base.maxIntensityPercent(),
                "Intensité maximale qu'on peut charger, en pourcentage (300 = trois fois le coût et l'effet).", 100, 100000, report);
        float speed = ConfigIO.getFloat(config, p + "projectile_speed", base.baseSpeed(),
                "Vitesse de base du projectile.", 0.01, 100, report);
        float size = ConfigIO.getFloat(config, p + "projectile_size", base.baseSize(),
                "Taille de base du projectile.", 0.01, 100, report);
        SpellEffect effect = base.effect();
        if (effect instanceof DamageEffect damage) {
            float value = ConfigIO.getFloat(config, p + "damage", damage.baseDamage(),
                    "Dégâts de base à 100 % d'intensité, avant les multiplicateurs de rang, de forme et d'école.", 0, 1e9, report);
            PhysicalDamageType type = ConfigIO.getEnum(config, p + "physical_type", damage.physicalType(),
                    "Type de dégâts physiques : NONE, BLUNT, SLASHING ou PIERCING (voir physical_types dans miyabi.toml).", report);
            effect = new DamageEffect(value, type);
        } else if (effect instanceof HealEffect heal) {
            float value = ConfigIO.getFloat(config, p + "heal", heal.baseHeal(),
                    "Soin de base à 100 % d'intensité.", 0, 1e9, report);
            effect = new HealEffect(value);
        }

        return new SpellDefinition(base.id(), base.school(), rank, base.shape(), base.category(), effect,
                speed, size, Math.max(1, Math.round(castSeconds * 20f)), manaCost, maxIntensity);
    }
}