package net.cosette.miyabi.skills.magic.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.cast.CastManager;
import net.cosette.miyabi.skills.damage.DamageResolver;
import net.cosette.miyabi.skills.damage.ModDamageTypes;
import net.cosette.miyabi.skills.damage.PhysicalDamageType;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellCastContext;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.status.ElementStatus;
import net.cosette.miyabi.skills.magic.status.ElementalReactions;

/**
 * Dégâts d'un sort. Peut aussi poser un état élémentaire (mouillé, brûlé) sur la cible, pendant statusTicks.
 * Pour un sort hybride, le bonus de l'école et le bonus de rang du lanceur sont la moyenne de ceux de ses écoles,
 * et la résistance de la cible aussi.
 */
public record DamageEffect(float baseDamage, PhysicalDamageType physicalType, ElementStatus status, int statusTicks)
        implements SpellEffect {
    public DamageEffect(float baseDamage) {
        this(baseDamage, PhysicalDamageType.NONE, null, 0);
    }
    public DamageEffect(float baseDamage, PhysicalDamageType physicalType) {
        this(baseDamage, physicalType, null, 0);
    }
    private static float averageSchoolMultiplier(SpellDefinition definition) {
        float sum = 0f;
        for (MagicSchool school : definition.schools()) {
            sum += school.getDamageMultiplier();
        }
        return sum / definition.schools().size();
    }
    private static float averageRankMultiplier(PlayerProgressionData progression, SpellDefinition definition) {
        float sum = 0f;
        for (MagicSchool school : definition.schools()) {
            sum += progression.getSchoolRank(school).getDamageMultiplier();
        }
        return sum / definition.schools().size();
    }
    /** Dégâts à 100 % d'intensité pour ce lanceur, sans résistance de la cible : pour l'affichage dans le menu. */
    public float previewDamage(SpellDefinition definition, ServerPlayer caster) {
        float rankMultiplier = averageRankMultiplier(caster.getData(ModAttachments.PROGRESSION), definition);
        return baseDamage * averageSchoolMultiplier(definition) * definition.shape().getDamageMultiplier()
                * physicalType.getDamageMultiplier() * rankMultiplier;
    }
    @Override
    public void apply(Entity target, SpellDefinition definition, SpellCastContext context) {
        float rankMultiplier = 1.0f;
        if (context.caster() instanceof ServerPlayer casterPlayer) {
            rankMultiplier = averageRankMultiplier(casterPlayer.getData(ModAttachments.PROGRESSION), definition);
        }
        float damage = baseDamage * averageSchoolMultiplier(definition) * definition.shape().getDamageMultiplier()
                * physicalType.getDamageMultiplier() * (context.intensityPercent() / 100f) * rankMultiplier;
        if (context.silentCast()) damage *= CastManager.SILENT_CAST_DAMAGE_MULTIPLIER;
        damage *= context.effectScale(); // sort mal appris ou retour de flamme : effet réduit
        // Résistance de la cible : moyenne de ses résistances dans les écoles du sort
        float resistance = 0f;
        for (MagicSchool school : definition.schools()) {
            resistance += target.getData(ModAttachments.SCHOOL_RESISTANCE).getResistancePercent(school);
        }
        resistance /= definition.schools().size();
        damage = Math.max(0f, damage * (1f - resistance / 100f));
        // Réaction entre éléments, après les résistances (avant de poser un nouvel état, pour ne pas réagir à soi-même)
        if (target instanceof LivingEntity living) {
            damage *= ElementalReactions.multiplier(living, definition.damageElement());
        }
        var damageTypeHolder = target.level().registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(ModDamageTypes.forSpell(definition));
        DamageSource source = new DamageSource(damageTypeHolder, context.sourceEntity(), context.caster());
        DamageResolver.deal(target, source, damage, physicalType, context.caster(), "Le spell");
        if (status != null && target instanceof LivingEntity living) {
            status.apply(living, Math.round(statusTicks * context.effectScale()));
        }
    }
}