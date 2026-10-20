package net.cosette.miyabi.skills.magic.effect;

import net.cosette.miyabi.skills.cast.CastManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.damage.DamageResolver;
import net.cosette.miyabi.skills.damage.ModDamageTypes;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.magic.SpellCastContext;
import net.cosette.miyabi.skills.magic.SpellDefinition;

public record DamageEffect(float baseDamage, PhysicalDamageType physicalType) implements SpellEffect {

    public DamageEffect(float baseDamage) {
        this(baseDamage, PhysicalDamageType.NONE);
    }
    /** Dégâts à 100 % d'intensité pour ce lanceur, sans résistance de la cible : pour l'affichage dans le menu. */
    public float previewDamage(SpellDefinition definition, ServerPlayer caster) {
        float rankMultiplier = caster.getData(ModAttachments.PROGRESSION)
                .getSchoolRank(definition.school()).getDamageMultiplier();
        return baseDamage * definition.school().getDamageMultiplier() * definition.shape().getDamageMultiplier()
                * physicalType.getDamageMultiplier() * rankMultiplier;
    }
    @Override
    public void apply(Entity target, SpellDefinition definition, SpellCastContext context) {
        float rankMultiplier = 1.0f;
        if (context.caster() instanceof ServerPlayer casterPlayer) {
            rankMultiplier = casterPlayer.getData(ModAttachments.PROGRESSION)
                    .getSchoolRank(definition.school()).getDamageMultiplier();
        }
        float damage = baseDamage * definition.school().getDamageMultiplier() * definition.shape().getDamageMultiplier()
                * physicalType.getDamageMultiplier() * (context.intensityPercent() / 100f) * rankMultiplier;
        if (context.silentCast()) damage *= CastManager.SILENT_CAST_DAMAGE_MULTIPLIER;
        damage *= context.effectScale(); // sort mal appris : effet divisé par 2
        float resistance = target.getData(ModAttachments.SCHOOL_RESISTANCE).getResistancePercent(definition.school());
        damage = Math.max(0f, damage * (1f - resistance / 100f));
        var damageTypeHolder = target.level().registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(ModDamageTypes.forSchool(definition.school()));
        DamageSource source = new DamageSource(damageTypeHolder, context.sourceEntity(), context.caster());
        DamageResolver.deal(target, source, damage, physicalType, context.caster(), "Le spell");
    }
}