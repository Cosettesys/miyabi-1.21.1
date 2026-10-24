package net.cosette.miyabi.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

import java.util.ArrayList;
import java.util.List;

/**
 * Serveur -> client : tout ce que le menu d'un entraîneur affiche pour ce joueur.
 *
 * @param activeKind 0 aucune quête, 1 évaluation d'une technique, 2 épreuve de rang
 * @param activeRemainingTicks temps restant de l'épreuve de rang en ticks de jeu, ou -1
 * @param cooldownTicks délai d'attente restant avant de commencer une quête
 */
public record TrainerDataPayload(
        String style, String styleRank, String nextRank, boolean needsTrial, boolean qualified, long cooldownTicks,
        int activeKind, String activeTechnique, String activeTargetRank, boolean activeCompleted, long activeRemainingTicks,
        List<ObjectiveEntry> objectives, List<TechniqueRow> techniques
) implements CustomPacketPayload {
    public static final int STATUS_AVAILABLE = 0;
    public static final int STATUS_RANK_TOO_LOW = 1;
    public static final int STATUS_IN_PROGRESS = 2;
    public static final int STATUS_READY = 3;
    public record ObjectiveEntry(String type, String target, int required, int progress, String description) {}
    public record TechniqueRow(String id, String requiredRank, int status) {}
    public static final Type<TrainerDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "trainer_data"));
    public static final StreamCodec<FriendlyByteBuf, TrainerDataPayload> STREAM_CODEC =
            StreamCodec.of(TrainerDataPayload::write, TrainerDataPayload::read);
    private static void write(FriendlyByteBuf buf, TrainerDataPayload p) {
        buf.writeUtf(p.style());
        buf.writeUtf(p.styleRank());
        buf.writeUtf(p.nextRank());
        buf.writeBoolean(p.needsTrial());
        buf.writeBoolean(p.qualified());
        buf.writeVarLong(p.cooldownTicks());
        buf.writeVarInt(p.activeKind());
        buf.writeUtf(p.activeTechnique());
        buf.writeUtf(p.activeTargetRank());
        buf.writeBoolean(p.activeCompleted());
        buf.writeLong(p.activeRemainingTicks());
        buf.writeVarInt(p.objectives().size());
        for (ObjectiveEntry e : p.objectives()) {
            buf.writeUtf(e.type());
            buf.writeUtf(e.target());
            buf.writeVarInt(e.required());
            buf.writeVarInt(e.progress());
            buf.writeUtf(e.description(), 1024);
        }
        buf.writeVarInt(p.techniques().size());
        for (TechniqueRow e : p.techniques()) {
            buf.writeUtf(e.id());
            buf.writeUtf(e.requiredRank());
            buf.writeVarInt(e.status());
        }
    }
    private static TrainerDataPayload read(FriendlyByteBuf buf) {
        String style = buf.readUtf();
        String styleRank = buf.readUtf();
        String nextRank = buf.readUtf();
        boolean needsTrial = buf.readBoolean();
        boolean qualified = buf.readBoolean();
        long cooldown = buf.readVarLong();
        int activeKind = buf.readVarInt();
        String activeTechnique = buf.readUtf();
        String activeTargetRank = buf.readUtf();
        boolean activeCompleted = buf.readBoolean();
        long remaining = buf.readLong();
        int objectiveCount = buf.readVarInt();
        List<ObjectiveEntry> objectives = new ArrayList<>();
        for (int i = 0; i < objectiveCount; i++) {
            objectives.add(new ObjectiveEntry(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(1024)));
        }
        int techniqueCount = buf.readVarInt();
        List<TechniqueRow> techniques = new ArrayList<>();
        for (int i = 0; i < techniqueCount; i++) {
            techniques.add(new TechniqueRow(buf.readUtf(), buf.readUtf(), buf.readVarInt()));
        }
        return new TrainerDataPayload(style, styleRank, nextRank, needsTrial, qualified, cooldown,
                activeKind, activeTechnique, activeTargetRank, activeCompleted, remaining, objectives, techniques);
    }
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}