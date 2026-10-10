package net.cosette.miyabi.skills;

import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.skills.swordtechniques.northgod.NorthGodTestTechniques;
import net.cosette.miyabi.skills.swordtechniques.swordgod.SwordGodTestTechniques;
import net.cosette.miyabi.skills.swordtechniques.watergod.WaterGodTestTechniques;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SwordTechniqueRegistry {
    private SwordTechniqueRegistry() {}

    private static final Map<String, SwordTechniqueDefinition> BY_ID = new LinkedHashMap<>();

    // Une technique = une ligne ici (ou une liste). Chaque technique est définie dans le package de son style.
    static {
        // Techniques factices pour tester le menu. À remplacer par les vraies techniques.
        SwordGodTestTechniques.all().forEach(SwordTechniqueRegistry::register);
        NorthGodTestTechniques.all().forEach(SwordTechniqueRegistry::register);
        WaterGodTestTechniques.all().forEach(SwordTechniqueRegistry::register);
    }

    private static void register(SwordTechniqueDefinition def) {
        // Sorts et techniques partagent les mêmes slots : leurs ids ne doivent jamais se chevaucher.
        if (SpellRegistry.byId(def.id()) != null || BY_ID.putIfAbsent(def.id(), def) != null) {
            throw new IllegalStateException("Id de technique déjà utilisé : " + def.id());
        }
    }

    public static SwordTechniqueDefinition byId(String id) {
        return BY_ID.get(id);
    }

    public static Collection<SwordTechniqueDefinition> all() {
        return BY_ID.values();
    }
}