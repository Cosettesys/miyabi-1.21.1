package net.cosette.miyabi.skills;

import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.earth.SlashingTest;
import net.cosette.miyabi.skills.magic.fire.FireTestSpells;
import net.cosette.miyabi.skills.magic.fire.Fireball;
import net.cosette.miyabi.skills.magic.water.AquaStaffTest;
import net.cosette.miyabi.skills.magic.water.BluntTest;
import net.cosette.miyabi.skills.magic.water.WaterTestSpells;
import net.cosette.miyabi.skills.magic.wind.PiercingTest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SpellRegistry {
    private SpellRegistry() {}
    /** Sorts tels que définis dans le code : valeurs par défaut des fichiers de configuration. */
    private static final Map<String, SpellDefinition> BASE = new LinkedHashMap<>();
    /** Sorts réellement utilisés : les définitions du code avec les valeurs des fichiers de configuration. */
    private static volatile Map<String, SpellDefinition> current;
    // Un sort = une ligne ici. Chaque sort est défini dans le package de son école.
    static {
        register(Fireball.DEFINITION);
        register(BluntTest.DEFINITION);
        register(SlashingTest.DEFINITION);
        register(PiercingTest.DEFINITION);
        register(AquaStaffTest.DEFINITION);
        // Sorts factices pour tester le menu. À supprimer plus tard.
        FireTestSpells.all().forEach(SpellRegistry::register);
        WaterTestSpells.all().forEach(SpellRegistry::register);
        current = new LinkedHashMap<>(BASE);
    }
    private static void register(SpellDefinition def) {
        if (BASE.putIfAbsent(def.id(), def) != null) {
            throw new IllegalStateException("Id de sort en double : " + def.id());
        }
    }
    public static SpellDefinition byId(String id) {
        return current.get(id);
    }
    public static Collection<String> ids() {
        return current.keySet();
    }
    /** Définitions du code, sans la configuration. */
    public static Collection<SpellDefinition> baseDefinitions() {
        return Collections.unmodifiableCollection(BASE.values());
    }
    /** Remplace les définitions utilisées (appelé par le chargement de la configuration). */
    public static void applyConfig(Map<String, SpellDefinition> definitions) {
        current = new LinkedHashMap<>(definitions);
    }
}