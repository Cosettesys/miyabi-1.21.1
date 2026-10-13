package net.cosette.miyabi.skills.swordtechniques;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.MasteryEffects;
import net.cosette.miyabi.progression.MasteryManager;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.registry.ModParticles;
import net.cosette.miyabi.skills.damage.DamageResolver;
import net.cosette.miyabi.skills.damage.ModDamageTypes;


public final class TechniqueManager {
    private TechniqueManager() {}
    /** Largeur du cône de visée : cos(30°). Plus la valeur est proche de 1, plus il faut viser précisément. */
    private static final double MIN_ALIGNMENT = 0.866;
    /** Distance entre les yeux du joueur et la trace du coup. */
    private static final double SLASH_DISTANCE = 2.1;
    public static void use(ServerPlayer player, SwordTechniqueDefinition technique) {
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        // Les techniques de rang Avancé ou plus demandent la battle aura (donc le mana éveillé).
        if (technique.rank().isAtLeast(MagicRank.ADVANCED) && !progression.isBattleAuraAwakened()) {
            player.displayClientMessage(Component.literal("Mon corps n'est pas assez fort pour cette technique."), true);
            return;
        }
        TechniqueCooldownData cooldown = player.getData(ModAttachments.TECHNIQUE_COOLDOWN);
        long now = player.level().getGameTime();
        if (!cooldown.isReady(technique.id(), now)) {
            int seconds = (int) Math.ceil(cooldown.remainingTicks(technique.id(), now) / 20.0);
            player.displayClientMessage(Component.literal("Technique en recharge (" + seconds + " s)."), true);
            return;
        }
        LivingEntity target = findTarget(player, technique.reach());
        if (target == null) {
            player.displayClientMessage(Component.literal("Aucune cible à portée."), true);
            return;
        }
        cooldown.startCooldown(technique.id(), now, effectiveCooldownTicks(player, technique));
        player.swing(InteractionHand.MAIN_HAND, true);
        spawnSlash(player);
        float rankMultiplier = progression.getStyleRank(technique.style()).getDamageMultiplier();
        float damage = technique.baseDamage() * technique.physicalType().getDamageMultiplier() * rankMultiplier;
        var damageTypeHolder = player.level().registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(ModDamageTypes.SWORD_TECHNIQUE);
        DamageSource source = new DamageSource(damageTypeHolder, player, player);

        DamageResolver.deal(target, source, damage, technique.physicalType(), player, "La technique");

        MasteryManager.onTechniqueUse(player, technique);
    }
    /** Recharge réelle de la technique pour ce joueur : réduite par sa maîtrise de la technique. */
    public static int effectiveCooldownTicks(ServerPlayer player, SwordTechniqueDefinition technique) {
        int uses = player.getData(ModAttachments.PROGRESSION).getTechniqueUses(technique.id());
        return MasteryEffects.techniqueCooldownTicks(technique.cooldownTicks(), technique.masteredCooldownTicks(), uses);
    }
    /** Trace de coup d'épée devant le joueur, à hauteur de torse. Visible par les joueurs proches. */
    private static void spawnSlash(ServerPlayer player) {
        Vec3 pos = player.getEyePosition()
                .add(player.getLookAngle().scale(SLASH_DISTANCE))
                .subtract(0.0, 0.25, 0.0);
        // count = 0 : les 3 valeurs suivantes sont transmises à la particule telles quelles (ici : taille supplémentaire).
        ((ServerLevel) player.level()).sendParticles(ModParticles.SLASH.get(), pos.x, pos.y, pos.z, 0, 0.0, 0.0, 0.0, 1.0);
    }
    /** La créature vivante la mieux alignée avec le regard du joueur, à portée, ou null. */
    private static LivingEntity findTarget(ServerPlayer player, float reach) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        LivingEntity best = null;
        double bestAlignment = MIN_ALIGNMENT;
        for (LivingEntity candidate : player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(reach),
                other -> other != player && other.isAlive())) {
            Vec3 toTarget = candidate.getBoundingBox().getCenter().subtract(eye);
            if (toTarget.length() > reach + candidate.getBbWidth() / 2.0) continue;

            double alignment = toTarget.normalize().dot(look);
            if (alignment > bestAlignment) {
                best = candidate;
                bestAlignment = alignment;
            }
        }
        return best;
    }
}