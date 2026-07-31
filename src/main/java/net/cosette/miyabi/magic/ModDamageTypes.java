package net.cosette.miyabi.magic;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.magic.spell.SpellElement;

public final class ModDamageTypes {
    private ModDamageTypes() {}

    public static final ResourceKey<DamageType> FIRE_MAGIC = key("fire_magic");
    public static final ResourceKey<DamageType> WATER_MAGIC = key("water_magic");
    public static final ResourceKey<DamageType> EARTH_MAGIC = key("earth_magic");
    public static final ResourceKey<DamageType> AIR_MAGIC = key("air_magic");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, name));
    }

    public static ResourceKey<DamageType> forElement(SpellElement element) {
        return switch (element) {
            case FIRE -> FIRE_MAGIC;
            case WATER -> WATER_MAGIC;
            case EARTH -> EARTH_MAGIC;
            case AIR -> AIR_MAGIC;
        };
    }
}