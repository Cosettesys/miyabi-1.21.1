package net.cosette.miyabi.skills.magic.hybrid;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.learning.LearnMethod;
import net.cosette.miyabi.skills.magic.DamageElement;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellCategory;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellShape;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;

/** Sort de test hybride : la foudre, née du vent et de l'eau. Dégâts de foudre, résistance moyenne vent/eau. */
public final class LightningTest {
    private LightningTest() {}
    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "lightning_test", MagicSchool.WIND, MagicSchool.WATER, DamageElement.LIGHTNING,
            MagicRank.BEGINNER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(4.0f, PhysicalDamageType.NONE),
            1.5f, 0.35f, 40, 12f, 300f,
            "Un éclair né de l'union du vent et de l'eau. Frappe plus fort une cible mouillée.",
            "Vent qui gronde, eau qui ruisselle, unissez-vous et frappez d'un seul éclat.",
            600,
            LearnMethod.SCROLL
    );
}