package net.cosette.miyabi.skills.magic.fire;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellCategory;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellShape;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;
import net.cosette.miyabi.skills.magic.status.ElementStatus;

/** Sort de test : brûle sa cible (10 s). Le vent fait ensuite plus de dégâts à une cible brûlée. */
public final class BurnTest {
    private BurnTest() {}
    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "burn_test", MagicSchool.FIRE, null, null, MagicRank.BEGINNER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(2.0f, PhysicalDamageType.NONE, ElementStatus.BURNED, 200),
            1.5f, 0.35f, 40, 10f, 300f,
            "Une flamme tenace qui laisse sa cible brûlée.",
            "Flamme, accroche-toi à ma cible et ne la lâche plus."
    );
}