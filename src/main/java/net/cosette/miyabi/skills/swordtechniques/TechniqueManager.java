package net.cosette.miyabi.skills.swordtechniques;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.cosette.miyabi.config.ConfigValue;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.MasteryEffects;
import net.cosette.miyabi.progression.MasteryManager;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.registry.ModParticles;
import net.cosette.miyabi.skills.damage.DamageResolver;
import net.cosette.miyabi.skills.damage.ModDamageTypes;
import net.cosette.miyabi.skills.learning.SpellLearning;
import net.cosette.miyabi.skills.learning.TechniqueLearning;
import net.cosette.miyabi.util.Chat;

/** Utilisation d'une technique d'épée : un coup de mêlée sur la cible visée, avec recharge. */
public final class TechniqueManager {
    private TechniqueManager() {}
    @ConfigValue(value = "sword_techniques.aim_min_alignment", comment = "Précision de visée exigée : cosinus de l'angle du cône (0,866 = 30 degrés ; plus proche de 1 = plus précis).", min = 0, max = 0.999)
    public static double MIN_ALIGNMENT = 0.866;
    @ConfigValue(value = "sword_techniques.slash_distance", comment = "Distance entre les yeux du joueur et la trace visuelle du coup, en blocs.", min = 0, max = 8)
    public static double SLASH_DISTANCE = 1.6;
    public static void use(ServerPlayer player, SwordTechniqueDefinition technique) {
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        TechniqueCooldownData cooldown = player.getData(ModAttachments.TECHNIQUE_COOLDOWN);
        long now = player.level().getGameTime();
        // La recharge passe avant toutes les autres vérifications.
        if (!cooldown.isReady(technique.id(), now)) {
            int seconds = (int) Math.ceil(cooldown.remainingTicks(technique.id(), now) / 20.0);
            player.displayClientMessage(Component.literal("Technique en recharge (" + seconds + " s)."), true);
            return;
        }
        // Chaque technique peut exiger une arme en main, détectée par un tag d'objets (donc compatible avec les mods).
        TagKey<Item> requiredWeapon = technique.requiredWeapon();
        if (requiredWeapon != null && !player.getMainHandItem().is(requiredWeapon)) {
            player.displayClientMessage(Component.literal("Il me faut une épée en main pour cette technique."), true);
            return;
        }
        if (!progression.isTechniqueUnlocked(technique.id())) {
            player.displayClientMessage(Component.literal("Je ne connais pas cette technique."), true);
            return;
        }
        // On ne peut ni utiliser ni apprendre une technique d'un rang supérieur au sien dans le style.
        if (!progression.getStyleRank(technique.style()).isAtLeast(technique.rank())) {
            player.displayClientMessage(Component.literal("Cette technique dépasse mon niveau dans ce style."), true);
            return;
        }
        // Les techniques de rang Avancé ou plus demandent la battle aura (donc le mana éveillé).
        if (technique.rank().isAtLeast(MagicRank.ADVANCED) && !progression.isBattleAuraAwakened()) {
            player.displayClientMessage(Component.literal("Mon corps n'est pas assez fort pour cette technique."), true);
            return;
        }
        LivingEntity target = findTarget(player, technique.reach());
        if (target == null) {
            player.displayClientMessage(Component.literal("Aucune cible à portée."), true);
            return;
        }
        RandomSource random = player.getRandom();
        boolean learned = progression.isTechniqueLearned(technique.id());
        TechniqueLearning.Debuffs debuffs = TechniqueLearning.Debuffs.NONE;
        int baseCooldown = effectiveCooldownTicks(player, technique);
        if (!learned) {
            // Chaque tentative fait monter la jauge d'apprentissage, qu'elle réussisse ou non.
            float gaugeBefore = progression.getTechniqueLearning(technique.id());
            float gaugeAfter = Math.min(SpellLearning.LEARNED, gaugeBefore + SpellLearning.gaugeGain(
                    progression.getStyleRank(technique.style()), technique.rank(), progression.isTechniqueSeenOnly(technique.id())));
            progression.setTechniqueLearning(technique.id(), gaugeAfter);
            if (gaugeAfter >= SpellLearning.LEARNED) {
                player.sendSystemMessage(Component.literal("J'ai " + Chat.good("appris") + " "
                        + Chat.style(technique.style()) + " : " + Chat.good(technique.id()) + " !"));
            }
            player.swing(InteractionHand.MAIN_HAND, true);
            if (random.nextFloat() >= SpellLearning.successChance(gaugeBefore)) {
                cooldown.startCooldown(technique.id(), now, baseCooldown);
                player.sendSystemMessage(Component.literal("La technique " + Chat.bad("échoue") + "."));
                return;
            }
            debuffs = TechniqueLearning.rollDebuffs(random);
            if (debuffs.any()) {
                player.sendSystemMessage(Component.literal(
                        "La technique est " + Chat.bad("instable") + " : " + debuffs.describe() + "."));
            }
        }
        cooldown.startCooldown(technique.id(), now, debuffs.cooldownDoubled() ? baseCooldown * 2 : baseCooldown);
        player.swing(InteractionHand.MAIN_HAND, true);
        spawnSlash(player);
        // Le rang dans le style donne le même bonus de dégâts que le rang d'une école de magie.
        float rankMultiplier = progression.getStyleRank(technique.style()).getDamageMultiplier();
        float damage = technique.baseDamage() * technique.physicalType().getDamageMultiplier() * rankMultiplier;
        if (debuffs.damageHalved()) damage *= 0.5f;
        var damageTypeHolder = player.level().registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(ModDamageTypes.SWORD_TECHNIQUE);
        DamageSource source = new DamageSource(damageTypeHolder, player, player);
        if (debuffs.backfire()) {
            // Retour de coup : la technique frappe le joueur lui-même à une fraction de sa force, et pas la cible.
            DamageResolver.deal(player, source, damage * SpellLearning.BACKFIRE_EFFECT_FRACTION, technique.physicalType(), player, "La technique");
            return;
        }
        DamageResolver.deal(target, source, damage, technique.physicalType(), player, "La technique");
        if (learned) {
            MasteryManager.onTechniqueUse(player, technique);
        }
        TechniqueLearning.unlockForWitnesses(player, technique);
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