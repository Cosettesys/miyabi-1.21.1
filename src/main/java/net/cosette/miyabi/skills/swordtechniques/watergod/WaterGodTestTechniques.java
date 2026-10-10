package net.cosette.miyabi.skills.swordtechniques.watergod;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;

import java.util.ArrayList;
import java.util.List;

/** Techniques factices pour tester le menu. À remplacer par les vraies techniques. */
public final class WaterGodTestTechniques {
    private WaterGodTestTechniques() {}

    public static List<SwordTechniqueDefinition> all() {
        List<SwordTechniqueDefinition> list = new ArrayList<>();
        MagicRank[] ranks = MagicRank.values();
        for (int i = 1; i <= 3; i++) {
            list.add(new SwordTechniqueDefinition("water_god_test_" + i, SwordStyle.WATER_GOD, ranks[(i - 1) % ranks.length]));
        }
        return list;
    }
}