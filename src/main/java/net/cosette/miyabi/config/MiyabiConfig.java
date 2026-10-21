package net.cosette.miyabi.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import net.cosette.miyabi.skills.magic.status.ElementalReactions;
import net.neoforged.fml.loading.FMLPaths;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.MasteryEffects;
import net.cosette.miyabi.progression.MasteryManager;
import net.cosette.miyabi.progression.MeditationManager;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import net.cosette.miyabi.skills.battleaura.BattleAuraBuffs;
import net.cosette.miyabi.skills.battleaura.BattleAuraLevels;
import net.cosette.miyabi.skills.battleaura.BattleAuraTracker;
import net.cosette.miyabi.skills.cast.CastManager;
import net.cosette.miyabi.skills.cast.StaffBonuses;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.learning.SpellLearning;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.skills.swordtechniques.TechniqueManager;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration du mod, dans le dossier config/miyabi :
 * <pre>
 * miyabi.toml        valeurs générales (mana, méditation, maîtrise, apprentissage, battle aura, rangs, types de dégâts...)
 * magic/*.toml       un fichier par école : les sorts et leurs statistiques
 * styles/*.toml      un fichier par style d'épée : les techniques et leurs statistiques
 * </pre>
 * Chaque fichier est créé avec les valeurs du code s'il n'existe pas, et complété si une valeur manque.
 * Un fichier illisible n'est jamais écrasé : les valeurs du code sont utilisées pour lui, et un avertissement est écrit.
 */
