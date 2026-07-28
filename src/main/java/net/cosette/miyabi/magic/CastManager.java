package net.cosette.miyabi.magic;

import net.cosette.miyabi.magic.spell.SpellDefinition;
import net.cosette.miyabi.magic.spell.SpellProjectile;
import net.cosette.miyabi.magic.spell.SpellRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.cosette.miyabi.Miyabi;

public final class CastManager {
    private CastManager() {}
    private static final ResourceLocation LOCK_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "cast_movement_lock");
    private static final ResourceLocation SILENT_SLOW_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "silent_cast_slow");
    public static final float SILENT_CAST_SLOW_PERCENT = 25f;
    public static final float DAMAGE_INTERRUPT_LOSS_PERCENT = 25f; // placeholder, à équilibrer
    public static final int KEEPALIVE_TIMEOUT_TICKS = 15; // paquets attendus toutes les 10 ticks
    public static void startCast(ServerPlayer player, CastData cast, int slot) {
        if (cast.isCasting()) return;
        boolean silent = player.getData(SilentCastData.SILENT_CAST).isEnabled();
        var pos = player.position();
        cast.start(slot, player.level().getGameTime(), silent, pos.x, pos.y, pos.z);
        applyMovementEffect(player, silent);
    }
    public static void keepAlive(ServerPlayer player, CastData cast, int slot) {
        if (!cast.isCasting() || cast.getSlot() != slot) return;
        cast.refreshInput(player.level().getGameTime());
    }
    public static void cancelCast(ServerPlayer player, CastData cast, String reason) {
        if (!cast.isCasting()) return;
        clearMovementEffect(player);
        cast.reset();
        player.sendSystemMessage(Component.literal("Incantation annulée (" + reason + ")."));
    }
    public static void finishCast(ServerPlayer player, CastData cast) {
        int slot = cast.getSlot();
        boolean silent = cast.isSilentAtStart();
        clearMovementEffect(player);
        cast.end(player.level().getGameTime());
        SpellDefinition def = SpellRegistry.forSlot(slot);
        if (def == null) {
            player.sendSystemMessage(Component.literal("Sort du slot " + (slot + 1) + " lancé (placeholder, pas encore d'effet)."));
            return;
        }
        var look = player.getLookAngle();
        float speed = def.computeSpeed();
        SpellProjectile projectile = new SpellProjectile(
                player.level(), player, look.x * speed, look.y * speed, look.z * speed, def.id(), silent);
        player.level().addFreshEntity(projectile);
    }
    public static void interruptByDamage(ServerPlayer player, CastData cast) {
        if (!cast.isCasting() || cast.isSilentAtStart()) return; // silent cast : pas d'interruption pour l'instant
        cast.reduceProgressByPercent(DAMAGE_INTERRUPT_LOSS_PERCENT);
        player.sendSystemMessage(Component.literal("Incantation perturbée !"));
    }
    private static void applyMovementEffect(ServerPlayer player, boolean silent) {
        if (!silent) return; // le cast normal est géré par l'ancrage de position, pas par un attribut
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
    public static final float SILENT_CAST_SPEED_BONUS_PERCENT = 40f;
    public static int effectiveDurationTicks(int slot, boolean silent) {
        int base = SpellSlots.durationTicks(slot);
        if (!silent) return base;
        return Math.max(1, Math.round(base * (1f - SILENT_CAST_SPEED_BONUS_PERCENT / 100f)));
    }
}