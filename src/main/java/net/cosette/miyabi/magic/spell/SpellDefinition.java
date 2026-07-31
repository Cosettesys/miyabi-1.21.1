package net.cosette.miyabi.magic.spell;

public record SpellDefinition(
        String id,
        SpellElement element,
        SpellShape shape,
        SpellCategory category,
        SpellEffect effect,
        float baseSpeed,
        float baseSize
) {
    public float computeSpeed() {
        return baseSpeed * shape.getSpeedMultiplier();
    }

    public float computeSize() {
        return baseSize * shape.getSizeMultiplier();
    }
}