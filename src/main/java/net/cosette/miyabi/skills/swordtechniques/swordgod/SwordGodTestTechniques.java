package net.cosette.miyabi.skills.swordtechniques.swordgod;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.registry.ModTags;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;

import java.util.ArrayList;
import java.util.List;

/** Techniques factices pour tester le menu. À remplacer par les vraies techniques. */
public final class SwordGodTestTechniques {
    private SwordGodTestTechniques() {}

    public static List<SwordTechniqueDefinition> all() {
        List<SwordTechniqueDefinition> list = new ArrayList<>();
        MagicRank[] ranks = MagicRank.values();
        for (int i = 1; i <= 12; i++) {
            list.add(new SwordTechniqueDefinition("sword_god_test_" + i, SwordStyle.SWORD_GOD, ranks[(i - 1) % ranks.length],
                    ModTags.SWORD_GOD_WEAPONS));
        }
        return list;
    }
}