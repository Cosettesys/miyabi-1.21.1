package net.cosette.miyabi.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;

/** Lit (et complète) la section d'une technique dans le fichier de son style, et renvoie la technique avec ses valeurs. */
public final class TechniqueConfig {
    private TechniqueConfig() {}
    public static SwordTechniqueDefinition apply(CommentedConfig config, SwordTechniqueDefinition base, ConfigReport report) {
        String section = "techniques." + base.id();
        String p = section + ".";
        ConfigIO.sectionComment(config, section, "Technique " + base.id() + " (style " + base.style().name() + ").");
        MagicRank rank = ConfigIO.getEnum(config, p + "rank", base.rank(),
                "Rang de la technique : BEGINNER, INTERMEDIATE, ADVANCED, SAINT, KING, EMPEROR ou GOD.\nÀ partir d'ADVANCED, la battle aura doit être éveillée pour l'utiliser.", report);
        float damage = ConfigIO.getFloat(config, p + "damage", base.baseDamage(),
                "Dégâts de base, avant les multiplicateurs de type de dégâts et de rang du style.", 0, 1e9, report);
        PhysicalDamageType type = ConfigIO.getEnum(config, p + "physical_type", base.physicalType(),
                "Type de dégâts physiques : NONE, BLUNT, SLASHING ou PIERCING (voir physical_types dans miyabi.toml).", report);
        float reach = ConfigIO.getFloat(config, p + "reach", base.reach(),
                "Portée de la technique, en blocs.", 0.5, 64, report);
        float cooldown = ConfigIO.getFloat(config, p + "cooldown_seconds", base.cooldownTicks() / 20f,
                "Recharge de la technique non maîtrisée, en secondes.", 0, 36000, report);
        float masteredCooldown = ConfigIO.getFloat(config, p + "mastered_cooldown_seconds", base.masteredCooldownTicks() / 20f,
                "Recharge de la technique maîtrisée, en secondes.", 0, 36000, report);
        String weaponDefault = base.requiredWeapon() == null ? "" : base.requiredWeapon().location().toString();
        String weapon = ConfigIO.getString(config, p + "required_weapon_tag", weaponDefault,
                "Tag d'objets que l'arme en main doit avoir (par exemple miyabi:sword_god_weapons).\nVide = aucune arme exigée.", report);
        TagKey<Item> weaponTag = null;
        if (!weapon.isBlank()) {
            ResourceLocation id = ResourceLocation.tryParse(weapon.trim());
            if (id == null) {
                report.warn(p + "required_weapon_tag : identifiant invalide \"" + weapon + "\", valeur du code utilisée");
                weaponTag = base.requiredWeapon();
            } else {
                weaponTag = TagKey.create(Registries.ITEM, id);
            }
        }
        return new SwordTechniqueDefinition(base.id(), base.style(), rank, damage, type, reach,
                Math.round(cooldown * 20f), Math.round(masteredCooldown * 20f), weaponTag);
    }
}