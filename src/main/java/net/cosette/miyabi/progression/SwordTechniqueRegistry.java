package net.cosette.miyabi.progression;

import net.cosette.miyabi.magic.spell.SpellRegistry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SwordTechniqueRegistry {
    private SwordTechniqueRegistry() {}
    private static final Map<String, SwordTechniqueDefinition> BY_ID = new LinkedHashMap<>();
    // Techniques factices pour tester le menu. À remplacer par les vraies techniques.
    static {
        for (int i = 1; i <= 12; i++) register("sword_god_test_" + i, SwordStyle.SWORD_GOD, rankFor(i));
        for (int i = 1; i <= 3; i++) register("north_god_test_" + i, SwordStyle.NORTH_GOD, rankFor(i));
        for (int i = 1; i <= 3; i++) register("water_god_test_" + i, SwordStyle.WATER_GOD, rankFor(i));
    }
    private static MagicRank rankFor(int i) {
        MagicRank[] ranks = MagicRank.values();
        return ranks[(i - 1) % ranks.length];
    }
    private static void register(String id, SwordStyle style, MagicRank rank) {
        // Sorts et techniques partagent les mêmes slots : leurs ids ne doivent jamais se chevaucher.
        if (SpellRegistry.byId(id) != null) {
            throw new IllegalStateException("Id de technique déjà utilisé par un sort : " + id);
        }
        BY_ID.put(id, new SwordTechniqueDefinition(id, style, rank));
    }
    public static SwordTechniqueDefinition byId(String id) {
        return BY_ID.get(id);
    }
    public static Collection<SwordTechniqueDefinition> all() {
        return BY_ID.values();
    }
}