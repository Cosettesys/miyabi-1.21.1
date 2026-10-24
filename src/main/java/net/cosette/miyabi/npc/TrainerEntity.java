package net.cosette.miyabi.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.cosette.miyabi.network.handler.TrainerPacketHandlers;
import net.cosette.miyabi.progression.SwordStyle;

/**
 * Entraîneur : un PNJ immobile et protégé, un par style de combat, qui ouvre un menu quand on lui parle.
 * Chaque joueur reçoit son propre menu, donc plusieurs joueurs peuvent parler au même entraîneur en même temps.
 */
public class TrainerEntity extends PathfinderMob {
    /** Style de combat enseigné (SWORD_GOD, NORTH_GOD ou WATER_GOD), synchronisé avec les clients. */
    private static final EntityDataAccessor<String> STYLE =
            SynchedEntityData.defineId(TrainerEntity.class, EntityDataSerializers.STRING);
    public TrainerEntity(EntityType<? extends TrainerEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }
    /**
     * Insensible à tout, même aux coups d'un joueur en créatif. Seuls /kill et la chute hors du monde
     * peuvent le retirer (pour le supprimer : /kill @e[type=miyabi:trainer]).
     */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !(source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD));
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STYLE, SwordStyle.SWORD_GOD.name());
    }
    public SwordStyle getStyle() {
        try {
            return SwordStyle.valueOf(entityData.get(STYLE));
        } catch (IllegalArgumentException e) {
            return SwordStyle.SWORD_GOD;
        }
    }
    public void setStyle(SwordStyle style) {
        entityData.set(STYLE, style.name());
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Style", getStyle().name());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Style")) {
            try {
                setStyle(SwordStyle.valueOf(tag.getString("Style")));
            } catch (IllegalArgumentException ignored) {
                // style inconnu : on garde celui par défaut
            }
        }
    }
    /** Nom affiché : "Entraîneur" suivi de son style (clés de langue entity.miyabi.trainer.<style>). */
    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.miyabi.trainer." + getStyle().name().toLowerCase());
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
    @Override
    public boolean isPushable() {
        return false;
    }
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider((id, inventory, p) -> new TrainerMenu(id, inventory, this),
                            Component.translatable("container.miyabi.trainer")),
                    buffer -> buffer.writeVarInt(getId()));
            TrainerPacketHandlers.sendSnapshot(serverPlayer, getStyle());
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }
}