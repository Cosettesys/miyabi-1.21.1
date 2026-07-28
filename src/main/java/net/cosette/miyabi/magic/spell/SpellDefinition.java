package net.cosette.miyabi.magic.spell;

public record SpellDefinition(
        String id,
        SpellElement element,
        SpellShape shape,
        float baseDamage,
        float baseSpeed,
        float baseSize
) {
    public float computeDamage(boolean silentCast) {
        float damage = baseDamage * element.getDamageMultiplier() * shape.getDamageMultiplier();
        return silentCast ? damage * 0.5f : damage;
    }
    public float computeSpeed() {
        return baseSpeed * shape.getSpeedMultiplier();
    }
    public float computeSize() {
        return baseSize * shape.getSizeMultiplier();
    }
}