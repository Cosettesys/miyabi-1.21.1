package net.cosette.miyabi.config;

import com.electronwill.nightconfig.core.CommentedConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/** Lie les champs statiques annotés @ConfigValue d'une classe à un fichier de configuration. */
public final class ConfigBinder {
    private ConfigBinder() {}
    /** Valeur du code de chaque champ, relevée au premier chargement : c'est le défaut, même après des rechargements. */
    private static final Map<Field, Object> CODE_DEFAULTS = new HashMap<>();
    public static void bind(CommentedConfig config, Class<?> clazz, ConfigReport report) {
        for (Field field : clazz.getDeclaredFields()) {
            ConfigValue annotation = field.getAnnotation(ConfigValue.class);
            if (annotation == null) continue;
            if (!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                report.warn(clazz.getSimpleName() + "." + field.getName() + " : doit être static et non final pour être configurable");
                continue;
            }
            try {
                field.setAccessible(true);
                Object def = CODE_DEFAULTS.computeIfAbsent(field, f -> {
                    try {
                        return f.get(null);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                });
                String path = annotation.value();
                String comment = annotation.comment();
                Class<?> type = field.getType();
                if (type == int.class) {
                    field.setInt(null, ConfigIO.getInt(config, path, (Integer) def, comment, annotation.min(), annotation.max(), report));
                } else if (type == float.class) {
                    field.setFloat(null, ConfigIO.getFloat(config, path, (Float) def, comment, annotation.min(), annotation.max(), report));
                } else if (type == double.class) {
                    field.setDouble(null, ConfigIO.getDouble(config, path, (Double) def, comment, annotation.min(), annotation.max(), report));
                } else if (type == boolean.class) {
                    field.setBoolean(null, ConfigIO.getBoolean(config, path, (Boolean) def, comment, report));
                } else if (type == String.class) {
                    field.set(null, ConfigIO.getString(config, path, (String) def, comment, report));
                } else {
                    report.warn(clazz.getSimpleName() + "." + field.getName() + " : type non pris en charge (" + type.getSimpleName() + ")");
                }
            } catch (IllegalAccessException e) {
                report.warn(clazz.getSimpleName() + "." + field.getName() + " : " + e.getMessage());
            }
        }
    }
}