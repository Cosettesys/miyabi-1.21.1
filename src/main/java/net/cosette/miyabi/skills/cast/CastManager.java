package net.cosette.miyabi.skills.cast;

import net.cosette.miyabi.config.ConfigValue;
import net.cosette.miyabi.progression.*;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.registry.ModAttributes;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.magic.SpellProjectile;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.skills.swordtechniques.TechniqueManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.util.Chat;
import net.cosette.miyabi.skills.learning.SpellLearning;
import net.cosette.miyabi.skills.magic.SpellCastContext;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;

public final class CastManager {
    private CastManager() {}
    private static final ResourceLocation SILENT_SLOW_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "silent_cast_slow");
    @ConfigValue(value = "cast.silent_cast_slow_percent", comment = "Ralentissement de déplacement pendant un silent cast, en pourcentage.", min = 0, max = 100)
    public static float SILENT_CAST_SLOW_PERCENT = 25f;
    @ConfigValue(value = "cast.damage_interrupt_loss_percent", comment = "Part de la charge perdue quand un joueur en incantation est blessé, en pourcentage.", min = 0, max = 100)
    public static float DAMAGE_INTERRUPT_LOSS_PERCENT = 25f;
    public static final int KEEPALIVE_TIMEOUT_TICKS = 15;
    @ConfigValue(value = "cast.silent_cast_speed_bonus_percent", comment = "Gain de vitesse d'incantation d'un silent cast, en pourcentage de temps en moins.", min = 0, max = 95)
    public static float SILENT_CAST_SPEED_BONUS_PERCENT = 40f;
    @ConfigValue(value = "cast.mana_max_gain_per_mana_spent", comment = "Mana maximum gagné par point de mana dépensé en lançant des sorts.", min = 0)
    public static float MANA_MAX_GAIN_PER_MANA_SPENT = 0.02f;
    @ConfigValue(value = "cast.silent_cast_damage_multiplier", comment = "Multiplicateur des dégâts d'un sort lancé en silent cast.", min = 0)
    public static float SILENT_CAST_DAMAGE_MULTIPLIER = 0.5f;
    /** Coût de base du sort, réduit par la maîtrise du joueur sur ce sort. */
    public static float effectiveManaCost(ServerPlayer player, SpellDefinition def) {
        int casts = player.getData(ModAttachments.PROGRESSION).getSpellCasts(def.id());
        return def.manaCost() * MasteryEffects.manaCostMultiplier(casts, def.masteryCastsRequired()) * StaffBonuses.costMultiplier(player, def.schools());
    }
    /** Sort actuellement assigné à ce slot pour ce joueur, ou null si le slot est vide. */
    public static SpellDefinition spellInSlot(ServerPlayer player, int slot) {
        String id = player.getData(ModAttachments.SPELL_SLOTS).getSpellId(slot);
        return id.isEmpty() ? null : SpellRegistry.byId(id);
    }
    public static int effectiveDurationTicks(ServerPlayer player, int slot, boolean silent) {
        SpellDefinition def = spellInSlot(player, slot);
        float base = (def != null) ? def.baseCastDurationTicks() : SpellSlots.durationTicks(slot);
        if (def != null) {
            int casts = player.getData(ModAttachments.PROGRESSION).getSpellCasts(def.id());
            base *= MasteryEffects.castTimeMultiplier(casts, def.masteryCastsRequired());
        }
        if (silent) {
            base *= (1f - SILENT_CAST_SPEED_BONUS_PERCENT / 100f);
        }
        // Vitesse d'incantation (bâtons, effets...) : 2 = deux fois plus vite, donc temps divisé par deux.
        base /= (float) Math.max(0.1, player.getAttributeValue(ModAttributes.CASTING_SPEED));
        return Math.max(1, Math.round(base));
    }
    /** Temps d'incantation en secondes de ce sort pour ce joueur (maîtrise et bâton compris), pour l'affichage dans le menu. */
    public static float castSeconds(ServerPlayer player, SpellDefinition def) {
        int casts = player.getData(ModAttachments.PROGRESSION).getSpellCasts(def.id());
        float base = def.baseCastDurationTicks() * MasteryEffects.castTimeMultiplier(casts, def.masteryCastsRequired());
        base /= (float) Math.max(0.1, player.getAttributeValue(ModAttributes.CASTING_SPEED));
        return base / 20f;
    }
    public static void startCast(ServerPlayer player, CastData cast, int slot) {
        if (cast.isCasting()) return;
        if (player.getData(ModAttachments.MEDITATION).isMeditating()) return;
        String slotId = player.getData(ModAttachments.SPELL_SLOTS).getSpellId(slot);
        // Technique d'épée : pas besoin de mana éveillé, effet immédiat (provisoire).
        SwordTechniqueDefinition technique = SwordTechniqueRegistry.byId(slotId);
        if (technique != null) {
            TechniqueManager.use(player, technique);
            return;
        }
        if (!player.getData(ModAttachments.PROGRESSION).isManaAwakened()) {
            player.sendSystemMessage(Component.literal("Ton " + Chat.mana("mana") + " n'est " + Chat.bad("pas éveillé") + "."));
            return;
        }
        SpellDefinition def = spellInSlot(player, slot);
        if (def == null) {
            player.sendSystemMessage(Component.literal(Chat.bad("Aucun sort") + " dans ce slot."));
            return;
        }
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        if (!progression.isSpellUnlocked(def.id())) {
            player.sendSystemMessage(Component.literal("Je " + Chat.bad("ne connais pas") + " ce sort."));
            return;
        }
        // On ne peut ni lancer ni apprendre un sort d'un rang supérieur au sien dans l'école.
        if (!SpellLearning.meetsRank(progression, def)) {
            player.sendSystemMessage(Component.literal("Ce sort " + Chat.bad("dépasse mon niveau") + " dans cette école."));
            return;
        }
        ManaData mana = player.getData(ModAttachments.MANA);
        if (mana.getCurrent() < effectiveManaCost(player, def)) {
            player.sendSystemMessage(Component.literal("Pas assez de " + Chat.mana("mana") + " pour ce sort."));
            return;
        }
        // Un sort pas encore appris ne peut pas être lancé en silent cast.
        boolean silent = player.getData(ModAttachments.SILENT_CAST).isEnabled() && progression.isSpellLearned(def.id());
        var pos = player.position();
        cast.start(slot, player.level().getGameTime(), silent, pos.x, pos.y, pos.z);
        applyMovementEffect(player, silent);
        IncantationSync.start(player, def, silent, effectiveDurationTicks(player, slot, silent), 0);
    }
    private static void useTechnique(ServerPlayer player, SwordTechniqueDefinition technique) {
        player.sendSystemMessage(Component.literal("Technique " + technique.id() + " ("
                + technique.style().name() + ", " + technique.rank().name() + ") : pas encore d'effet."));
    }
    public static void keepAlive(ServerPlayer player, CastData cast, int slot) {
        if (!cast.isCasting() || cast.getSlot() != slot) return;
        cast.refreshInput(player.level().getGameTime());
    }
    /** Appelée au relâchement de la touche : lance le sort si au moins 100% est atteint, sinon annule. */
    public static void release(ServerPlayer player, CastData cast) {
        if (!cast.isCasting()) return;
        int duration = effectiveDurationTicks(player, cast.getSlot(), cast.isSilentAtStart());
        if (cast.getProgressTicks() < duration) {
            cancelCast(player, cast, "relâchée");
            return;
        }
        finishCast(player, cast);
    }
    public static void cancelCast(ServerPlayer player, CastData cast, String reason) {
        if (!cast.isCasting()) return;
        clearMovementEffect(player);
        cast.end(player.level().getGameTime());
        IncantationSync.clear(player);
        player.sendSystemMessage(Component.literal("Incantation " + Chat.bad("annulée") + " (" + reason + ")."));
    }
    public static void finishCast(ServerPlayer player, CastData cast) {
        int slot = cast.getSlot();
        boolean silent = cast.isSilentAtStart();
        int progressTicks = cast.getProgressTicks();
        clearMovementEffect(player);
        cast.end(player.level().getGameTime());
        IncantationSync.clear(player);
        SpellDefinition def = spellInSlot(player, slot);
        if (def == null) {
            player.sendSystemMessage(Component.literal("Sort du slot " + (slot + 1) + " lancé (placeholder, pas encore d'effet)."));
            return;
        }
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        ManaData mana = player.getData(ModAttachments.MANA);
        RandomSource random = player.getRandom();
        int duration = effectiveDurationTicks(player, slot, silent);
        float intensityPercent = Math.max(100f, (progressTicks * 100f) / duration);
        intensityPercent = Math.min(intensityPercent, def.maxIntensityPercent());
        // Le coût utilise la maîtrise AVANT ce lancer ; onSpellCast incrémente ensuite le compteur.
        float manaCost = effectiveManaCost(player, def) * (intensityPercent / 100f);
        boolean learned = progression.isSpellLearned(def.id());
        SpellLearning.Debuffs debuffs = SpellLearning.Debuffs.NONE;
        if (!learned) {
            // Chaque tentative fait monter la jauge d'apprentissage, qu'elle réussisse ou non.
            float gaugeBefore = progression.getSpellLearning(def.id());
            float gaugeAfter = Math.min(SpellLearning.LEARNED,
                    gaugeBefore + SpellLearning.gaugeGain(SpellLearning.learningRank(progression, def), def.rank(),
                            progression.isSpellSeenOnly(def.id())));
            progression.setSpellLearning(def.id(), gaugeAfter);
            if (gaugeAfter >= SpellLearning.LEARNED) {
                player.sendSystemMessage(Component.literal("J'ai " + Chat.good("appris") + " "
                        + Chat.schools(def.schools()) + " : " + Chat.good(def.id()) + " !"));
            }
            if (random.nextFloat() >= SpellLearning.successChance(gaugeBefore)) {
                spendMana(mana, manaCost); // un échec consomme quand même le mana
                player.sendSystemMessage(Component.literal("Le sort " + Chat.bad("échoue") + "."));
                return;
            }
            debuffs = SpellLearning.rollDebuffs(random, def.canBackfire());
            if (debuffs.any()) {
                player.sendSystemMessage(Component.literal(
                        "Le sort est " + Chat.bad("instable") + " : " + debuffs.describe() + "."));
            }
        }
        if (debuffs.doubledMana()) {
            manaCost *= 2f;
        }
        if (mana.getCurrent() < manaCost) {
            // Pas assez de mana pour le coût doublé : le sort ne part pas, et le mana restant est perdu.
            spendMana(mana, mana.getCurrent());
            player.sendSystemMessage(Component.literal("Je n'ai pas assez de " + Chat.mana("mana")
                    + " : le sort " + Chat.bad("s'éteint") + "."));
            return;
        }
        spendMana(mana, manaCost);
        if (debuffs.backfire()) {
            // Retour de flamme : le sort est lancé sur le lanceur lui-même (dégâts, soin, états) à une fraction de sa force.
            def.effect().apply(player, def,
                    new SpellCastContext(player, player, silent, intensityPercent, SpellLearning.BACKFIRE_EFFECT_FRACTION));
            return;
        }
        if (learned) {
            MasteryManager.onSpellCast(player, def);
        }
        Vec3 direction = player.getLookAngle();
        if (debuffs.deviated()) {
            direction = SpellLearning.deviate(direction, random);
        }
        float effectScale = debuffs.halvedEffect() ? 0.5f : 1.0f;
        float speed = def.computeSpeed();
        SpellProjectile projectile = new SpellProjectile(
                player.level(), player, direction.x * speed, direction.y * speed, direction.z * speed,
                def.id(), silent, intensityPercent, effectScale);
        player.level().addFreshEntity(projectile);
        SpellLearning.unlockForWitnesses(player, def);
    }
    /** Dépense du mana ; la réserve grandit lentement (plafonnée comme pour la méditation). */
    private static void spendMana(ManaData mana, float amount) {
        mana.spend(amount);
        mana.setMax(Math.min(MeditationManager.MANA_HARD_CAP, mana.getMax() + amount * MANA_MAX_GAIN_PER_MANA_SPENT));
    }
    public static void interruptByDamage(ServerPlayer player, CastData cast) {
        if (!cast.isCasting() || cast.isSilentAtStart()) return;
        cast.reduceProgressByPercent(DAMAGE_INTERRUPT_LOSS_PERCENT);
        // Les mots affichés reculent avec la charge
        SpellDefinition interrupted = spellInSlot(player, cast.getSlot());
        if (interrupted != null) {
            IncantationSync.start(player, interrupted, false, effectiveDurationTicks(player, cast.getSlot(), false), cast.getProgressTicks());
        }
        player.sendSystemMessage(Component.literal("Incantation " + Chat.bad("perturbée") + " !"));
    }
    private static void applyMovementEffect(ServerPlayer player, boolean silent) {
        if (!silent) return;
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;
        attribute.addTransientModifier(new AttributeModifier(
                SILENT_SLOW_MODIFIER_ID, -(SILENT_CAST_SLOW_PERCENT / 100f), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    private static void clearMovementEffect(ServerPlayer player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;
        attribute.removeModifier(SILENT_SLOW_MODIFIER_ID);
    }
}