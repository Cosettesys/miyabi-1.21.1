package net.cosette.miyabi.skills.battleaura;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.damage.ModDamageTypes;

/** Mesure l'entraînement du corps : blocs cassés, distance à pied, sauts, ennemis hostiles vaincus en mêlée. */
@EventBusSubscriber(modid = Miyabi.MODID)
public final class BattleAuraTracker {
    private BattleAuraTracker() {}

    /** Dégâts d'attaque minimum d'une arme pour que ses victimes comptent (épées, haches, pioches de fer...). */
    private static final double MIN_WEAPON_DAMAGE = 4.0;
    /** Un déplacement plus grand que ça en un tick (téléportation, glace...) n'est pas de la marche. */
    private static final double MAX_STEP_PER_TICK = 1.0;

    private static PlayerProgressionData data(ServerPlayer player) {
        return player.getData(ModAttachments.PROGRESSION);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) return;
        if (event.getPlayer() instanceof ServerPlayer player && !player.isCreative()) {
            data(player).addAuraCounter(AuraCriterion.BLOCKS_BROKEN, 1);
        }
    }
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            data(player).addAuraCounter(AuraCriterion.JUMPS, 1);
        }
    }
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Enemy)) return; // monstres hostiles uniquement
        DamageSource source = event.getSource();
        // Mêlée seulement : l'attaquant direct est le joueur lui-même (pas une flèche ni un sort).
        if (!(source.getEntity() instanceof ServerPlayer player) || source.getDirectEntity() != player) return;
        boolean technique = source.is(ModDamageTypes.SWORD_TECHNIQUE);
        if (technique || qualifiesAsWeapon(player.getMainHandItem())) {
            data(player).addAuraCounter(AuraCriterion.MELEE_KILLS, 1);
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AuraTrackerState state = player.getData(ModAttachments.AURA_TRACKER);
        double x = player.getX();
        double z = player.getZ();
        if (state.hasLast() && onFoot(player)) {
            double step = Math.hypot(x - state.getLastX(), z - state.getLastZ());
            if (step > 0 && step <= MAX_STEP_PER_TICK) {
                data(player).addAuraCounter(AuraCriterion.DISTANCE, step);
            }
        }
        state.setLast(x, z);
        if (player.tickCount % 20 == 0) {
            BattleAuraManager.checkLevelUp(player);
        }
    }
    private static boolean onFoot(ServerPlayer player) {
        return !player.isPassenger() && !player.isSpectator() && !player.getAbilities().flying
                && !player.isFallFlying() && !player.isSwimming();
    }
    /** Poings, ou tout objet tenu qui donne au moins 4 de dégâts d'attaque : épées, haches, outils solides. */
    private static boolean qualifiesAsWeapon(ItemStack stack) {
        if (stack.isEmpty()) return true;
        double[] bonus = {0.0};
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.value() == Attributes.ATTACK_DAMAGE.value()
                    && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                bonus[0] += modifier.amount();
            }
        });
        return 1.0 + bonus[0] >= MIN_WEAPON_DAMAGE; // 1 = dégâts de base du poing
    }
}