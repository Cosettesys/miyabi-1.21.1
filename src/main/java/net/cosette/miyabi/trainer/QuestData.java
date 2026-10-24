package net.cosette.miyabi.trainer;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Quête en cours d'un joueur (une seule à la fois) : l'évaluation pour apprendre une technique, ou l'épreuve d'un
 * rang. Sauvegardée, et conservée à la mort : la mort n'arrête pas une épreuve.
 */
public final class QuestData {
    public enum Kind { NONE, LEARN, RANK_UP }
    /** Un objectif tiré : quantité à atteindre et avancement. */
    public static final class Objective {
        private final String definitionId;
        private final ObjectiveType type;
        private final String target;
        private final int required;
        private int progress;
        public Objective(String definitionId, ObjectiveType type, String target, int required, int progress) {
            this.definitionId = definitionId;
            this.type = type;
            this.target = target;
            this.required = required;
            this.progress = progress;
        }
        public String getDefinitionId() { return definitionId; }
        public ObjectiveType getType() { return type; }
        public String getTarget() { return target; }
        public int getRequired() { return required; }
        public int getProgress() { return progress; }
        public void setProgress(int value) { this.progress = Math.max(0, Math.min(required, value)); }
        public boolean isDone() { return progress >= required; }
    }
    private Kind kind = Kind.NONE;
    private SwordStyle style = SwordStyle.SWORD_GOD;
    private String techniqueId = "";
    private MagicRank targetRank = MagicRank.BEGINNER;
    private final List<Objective> objectives = new ArrayList<>();
    private long deadlineTick = 0;     // épreuve de rang : date limite en temps de jeu
    private boolean completed = false;
    private long cooldownUntilTick = 0; // pas de nouvelle quête avant cette date
    public Kind getKind() { return kind; }
    public SwordStyle getStyle() { return style; }
    public String getTechniqueId() { return techniqueId; }
    public MagicRank getTargetRank() { return targetRank; }
    public List<Objective> getObjectives() { return objectives; }
    public long getDeadlineTick() { return deadlineTick; }
    public boolean isCompleted() { return completed; }
    public long getCooldownUntilTick() { return cooldownUntilTick; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCooldownUntilTick(long tick) { this.cooldownUntilTick = tick; }
    public boolean isActive() { return kind != Kind.NONE; }
    public void start(Kind kind, SwordStyle style, String techniqueId, MagicRank targetRank,
                      List<Objective> rolled, long deadlineTick) {
        this.kind = kind;
        this.style = style;
        this.techniqueId = techniqueId;
        this.targetRank = targetRank;
        this.objectives.clear();
        this.objectives.addAll(rolled);
        this.deadlineTick = deadlineTick;
        this.completed = false;
    }
    /** Termine la quête (réussie, abandonnée ou échouée) sans toucher au délai d'attente. */
    public void clear() {
        this.kind = Kind.NONE;
        this.techniqueId = "";
        this.objectives.clear();
        this.deadlineTick = 0;
        this.completed = false;
    }
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", kind.name());
        tag.putString("style", style.name());
        tag.putString("technique", techniqueId);
        tag.putString("targetRank", targetRank.name());
        tag.putLong("deadline", deadlineTick);
        tag.putBoolean("completed", completed);
        tag.putLong("cooldownUntil", cooldownUntilTick);
        ListTag list = new ListTag();
        for (Objective objective : objectives) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", objective.getDefinitionId());
            entry.putString("type", objective.getType().name());
            entry.putString("target", objective.getTarget());
            entry.putInt("required", objective.getRequired());
            entry.putInt("progress", objective.getProgress());
            list.add(entry);
        }
        tag.put("objectives", list);
        return tag;
    }
    public static QuestData deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        QuestData data = new QuestData();
        try {
            data.kind = Kind.valueOf(tag.getString("kind"));
        } catch (IllegalArgumentException e) {
            data.kind = Kind.NONE;
        }
        try {
            data.style = SwordStyle.valueOf(tag.getString("style"));
        } catch (IllegalArgumentException e) {
            data.style = SwordStyle.SWORD_GOD;
        }
        data.techniqueId = tag.getString("technique");
        data.targetRank = MagicRank.fromName(tag.getString("targetRank"));
        data.deadlineTick = tag.getLong("deadline");
        data.completed = tag.getBoolean("completed");
        data.cooldownUntilTick = tag.getLong("cooldownUntil");
        ListTag list = tag.getList("objectives", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            try {
                ObjectiveType type = ObjectiveType.valueOf(entry.getString("type"));
                data.objectives.add(new Objective(entry.getString("id"), type, entry.getString("target"),
                        entry.getInt("required"), entry.getInt("progress")));
            } catch (IllegalArgumentException e) {
                // objectif d'un type disparu : ignoré
            }
        }
        if (data.kind != Kind.NONE && data.objectives.isEmpty() && !data.completed) {
            data.completed = true; // une quête sans objectif (ex. configuration à 0) est déjà remplie
        }
        return data;
    }
}