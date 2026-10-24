package net.cosette.miyabi.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lecture et écriture de fichiers TOML. Chaque lecture d'une valeur est aussi une écriture : si la clé manque, la valeur
 * par défaut est ajoutée au fichier avec son commentaire, ce qui garde les fichiers complets quand le mod évolue.
 * Une valeur invalide est ignorée avec un avertissement (le fichier de l'utilisateur n'est pas modifié pour autant).
 */
public final class ConfigIO {
    private ConfigIO() {}
    /** Ouvre (et crée si besoin, dossiers compris) puis charge un fichier TOML. Lève une exception si le TOML est invalide. */
    public static CommentedFileConfig open(Path file) {
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        CommentedFileConfig config = CommentedFileConfig.builder(file, TomlFormat.instance())
                .sync() // écritures immédiates et dans l'ordre, pas dans un thread à part
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .onFileNotFound(FileNotFoundAction.CREATE_EMPTY)
                .build();
        config.load();
        return config;
    }
    /** Les commentaires TOML commencent par "# " : on ajoute l'espace de tête à chaque ligne. */
    private static String format(String comment) {
        StringBuilder out = new StringBuilder();
        for (String line : comment.split("\n")) {
            if (out.length() > 0) out.append('\n');
            out.append(' ').append(line);
        }
        return out.toString();
    }
    private static void describe(CommentedConfig config, String path, String comment) {
        if (comment != null && !comment.isEmpty()) {
            config.setComment(path, format(comment));
        }
    }
    public static void sectionComment(CommentedConfig config, String path, String comment) {
        describe(config, path, comment);
    }
    /** Noms des sous-tableaux d'un tableau (par exemple les sections [objectives.xxx] de "objectives"). */
    public static List<String> tableNames(CommentedConfig config, String path) {
        List<String> names = new ArrayList<>();
        Object raw = config.get(path);
        if (raw instanceof UnmodifiableConfig table) {
            for (Map.Entry<String, Object> entry : table.valueMap().entrySet()) {
                if (entry.getValue() instanceof UnmodifiableConfig) {
                    names.add(entry.getKey());
                }
            }
        }
        return names;
    }

    public static List<String> getStringList(CommentedConfig config, String path, List<String> def, String comment, ConfigReport report) {
        Object raw = config.get(path);
        List<String> value = def;
        if (raw == null) {
            config.set(path, new ArrayList<>(def));
            report.valueAdded();
        } else if (raw instanceof List<?> list) {
            value = list.stream().map(String::valueOf).toList();
        } else {
            report.warn(path + " : liste de textes attendue, valeur par défaut utilisée (" + raw + ")");
        }
        describe(config, path, comment);
        return value;
    }
    public static double getDouble(CommentedConfig config, String path, double def, String comment,
                                   double min, double max, ConfigReport report) {
        Object raw = config.get(path);
        double value = def;
        if (raw == null) {
            config.set(path, def);
            report.valueAdded();
        } else if (raw instanceof Number number) {
            value = number.doubleValue();
            if (value < min || value > max) {
                report.warn(path + " : " + value + " hors de [" + min + " ; " + max + "], valeur bornée");
                value = Math.max(min, Math.min(max, value));
            }
        } else {
            report.warn(path + " : nombre attendu, valeur par défaut utilisée (" + raw + ")");
        }
        describe(config, path, comment);
        return value;
    }
    public static float getFloat(CommentedConfig config, String path, float def, String comment,
                                 double min, double max, ConfigReport report) {
        // Float.toString évite d'écrire 0.10000000149011612 pour 0.1f
        Object raw = config.get(path);
        if (raw == null) {
            config.set(path, Double.parseDouble(Float.toString(def)));
            report.valueAdded();
            describe(config, path, comment);
            return def;
        }
        return (float) getDouble(config, path, def, comment, min, max, report);
    }
    public static int getInt(CommentedConfig config, String path, int def, String comment,
                             double min, double max, ConfigReport report) {
        Object raw = config.get(path);
        int value = def;
        if (raw == null) {
            config.set(path, def);
            report.valueAdded();
        } else if (raw instanceof Number number) {
            if (number.doubleValue() != Math.rint(number.doubleValue())) {
                report.warn(path + " : entier attendu, " + raw + " arrondi");
            }
            value = (int) Math.round(number.doubleValue());
            if (value < min || value > max) {
                report.warn(path + " : " + value + " hors de [" + min + " ; " + max + "], valeur bornée");
                value = (int) Math.max(min, Math.min(max, value));
            }
        } else {
            report.warn(path + " : entier attendu, valeur par défaut utilisée (" + raw + ")");
        }
        describe(config, path, comment);
        return value;
    }
    public static boolean getBoolean(CommentedConfig config, String path, boolean def, String comment, ConfigReport report) {
        Object raw = config.get(path);
        boolean value = def;
        if (raw == null) {
            config.set(path, def);
            report.valueAdded();
        } else if (raw instanceof Boolean bool) {
            value = bool;
        } else {
            report.warn(path + " : true ou false attendu, valeur par défaut utilisée (" + raw + ")");
        }
        describe(config, path, comment);
        return value;
    }
    public static String getString(CommentedConfig config, String path, String def, String comment, ConfigReport report) {
        Object raw = config.get(path);
        String value = def;
        if (raw == null) {
            config.set(path, def);
            report.valueAdded();
        } else if (raw instanceof String string) {
            value = string;
        } else {
            report.warn(path + " : texte attendu, valeur par défaut utilisée (" + raw + ")");
        }
        describe(config, path, comment);
        return value;
    }
    public static <E extends Enum<E>> E getEnum(CommentedConfig config, String path, E def, String comment, ConfigReport report) {
        Object raw = config.get(path);
        E value = def;
        if (raw == null) {
            config.set(path, def.name());
            report.valueAdded();
        } else if (raw instanceof String string) {
            try {
                value = Enum.valueOf(def.getDeclaringClass(), string.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                report.warn(path + " : valeur inconnue \"" + string + "\", valeur par défaut utilisée");
            }
        } else {
            report.warn(path + " : texte attendu, valeur par défaut utilisée (" + raw + ")");
        }
        describe(config, path, comment);
        return value;
    }
}