package net.cosette.miyabi.magic.spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record SpellCastContext(LivingEntity caster, Entity sourceEntity, boolean silentCast) {}