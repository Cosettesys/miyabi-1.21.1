package net.cosette.miyabi.skills;

import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.skills.swordtechniques.northgod.NorthGodTestTechniques;
import net.cosette.miyabi.skills.swordtechniques.swordgod.SwordGodTestTechniques;
import net.cosette.miyabi.skills.swordtechniques.watergod.WaterGodTestTechniques;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SwordTechniqueRegistry {
    private SwordTechniqueRegistry() {}
    /** Techniques telles que définies dans le code : valeurs par défaut des fichiers de configuration. */
    private static final Map<String, SwordTechniqueDefinition> BASE = new LinkedHashMap<>();
    /** Techniques réellement utilisées : les définitions du code avec les valeurs des fichiers de configuration. */
    private static volatile Map<String, SwordTechniqueDefinition> current;
    // Une technique = une ligne ici (ou une liste). Chaque technique est définie dans le package de son style.
    static {
        // Techniques factices pour tester le menu. À remplacer par les vraies techniques.
        SwordGodTestTechniques.all().forEach(SwordTechniqueRegistry::register);
        NorthGodTestTechniques.all().forEach(SwordTechniqueRegistry::register);
        WaterGodTestTechniques.all().forEach(SwordTechniqueRegistry::register);
        current = new LinkedHashMap<>(BASE);
    }
    private static void register(SwordTechniqueDefinition def) {
        // Sorts et techniques partagent les mêmes slots : leurs ids ne doivent jamais se chevaucher.
        if (SpellRegistry.byId(def.id()) != null || BASE.putIfAbsent(def.id(), def) != null) {
            throw new IllegalStateException("Id de technique déjà utilisé : " + def.id());
        }
    }
    public static SwordTechniqueDefinition byId(String id) {
        return current.get(id);
    }
    public static Collection<SwordTechniqueDefinition> all() {
        return current.values();
    }
    /** Définitions du code, sans la configuration. */
    public static Collection<SwordTechniqueDefinition> baseDefinitions() {
        return Collections.unmodifiableCollection(BASE.values());
    }
    /** Remplace les définitions utilisées (appelé par le chargement de la configuration). */
    public static void applyConfig(Map<String, SwordTechniqueDefinition> definitions) {
        current = new LinkedHashMap<>(definitions);
    }
}