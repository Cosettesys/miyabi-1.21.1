package net.cosette.miyabi.skills.magic.water;

import net.cosette.miyabi.skills.magic.effect.DamageEffect;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.magic.SpellCategory;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellShape;
import net.cosette.miyabi.progression.MagicRank;

public final class BluntTest {
    private BluntTest() {}

    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "blunt_test", MagicSchool.WATER, MagicRank.INTERMEDIATE, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.BLUNT),
            1.5f, 0.35f, 40, 10f, 300f
    );
}