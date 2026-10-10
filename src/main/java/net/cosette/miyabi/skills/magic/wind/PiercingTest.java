package net.cosette.miyabi.skills.magic.wind;

import net.cosette.miyabi.magic.spell.DamageEffect;
import net.cosette.miyabi.magic.spell.MagicSchool;
import net.cosette.miyabi.magic.spell.PhysicalDamageType;
import net.cosette.miyabi.magic.spell.SpellCategory;
import net.cosette.miyabi.magic.spell.SpellDefinition;
import net.cosette.miyabi.magic.spell.SpellShape;
import net.cosette.miyabi.progression.MagicRank;

public final class PiercingTest {
    private PiercingTest() {}

    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "piercing_test", MagicSchool.WIND, MagicRank.BEGINNER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.PIERCING),
            1.5f, 0.35f, 40, 10f, 300f
    );
}