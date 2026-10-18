package net.cosette.miyabi.skills.swordtechniques;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;

/**
 * @param requiredWeapon tag d'objets que l'arme en main doit avoir pour utiliser cette technique, ou null si aucune arme n'est exigée
 */
public record SwordTechniqueDefinition(
        String id,
        SwordStyle style,
        MagicRank rank,
        float baseDamage,
        PhysicalDamageType physicalType,
        float reach,
        int cooldownTicks,
        int masteredCooldownTicks,
        TagKey<Item> requiredWeapon
) {
    public static final float DEFAULT_DAMAGE = 4.0f;
    public static final float DEFAULT_REACH = 3.5f;
    public static final int DEFAULT_COOLDOWN_TICKS = 160;           // 8 s avant maîtrise
    public static final int DEFAULT_MASTERED_COOLDOWN_TICKS = 40;   // 2 s une fois maîtrisée

    /** Technique avec les valeurs par défaut et sans arme exigée. */
    public SwordTechniqueDefinition(String id, SwordStyle style, MagicRank rank) {
        this(id, style, rank, (TagKey<Item>) null);
    }

    /** Technique avec les valeurs par défaut : tranchant, 4 de dégâts, 3.5 blocs de portée, recharge de 8 s à 2 s. */
    public SwordTechniqueDefinition(String id, SwordStyle style, MagicRank rank, TagKey<Item> requiredWeapon) {
        this(id, style, rank, DEFAULT_DAMAGE, PhysicalDamageType.SLASHING, DEFAULT_REACH,
                DEFAULT_COOLDOWN_TICKS, DEFAULT_MASTERED_COOLDOWN_TICKS, requiredWeapon);
    }
}