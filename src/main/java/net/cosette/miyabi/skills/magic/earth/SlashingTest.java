package net.cosette.miyabi.skills.magic.earth;

import net.cosette.miyabi.magic.spell.DamageEffect;
import net.cosette.miyabi.magic.spell.MagicSchool;
import net.cosette.miyabi.magic.spell.PhysicalDamageType;
import net.cosette.miyabi.magic.spell.SpellCategory;
import net.cosette.miyabi.magic.spell.SpellDefinition;
import net.cosette.miyabi.magic.spell.SpellShape;
import net.cosette.miyabi.progression.MagicRank;

public final class SlashingTest {
    private SlashingTest() {}

    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "slashing_test", MagicSchool.EARTH, MagicRank.ADVANCED, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f, PhysicalDamageType.SLASHING),
            1.5f, 0.35f, 40, 10f, 300f
    );
}