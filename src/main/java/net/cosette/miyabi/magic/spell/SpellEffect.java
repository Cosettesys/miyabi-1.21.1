package net.cosette.miyabi.magic.spell;

import net.minecraft.world.entity.Entity;

public interface SpellEffect {
    void apply(Entity target, SpellDefinition definition, SpellCastContext context);
}