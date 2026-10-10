package net.cosette.miyabi.progression;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.magic.spell.MagicSchool;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class PlayerProgressionData {
    private final Map<MagicSchool, MagicRank> schoolRanks = new EnumMap<>(MagicSchool.class);
    private final Map<SwordStyle, MagicRank> styleRanks = new EnumMap<>(SwordStyle.class);
    private final Map<MagicSchool, Integer> schoolMastery = new EnumMap<>(MagicSchool.class);
    private final Map<String, Integer> spellCasts = new HashMap<>();
    private boolean manaAwakened = false;
    private boolean battleAuraAwakened = false;
    private int battleAuraLevel = 0;
    private float awakeningProgress = 0f;
    public float getAwakeningProgress() { return awakeningProgress; }
    public void setAwakeningProgress(float value) { this.awakeningProgress = Math.max(0f, value); }
    public MagicRank getSchoolRank(MagicSchool school) {
        return schoolRanks.getOrDefault(school, MagicRank.BEGINNER);
    }
    public void setSchoolRank(MagicSchool school, MagicRank rank) {
        schoolRanks.put(school, rank);
    }
    public MagicRank getStyleRank(SwordStyle style) {
        return styleRanks.getOrDefault(style, MagicRank.BEGINNER);
    }
    public void setStyleRank(SwordStyle style, MagicRank rank) {
        styleRanks.put(style, rank);
    }
    public boolean isManaAwakened() { return manaAwakened; }
    public void setManaAwakened(boolean value) { this.manaAwakened = value; }
    public boolean isBattleAuraAwakened() { return battleAuraAwakened; }
    public void setBattleAuraAwakened(boolean value) { this.battleAuraAwakened = value; }
    public int getBattleAuraLevel() { return battleAuraLevel; }
    public void setBattleAuraLevel(int level) { this.battleAuraLevel = Math.max(0, level); }
    public int getSchoolMastery(MagicSchool school) {
        return schoolMastery.getOrDefault(school, 0);
    }
    public void setSchoolMastery(MagicSchool school, int value) {
        schoolMastery.put(school, Math.max(0, value));
    }
    public int getSpellCasts(String spellId) {
        return spellCasts.getOrDefault(spellId, 0);
    }
    public void setSpellCasts(String spellId, int value) {
        spellCasts.put(spellId, Math.max(0, value));
    }
    /** @return le nouveau total de lancers de ce sort. */
    public int incrementSpellCasts(String spellId) {
        int value = getSpellCasts(spellId) + 1;
        spellCasts.put(spellId, value);
        return value;
    }
    public Map<String, Integer> getAllSpellCasts() {
        return Collections.unmodifiableMap(spellCasts);
    }
    // --- Sérialisation ---
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag schools = new CompoundTag();
        for (MagicSchool school : MagicSchool.values()) {
            schools.putString(school.name(), getSchoolRank(school).name());
        }
        tag.put("schools", schools);
        CompoundTag styles = new CompoundTag();
        for (SwordStyle style : SwordStyle.values()) {
            styles.putString(style.name(), getStyleRank(style).name());
        }
        tag.put("styles", styles);
        tag.putBoolean("manaAwakened", manaAwakened);
        tag.putBoolean("battleAuraAwakened", battleAuraAwakened);
        tag.putInt("battleAuraLevel", battleAuraLevel);
        tag.putFloat("awakeningProgress", awakeningProgress);
        CompoundTag mastery = new CompoundTag();
        for (MagicSchool school : MagicSchool.values()) {
            mastery.putInt(school.name(), getSchoolMastery(school));
        }
        tag.put("schoolMastery", mastery);
        CompoundTag casts = new CompoundTag();
        for (Map.Entry<String, Integer> entry : spellCasts.entrySet()) {
            casts.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("spellCasts", casts);
        return tag;
    }
    public static PlayerProgressionData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        PlayerProgressionData data = new PlayerProgressionData();
        CompoundTag schools = tag.getCompound("schools");
        for (MagicSchool school : MagicSchool.values()) {
            if (schools.contains(school.name())) {
                data.schoolRanks.put(school, MagicRank.fromName(schools.getString(school.name())));
            }
        }
        CompoundTag styles = tag.getCompound("styles");
        for (SwordStyle style : SwordStyle.values()) {
            if (styles.contains(style.name())) {
                data.styleRanks.put(style, MagicRank.fromName(styles.getString(style.name())));
            }
        }
        data.manaAwakened = tag.getBoolean("manaAwakened");
        data.battleAuraAwakened = tag.getBoolean("battleAuraAwakened");
        data.battleAuraLevel = tag.getInt("battleAuraLevel");
        data.awakeningProgress = tag.getFloat("awakeningProgress");
        CompoundTag mastery = tag.getCompound("schoolMastery");
        for (MagicSchool school : MagicSchool.values()) {
            if (mastery.contains(school.name())) {
                data.schoolMastery.put(school, mastery.getInt(school.name()));
            }
        }
        CompoundTag casts = tag.getCompound("spellCasts");
        for (String key : casts.getAllKeys()) {
            data.spellCasts.put(key, casts.getInt(key));
        }
        return data;
    }
    // --- Attachment ---
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Miyabi.MODID);
    public static final Supplier<AttachmentType<PlayerProgressionData>> PROGRESSION =
            ATTACHMENT_TYPES.register("progression", () -> AttachmentType.builder(holder -> new PlayerProgressionData())
                    .serialize(new IAttachmentSerializer<CompoundTag, PlayerProgressionData>() {
                        @Override
                        public PlayerProgressionData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                            return deserializeNBT(tag, provider);
                        }
                        @Override
                        public CompoundTag write(PlayerProgressionData data, HolderLookup.Provider provider) {
                            return data.serializeNBT(provider);
                        }
                    })
                    .copyOnDeath()
                    .build());
}