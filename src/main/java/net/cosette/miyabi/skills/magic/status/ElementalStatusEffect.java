package net.cosette.miyabi.skills.magic.status;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** État élémentaire (mouillé, brûlé...). Sert de marqueur pour les réactions entre éléments. */
public class ElementalStatusEffect extends MobEffect {
    public ElementalStatusEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}