package net.cosette.miyabi.skills.battleaura;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.network.payload.AuraStatePayload;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.util.Chat;

/** Renforcement du corps : activation, niveau choisi, dépense de mana en continu, fatigue. */
public final class BattleAuraBuffManager {
    private BattleAuraBuffManager() {}

    public static final int STATE_INACTIVE = 0;
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_FATIGUE = 2;

    private static final ResourceLocation MOVE_SPEED_ID = ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "aura_move_speed");
    private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "aura_attack_speed");
    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "aura_attack_damage");

    private static boolean unlocked(PlayerProgressionData progression) {
        return progression.isManaAwakened() && progression.getBattleAuraLevel() >= BattleAuraBuffs.UNLOCK_LEVEL;
    }
    /** Touche relâchée sans molette : active ou désactive. Tant que le niveau 5 n'est pas atteint, ne fait rien du tout. */
    public static void toggle(ServerPlayer player) {
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        if (!unlocked(progression)) return;
        BattleAuraBuffData data = player.getData(ModAttachments.AURA_BUFF);
        if (data.isActive()) {
            deactivate(player, null);
            return;
        }
        long now = player.level().getGameTime();
        if (data.remainingFatigue(now) > 0) {
            message(player, "Mon corps est trop " + Chat.bad("épuisé") + " pour renforcer mon aura.");
            return;
        }
        int level = data.effectiveLevel(progression.getBattleAuraLevel());
        ManaData mana = player.getData(ModAttachments.MANA);
        if (mana.getCurrent() < drainPerTick(level)) {
            message(player, "Pas assez de " + Chat.mana("mana") + " pour renforcer mon corps.");
            return;
        }
        data.activate(mana.getTotalSpent());
        applyModifiers(player, level);
        message(player, "Mon corps est " + Chat.aura("renforcé") + ".");
    }
    /** Touche maintenue + molette : choisit parmi les niveaux débloqués. Sans effet avant le niveau 5. */
    public static void scroll(ServerPlayer player, int delta) {
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        if (!unlocked(progression) || delta == 0) return;
        BattleAuraBuffData data = player.getData(ModAttachments.AURA_BUFF);
        int unlockedLevel = progression.getBattleAuraLevel();
        int level = Math.max(1, Math.min(unlockedLevel, data.effectiveLevel(unlockedLevel) + delta));
        data.setSelectedLevel(level);
        if (data.isActive()) {
            applyModifiers(player, level);
        }
        message(player, "Renforcement du corps : niveau " + Chat.aura(level));
    }
    /** Appelée à chaque tick : dépense le mana, désactive à court de mana, et synchronise le client. */
    public static void tick(ServerPlayer player) {
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        BattleAuraBuffData data = player.getData(ModAttachments.AURA_BUFF);
        if (data.isActive()) {
            if (!unlocked(progression)) {
                deactivate(player, null);
            } else {
                double drain = drainPerTick(data.effectiveLevel(progression.getBattleAuraLevel()));
                // spend() remet aussi à zéro le délai de régénération : le mana ne se régénère pas pendant l'aura.
                if (!player.getData(ModAttachments.MANA).spend((float) drain)) {
                    deactivate(player, "Je n'ai plus assez de " + Chat.mana("mana") + " pour maintenir mon aura.");
                }
            }
        }
        long now = player.level().getGameTime();
        int state = data.isActive() ? STATE_ACTIVE : (data.remainingFatigue(now) > 0 ? STATE_FATIGUE : STATE_INACTIVE);
        boolean awakened = progression.isBattleAuraAwakened();
        if (data.needsSync(awakened, state)) {
            PacketDistributor.sendToPlayer(player, new AuraStatePayload(awakened, state));
            data.markSynced(awakened, state);
        }
    }
    /** Arrête le renforcement et calcule la fatigue d'après la part du mana max dépensée. */
    public static void deactivate(ServerPlayer player, String reason) {
        BattleAuraBuffData data = player.getData(ModAttachments.AURA_BUFF);
        if (!data.isActive()) return;
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        ManaData mana = player.getData(ModAttachments.MANA);
        // Tout le mana dépensé pendant le renforcement compte : le maintien de l'aura, mais aussi les sorts lancés.
        double spent = data.spentSinceActivation(mana.getTotalSpent());
        double spentPercent = spent / Math.max(1.0, mana.getMax()) * 100.0;
        int maxSteps = (int) Math.round(100.0 / BattleAuraBuffs.FATIGUE_STEP_PERCENT);
        int steps = Math.min(maxSteps, (int) (spentPercent / BattleAuraBuffs.FATIGUE_STEP_PERCENT));
        long fatigueTicks = Math.round(
                BattleAuraBuffs.maxFatigueSeconds(progression.getBattleAuraLevel()) * 20.0 * steps / maxSteps);
        removeModifiers(player);
        data.deactivate();
        String text = reason != null ? reason : "Mon aura se " + Chat.dim("dissipe") + ".";
        if (fatigueTicks > 0) {
            data.startFatigue(player.level().getGameTime(), fatigueTicks);
            text += " Je suis " + Chat.bad("épuisé") + ".";
        }
        message(player, text);
    }
    private static double drainPerTick(int level) {
        return BattleAuraBuffs.manaDrainPerSecond(level) / 20.0;
    }
    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), true); // au-dessus de la hotbar
    }
    // --- Bonus de statistiques ---
    private static void applyModifiers(ServerPlayer player, int level) {
        apply(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_ID, BattleAuraBuffs.moveSpeed(level));
        apply(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID, BattleAuraBuffs.attackSpeed(level));
        apply(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID, BattleAuraBuffs.attackDamage(level));
    }
    private static void apply(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    private static void removeModifiers(ServerPlayer player) {
        for (var entry : java.util.List.of(
                java.util.Map.entry(Attributes.MOVEMENT_SPEED, MOVE_SPEED_ID),
                java.util.Map.entry(Attributes.ATTACK_SPEED, ATTACK_SPEED_ID),
                java.util.Map.entry(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID))) {
            AttributeInstance instance = player.getAttribute(entry.getKey());
            if (instance != null) {
                instance.removeModifier(entry.getValue());
            }
        }
    }
}