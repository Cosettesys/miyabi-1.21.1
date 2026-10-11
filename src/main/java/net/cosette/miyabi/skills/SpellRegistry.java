package net.cosette.miyabi.skills;

import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.earth.SlashingTest;
import net.cosette.miyabi.skills.magic.fire.FireTestSpells;
import net.cosette.miyabi.skills.magic.fire.Fireball;
import net.cosette.miyabi.skills.magic.water.BluntTest;
import net.cosette.miyabi.skills.magic.water.WaterTestSpells;
import net.cosette.miyabi.skills.magic.wind.PiercingTest;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SpellRegistry {
    private SpellRegistry() {}

    private static final Map<String, SpellDefinition> BY_ID = new LinkedHashMap<>();

    // Un sort = une ligne ici. Chaque sort est défini dans le package de son école.
    static {
        register(Fireball.DEFINITION);
        register(BluntTest.DEFINITION);
        register(SlashingTest.DEFINITION);
        register(PiercingTest.DEFINITION);

        // Sorts factices pour tester le menu. À supprimer plus tard.
        FireTestSpells.all().forEach(SpellRegistry::register);
        WaterTestSpells.all().forEach(SpellRegistry::register);
    }

    private static void register(SpellDefinition def) {
        if (BY_ID.putIfAbsent(def.id(), def) != null) {
            throw new IllegalStateException("Id de sort en double : " + def.id());
        }
    }

    public static SpellDefinition byId(String id) {
        return BY_ID.get(id);
    }

    public static Collection<String> ids() {
        return BY_ID.keySet();
    }
}