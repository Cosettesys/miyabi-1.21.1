package net.cosette.miyabi.skills.learning;

import net.cosette.miyabi.skills.magic.MagicSchool;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.cosette.miyabi.config.ConfigValue;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.util.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Apprentissage des sorts. Un sort débloqué mais pas appris (jauge sous 100) peut échouer ; réussi, il peut aussi
 * recevoir des malus. Toutes les valeurs sont réglables dans miyabi.toml (section "learning").
 */
public final class SpellLearning {
    private SpellLearning() {}
    public static final float LEARNED = 100f;
    private static final float LAST_GAUGE_BEFORE_LEARNED = 99f;
    // --- Chance de réussite : environ 5 % au début, 70 % à 99 % d'apprentissage, 100 % une fois appris ---
    @ConfigValue(value = "learning.min_chance", comment = "Chance de réussite d'un sort dont la jauge d'apprentissage est à 0 (0 à 1).", min = 0, max = 1)
    public static float MIN_CHANCE = 0.05f;
    @ConfigValue(value = "learning.max_chance_before_learned", comment = "Chance de réussite juste avant d'avoir appris le sort, à 99 % de jauge (0 à 1).", min = 0, max = 1)
    public static float MAX_CHANCE_BEFORE_LEARNED = 0.70f;
    @ConfigValue(value = "learning.curve_exponent", comment = "Forme de la courbe : 1 = linéaire, plus grand = la chance monte lentement puis de plus en plus vite.", min = 0.1, max = 10)
    public static double CURVE_EXPONENT = 1.6;
    // --- Gain de jauge par tentative (réussie ou non) ---
    @ConfigValue(value = "learning.base_gain", comment = "Jauge gagnée par tentative (sur 100) quand le sort est du même rang que le joueur dans l'école.", min = 0)
    public static float BASE_GAIN = 2.0f;
    @ConfigValue(value = "learning.gain_bonus_per_rank_below_player", comment = "Bonus de gain par rang d'écart quand le sort est d'un rang inférieur à celui du joueur (0,5 = +50 % par rang).", min = 0)
    public static float GAIN_BONUS_PER_RANK_BELOW_PLAYER = 0.5f;
    @ConfigValue(value = "learning.gain_factor_per_rank_above_player", comment = "Facteur de gain par rang d'écart quand le sort est d'un rang supérieur (0,5 = moitié moins par rang).", min = 0, max = 1)
    public static float GAIN_FACTOR_PER_RANK_ABOVE_PLAYER = 0.5f;
    @ConfigValue(value = "learning.seen_only_gain_divisor", comment = "Un sort appris en le voyant lancer, sans la formule exacte, progresse ce nombre de fois plus lentement.", min = 1)
    public static float SEEN_ONLY_GAIN_DIVISOR = 3.0f;
    // --- Malus d'un sort réussi mais pas appris à 100 % ---
    @ConfigValue(value = "learning.debuff_chance", comment = "Chance de chacun des malus mana doublé, effet divisé par 2 et trajectoire déviée (indépendants, 0 à 1).", min = 0, max = 1)
    public static float DEBUFF_CHANCE = 0.5f;
    @ConfigValue(value = "learning.backfire_chance", comment = "Chance d'un retour de flamme : le sort est lancé sur le lanceur lui-même au lieu de partir. Quand il se produit, les autres malus ne se produisent pas. Impossible pour un sort qui vise déjà le lanceur.", min = 0, max = 1)
    public static float BACKFIRE_CHANCE = 0.20f;
    @ConfigValue(value = "learning.backfire_effect_fraction", comment = "Force du sort quand il se retourne contre le lanceur : dégâts, soin et durée des états (0,8 = 80 %).", min = 0)
    public static float BACKFIRE_EFFECT_FRACTION = 0.8f;
    @ConfigValue(value = "learning.witness_radius", comment = "Distance en blocs à laquelle un sort lancé peut être vu, et donc débloqué.", min = 1)
    public static double WITNESS_RADIUS = 16.0;
    public static float successChance(float gauge) {
        if (gauge >= LEARNED) return 1f;
        float t = Math.min(gauge, LAST_GAUGE_BEFORE_LEARNED) / LAST_GAUGE_BEFORE_LEARNED;
        return MIN_CHANCE + (MAX_CHANCE_BEFORE_LEARNED - MIN_CHANCE) * (float) Math.pow(t, CURVE_EXPONENT);
    }

