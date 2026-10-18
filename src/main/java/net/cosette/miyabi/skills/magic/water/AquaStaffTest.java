package net.cosette.miyabi.skills.magic.water;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.magic.SpellCategory;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellShape;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;

/** Sort de test pour les bâtons : 1000 mana et 30 s d'incantation de base (200 mana et 15 s avec Aqua Heartia). */
public final class AquaStaffTest {
    private AquaStaffTest() {}
    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "aqua_staff_test", MagicSchool.WATER, MagicRank.SAINT, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(12.0f),
            1.5f, 0.35f, 600, 1000f, 300f
    );
}