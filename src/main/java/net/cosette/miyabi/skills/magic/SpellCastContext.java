package net.cosette.miyabi.skills.magic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** @param effectScale multiplicateur de l'effet du sort : 1 normalement, 0,5 pour un sort mal appris ("effet divisé par 2") */
public record SpellCastContext(LivingEntity caster, Entity sourceEntity, boolean silentCast, float intensityPercent, float effectScale) {

    public SpellCastContext(LivingEntity caster, Entity sourceEntity, boolean silentCast, float intensityPercent) {
        this(caster, sourceEntity, silentCast, intensityPercent, 1.0f);
    }
}