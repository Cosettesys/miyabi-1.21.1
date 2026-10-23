package net.cosette.miyabi.skills.magic.water;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.learning.LearnMethod;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellCategory;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellShape;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;

/** Sort de test pour les bâtons : 1000 mana et 30 s d'incantation de base (200 mana et 15 s avec Aqua Heartia). */
public final class AquaStaffTest {
    private AquaStaffTest() {}

    public static final SpellDefinition DEFINITION = new SpellDefinition(
            "aqua_staff_test", MagicSchool.WATER, null, null, MagicRank.SAINT, SpellShape.SPHERE, SpellCategory.PROJECTILE,
            new DamageEffect(12.0f),
            1.5f, 0.35f, 600, 1000f, 300f,
            "Une immense vague d'eau, impossible à lancer sans bâton.",
            "Ô eau qui sommeilles au fond des océans, toi qui as sculpté les falaises et nourri les rivières, entends mon appel. "
                    + "Que les marées se lèvent, que les courants s'unissent et que ta fureur, longtemps contenue, "
                    + "se déchaîne sur ceux qui se dressent devant moi. Je te le demande par le bâton que je tiens : déferle !",
            250,
            LearnMethod.GRIMOIRE
    );
}