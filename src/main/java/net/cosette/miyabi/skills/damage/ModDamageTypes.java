package net.cosette.miyabi.skills.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.magic.DamageElement;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellDefinition;

public final class ModDamageTypes {
    private ModDamageTypes() {}
    public static final ResourceKey<DamageType> FIRE_MAGIC = key("fire_magic");
    public static final ResourceKey<DamageType> WATER_MAGIC = key("water_magic");
    public static final ResourceKey<DamageType> EARTH_MAGIC = key("earth_magic");
    public static final ResourceKey<DamageType> WIND_MAGIC = key("wind_magic");
    public static final ResourceKey<DamageType> GRAVITY_MAGIC = key("gravity_magic");
    public static final ResourceKey<DamageType> LIGHTNING_MAGIC = key("lightning_magic");
    public static final ResourceKey<DamageType> ARCANE_MAGIC = key("arcane_magic");
    public static final ResourceKey<DamageType> SWORD_TECHNIQUE = key("sword_technique");
    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, name));
    }
    public static ResourceKey<DamageType> forSchool(MagicSchool school) {
        return switch (school) {
            case FIRE -> FIRE_MAGIC;
            case WATER -> WATER_MAGIC;
            case EARTH -> EARTH_MAGIC;
            case WIND -> WIND_MAGIC;
            case GRAVITY -> GRAVITY_MAGIC;
            default -> ARCANE_MAGIC;
        };
    }
    /** Type de dégâts d'un sort : la foudre a le sien, les autres suivent leur école principale. */
    public static ResourceKey<DamageType> forSpell(SpellDefinition spell) {
        return spell.damageElement() == DamageElement.LIGHTNING ? LIGHTNING_MAGIC : forSchool(spell.school());
    }
}