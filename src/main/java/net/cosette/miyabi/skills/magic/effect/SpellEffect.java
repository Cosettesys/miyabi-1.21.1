package net.cosette.miyabi.skills.magic.effect;

import net.minecraft.world.entity.Entity;
import net.cosette.miyabi.skills.magic.SpellCastContext;
import net.cosette.miyabi.skills.magic.SpellDefinition;

public interface SpellEffect {
    void apply(Entity target, SpellDefinition definition, SpellCastContext context);
}