    /**
     * Apprendre un sort bien inférieur à son rang va plus vite ; viser un sort supérieur à son rang est bien plus lent.
     * Apprendre en voyant le sort (sans sa formule) est plus lent que d'apprendre en le lisant.
     */
    public static float gaugeGain(MagicRank playerRank, MagicRank spellRank, boolean seenOnly) {
        int gap = playerRank.ordinal() - spellRank.ordinal();
        float gain = gap >= 0
                ? BASE_GAIN * (1f + GAIN_BONUS_PER_RANK_BELOW_PLAYER * gap)
                : BASE_GAIN * (float) Math.pow(GAIN_FACTOR_PER_RANK_ABOVE_PLAYER, -gap);
        return seenOnly ? gain / SEEN_ONLY_GAIN_DIVISOR : gain;
    }
    public record Debuffs(boolean doubledMana, boolean halvedEffect, boolean deviated, boolean backfire) {
        public static final Debuffs NONE = new Debuffs(false, false, false, false);
        public boolean any() {
            return doubledMana || halvedEffect || deviated || backfire;
        }
        public String describe() {
            List<String> parts = new ArrayList<>();
            if (doubledMana) parts.add("mana doublé");
            if (halvedEffect) parts.add("effet réduit");
            if (deviated) parts.add("trajectoire déviée");
            if (backfire) parts.add("retour de flamme");
            return Chat.dim(String.join(", ", parts));
        }
    }
    public static Debuffs rollDebuffs(RandomSource random, boolean canBackfire) {
        // Un retour de flamme exclut tous les autres malus.
        if (canBackfire && random.nextFloat() < BACKFIRE_CHANCE) {
            return new Debuffs(false, false, false, true);
        }
        return new Debuffs(
                random.nextFloat() < DEBUFF_CHANCE,
                random.nextFloat() < DEBUFF_CHANCE,
                random.nextFloat() < DEBUFF_CHANCE,
                false);
    }
    /** Un sort hybride exige le rang du sort dans chacune de ses écoles. */
    public static boolean meetsRank(PlayerProgressionData data, SpellDefinition def) {
        for (MagicSchool school : def.schools()) {
            if (!data.getSchoolRank(school).isAtLeast(def.rank())) return false;
        }
        return true;
    }

    /** Rang qui règle la vitesse d'apprentissage : le plus bas des rangs du joueur dans les écoles du sort. */
    public static MagicRank learningRank(PlayerProgressionData data, SpellDefinition def) {
        MagicRank lowest = null;
        for (MagicSchool school : def.schools()) {
            MagicRank rank = data.getSchoolRank(school);
            if (lowest == null || rank.ordinal() < lowest.ordinal()) lowest = rank;
        }
        return lowest;
    }
    /**
     * Lire un parchemin : le sort est débloqué avec sa formule exacte (apprentissage à vitesse normale).
     * @return true si le parchemin doit être consommé
     */
    public static boolean readScroll(ServerPlayer player, SpellDefinition def) {
        PlayerProgressionData data = player.getData(ModAttachments.PROGRESSION);
        if (!data.isManaAwakened()) {
            player.sendSystemMessage(Component.literal("Ces lignes ne me disent rien : mon " + Chat.mana("mana") + " n'est " + Chat.bad("pas éveillé") + "."));
            return false;
        }
        if (!meetsRank(data, def)) {
            player.sendSystemMessage(Component.literal("Ce sort " + Chat.bad("dépasse mon niveau") + " dans cette école."));
            return false;
        }
        if (data.isSpellLearned(def.id())) {
            player.sendSystemMessage(Component.literal("Je " + Chat.good("maîtrise déjà la formule") + " de ce sort."));
            return false;
        }
        if (data.isSpellUnlocked(def.id()) && !data.isSpellSeenOnly(def.id())) {
            player.sendSystemMessage(Component.literal("Je connais " + Chat.good("déjà la formule") + " de ce sort."));
            return false;
        }

        boolean alreadySeen = data.isSpellUnlocked(def.id());
        data.unlockSpell(def.id());
        data.setSpellSeenOnly(def.id(), false);
        player.sendSystemMessage(Component.literal(alreadySeen
                ? "Je connais maintenant la " + Chat.good("formule exacte") + " de " + Chat.schools(def.schools()) + " : " + Chat.good(def.id()) + ". L'apprentissage ira plus vite."
                : "J'ai lu la " + Chat.good("formule") + " de " + Chat.schools(def.schools()) + " : " + Chat.good(def.id()) + ". Je peux apprendre ce sort."));
        return true;
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
     * l'école est au moins celui du sort. Le mana doit être éveillé. Un sort appris ainsi est marqué "vu" : sans la
     * formule exacte, il progresse plus lentement.
     */
    public static void unlockForWitnesses(ServerPlayer caster, SpellDefinition def) {
        for (ServerPlayer witness : ((ServerLevel) caster.level()).players()) {
            if (witness == caster) continue;
            if (witness.distanceToSqr(caster) > WITNESS_RADIUS * WITNESS_RADIUS || !witness.hasLineOfSight(caster)) continue;
            PlayerProgressionData data = witness.getData(ModAttachments.PROGRESSION);
            if (!data.isManaAwakened() || data.isSpellUnlocked(def.id())) continue;
            if (!meetsRank(data, def)) continue;
            data.unlockSpell(def.id());
            data.setSpellSeenOnly(def.id(), true);
            witness.sendSystemMessage(Component.literal("J'ai vu " + Chat.schools(def.schools()) + " : "
                    + Chat.good(def.id()) + ". Je pourrais " + Chat.good("apprendre") + " ce sort, mais sans sa formule ce sera "
                    + Chat.dim("long") + "."));
        }
    }
}