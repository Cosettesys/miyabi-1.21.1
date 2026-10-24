package net.cosette.miyabi.network.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.client.screen.ClientHooks;
import net.cosette.miyabi.npc.TrainerMenu;
import net.cosette.miyabi.network.payload.TrainerDataPayload;
import net.cosette.miyabi.network.payload.TrainerRequestPayload;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.trainer.ObjectiveDefinition;
import net.cosette.miyabi.trainer.ObjectivePool;
import net.cosette.miyabi.trainer.QuestData;
import net.cosette.miyabi.trainer.QuestManager;
import net.cosette.miyabi.trainer.TrainerConfig;
import net.cosette.miyabi.trainer.TrainerRules;
import java.util.ArrayList;
import java.util.List;

public final class TrainerPacketHandlers {
    private TrainerPacketHandlers() {}
    // Serveur : une action n'est acceptée que si le joueur a bien le menu d'un entraîneur ouvert devant lui.
    public static void onRequest(TrainerRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof TrainerMenu menu) || !menu.stillValid(player)) return;
            SwordStyle style = menu.getStyle();
            switch (payload.action()) {
                case "learn_start" -> QuestManager.startLearning(player, style, payload.arg());
                case "rankup_start" -> QuestManager.startRankUp(player, style);
                case "abandon" -> QuestManager.abandon(player);
                case "claim" -> QuestManager.claim(player, style);
                default -> { }
            }
            sendSnapshot(player, style);
        });
    }
    // Client
    public static void onData(TrainerDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHooks.updateTrainer(payload));
    }
    /** Envoie au joueur l'état complet du menu de cet entraîneur. */
    public static void sendSnapshot(ServerPlayer player, SwordStyle style) {
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        QuestData quest = player.getData(ModAttachments.QUEST);
        long now = player.level().getGameTime();
        MagicRank rank = progression.getStyleRank(style);
        MagicRank next = rank.next();
        boolean needsTrial = next != null && TrainerRules.trialRequired(rank);
        boolean qualified = next != null && progression.getStyleMastery(style) >= rank.getMasteryToNext() - 1;
        boolean questHere = quest.isActive() && quest.getStyle() == style;
        List<TrainerDataPayload.ObjectiveEntry> objectives = new ArrayList<>();
        if (questHere) {
            for (QuestData.Objective objective : quest.getObjectives()) {
                ObjectiveDefinition def = ObjectivePool.byId(objective.getDefinitionId());
                objectives.add(new TrainerDataPayload.ObjectiveEntry(objective.getType().name(), objective.getTarget(),
                        objective.getRequired(), objective.getProgress(), def == null ? "" : def.description()));
            }
        }
        List<TrainerDataPayload.TechniqueRow> techniques = new ArrayList<>();
        for (SwordTechniqueDefinition technique : SwordTechniqueRegistry.all()) {
            if (technique.style() != style) continue;
            TrainerConfig.LearningRule rule = TrainerConfig.of(style).learning().get(technique.id());
            if (rule == null || !rule.enabled()) continue;
            if (progression.isTechniqueUnlocked(technique.id())) continue; // apprise ou apprenable : disparaît de la liste
            int status;
            if (questHere && quest.getKind() == QuestData.Kind.LEARN && quest.getTechniqueId().equals(technique.id())) {
                status = quest.isCompleted() ? TrainerDataPayload.STATUS_READY : TrainerDataPayload.STATUS_IN_PROGRESS;
            } else if (!rank.isAtLeast(rule.requiredRank())) {
                status = TrainerDataPayload.STATUS_RANK_TOO_LOW;
            } else {
                status = TrainerDataPayload.STATUS_AVAILABLE;
            }
            techniques.add(new TrainerDataPayload.TechniqueRow(technique.id(), rule.requiredRank().name(), status));
        }
        long remaining = questHere && quest.getKind() == QuestData.Kind.RANK_UP && !quest.isCompleted()
                ? Math.max(0L, quest.getDeadlineTick() - now) : -1L;
        PacketDistributor.sendToPlayer(player, new TrainerDataPayload(
                style.name(), rank.name(), next == null ? "" : next.name(), needsTrial, qualified,
                Math.max(0L, quest.getCooldownUntilTick() - now),
                questHere ? (quest.getKind() == QuestData.Kind.LEARN ? 1 : 2) : (quest.isActive() ? 3 : 0),
                questHere ? quest.getTechniqueId() : "",
                questHere ? quest.getTargetRank().name() : "",
                questHere && quest.isCompleted(), remaining, objectives, techniques));
    }
}