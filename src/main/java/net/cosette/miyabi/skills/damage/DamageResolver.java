package net.cosette.miyabi.skills.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.cosette.miyabi.registry.ModAttachments;

/**
 * Applique un coup à une cible, que ce soit un sort ou une technique d'épée : armure réduite selon la pénétration
 * du type physique, usure d'armure, dégâts, puis journal de dégâts de l'attaquant s'il l'a activé.
 */
public final class DamageResolver {
    private DamageResolver() {}
    /**
     * @param damage   dégâts déjà calculés (bonus de rang, résistances élémentaires, etc.), avant armure
     * @param attacker l'auteur du coup, ou null
     * @param logLabel début de la phrase du journal de dégâts : "Le spell", "La technique"...
     */
    public static void deal(Entity target, DamageSource source, float damage, PhysicalDamageType physicalType,
                            Entity attacker, String logLabel) {
        if (target instanceof LivingEntity livingTarget) {
            float armor = (float) livingTarget.getAttributeValue(Attributes.ARMOR);
            float toughness = (float) livingTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float effectiveArmor = armor * (1f - physicalType.getArmorPenetrationPercent() / 100f);
            damage = CombatRules.getDamageAfterAbsorb(livingTarget, damage, source, effectiveArmor, toughness);
            damageArmorDurability(livingTarget, damage, physicalType);
        }
        float healthBefore = target instanceof LivingEntity living ? living.getHealth() : 0f;
        target.hurt(source, damage);
        if (attacker instanceof ServerPlayer player
                && player.getData(ModAttachments.DAMAGE_LOG).isEnabled()
                && target instanceof LivingEntity living) {
            float actualDamage = Math.max(0f, healthBefore - living.getHealth());
            player.sendSystemMessage(Component.literal(String.format(
                    "%s (%s) a réussi à faire %.2f dégâts sur %s, PV restants : %.2f/%.1f (armure ignorée : %.0f%%)",
                    logLabel, physicalType.name(), actualDamage, target.getName().getString(),
                    living.getHealth(), living.getMaxHealth(), physicalType.getArmorPenetrationPercent())));
        }
    }
    private static void damageArmorDurability(LivingEntity target, float damage, PhysicalDamageType physicalType) {
        if (physicalType.getArmorDurabilityMultiplier() <= 0f) return;

        int amount = Math.max(1, Math.round((damage / 4f) * physicalType.getArmorDurabilityMultiplier()));
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack armorPiece = target.getItemBySlot(slot);
            if (!armorPiece.isEmpty()) {
                armorPiece.hurtAndBreak(amount, target, slot);
            }
        }
    }
}