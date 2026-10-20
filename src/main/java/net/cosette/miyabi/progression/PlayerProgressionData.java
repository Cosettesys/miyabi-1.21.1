package net.cosette.miyabi.progression;

import net.cosette.miyabi.skills.battleaura.AuraCriterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.skills.magic.MagicSchool;

import java.util.*;
import java.util.function.Supplier;

public final class PlayerProgressionData {
    private final Map<MagicSchool, MagicRank> schoolRanks = new EnumMap<>(MagicSchool.class);
    private final Map<SwordStyle, MagicRank> styleRanks = new EnumMap<>(SwordStyle.class);
    private final Map<MagicSchool, Integer> schoolMastery = new EnumMap<>(MagicSchool.class);
    private final Map<String, Integer> spellCasts = new HashMap<>();
    private final Map<SwordStyle, Integer> styleMastery = new EnumMap<>(SwordStyle.class);
    private final Map<String, Integer> techniqueUses = new HashMap<>();
    private boolean manaAwakened = false;
    private final Map<AuraCriterion, Double> auraCounters = new EnumMap<>(AuraCriterion.class);
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
    public boolean isBattleAuraAwakened() { return battleAuraLevel >= 1; }
    public void setBattleAuraAwakened(boolean value) { this.battleAuraLevel = value ? Math.max(1, battleAuraLevel) : 0; }
    public int getBattleAuraLevel() { return battleAuraLevel; }
    public void setBattleAuraLevel(int level) { this.battleAuraLevel = Math.max(0, level); }
    /** Jauge d'apprentissage des sorts : absent = pas débloqué, 0 à 99 = à apprendre, 100 = appris. */
    private final Map<String, Float> spellLearning = new HashMap<>();
    /** Sorts débloqués en les voyant lancer (sans en lire la formule) : ils s'apprennent plus lentement. */
    private final Set<String> spellSeenOnly = new HashSet<>();
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
    public boolean isSpellUnlocked(String spellId) {
        return spellLearning.containsKey(spellId);
    }
    public float getSpellLearning(String spellId) {
        return spellLearning.getOrDefault(spellId, 0f);
    }
    public boolean isSpellLearned(String spellId) {
        return getSpellLearning(spellId) >= 100f;
    }
    /** Débloque le sort (le rend visible et assignable) sans le faire progresser. */
    public void unlockSpell(String spellId) {
        spellLearning.putIfAbsent(spellId, 0f);
    }
    /** Fixe la jauge d'apprentissage (0 à 100) ; une valeur négative reverrouille le sort. */
    public void setSpellLearning(String spellId, float value) {
        if (value < 0f) {
            spellLearning.remove(spellId);
        } else {
            spellLearning.put(spellId, Math.min(100f, value));
        }
    }
    public boolean isSpellSeenOnly(String spellId) {
        return spellSeenOnly.contains(spellId);
    }

    public void setSpellSeenOnly(String spellId, boolean seenOnly) {
        if (seenOnly) {
            spellSeenOnly.add(spellId);
        } else {
            spellSeenOnly.remove(spellId);
        }
    }
    public Map<String, Float> getAllSpellLearning() {
        return Collections.unmodifiableMap(spellLearning);
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
        CompoundTag learning = new CompoundTag();
        for (Map.Entry<String, Float> entry : spellLearning.entrySet()) {
            learning.putFloat(entry.getKey(), entry.getValue());
        }
        tag.put("spellLearning", learning);
        CompoundTag seen = new CompoundTag();
        for (String id : spellSeenOnly) {
            seen.putBoolean(id, true);
        }
        tag.put("spellSeenOnly", seen);
        CompoundTag counters = new CompoundTag();
        for (AuraCriterion criterion : AuraCriterion.values()) {
            counters.putDouble(criterion.name(), getAuraCounter(criterion));
        }
        tag.put("auraCounters", counters);
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
        CompoundTag learning = tag.getCompound("spellLearning");
        for (String key : learning.getAllKeys()) {
            data.spellLearning.put(key, learning.getFloat(key));
        }
        CompoundTag seen = tag.getCompound("spellSeenOnly");
        for (String key : seen.getAllKeys()) {
            data.spellSeenOnly.add(key);
        }
        // Anciennes sauvegardes : un sort déjà lancé est considéré comme appris.
        for (Map.Entry<String, Integer> entry : data.spellCasts.entrySet()) {
            if (entry.getValue() > 0) {
                data.spellLearning.putIfAbsent(entry.getKey(), 100f);
            }
        }
        CompoundTag counters = tag.getCompound("auraCounters");
        for (AuraCriterion criterion : AuraCriterion.values()) {
            if (counters.contains(criterion.name())) {
                data.auraCounters.put(criterion, counters.getDouble(criterion.name()));
            }
        }
        // Anciennes sauvegardes : l'éveil était un booléen séparé du niveau.
        if (tag.getBoolean("battleAuraAwakened") && data.battleAuraLevel < 1) {
            data.battleAuraLevel = 1;
        }
        return data;
    }
    public double getAuraCounter(AuraCriterion criterion) {
        return auraCounters.getOrDefault(criterion, 0.0);
    }

    public void setAuraCounter(AuraCriterion criterion, double value) {
        auraCounters.put(criterion, Math.max(0.0, value));
    }

    public void addAuraCounter(AuraCriterion criterion, double amount) {
        auraCounters.put(criterion, getAuraCounter(criterion) + amount);
    }
}