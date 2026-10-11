package net.cosette.miyabi.skills.magic.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.cosette.miyabi.skills.magic.SpellCastContext;
import net.cosette.miyabi.skills.magic.SpellDefinition;

public record HealEffect(float baseHeal) implements SpellEffect {
    @Override
    public void apply(Entity target, SpellDefinition definition, SpellCastContext context) {
        if (target instanceof LivingEntity living) {
            living.heal(baseHeal);
        }
    }
}