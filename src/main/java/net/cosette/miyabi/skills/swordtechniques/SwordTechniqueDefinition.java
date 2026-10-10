package net.cosette.miyabi.skills.swordtechniques;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;

public record SwordTechniqueDefinition(String id, SwordStyle style, MagicRank rank) {}