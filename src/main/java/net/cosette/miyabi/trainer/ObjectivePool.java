package net.cosette.miyabi.trainer;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.cosette.miyabi.config.ConfigIO;
import net.cosette.miyabi.config.ConfigReport;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Liste de tous les objectifs que les entraîneurs peuvent tirer, lue dans config/miyabi/objectives.toml.
 * Le fichier est créé avec une liste par défaut ; ensuite c'est la liste du fichier qui compte (on peut en ajouter,
 * en retirer ou les modifier librement).
 */
public final class ObjectivePool {
    private ObjectivePool() {}
    private static volatile List<ObjectiveDefinition> objectives = List.of();
    public static List<ObjectiveDefinition> all() { return objectives; }
    public static ObjectiveDefinition byId(String id) {
        for (ObjectiveDefinition objective : objectives) {
            if (objective.id().equals(id)) return objective;
        }
        return null;
    }
    private static ObjectiveDefinition def(String id, ObjectiveType type, String target, int min, int max,
                                           MagicRank minRank, MagicRank maxRank, SwordStyle... styles) {
        return new ObjectiveDefinition(id, type, target, min, max, minRank, maxRank, List.of(styles), "");
    }
    /** Liste écrite dans le fichier quand il n'existe pas encore. */
    private static List<ObjectiveDefinition> defaults() {
        return List.of(
                def("kill_zombies", ObjectiveType.KILL_ENTITY, "minecraft:zombie", 100, 250, MagicRank.BEGINNER, MagicRank.GOD),
                def("kill_skeletons", ObjectiveType.KILL_ENTITY, "minecraft:skeleton", 80, 200, MagicRank.BEGINNER, MagicRank.GOD),
                def("kill_spiders", ObjectiveType.KILL_ENTITY, "minecraft:spider", 80, 200, MagicRank.BEGINNER, MagicRank.GOD),
                def("kill_undead", ObjectiveType.KILL_TAG, "minecraft:undead", 150, 400, MagicRank.INTERMEDIATE, MagicRank.GOD),
                def("kill_hostiles", ObjectiveType.KILL_HOSTILE, "", 200, 500, MagicRank.BEGINNER, MagicRank.GOD),
                def("kill_hostiles_unarmed", ObjectiveType.KILL_HOSTILE_UNARMED, "", 50, 150, MagicRank.BEGINNER, MagicRank.GOD, SwordStyle.NORTH_GOD),
                def("reach_aura_5", ObjectiveType.AURA_LEVEL, "", 5, 5, MagicRank.ADVANCED, MagicRank.SAINT),
                def("reach_aura_10", ObjectiveType.AURA_LEVEL, "", 10, 10, MagicRank.SAINT, MagicRank.KING),
                def("reach_aura_15", ObjectiveType.AURA_LEVEL, "", 15, 15, MagicRank.KING, MagicRank.GOD),
                def("master_sword_god_technique", ObjectiveType.MASTER_TECHNIQUE, "sword_god_test_1", 1, 1, MagicRank.BEGINNER, MagicRank.GOD, SwordStyle.SWORD_GOD)
        );
    }
    public static void load(Path file, ConfigReport report) {
        List<ObjectiveDefinition> loaded = new ArrayList<>();
        try (CommentedFileConfig config = ConfigIO.open(file)) {
            ConfigIO.getInt(config, "config_version", 1,
                    "Objectifs que les entraîneurs peuvent tirer au hasard. Un tableau [objectives.<id>] par objectif.\nLes entraîneurs (dossier trainers) disent combien d'objectifs tirer ; ici on liste tous les objectifs possibles.\nTu peux ajouter, retirer ou modifier des objectifs librement.\nRecharger sans redémarrer : /miyabi config reload", 1, 1, report);
            List<String> names = ConfigIO.tableNames(config, "objectives");
            if (names.isEmpty()) {
                // Premier lancement : on écrit la liste par défaut
                for (ObjectiveDefinition def : defaults()) {
                    loaded.add(read(config, def.id(), def, report));
                }
            } else {
                ObjectiveDefinition generic = def("", ObjectiveType.KILL_HOSTILE, "", 100, 100, MagicRank.BEGINNER, MagicRank.GOD);
                for (String id : names) {
                    loaded.add(read(config, id, generic, report));
                }
            }
            config.save();
            report.fileLoaded();
        } catch (Exception e) {
            report.warn(file.getFileName() + " illisible, liste d'objectifs par défaut utilisée : " + e.getMessage());
            loaded = new ArrayList<>(defaults());
        }
        objectives = List.copyOf(loaded);
    }
    private static ObjectiveDefinition read(CommentedConfig config, String id, ObjectiveDefinition def, ConfigReport report) {
        String section = "objectives." + id;
        String p = section + ".";
        ConfigIO.sectionComment(config, section, "Objectif " + id + ".");

        ObjectiveType type = ConfigIO.getEnum(config, p + "type", def.type(),
                "Type d'objectif : KILL_ENTITY (mob précis), KILL_TAG (classe de mobs), KILL_HOSTILE (tout monstre hostile),\nKILL_HOSTILE_UNARMED (monstre hostile à mains nues), MASTER_TECHNIQUE, LEARN_TECHNIQUE ou AURA_LEVEL.", report);
        String target = ConfigIO.getString(config, p + "target", def.target(),
                "Cible : identifiant du mob (minecraft:zombie), du tag de mobs (minecraft:undead) ou de la technique (sword_god_test_1).\nVide pour les types qui n'en ont pas besoin.", report);
        int min = ConfigIO.getInt(config, p + "count_min", def.countMin(),
                "Quantité minimale tirée : nombre de mobs, ou niveau d'aura. Mettre 1 pour une technique.", 1, 1e6, report);
        int max = ConfigIO.getInt(config, p + "count_max", def.countMax(),
                "Quantité maximale tirée (la quantité est choisie au hasard entre les deux).", 1, 1e6, report);
        MagicRank minRank = ConfigIO.getEnum(config, p + "min_rank", def.minRank(),
                "Rang visé minimal pour lequel cet objectif peut être tiré (pour passer Avancé -> Saint, le rang visé est Saint).", report);
        MagicRank maxRank = ConfigIO.getEnum(config, p + "max_rank", def.maxRank(),
                "Rang visé maximal pour lequel cet objectif peut être tiré.", report);
        List<String> styleNames = ConfigIO.getStringList(config, p + "styles", def.styles().stream().map(Enum::name).toList(),
                "Styles concernés : SWORD_GOD, NORTH_GOD, WATER_GOD. Liste vide = tous les styles.", report);
        String description = ConfigIO.getString(config, p + "description", def.description(),
                "Texte affiché au joueur ({count} = quantité). Vide = texte automatique.", report);
        List<SwordStyle> styles = new ArrayList<>();
        for (String name : styleNames) {
            try {
                styles.add(SwordStyle.valueOf(name.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                report.warn(p + "styles : style inconnu \"" + name + "\", ignoré");
            }
        }
        if (max < min) max = min;
        return new ObjectiveDefinition(id, type, target, min, max, minRank, maxRank, styles, description);
    }
}