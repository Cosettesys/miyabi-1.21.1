package net.cosette.miyabi.trainer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.MasteryManager;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.util.Chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quêtes des entraîneurs : évaluation pour apprendre une technique, épreuve pour monter de rang.
 * Une seule quête à la fois ; l'abandonner est le seul moyen d'en prendre une autre, et déclenche un délai d'attente.
 */
public final class QuestManager {
    private QuestManager() {}
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static QuestData quest(ServerPlayer player) {
        return player.getData(ModAttachments.QUEST);
    }
    private static PlayerProgressionData progression(ServerPlayer player) {
        return player.getData(ModAttachments.PROGRESSION);
    }
    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }
    /** Durée en heures et minutes de jeu. */
    public static String formatTicks(long ticks) {
        long hours = ticks / 1000;
        long minutes = (ticks % 1000) * 60 / 1000;
        return hours + " h " + String.format("%02d", minutes) + " de jeu";
    }
    // ---------- tirage des objectifs ----------
    private static List<QuestData.Objective> roll(SwordStyle style, MagicRank tier, int count, RandomSource random) {
        List<ObjectiveDefinition> eligible = new ArrayList<>();
        for (ObjectiveDefinition objective : ObjectivePool.all()) {
            if (objective.isAvailableFor(style, tier)) eligible.add(objective);
        }
        Collections.shuffle(eligible, new java.util.Random(random.nextLong()));
        List<QuestData.Objective> result = new ArrayList<>();
        for (int i = 0; i < Math.min(count, eligible.size()); i++) {
            ObjectiveDefinition def = eligible.get(i);
            int span = Math.max(0, def.countMax() - def.countMin());
            int required = def.countMin() + (span == 0 ? 0 : random.nextInt(span + 1));
            result.add(new QuestData.Objective(def.id(), def.type(), def.target(), required, 0));
        }
        return result;
    }
    // ---------- démarrage ----------
    private static boolean canStart(ServerPlayer player, QuestData quest) {
        if (quest.isActive()) {
            say(player, "J'ai déjà une " + Chat.aura("évaluation") + " en cours.");
            return false;
        }
        long remaining = quest.getCooldownUntilTick() - player.level().getGameTime();
        if (remaining > 0) {
            say(player, "Mon maître me demande d'attendre encore " + Chat.bad(formatTicks(remaining)) + ".");
            return false;
        }
        return true;
    }
    public static void startLearning(ServerPlayer player, SwordStyle style, String techniqueId) {
        QuestData quest = quest(player);
        PlayerProgressionData progression = progression(player);
        SwordTechniqueDefinition technique = SwordTechniqueRegistry.byId(techniqueId);
        TrainerConfig.LearningRule rule = TrainerConfig.of(style).learning().get(techniqueId);
        if (technique == null || rule == null || !rule.enabled() || technique.style() != style) return;
        if (progression.isTechniqueUnlocked(techniqueId)) {
            say(player, "Je connais " + Chat.good("déjà") + " cette technique.");
            return;
        }
        if (!progression.getStyleRank(style).isAtLeast(rule.requiredRank())) {
            say(player, "Mon rang est " + Chat.bad("insuffisant") + " pour que mon maître m'enseigne cette technique.");
            return;
        }
        if (!canStart(player, quest)) return;
        quest.start(QuestData.Kind.LEARN, style, techniqueId, technique.rank(),
                roll(style, technique.rank(), rule.objectivesToRoll(), player.getRandom()), 0L);
        say(player, "Mon maître m'a confié une " + Chat.aura("évaluation") + " pour " + Chat.style(style) + " : " + Chat.good(techniqueId) + ".");
        refresh(player);
    }
    public static void startRankUp(ServerPlayer player, SwordStyle style) {
        QuestData quest = quest(player);
        PlayerProgressionData progression = progression(player);
        MagicRank current = progression.getStyleRank(style);
        MagicRank next = current.next();
        if (next == null || !TrainerRules.trialRequired(current)) return;
        if (progression.getStyleMastery(style) < current.getMasteryToNext() - 1) {
            say(player, "Je ne suis " + Chat.bad("pas encore qualifié") + " pour monter en rang.");
            return;
        }
        if (!canStart(player, quest)) return;
        TrainerConfig.RankUpRule rule = TrainerConfig.of(style).rankUps().get(next);
        int count = rule == null ? 3 : rule.objectivesToRoll();
        float hours = rule == null ? 24f : rule.timeLimitHours();
        long deadline = player.level().getGameTime() + TrainerRules.hoursToTicks(hours);
        // Pour passer au rang Saint, on tire des objectifs de rang Saint : on vérifie qu'il mérite ce rang
        quest.start(QuestData.Kind.RANK_UP, style, "", next, roll(style, next, count, player.getRandom()), deadline);
        say(player, "L'" + Chat.aura("épreuve") + " du rang " + Chat.rank(next) + " commence. Je dispose de "
                + Chat.bad(formatTicks(TrainerRules.hoursToTicks(hours))) + ".");
        refresh(player);
    }
    // ---------- abandon, validation ----------
    public static void abandon(ServerPlayer player) {
        QuestData quest = quest(player);
        if (!quest.isActive()) return;
        quest.clear();
        quest.setCooldownUntilTick(player.level().getGameTime() + TrainerRules.hoursToTicks(TrainerRules.ABANDON_COOLDOWN_HOURS));
        say(player, "J'abandonne cette " + Chat.dim("évaluation") + ".");
    }
    /** Validation chez l'entraîneur : apprend la technique ou monte de rang. */
    public static void claim(ServerPlayer player, SwordStyle style) {
        QuestData quest = quest(player);
        PlayerProgressionData progression = progression(player);
        if (!quest.isActive() || !quest.isCompleted() || quest.getStyle() != style) return;

        if (quest.getKind() == QuestData.Kind.LEARN) {
            String id = quest.getTechniqueId();
            // Enseignée par un entraîneur : comme un parchemin, pas de ralentissement "vue"
            progression.unlockTechnique(id);
            progression.setTechniqueSeenOnly(id, false);
            quest.clear();
            say(player, "Mon maître m'a " + Chat.good("enseigné") + " " + Chat.style(style) + " : " + Chat.good(id) + ". Je peux maintenant la travailler.");
        } else {
            MagicRank current = progression.getStyleRank(style);
            MagicRank next = quest.getTargetRank();
            quest.clear();
            if (current.next() == next) {
                progression.setStyleRank(style, next);
                progression.setStyleMastery(style, 0);
                say(player, "Rang " + Chat.style(style) + " : " + Chat.rank(current) + " -> " + Chat.rank(next) + " !");
            }
        }
    }
    // ---------- suivi ----------
    /** Appelée à chaque mort : fait avancer les objectifs de type "vaincre". */
    public static void onKill(ServerPlayer player, LivingEntity victim, DamageSource source) {
        QuestData quest = quest(player);
        if (!quest.isActive() || quest.isCompleted()) return;

        boolean changed = false;
        for (QuestData.Objective objective : quest.getObjectives()) {
            if (objective.isDone() || !objective.getType().isKill()) continue;
            if (matches(objective, victim, source, player)) {
                objective.setProgress(objective.getProgress() + 1);
                changed = true;
            }
        }
        if (changed) checkCompletion(player, quest);
    }
    private static boolean matches(QuestData.Objective objective, LivingEntity victim, DamageSource source, ServerPlayer player) {
        return switch (objective.getType()) {
            case KILL_ENTITY -> {
                ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
                yield key.toString().equals(objective.getTarget());
            }
            case KILL_TAG -> {
                ResourceLocation tag = ResourceLocation.tryParse(objective.getTarget());
                yield tag != null && victim.getType().is(TagKey.<EntityType<?>>create(Registries.ENTITY_TYPE, tag));
            }
            case KILL_HOSTILE -> victim instanceof Enemy;
            case KILL_HOSTILE_UNARMED -> victim instanceof Enemy
                    && source.getDirectEntity() == player && player.getMainHandItem().isEmpty();
            default -> false;
        };
    }
    /** Appelée à chaque tick : une fois par seconde, met à jour les objectifs d'état et vérifie la date limite. */
    public static void tick(ServerPlayer player) {
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;
        QuestData quest = quest(player);
        if (!quest.isActive()) return;
        if (!quest.isCompleted()) {
            updateStateObjectives(player, quest);
            checkCompletion(player, quest);
        }
        // Épreuve de rang dont le temps est écoulé : échec, et délai avant de retenter
        if (quest.isActive() && !quest.isCompleted() && quest.getKind() == QuestData.Kind.RANK_UP
                && player.level().getGameTime() > quest.getDeadlineTick()) {
            quest.clear();
            quest.setCooldownUntilTick(player.level().getGameTime() + TrainerRules.hoursToTicks(TrainerRules.RANKUP_FAILURE_COOLDOWN_HOURS));
            say(player, "Le temps est écoulé : j'ai " + Chat.bad("échoué") + " à l'épreuve.");
        }
    }
    /** Objectifs qui décrivent un état (niveau d'aura, technique maîtrisée ou apprise) plutôt qu'un compteur. */
    private static void updateStateObjectives(ServerPlayer player, QuestData quest) {
        PlayerProgressionData progression = progression(player);
        for (QuestData.Objective objective : quest.getObjectives()) {
            switch (objective.getType()) {
                case AURA_LEVEL -> objective.setProgress(progression.getBattleAuraLevel());
                case MASTER_TECHNIQUE -> objective.setProgress(
                        progression.getTechniqueUses(objective.getTarget()) >= MasteryManager.TECHNIQUE_MASTERY_USES ? objective.getRequired() : 0);
                case LEARN_TECHNIQUE -> objective.setProgress(
                        progression.isTechniqueLearned(objective.getTarget()) ? objective.getRequired() : 0);
                default -> { }
            }
        }
    }
    private static void checkCompletion(ServerPlayer player, QuestData quest) {
        for (QuestData.Objective objective : quest.getObjectives()) {
            if (!objective.isDone()) return;
        }
        quest.setCompleted(true);
        say(player, "J'ai accompli ce que mon maître demandait. Je peux retourner le " + Chat.good("voir") + ".");
    }
    /** Évalue tout de suite les objectifs d'état (déjà remplis au moment du démarrage). */
    private static void refresh(ServerPlayer player) {
        QuestData quest = quest(player);
        updateStateObjectives(player, quest);
        checkCompletion(player, quest);
    }
    /** Pour les commandes de test : remplit tous les objectifs. */
    public static void forceComplete(ServerPlayer player) {
        QuestData quest = quest(player);
        for (QuestData.Objective objective : quest.getObjectives()) {
            objective.setProgress(objective.getRequired());
        }
        if (quest.isActive()) quest.setCompleted(true);
    }
    /** Pour les commandes de test : efface la quête et le délai d'attente. */
    public static void reset(ServerPlayer player) {
        QuestData quest = quest(player);
        quest.clear();
        quest.setCooldownUntilTick(0);
    }
}