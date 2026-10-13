package net.cosette.miyabi.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.cosette.miyabi.Miyabi;

import java.util.function.Supplier;

public final class ModParticles {
    private ModParticles() {}
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Miyabi.MODID);
    public static final Supplier<SimpleParticleType> SLASH =
            PARTICLE_TYPES.register("slash", () -> new SimpleParticleType(false));
}