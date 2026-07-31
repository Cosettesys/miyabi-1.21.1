package net.cosette.miyabi.magic.spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record HealEffect(float baseHeal) implements SpellEffect {
    @Override
    public void apply(Entity target, SpellDefinition definition, SpellCastContext context) {
        if (target instanceof LivingEntity living) {
            living.heal(baseHeal);
        }
    }
}