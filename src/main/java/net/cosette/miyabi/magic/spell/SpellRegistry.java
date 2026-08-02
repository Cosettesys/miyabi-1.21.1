package net.cosette.miyabi.magic.spell;

import java.util.HashMap;
import java.util.Map;

public final class SpellRegistry {
    private SpellRegistry() {}
    private static final Map<String, SpellDefinition> BY_ID = new HashMap<>();
    private static final int TEST_DURATION_TICKS = 40;
    private static final float TEST_MANA_COST = 30f;
    private static final float TEST_MAX_INTENSITY = 300f;
    public static final SpellDefinition FIREBALL = register(new SpellDefinition(
            "fireball", SpellElement.FIRE, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f),
            1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
    ));
    public static final SpellDefinition BLUNT_TEST = register(new SpellDefinition(
            "blunt_test", SpellElement.WATER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.BLUNT),
            1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
    ));
    public static final SpellDefinition SLASHING_TEST = register(new SpellDefinition(
            "slashing_test", SpellElement.EARTH, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.SLASHING),
            1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
    ));
    public static final SpellDefinition PIERCING_TEST = register(new SpellDefinition(
            "piercing_test", SpellElement.AIR, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.PIERCING),
            1.5f, 0.35f, TEST_DURATION_TICKS, TEST_MANA_COST, TEST_MAX_INTENSITY
    ));
    private static SpellDefinition register(SpellDefinition def) {
        BY_ID.put(def.id(), def);
        return def;
    }
    public static SpellDefinition byId(String id) {
        return BY_ID.get(id);
    }
    public static SpellDefinition forSlot(int slot) {
        return switch (slot) {
            case 0 -> FIREBALL;
            case 1 -> BLUNT_TEST;
            case 2 -> SLASHING_TEST;
            case 3 -> PIERCING_TEST;
            default -> null;
        };
    }
}