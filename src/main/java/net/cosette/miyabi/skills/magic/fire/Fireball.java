package net.cosette.miyabi.skills.magic.fire;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.learning.LearnMethod;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellCategory;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellShape;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;

public final class Fireball {
    private Fireball() {}

    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "fireball", MagicSchool.FIRE, null, null, MagicRank.BEGINNER, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(3.0f),
            1.5f, 0.35f, 40, 10f, 300f,
            "Une sphère de feu lancée devant soi.",
            "Ô flamme qui dévore, rassemble-toi en sphère et frappe ce que je vise.",
            200, LearnMethod.SCROLL
    );
}