public final class MiyabiConfig {
    private MiyabiConfig() {}
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Classes dont les champs statiques annotés @ConfigValue sont réglables dans miyabi.toml. */
    private static final Class<?>[] BOUND_CLASSES = {
            ManaData.class, MeditationManager.class, CastManager.class, StaffBonuses.class,
            MasteryManager.class, MasteryEffects.class, SpellLearning.class,
            BattleAuraLevels.class, BattleAuraBuffs.class, BattleAuraTracker.class, TechniqueManager.class, ElementalReactions.class
    };
    private static final Map<String, String> SECTION_COMMENTS = new LinkedHashMap<>();
    static {
        SECTION_COMMENTS.put("mana", "Mana : régénération et plafond.");
        SECTION_COMMENTS.put("meditation", "Méditation et éveil du mana.");
        SECTION_COMMENTS.put("cast", "Incantation des sorts : silent cast et interruptions.");
        SECTION_COMMENTS.put("staffs", "Bâtons magiques.");
        SECTION_COMMENTS.put("mastery", "Maîtrise des sorts et des techniques (réductions de coût, de temps et de recharge).");
        SECTION_COMMENTS.put("learning", "Apprentissage des sorts : chances d'échec, vitesse et malus.");
        SECTION_COMMENTS.put("battle_aura", "Battle aura : seuils d'entraînement, renforcement du corps et fatigue.");
        SECTION_COMMENTS.put("sword_techniques", "Utilisation des techniques d'épée.");
        SECTION_COMMENTS.put("ranks", "Rangs : points de maîtrise pour passer au rang suivant et multiplicateur de dégâts des sorts.");
        SECTION_COMMENTS.put("physical_types", "Types de dégâts physiques : multiplicateur de dégâts, pénétration d'armure et usure de l'armure.");
    }
    public static Path directory() {
        return FMLPaths.CONFIGDIR.get().resolve("miyabi");
    }
    /** Charge (ou recharge) tous les fichiers. Appelé au démarrage et par /miyabi config reload. */
    public static synchronized ConfigReport reload() {
        ConfigReport report = new ConfigReport();
        Path dir = directory();
        loadGeneral(dir.resolve("miyabi.toml"), report);
        loadSpells(dir.resolve("magic"), report);
        loadTechniques(dir.resolve("styles"), report);
        for (String warning : report.warnings()) {
            LOGGER.warn("[Miyabi config] {}", warning);
        }
        LOGGER.info("[Miyabi config] {} fichier(s) chargé(s), {} valeur(s) ajoutée(s), {} avertissement(s)",
                report.files(), report.added(), report.warnings().size());
        return report;
    }
    private static void loadGeneral(Path file, ConfigReport report) {
        try (CommentedFileConfig config = ConfigIO.open(file)) {
            ConfigIO.getInt(config, "config_version", 1,
                    "Configuration générale de Miyabi. Les valeurs par défaut sont celles du mod.\nRecharger sans redémarrer : /miyabi config reload", 1, 1, report);
            for (Class<?> clazz : BOUND_CLASSES) {
                ConfigBinder.bind(config, clazz, report);
            }
            // Anciennes clés, renommées ou retirées
            config.remove("learning.self_damage_chance");
            config.remove("learning.self_damage_fraction");
            SECTION_COMMENTS.put("reactions", "Réactions entre éléments (par exemple la foudre sur une cible mouillée).");
            bindRanks(config, report);
            bindPhysicalTypes(config, report);
            SECTION_COMMENTS.forEach((section, comment) -> {
                if (config.contains(section)) {
                    ConfigIO.sectionComment(config, section, comment);
                }
            });
            config.save();
            report.fileLoaded();
        } catch (Exception e) {
            report.warn(file.getFileName() + " illisible, valeurs actuelles conservées : " + e.getMessage());
        }
    }
    private static void bindRanks(CommentedConfig config, ConfigReport report) {
        for (MagicRank rank : MagicRank.values()) {
            String p = "ranks." + rank.name().toLowerCase() + ".";
            int toNext = ConfigIO.getInt(config, p + "mastery_to_next", rank.getDefaultMasteryToNext(),
                    "Points de maîtrise pour passer au rang suivant (0 pour le rang maximal).", 0, 1e9, report);
            float damage = ConfigIO.getFloat(config, p + "damage_multiplier", rank.getDefaultDamageMultiplier(),
                    "Multiplicateur de dégâts des sorts de l'école à ce rang.", 0, 1000, report);
            rank.configure(toNext, damage);
        }
    }
    private static void bindPhysicalTypes(CommentedConfig config, ConfigReport report) {
        for (PhysicalDamageType type : PhysicalDamageType.values()) {
            String p = "physical_types." + type.name().toLowerCase() + ".";
            float damage = ConfigIO.getFloat(config, p + "damage_multiplier", type.getDefaultDamageMultiplier(),
                    "Multiplicateur de dégâts.", 0, 1000, report);
            float penetration = ConfigIO.getFloat(config, p + "armor_penetration_percent", type.getDefaultArmorPenetrationPercent(),
                    "Part de l'armure ignorée, en pourcentage.", 0, 100, report);
            float durability = ConfigIO.getFloat(config, p + "armor_durability_multiplier", type.getDefaultArmorDurabilityMultiplier(),
                    "Multiplicateur de l'usure de l'armure de la cible.", 0, 1000, report);
            type.configure(damage, penetration, durability);
        }
    }
    private static void loadSpells(Path dir, ConfigReport report) {
        Map<String, SpellDefinition> loaded = new LinkedHashMap<>();
        for (MagicSchool school : MagicSchool.values()) {
            List<SpellDefinition> bases = SpellRegistry.baseDefinitions().stream()
                    .filter(spell -> spell.school() == school).toList();
            Path file = dir.resolve(school.name().toLowerCase() + ".toml");
            Map<String, SpellDefinition> fromFile = new LinkedHashMap<>();
            try (CommentedFileConfig config = ConfigIO.open(file)) {
                ConfigIO.getInt(config, "config_version", 1, "Magie " + school.name() + " : "
                        + (bases.isEmpty() ? "aucun sort pour l'instant." : "un tableau [spells.<id>] par sort.\nRecharger sans redémarrer : /miyabi config reload"), 1, 1, report);
                for (SpellDefinition base : bases) {
                    fromFile.put(base.id(), SpellConfig.apply(config, base, report));
                }
                config.save();
                report.fileLoaded();
            } catch (Exception e) {
                report.warn(file.getFileName() + " illisible, valeurs du code utilisées pour ses sorts : " + e.getMessage());
                fromFile.clear();
                bases.forEach(base -> fromFile.put(base.id(), base));
            }
            loaded.putAll(fromFile);
        }
        Map<String, SpellDefinition> ordered = new LinkedHashMap<>();
        for (SpellDefinition base : SpellRegistry.baseDefinitions()) {
            ordered.put(base.id(), loaded.getOrDefault(base.id(), base));
        }
        SpellRegistry.applyConfig(ordered);
    }
    private static void loadTechniques(Path dir, ConfigReport report) {
        Map<String, SwordTechniqueDefinition> loaded = new LinkedHashMap<>();
        for (SwordStyle style : SwordStyle.values()) {
            List<SwordTechniqueDefinition> bases = SwordTechniqueRegistry.baseDefinitions().stream()
                    .filter(technique -> technique.style() == style).toList();
            Path file = dir.resolve(style.name().toLowerCase() + ".toml");
            Map<String, SwordTechniqueDefinition> fromFile = new LinkedHashMap<>();
            try (CommentedFileConfig config = ConfigIO.open(file)) {
                ConfigIO.getInt(config, "config_version", 1, "Style " + style.name() + " : "
                        + (bases.isEmpty() ? "aucune technique pour l'instant." : "un tableau [techniques.<id>] par technique.\nRecharger sans redémarrer : /miyabi config reload"), 1, 1, report);
                for (SwordTechniqueDefinition base : bases) {
                    fromFile.put(base.id(), TechniqueConfig.apply(config, base, report));
                }
                config.save();
                report.fileLoaded();
            } catch (Exception e) {
                report.warn(file.getFileName() + " illisible, valeurs du code utilisées pour ses techniques : " + e.getMessage());
                fromFile.clear();
                bases.forEach(base -> fromFile.put(base.id(), base));
            }
            loaded.putAll(fromFile);
        }
        Map<String, SwordTechniqueDefinition> ordered = new LinkedHashMap<>();
        for (SwordTechniqueDefinition base : SwordTechniqueRegistry.baseDefinitions()) {
            ordered.put(base.id(), loaded.getOrDefault(base.id(), base));
        }
        SwordTechniqueRegistry.applyConfig(ordered);
    }
}