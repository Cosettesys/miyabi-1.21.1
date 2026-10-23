package net.cosette.miyabi.skills.magic.water;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.learning.LearnMethod;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellCategory;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellShape;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;
import net.cosette.miyabi.skills.magic.status.ElementStatus;

/** Sort de test : mouille sa cible (10 s). La foudre fait ensuite plus de dégâts à une cible mouillée. */
public final class WetTest {
    private WetTest() {}
    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "wet_test", MagicSchool.WATER, null, null, MagicRank.BEGINNER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(2.0f, PhysicalDamageType.NONE, ElementStatus.WET, 200),
            1.5f, 0.35f, 40, 10f, 300f,
            "Une gerbe d'eau qui trempe sa cible.",
            "Eau, enveloppe ma cible et ne laisse rien de sec.",
            200,
            LearnMethod.SCROLL
    );
}