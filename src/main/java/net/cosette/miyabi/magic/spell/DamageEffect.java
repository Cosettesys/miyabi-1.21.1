package net.cosette.miyabi.magic.spell;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.cosette.miyabi.magic.DamageLogData;
import net.cosette.miyabi.magic.ElementResistanceData;
import net.cosette.miyabi.magic.ModDamageTypes;

public record DamageEffect(float baseDamage) implements SpellEffect {
    @Override
    public void apply(Entity target, SpellDefinition definition, SpellCastContext context) {
        float damage = baseDamage * definition.element().getDamageMultiplier() * definition.shape().getDamageMultiplier();
        if (context.silentCast()) damage *= 0.5f;

        float resistance = target.getData(ElementResistanceData.ELEMENT_RESISTANCE).getResistancePercent(definition.element());
        damage = Math.max(0f, damage * (1f - resistance / 100f));

        var damageTypeHolder = target.level().registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(ModDamageTypes.forElement(definition.element()));
        DamageSource source = new DamageSource(damageTypeHolder, context.sourceEntity(), context.caster());

        float healthBefore = target instanceof LivingEntity living ? living.getHealth() : 0f;
        target.hurt(source, damage);
        float healthAfter = target instanceof LivingEntity living ? living.getHealth() : 0f;

        if (context.caster() instanceof ServerPlayer caster
                && caster.getData(DamageLogData.DAMAGE_LOG).isEnabled()
                && target instanceof LivingEntity living) {
            float actualDamage = Math.max(0f, healthBefore - healthAfter);
            caster.sendSystemMessage(Component.literal(String.format(
                    "Le spell a réussi à faire %.1f dégâts sur %s, PV restants : %.1f/%.1f",
                    actualDamage, target.getName().getString(), living.getHealth(), living.getMaxHealth())));
        }
    }
}