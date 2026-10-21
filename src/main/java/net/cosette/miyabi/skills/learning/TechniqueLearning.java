package net.cosette.miyabi.skills.learning;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.util.Chat;
import java.util.ArrayList;
import java.util.List;

/**
 * Apprentissage des techniques d'épée, sur les mêmes bases que les sorts (mêmes courbes et mêmes réglages, voir
 * SpellLearning) : on les débloque en les voyant utiliser, une technique pas apprise peut échouer ou être affaiblie.
 * Plus tard : un entraîneur (PNJ) dans un dojo, comme les parchemins pour les sorts.
 */
public final class TechniqueLearning {
    private TechniqueLearning() {}
    public record Debuffs(boolean cooldownDoubled, boolean damageHalved, boolean backfire) {
        public static final Debuffs NONE = new Debuffs(false, false, false);
        public boolean any() {
            return cooldownDoubled || damageHalved || backfire;
        }
        public String describe() {
            List<String> parts = new ArrayList<>();
            if (cooldownDoubled) parts.add("recharge doublée");
            if (damageHalved) parts.add("dégâts réduits");
            if (backfire) parts.add("retour de coup");
            return Chat.dim(String.join(", ", parts));
        }
    }
    public static Debuffs rollDebuffs(RandomSource random) {
        // Un retour de coup exclut les autres malus.
        if (random.nextFloat() < SpellLearning.BACKFIRE_CHANCE) {
            return new Debuffs(false, false, true);
        }
        return new Debuffs(random.nextFloat() < SpellLearning.DEBUFF_CHANCE, random.nextFloat() < SpellLearning.DEBUFF_CHANCE, false);
    }
    /**
     * Une technique vue de près peut être débloquée par les joueurs à portée dont le rang dans le style est au moins
     * celui de la technique. Elle est alors marquée "vue" : sans formation, elle progresse plus lentement.
     */
    public static void unlockForWitnesses(ServerPlayer user, SwordTechniqueDefinition technique) {
        double radius = SpellLearning.WITNESS_RADIUS;
        for (ServerPlayer witness : ((ServerLevel) user.level()).players()) {
            if (witness == user) continue;
            if (witness.distanceToSqr(user) > radius * radius || !witness.hasLineOfSight(user)) continue;
            PlayerProgressionData data = witness.getData(ModAttachments.PROGRESSION);
            if (data.isTechniqueUnlocked(technique.id())) continue;
            if (!data.getStyleRank(technique.style()).isAtLeast(technique.rank())) continue;
            data.unlockTechnique(technique.id());
            data.setTechniqueSeenOnly(technique.id(), true);
            witness.sendSystemMessage(Component.literal("J'ai vu " + Chat.style(technique.style()) + " : "
                    + Chat.good(technique.id()) + ". Je pourrais " + Chat.good("apprendre") + " cette technique, mais sans formation ce sera "
                    + Chat.dim("long") + "."));
        }
    }
}