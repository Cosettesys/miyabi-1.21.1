package net.cosette.miyabi.magic.spell;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.cosette.miyabi.magic.DamageLogData;
import net.cosette.miyabi.magic.ElementResistanceData;
import net.cosette.miyabi.magic.ModDamageTypes;

public record DamageEffect(float baseDamage, PhysicalDamageType physicalType) implements SpellEffect {
    public DamageEffect(float baseDamage) {
        this(baseDamage, PhysicalDamageType.NONE);
    }
    @Override
    public void apply(Entity target, SpellDefinition definition, SpellCastContext context) {
        float damage = baseDamage * definition.element().getDamageMultiplier() * definition.shape().getDamageMultiplier()
                * physicalType.getDamageMultiplier() * (context.intensityPercent() / 100f);
        if (context.silentCast()) damage *= 0.5f;
        float resistance = target.getData(ElementResistanceData.ELEMENT_RESISTANCE).getResistancePercent(definition.element());
        damage = Math.max(0f, damage * (1f - resistance / 100f));
        var damageTypeHolder = target.level().registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(ModDamageTypes.forElement(definition.element()));
        DamageSource source = new DamageSource(damageTypeHolder, context.sourceEntity(), context.caster());
        float armor = 0f;
        float armorIgnored = 0f;
        if (target instanceof LivingEntity livingTarget) {
            armor = (float) livingTarget.getAttributeValue(Attributes.ARMOR);
            float toughness = (float) livingTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float effectiveArmor = armor * (1f - physicalType.getArmorPenetrationPercent() / 100f);
            armorIgnored = armor - effectiveArmor;
            damage = CombatRules.getDamageAfterAbsorb(livingTarget, damage, source, effectiveArmor, toughness);
            damageArmorDurability(livingTarget, damage);
        }
        float healthBefore = target instanceof LivingEntity living ? living.getHealth() : 0f;
        target.hurt(source, damage);
        float healthAfter = target instanceof LivingEntity living ? living.getHealth() : 0f;
        if (context.caster() instanceof ServerPlayer caster
                && caster.getData(DamageLogData.DAMAGE_LOG).isEnabled()
                && target instanceof LivingEntity living) {
            float actualDamage = Math.max(0f, healthBefore - healthAfter);
            caster.sendSystemMessage(Component.literal(String.format(
                    "Le spell (%s) a réussi à faire %.2f dégâts sur %s, PV restants : %.2f/%.1f (armure ignorée : %.0f%%)",
                    physicalType.name(), actualDamage, target.getName().getString(),
                    living.getHealth(), living.getMaxHealth(), physicalType.getArmorPenetrationPercent())));
        }
    }
    private void damageArmorDurability(LivingEntity target, float damage) {
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