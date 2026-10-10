package net.cosette.miyabi.skills.magic.fire;

import net.cosette.miyabi.magic.spell.DamageEffect;
import net.cosette.miyabi.magic.spell.MagicSchool;
import net.cosette.miyabi.magic.spell.SpellCategory;
import net.cosette.miyabi.magic.spell.SpellDefinition;
import net.cosette.miyabi.magic.spell.SpellShape;
import net.cosette.miyabi.progression.MagicRank;

import java.util.ArrayList;
import java.util.List;

/** Sorts de feu factices pour tester le menu. À supprimer plus tard. */
public final class FireTestSpells {
    private FireTestSpells() {}

    public static List<SpellDefinition> all() {
        List<SpellDefinition> list = new ArrayList<>();
        MagicRank[] ranks = MagicRank.values();
        for (int i = 1; i <= 20; i++) {
            list.add(new SpellDefinition(
                    "fire_test_" + i, MagicSchool.FIRE, ranks[(i - 1) % ranks.length],
                    SpellShape.SPHERE, SpellCategory.PROJECTILE,
                    new DamageEffect(3.0f),
                    1.5f, 0.35f, 40, 10f, 300f));
        }
        return list;
    }
}