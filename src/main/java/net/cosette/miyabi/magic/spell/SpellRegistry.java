package net.cosette.miyabi.magic.spell;

import net.cosette.miyabi.progression.MagicRank;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class SpellRegistry {
    private SpellRegistry() {}
    private static final Map<String, SpellDefinition> BY_ID = new HashMap<>();
    private static final int TEST_DURATION_TICKS = 40;
    private static final float TEST_MANA_COST = 10f;
    private static final float TEST_MAX_INTENSITY = 300f;
    public static final SpellDefinition FIREBALL = register(new SpellDefinition(
            "fireball", MagicSchool.FIRE, MagicRank.BEGINNER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f),
            1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
    ));
    public static final SpellDefinition BLUNT_TEST = register(new SpellDefinition(
            "blunt_test", MagicSchool.WATER, MagicRank.INTERMEDIATE, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.BLUNT),
            1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
    ));
    public static final SpellDefinition SLASHING_TEST = register(new SpellDefinition(
            "slashing_test", MagicSchool.EARTH, MagicRank.ADVANCED, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.SLASHING),
            1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
    ));
    public static final SpellDefinition PIERCING_TEST = register(new SpellDefinition(
            "piercing_test", MagicSchool.WIND, MagicRank.BEGINNER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.PIERCING),
            1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
    ));
    private static void registerFake(String id, MagicSchool school, MagicRank rank) {
        register(new SpellDefinition(
                id, school, rank, SpellShape.SPHERE, SpellCategory.PROJECTILE,
                new DamageEffect(3.0f),
                1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
        ));
    }
    // Sorts factices pour tester le menu (défilement, séparation par école). À supprimer plus tard.
    static {
        MagicRank[] ranks = MagicRank.values();
        for (int i = 1; i <= 12; i++) {
            registerFake("fire_test_" + i, MagicSchool.FIRE, ranks[(i - 1) % ranks.length]);
        }
        for (int i = 1; i <= 2; i++) {
            registerFake("water_test_" + i, MagicSchool.WATER, ranks[(i - 1) % ranks.length]);
        }
    }
    private static SpellDefinition register(SpellDefinition def) {
        BY_ID.put(def.id(), def);
        return def;
    }
    public static SpellDefinition byId(String id) {
        return BY_ID.get(id);
    }
    public static Collection<String> ids() {
        return BY_ID.keySet();
    }
}