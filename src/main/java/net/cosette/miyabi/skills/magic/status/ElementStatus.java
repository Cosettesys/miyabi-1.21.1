package net.cosette.miyabi.skills.magic.status;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.cosette.miyabi.registry.ModEffects;

/** États élémentaires qu'un sort peut poser sur sa cible. */
public enum ElementStatus {
    WET,
    BURNED;
    public Holder<MobEffect> effect() {
        return switch (this) {
            case WET -> ModEffects.WET;
            case BURNED -> ModEffects.BURNED;
        };
    }
    public boolean isOn(LivingEntity entity) {
        return entity.hasEffect(effect());
    }
    public void apply(LivingEntity entity, int durationTicks) {
        if (durationTicks > 0) {
            entity.addEffect(new MobEffectInstance(effect(), durationTicks));
        }
    }
}