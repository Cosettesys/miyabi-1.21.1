package net.cosette.miyabi.skills.magic.water;

import net.cosette.miyabi.magic.spell.DamageEffect;
import net.cosette.miyabi.magic.spell.MagicSchool;
import net.cosette.miyabi.magic.spell.PhysicalDamageType;
import net.cosette.miyabi.magic.spell.SpellCategory;
import net.cosette.miyabi.magic.spell.SpellDefinition;
import net.cosette.miyabi.magic.spell.SpellShape;
import net.cosette.miyabi.progression.MagicRank;

public final class BluntTest {
    private BluntTest() {}

    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "blunt_test", MagicSchool.WATER, MagicRank.INTERMEDIATE, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.BLUNT),
            1.5f, 0.35f, 40, 10f, 300f
    );
}