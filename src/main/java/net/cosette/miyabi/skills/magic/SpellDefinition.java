package net.cosette.miyabi.skills.magic;

import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.magic.effect.SpellEffect;

public record SpellDefinition(
        String id,
        MagicSchool school,
        MagicRank rank,
        SpellShape shape,
        SpellCategory category,
        SpellEffect effect,
        float baseSpeed,
        float baseSize,
        int baseCastDurationTicks,
        float manaCost,
        float maxIntensityPercent
) {
    public float computeSpeed() {
        return baseSpeed * shape.getSpeedMultiplier();
    }
    public float computeSize() {
        return baseSize * shape.getSizeMultiplier();
    }
}