package net.cosette.miyabi.skills.magic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record SpellCastContext(LivingEntity caster, Entity sourceEntity, boolean silentCast, float intensityPercent) {}