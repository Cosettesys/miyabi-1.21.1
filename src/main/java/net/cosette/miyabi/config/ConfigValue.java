package net.cosette.miyabi.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque un champ statique non final comme réglable dans miyabi.toml.
 * La valeur du champ dans le code sert de valeur par défaut : elle est écrite dans le fichier s'il ne la contient pas encore.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {
    /** Chemin dans le fichier, par exemple "meditation.success_chance" (section "meditation", clé "success_chance"). */
    String value();
    /** Commentaire écrit au-dessus de la valeur dans le fichier. */
    String comment() default "";
    double min() default -Double.MAX_VALUE;
    double max() default Double.MAX_VALUE;
}