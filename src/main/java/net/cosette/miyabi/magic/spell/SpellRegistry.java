package net.cosette.miyabi.magic.spell;

import java.util.HashMap;
import java.util.Map;

public final class SpellRegistry {
    private SpellRegistry() {}
    private static final Map<String, SpellDefinition> BY_ID = new HashMap<>();
    public static final SpellDefinition FIREBALL = register(new SpellDefinition(
            "fireball", SpellElement.FIRE, SpellShape.SPHERE,
            /* baseDamage */ 3.0f, /* baseSpeed */ 1.5f, /* baseSize */ 0.35f
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
            default -> null;
        };
    }
}