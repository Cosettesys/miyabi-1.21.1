package net.cosette.miyabi.trainer;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.cosette.miyabi.config.ConfigIO;
import net.cosette.miyabi.config.ConfigReport;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Règles de chaque entraîneur (un par style), lues dans config/miyabi/trainers/<style>.toml :
 * quelles techniques il enseigne (avec le rang requis et le nombre d'objectifs à tirer) et ses épreuves de rang.
 */
public final class TrainerConfig {
    private TrainerConfig() {}
    public record LearningRule(String techniqueId, boolean enabled, MagicRank requiredRank, int objectivesToRoll) {}
    public record RankUpRule(MagicRank targetRank, int objectivesToRoll, float timeLimitHours) {}
    public record StyleRules(Map<String, LearningRule> learning, Map<MagicRank, RankUpRule> rankUps) {}
    private static volatile Map<SwordStyle, StyleRules> rules = Map.of();
    public static StyleRules of(SwordStyle style) {
        StyleRules found = rules.get(style);
        return found != null ? found : new StyleRules(Map.of(), Map.of());
    }
    private static final int DEFAULT_OBJECTIVES = 3;
    private static final float DEFAULT_TIME_LIMIT_HOURS = 24f;
    public static void load(Path dir, ConfigReport report) {
        Map<SwordStyle, StyleRules> loaded = new EnumMap<>(SwordStyle.class);
        for (SwordStyle style : SwordStyle.values()) {
            Path file = dir.resolve(style.name().toLowerCase() + ".toml");
            Map<String, LearningRule> learning = new LinkedHashMap<>();
            Map<MagicRank, RankUpRule> rankUps = new EnumMap<>(MagicRank.class);
            try (CommentedFileConfig config = ConfigIO.open(file)) {
                ConfigIO.getInt(config, "config_version", 1,
                        "Entraîneur du style " + style.name() + ".\n[learning.<technique>] : techniques qu'il enseigne. [rankup.<rang>] : épreuve pour atteindre ce rang.\nLes objectifs sont tirés dans objectives.toml. Recharger sans redémarrer : /miyabi config reload", 1, 1, report);
                for (SwordTechniqueDefinition technique : SwordTechniqueRegistry.all()) {
                    if (technique.style() != style) continue;
                    String section = "learning." + technique.id();
                    String p = section + ".";
                    ConfigIO.sectionComment(config, section, "Technique " + technique.id() + ".");
                    boolean enabled = ConfigIO.getBoolean(config, p + "enabled", true,
                            "false : cet entraîneur n'enseigne pas cette technique.", report);
                    MagicRank required = ConfigIO.getEnum(config, p + "required_rank", technique.rank(),
                            "Rang minimum dans le style pour que l'entraîneur accepte d'enseigner cette technique.", report);
                    int roll = ConfigIO.getInt(config, p + "objectives_to_roll", DEFAULT_OBJECTIVES,
                            "Nombre d'objectifs tirés au hasard (dans objectives.toml, selon le rang de la technique) à accomplir avant de l'apprendre.", 0, 20, report);
                    learning.put(technique.id(), new LearningRule(technique.id(), enabled, required, roll));
                }
                for (MagicRank target : MagicRank.values()) {
                    if (target.ordinal() <= TrainerRules.trialFromRank().ordinal()) continue;
                    String section = "rankup." + target.name();
                    String p = section + ".";
                    ConfigIO.sectionComment(config, section, "Épreuve pour obtenir le rang " + target.name() + " : les objectifs tirés sont ceux du rang " + target.name() + ".");
                    int roll = ConfigIO.getInt(config, p + "objectives_to_roll", DEFAULT_OBJECTIVES,
                            "Nombre d'objectifs tirés au hasard pour cette épreuve.", 0, 20, report);
                    float hours = ConfigIO.getFloat(config, p + "time_limit_hours", DEFAULT_TIME_LIMIT_HOURS,
                            "Temps accordé pour réussir l'épreuve, en heures de jeu (24 = un jour de jeu).", 0.1, 10000, report);
                    rankUps.put(target, new RankUpRule(target, roll, hours));
                }
                config.save();
                report.fileLoaded();
            } catch (Exception e) {
                report.warn(file.getFileName() + " illisible, règles par défaut utilisées : " + e.getMessage());
                learning.clear();
                rankUps.clear();
                for (SwordTechniqueDefinition technique : SwordTechniqueRegistry.all()) {
                    if (technique.style() == style) {
                        learning.put(technique.id(), new LearningRule(technique.id(), true, technique.rank(), DEFAULT_OBJECTIVES));
                    }
                }
                for (MagicRank target : MagicRank.values()) {
                    if (target.ordinal() > TrainerRules.trialFromRank().ordinal()) {
                        rankUps.put(target, new RankUpRule(target, DEFAULT_OBJECTIVES, DEFAULT_TIME_LIMIT_HOURS));
                    }
                }
            }
            loaded.put(style, new StyleRules(learning, rankUps));
        }
        rules = loaded;
    }
}