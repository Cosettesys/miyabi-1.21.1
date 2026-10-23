package net.cosette.miyabi.skills.magic.wind;

import net.cosette.miyabi.skills.magic.effect.DamageEffect;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.magic.SpellCategory;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellShape;
import net.cosette.miyabi.progression.MagicRank;

public final class PiercingTest {
    private PiercingTest() {}

    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "piercing_test", MagicSchool.WIND, MagicRank.BEGINNER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.PIERCING),
            1.5f, 0.35f, 40, 10f, 300f, 200
    );
}