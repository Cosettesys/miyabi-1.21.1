package net.cosette.miyabi.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.magic.status.ElementalStatusEffect;

public final class ModEffects {
    private ModEffects() {}
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Miyabi.MODID);
    /** Mouillé : posé par certains sorts d'eau. La foudre fait plus de dégâts à une cible mouillée. */
    public static final DeferredHolder<MobEffect, MobEffect> WET =
            EFFECTS.register("wet", () -> new ElementalStatusEffect(MobEffectCategory.NEUTRAL, 0x3C78FF));
    /** Brûlé : posé par certains sorts de feu. Le vent fait plus de dégâts à une cible brûlée. */
    public static final DeferredHolder<MobEffect, MobEffect> BURNED =
            EFFECTS.register("burned", () -> new ElementalStatusEffect(MobEffectCategory.HARMFUL, 0xFF6A1A));
}