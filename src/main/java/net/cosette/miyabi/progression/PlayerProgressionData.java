package net.cosette.miyabi.progression;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.magic.MagicSchool;

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
    private final Map<SwordStyle, Integer> styleMastery = new EnumMap<>(SwordStyle.class);
    private final Map<String, Integer> techniqueUses = new HashMap<>();
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
    public int incrementSpellCasts(String spellId) {
        int value = getSpellCasts(spellId) + 1;
        spellCasts.put(spellId, value);
        return value;
    }
    public Map<String, Integer> getAllSpellCasts() {
        return Collections.unmodifiableMap(spellCasts);
    }
    public int getStyleMastery(SwordStyle style) {
        return styleMastery.getOrDefault(style, 0);
    }
    public void setStyleMastery(SwordStyle style, int value) {
        styleMastery.put(style, Math.max(0, value));
    }
    public int getTechniqueUses(String techniqueId) {
        return techniqueUses.getOrDefault(techniqueId, 0);
    }
    public void setTechniqueUses(String techniqueId, int value) {
        techniqueUses.put(techniqueId, Math.max(0, value));
    }
    public int incrementTechniqueUses(String techniqueId) {
        int value = getTechniqueUses(techniqueId) + 1;
        techniqueUses.put(techniqueId, value);
        return value;
    }
    public Map<String, Integer> getAllTechniqueUses() {
        return Collections.unmodifiableMap(techniqueUses);
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
        CompoundTag styleMasteryTag = new CompoundTag();
        for (SwordStyle style : SwordStyle.values()) {
            styleMasteryTag.putInt(style.name(), getStyleMastery(style));
        }
        tag.put("styleMastery", styleMasteryTag);
        CompoundTag uses = new CompoundTag();
        for (Map.Entry<String, Integer> entry : techniqueUses.entrySet()) {
            uses.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("techniqueUses", uses);
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
        CompoundTag styleMasteryTag = tag.getCompound("styleMastery");
        for (SwordStyle style : SwordStyle.values()) {
            if (styleMasteryTag.contains(style.name())) {
                data.styleMastery.put(style, styleMasteryTag.getInt(style.name()));
            }
        }
        CompoundTag uses = tag.getCompound("techniqueUses");
        for (String key : uses.getAllKeys()) {
            data.techniqueUses.put(key, uses.getInt(key));
        }
        return data;
    }
}