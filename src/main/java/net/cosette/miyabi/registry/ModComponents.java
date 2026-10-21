package net.cosette.miyabi.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.cosette.miyabi.Miyabi;

/** Composants de données d'objets (le remplaçant du NBT depuis Minecraft 1.20.5). */
public final class ModComponents {
    private ModComponents() {}
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Miyabi.MODID);
    /** Id du sort qu'enseigne un parchemin. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> TAUGHT_SPELL =
            COMPONENTS.register("taught_spell", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());
}