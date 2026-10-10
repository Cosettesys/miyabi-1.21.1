package net.cosette.miyabi.magic;

import net.cosette.miyabi.magic.spell.SpellDefinition;
import net.cosette.miyabi.magic.spell.SpellProjectile;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.progression.MasteryEffects;
import net.cosette.miyabi.progression.MasteryManager;
import net.cosette.miyabi.progression.MeditationData;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;

public final class CastManager {
    private CastManager() {}
    private static final ResourceLocation SILENT_SLOW_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "silent_cast_slow");
    public static final float SILENT_CAST_SLOW_PERCENT = 25f;
    public static final float DAMAGE_INTERRUPT_LOSS_PERCENT = 25f;
    public static final int KEEPALIVE_TIMEOUT_TICKS = 15;
    public static final float SILENT_CAST_SPEED_BONUS_PERCENT = 40f;
    /** Coût de base du sort, réduit par la maîtrise du joueur sur ce sort. */
    public static float effectiveManaCost(ServerPlayer player, SpellDefinition def) {
        int casts = player.getData(PlayerProgressionData.PROGRESSION).getSpellCasts(def.id());
        return def.manaCost() * MasteryEffects.manaCostMultiplier(casts);
    }
    /** Sort actuellement assigné à ce slot pour ce joueur, ou null si le slot est vide. */
    public static SpellDefinition spellInSlot(ServerPlayer player, int slot) {
        String id = player.getData(SpellSlotsData.SPELL_SLOTS).getSpellId(slot);
        return id.isEmpty() ? null : SpellRegistry.byId(id);
    }
    public static int effectiveDurationTicks(ServerPlayer player, int slot, boolean silent) {
        SpellDefinition def = spellInSlot(player, slot);
        float base = (def != null) ? def.baseCastDurationTicks() : SpellSlots.durationTicks(slot);
        if (def != null) {
            int casts = player.getData(PlayerProgressionData.PROGRESSION).getSpellCasts(def.id());
            base *= MasteryEffects.castTimeMultiplier(casts);
        }
        if (silent) {
            base *= (1f - SILENT_CAST_SPEED_BONUS_PERCENT / 100f);
        }
        return Math.max(1, Math.round(base));
    }
    public static void startCast(ServerPlayer player, CastData cast, int slot) {
        if (cast.isCasting()) return;
        if (player.getData(MeditationData.MEDITATION).isMeditating()) return;
        String slotId = player.getData(SpellSlotsData.SPELL_SLOTS).getSpellId(slot);
        // Technique d'épée : pas besoin de mana éveillé, effet immédiat (provisoire).
        SwordTechniqueDefinition technique = SwordTechniqueRegistry.byId(slotId);
        if (technique != null) {
            useTechnique(player, technique);
            return;
        }
        if (!player.getData(PlayerProgressionData.PROGRESSION).isManaAwakened()) {
            player.sendSystemMessage(Component.literal("Ton mana n'est pas éveillé."));
            return;
        }
        SpellDefinition def = spellInSlot(player, slot);
        if (def == null) {
            player.sendSystemMessage(Component.literal("Aucun sort dans ce slot."));
            return;
        }
        ManaData mana = player.getData(ManaData.MANA);
        if (mana.getCurrent() < effectiveManaCost(player, def)) {
            player.sendSystemMessage(Component.literal("Pas assez de mana pour ce sort."));
            return;
        }
        boolean silent = player.getData(SilentCastData.SILENT_CAST).isEnabled();
        var pos = player.position();
        cast.start(slot, player.level().getGameTime(), silent, pos.x, pos.y, pos.z);
        applyMovementEffect(player, silent);
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
        player.sendSystemMessage(Component.literal("Incantation annulée (" + reason + ")."));
    }
    public static void finishCast(ServerPlayer player, CastData cast) {
        int slot = cast.getSlot();
        boolean silent = cast.isSilentAtStart();
        int progressTicks = cast.getProgressTicks();
        clearMovementEffect(player);
        cast.end(player.level().getGameTime());
        SpellDefinition def = spellInSlot(player, slot);
        if (def == null) {
            player.sendSystemMessage(Component.literal("Sort du slot " + (slot + 1) + " lancé (placeholder, pas encore d'effet)."));
            return;
        }
        int duration = effectiveDurationTicks(player, slot, silent);
        float intensityPercent = Math.max(100f, (progressTicks * 100f) / duration);
        intensityPercent = Math.min(intensityPercent, def.maxIntensityPercent());
        // Le coût utilise la maîtrise AVANT ce lancer ; onSpellCast incrémente ensuite le compteur.
        float actualManaCost = effectiveManaCost(player, def) * (intensityPercent / 100f);
        player.getData(ManaData.MANA).spend(actualManaCost);
        MasteryManager.onSpellCast(player, def);
        var look = player.getLookAngle();
        float speed = def.computeSpeed();
        SpellProjectile projectile = new SpellProjectile(
                player.level(), player, look.x * speed, look.y * speed, look.z * speed,
                def.id(), silent, intensityPercent);
        player.level().addFreshEntity(projectile);
    }
    public static void interruptByDamage(ServerPlayer player, CastData cast) {
        if (!cast.isCasting() || cast.isSilentAtStart()) return;
        cast.reduceProgressByPercent(DAMAGE_INTERRUPT_LOSS_PERCENT);
        player.sendSystemMessage(Component.literal("Incantation perturbée !"));
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