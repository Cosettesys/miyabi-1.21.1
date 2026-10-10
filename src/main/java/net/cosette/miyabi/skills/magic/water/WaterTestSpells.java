package net.cosette.miyabi.skills.magic.water;

import net.cosette.miyabi.magic.spell.DamageEffect;
import net.cosette.miyabi.magic.spell.MagicSchool;
import net.cosette.miyabi.magic.spell.SpellCategory;
import net.cosette.miyabi.magic.spell.SpellDefinition;
import net.cosette.miyabi.magic.spell.SpellShape;
import net.cosette.miyabi.progression.MagicRank;

import java.util.ArrayList;
import java.util.List;

/** Sorts d'eau factices pour tester le menu. À supprimer plus tard. */
public final class WaterTestSpells {
    private WaterTestSpells() {}

    public static List<SpellDefinition> all() {
        List<SpellDefinition> list = new ArrayList<>();
        MagicRank[] ranks = MagicRank.values();
        for (int i = 1; i <= 2; i++) {
            list.add(new SpellDefinition(
                    "water_test_" + i, MagicSchool.WATER, ranks[(i - 1) % ranks.length],
                    SpellShape.SPHERE, SpellCategory.PROJECTILE,
                    new DamageEffect(3.0f),
                    1.5f, 0.35f, 40, 10f, 300f));
        }
        return list;
    }
}