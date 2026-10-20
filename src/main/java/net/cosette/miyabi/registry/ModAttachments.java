package net.cosette.miyabi.registry;

import net.cosette.miyabi.skills.battleaura.AuraTrackerState;
import net.cosette.miyabi.skills.battleaura.BattleAuraBuffData;
import net.cosette.miyabi.skills.cast.SlotHudSyncState;
import net.cosette.miyabi.skills.swordtechniques.TechniqueCooldownData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.progression.MeditationData;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.skills.cast.CastData;
import net.cosette.miyabi.skills.cast.SilentCastData;
import net.cosette.miyabi.skills.cast.SpellSlotsData;
import net.cosette.miyabi.skills.damage.DamageLogData;
import net.cosette.miyabi.skills.damage.SchoolResistanceData;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.skills.mana.ManaDebugData;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Tous les attachments (données attachées aux joueurs/entités) du mod, déclarés au même endroit.
 * Les classes de données ne contiennent plus que leur logique et leur (dé)sérialisation.
 */
public final class ModAttachments {
    private ModAttachments() {}
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Miyabi.MODID);
    // --- Sauvegardées avec le joueur et conservées à la mort ---
    public static final Supplier<AttachmentType<ManaData>> MANA = persistent("mana",
            () -> new ManaData(0f, ManaData.DEFAULT_REGEN_FLAT, ManaData.DEFAULT_REGEN_SCALING_PERCENT), // le mana n'existe qu'après l'éveil
            ManaData::deserializeNBT, ManaData::serializeNBT, true);
    public static final Supplier<AttachmentType<SilentCastData>> SILENT_CAST = persistent("silent_cast",
            SilentCastData::new,
            SilentCastData::deserializeNBT, SilentCastData::serializeNBT, true);
    public static final Supplier<AttachmentType<SpellSlotsData>> SPELL_SLOTS = persistent("spell_slots",
            SpellSlotsData::new,
            SpellSlotsData::deserializeNBT, SpellSlotsData::serializeNBT, true);
    public static final Supplier<AttachmentType<PlayerProgressionData>> PROGRESSION = persistent("progression",
            PlayerProgressionData::new,
            PlayerProgressionData::deserializeNBT, PlayerProgressionData::serializeNBT, true);
    public static final Supplier<AttachmentType<TechniqueCooldownData>> TECHNIQUE_COOLDOWN = persistent("technique_cooldown",
            TechniqueCooldownData::new,
            TechniqueCooldownData::deserializeNBT, TechniqueCooldownData::serializeNBT, true);
    public static final Supplier<AttachmentType<BattleAuraBuffData>> AURA_BUFF = persistent("aura_buff",
            BattleAuraBuffData::new,
            BattleAuraBuffData::deserializeNBT, BattleAuraBuffData::serializeNBT, true);
    // --- Sauvegardée mais remise à zéro à la mort ---
    public static final Supplier<AttachmentType<SchoolResistanceData>> SCHOOL_RESISTANCE = persistent("school_resistance",
            SchoolResistanceData::new,
            SchoolResistanceData::deserializeNBT, SchoolResistanceData::serializeNBT, false);
    // --- Temporaires : état de session, jamais sauvegardé ---
    public static final Supplier<AttachmentType<CastData>> CAST = temporary("cast", CastData::new);
    public static final Supplier<AttachmentType<MeditationData>> MEDITATION = temporary("meditation", MeditationData::new);
    public static final Supplier<AttachmentType<ManaDebugData>> MANA_DEBUG = temporary("mana_debug", ManaDebugData::new);
    public static final Supplier<AttachmentType<DamageLogData>> DAMAGE_LOG = temporary("damage_log", DamageLogData::new);
    public static final Supplier<AttachmentType<SlotHudSyncState>> SLOT_HUD_SYNC = temporary("slot_hud_sync", SlotHudSyncState::new);
    public static final Supplier<AttachmentType<AuraTrackerState>> AURA_TRACKER = temporary("aura_tracker", AuraTrackerState::new);
    // --- Helpers ---
    private static <T> Supplier<AttachmentType<T>> persistent(
            String name, Supplier<T> factory,
            BiFunction<CompoundTag, HolderLookup.Provider, T> reader,
            BiFunction<T, HolderLookup.Provider, CompoundTag> writer,
            boolean keepOnDeath) {
        return ATTACHMENT_TYPES.register(name, () -> {
            AttachmentType.Builder<T> builder = AttachmentType.<T>builder(holder -> factory.get())
                    .serialize(new IAttachmentSerializer<CompoundTag, T>() {
                        @Override
                        public T read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            return reader.apply(tag, provider);
                        }
                        @Override
                        public CompoundTag write(T data, HolderLookup.Provider provider) {
                            return writer.apply(data, provider);
                        }
                    });
            if (keepOnDeath) {
                builder = builder.copyOnDeath();
            }
            return builder.build();
        });
    }
    private static <T> Supplier<AttachmentType<T>> temporary(String name, Supplier<T> factory) {
        return ATTACHMENT_TYPES.register(name, () -> AttachmentType.<T>builder(holder -> factory.get()).build());
    }
}