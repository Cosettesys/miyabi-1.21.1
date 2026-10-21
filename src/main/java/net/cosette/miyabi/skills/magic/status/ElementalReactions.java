package net.cosette.miyabi.skills.magic.status;

import net.minecraft.world.entity.LivingEntity;
import net.cosette.miyabi.config.ConfigValue;
import net.cosette.miyabi.skills.magic.DamageElement;

/** Réactions entre éléments : bonus de dégâts quand la cible porte déjà l'état qui y répond. */
public final class ElementalReactions {
    private ElementalReactions() {}
    @ConfigValue(value = "reactions.wet_lightning_multiplier", comment = "Multiplicateur des dégâts de foudre sur une cible mouillée (1,5 = +50 %).", min = 0)
    public static float WET_LIGHTNING_MULTIPLIER = 1.5f;
    @ConfigValue(value = "reactions.burned_wind_multiplier", comment = "Multiplicateur des dégâts de vent sur une cible brûlée (1,5 = +50 %).", min = 0)
    public static float BURNED_WIND_MULTIPLIER = 1.5f;
    /** Multiplicateur de dégâts de cet élément sur cette cible (1 si aucune réaction). À appliquer après les résistances. */
    public static float multiplier(LivingEntity target, DamageElement element) {
        float multiplier = 1f;
        if (element == DamageElement.LIGHTNING && ElementStatus.WET.isOn(target)) {
            multiplier *= WET_LIGHTNING_MULTIPLIER;
        }
        if (element == DamageElement.WIND && ElementStatus.BURNED.isOn(target)) {
            multiplier *= BURNED_WIND_MULTIPLIER;
        }
        return multiplier;
    }
}