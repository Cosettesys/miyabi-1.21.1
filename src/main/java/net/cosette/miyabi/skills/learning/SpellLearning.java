package net.cosette.miyabi.skills.learning;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.util.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Apprentissage des sorts. Un sort débloqué mais pas appris (jauge sous 100) peut échouer ; réussi, il peut aussi
 * recevoir des malus indépendants. Toutes les valeurs sont provisoires, regroupées ici pour passer en configuration.
 */
public final class SpellLearning {
    private SpellLearning() {}
    public static final float LEARNED = 100f;
    // --- Chance de réussite : environ 5 % au début, 70 % à 99 % d'apprentissage, 100 % une fois appris ---
    private static final float MIN_CHANCE = 0.05f;
    private static final float MAX_CHANCE_BEFORE_LEARNED = 0.70f;
    private static final float LAST_GAUGE_BEFORE_LEARNED = 99f;
    /** Plus grand que 1 : la chance monte lentement au début puis de plus en plus vite. */
    private static final double CURVE_EXPONENT = 1.6;
    // --- Gain de jauge par tentative (réussie ou non) ---
    private static final float BASE_GAIN = 2.0f;
    /** Bonus par rang d'écart quand le sort est de rang inférieur au joueur dans l'école. */
    private static final float GAIN_BONUS_PER_RANK_BELOW_PLAYER = 0.5f;
    /** Facteur par rang d'écart quand le sort est de rang supérieur au joueur. */
    private static final float GAIN_FACTOR_PER_RANK_ABOVE_PLAYER = 0.5f;
    // --- Malus d'un sort réussi mais pas appris à 100 % : indépendants les uns des autres ---
    private static final float DEBUFF_CHANCE = 0.5f;
    private static final float SELF_DAMAGE_CHANCE = 0.15f;
    /** Part des dégâts du sort infligée au lanceur en cas de retour de flamme. */
    public static final float SELF_DAMAGE_FRACTION = 0.5f;
    /** Distance à laquelle on voit un sort lancé et peut le débloquer. */
    private static final double WITNESS_RADIUS = 16.0;
    public static float successChance(float gauge) {
        if (gauge >= LEARNED) return 1f;
        float t = Math.min(gauge, LAST_GAUGE_BEFORE_LEARNED) / LAST_GAUGE_BEFORE_LEARNED;
        return MIN_CHANCE + (MAX_CHANCE_BEFORE_LEARNED - MIN_CHANCE) * (float) Math.pow(t, CURVE_EXPONENT);
    }
    /** Apprendre un sort bien inférieur à son rang va plus vite ; viser un sort supérieur à son rang est bien plus lent. */
    public static float gaugeGain(MagicRank playerRank, MagicRank spellRank) {
        int gap = playerRank.ordinal() - spellRank.ordinal();
        if (gap >= 0) {
            return BASE_GAIN * (1f + GAIN_BONUS_PER_RANK_BELOW_PLAYER * gap);
        }
        return BASE_GAIN * (float) Math.pow(GAIN_FACTOR_PER_RANK_ABOVE_PLAYER, -gap);
    }
    public record Debuffs(boolean doubledMana, boolean halvedEffect, boolean deviated, boolean selfDamage) {
        public static final Debuffs NONE = new Debuffs(false, false, false, false);
        public boolean any() {
            return doubledMana || halvedEffect || deviated || selfDamage;
        }
        public String describe() {
            List<String> parts = new ArrayList<>();
            if (doubledMana) parts.add("mana doublé");
            if (halvedEffect) parts.add("effet réduit");
            if (deviated) parts.add("trajectoire déviée");
            if (selfDamage) parts.add("retour de flamme");
            return Chat.dim(String.join(", ", parts));
        }
    }
    public static Debuffs rollDebuffs(RandomSource random, boolean damagingSpell) {
        return new Debuffs(
                random.nextFloat() < DEBUFF_CHANCE,
                random.nextFloat() < DEBUFF_CHANCE,
                random.nextFloat() < DEBUFF_CHANCE,
                damagingSpell && random.nextFloat() < SELF_DAMAGE_CHANCE);
    }
    /** Écarte la direction du sort de façon bien visible. */
    public static Vec3 deviate(Vec3 direction, RandomSource random) {
        double dx = (0.25 + random.nextDouble() * 0.3) * (random.nextBoolean() ? 1 : -1);
        double dz = (0.25 + random.nextDouble() * 0.3) * (random.nextBoolean() ? 1 : -1);
        double dy = (random.nextDouble() - 0.5) * 0.3;
        return direction.add(dx, dy, dz).normalize();
    }
    /**
     * Un sort vu de près peut être débloqué (donc devenir "à apprendre") par les joueurs à portée dont le rang dans
     * l'école est au moins celui du sort. Le mana doit être éveillé.
     */
    public static void unlockForWitnesses(ServerPlayer caster, SpellDefinition def) {
        for (ServerPlayer witness : ((ServerLevel) caster.level()).players()) {
            if (witness == caster) continue;
            if (witness.distanceToSqr(caster) > WITNESS_RADIUS * WITNESS_RADIUS || !witness.hasLineOfSight(caster)) continue;
            PlayerProgressionData data = witness.getData(ModAttachments.PROGRESSION);
            if (!data.isManaAwakened() || data.isSpellUnlocked(def.id())) continue;
            if (!data.getSchoolRank(def.school()).isAtLeast(def.rank())) continue;
            data.unlockSpell(def.id());
            witness.sendSystemMessage(Component.literal("J'ai vu " + Chat.school(def.school()) + " : "
                    + Chat.good(def.id()) + ". Je pourrais " + Chat.good("apprendre") + " ce sort."));
        }
    }
}