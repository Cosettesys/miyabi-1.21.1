package net.cosette.miyabi.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.cosette.miyabi.network.payload.TrainerDataPayload;
import net.cosette.miyabi.network.payload.TrainerRequestPayload;
import net.cosette.miyabi.npc.TrainerMenu;

import java.util.List;

/** Menu de l'entraîneur : onglet Apprentissage (techniques de son style) et onglet Rank-up (épreuve de rang). */
public class TrainerScreen extends AbstractContainerScreen<TrainerMenu> {
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 214;
    private static final int LINE_H = 11;
    private static final int LIST_ROWS = 10;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFFAAAAAA;
    private static final int DIM = 0xFF888888;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int ORANGE = 0xFFFFAA33;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private enum Tab { LEARNING, RANK_UP }
    private Tab tab = Tab.LEARNING;
    private String selected = null;
    private int listScroll = 0;
    private Button learningTab;
    private Button rankUpTab;
    private Button mainButton;
    private Button abandonButton;
    public TrainerScreen(TrainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_W;
        this.imageHeight = PANEL_H;
        this.inventoryLabelY = 10000; // pas d'inventaire dans ce menu
        this.titleLabelY = 6;
    }
    @Override
    protected void init() {
        super.init();
        learningTab = addRenderableWidget(Button.builder(Component.literal("Apprentissage"), b -> tab = Tab.LEARNING)
                .bounds(leftPos + 8, topPos + 20, 100, 18).build());
        rankUpTab = addRenderableWidget(Button.builder(Component.literal("Rank-up"), b -> tab = Tab.RANK_UP)
                .bounds(leftPos + 112, topPos + 20, 80, 18).build());
        mainButton = addRenderableWidget(Button.builder(Component.empty(), b -> onMain())
                .bounds(leftPos + 8, topPos + PANEL_H - 26, 160, 20).build());
        abandonButton = addRenderableWidget(Button.builder(Component.literal("Abandonner"), b -> send("abandon", ""))
                .bounds(leftPos + PANEL_W - 108, topPos + PANEL_H - 26, 100, 20).build());
    }
    private static void send(String action, String arg) {
        PacketDistributor.sendToServer(new TrainerRequestPayload(action, arg));
    }
    private TrainerDataPayload.TechniqueRow selectedRow(TrainerDataPayload data) {
        for (TrainerDataPayload.TechniqueRow row : data.techniques()) {
            if (row.id().equals(selected)) return row;
        }
        return null;
    }
    private void onMain() {
        TrainerDataPayload data = ClientTrainerState.get();
        if (data == null) return;
        if (tab == Tab.LEARNING) {
            TrainerDataPayload.TechniqueRow row = selectedRow(data);
            if (row == null) return;
            if (row.status() == TrainerDataPayload.STATUS_READY) send("claim", "");
            else if (row.status() == TrainerDataPayload.STATUS_AVAILABLE) send("learn_start", row.id());
        } else {
            if (data.activeKind() == 2 && data.activeCompleted()) send("claim", "");
            else if (data.activeKind() == 0 && data.needsTrial() && data.qualified()) send("rankup_start", "");
        }
    }
    private void refreshButtons(TrainerDataPayload data) {
        learningTab.active = tab != Tab.LEARNING;
        rankUpTab.active = tab != Tab.RANK_UP;
        abandonButton.visible = data.activeKind() == 1 || data.activeKind() == 2;
        mainButton.visible = false;
        boolean idle = data.activeKind() == 0 && data.cooldownTicks() <= 0;
        if (tab == Tab.LEARNING) {
            TrainerDataPayload.TechniqueRow row = selectedRow(data);
            if (row != null && row.status() == TrainerDataPayload.STATUS_READY) {
                showMain("Apprendre", true);
            } else if (row != null && row.status() == TrainerDataPayload.STATUS_AVAILABLE) {
                showMain("Commencer l'évaluation", idle);
            }
        } else {
            if (data.activeKind() == 2 && data.activeCompleted()) {
                showMain("Valider l'épreuve", true);
            } else if (data.needsTrial() && data.qualified() && !data.nextRank().isEmpty() && data.activeKind() == 0) {
                showMain("Passer l'épreuve", idle);
            }
        }
    }
    private void showMain(String label, boolean active) {
        mainButton.setMessage(Component.literal(label));
        mainButton.visible = true;
        mainButton.active = active;
    }
    // ---------- affichage ----------
    private int line(GuiGraphics g, String text, int x, int y, int color) {
        g.drawString(this.font, text, x, y, color, false);
        return y + LINE_H;
    }
    /** Texte d'un objectif. {count} d'une description personnalisée est remplacé par la quantité. */
    private static String describe(TrainerDataPayload.ObjectiveEntry o) {
        if (!o.description().isEmpty()) {
            return o.description().replace("{count}", String.valueOf(o.required()));
        }
        return switch (o.type()) {
            case "KILL_ENTITY" -> "Vaincre " + o.required() + " " + entityName(o.target());
            case "KILL_TAG" -> "Vaincre " + o.required() + " " + tagName(o.target());
            case "KILL_HOSTILE" -> "Vaincre " + o.required() + " monstres hostiles";
            case "KILL_HOSTILE_UNARMED" -> "Vaincre " + o.required() + " monstres hostiles à mains nues";
            case "MASTER_TECHNIQUE" -> "Maîtriser la technique " + o.target();
            case "LEARN_TECHNIQUE" -> "Apprendre la technique " + o.target();
            case "AURA_LEVEL" -> "Atteindre le niveau " + o.required() + " de battle aura";
            default -> o.type();
        };
    }
    private static String entityName(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key != null && BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
            return type.getDescription().getString();
        }
        return id;
    }
    private static String tagName(String id) {
        return switch (id) {
            case "minecraft:undead" -> "morts-vivants";
            case "minecraft:arthropod" -> "arthropodes";
            case "minecraft:raiders" -> "pillards";
            case "minecraft:aquatic" -> "créatures aquatiques";
            default -> id;
        };
    }
    private int drawObjectives(GuiGraphics g, TrainerDataPayload data, int x, int y) {
        for (TrainerDataPayload.ObjectiveEntry o : data.objectives()) {
            boolean done = o.progress() >= o.required();
            y = line(g, describe(o), x, y, done ? GREEN : WHITE);
            float ratio = o.required() <= 0 ? 1f : Mth.clamp(o.progress() / (float) o.required(), 0f, 1f);
            int barX = x;
            int barW = 120;
            g.fill(barX - 1, y, barX + barW + 1, y + 7, 0xFF000000);
            g.fill(barX, y + 1, barX + barW, y + 6, 0xFF333333);
            g.fill(barX, y + 1, barX + Math.round(barW * ratio), y + 6, done ? 0xFF55DD55 : 0xFFCC9933);
            g.drawString(this.font, o.progress() + "/" + o.required(), barX + barW + 6, y - 1, GREY, false);
            y += 12;
        }
        return y;
    }
    private static String duration(long ticks) {
        long hours = ticks / 1000;
        long minutes = (ticks % 1000) * 60 / 1000;
        return hours + " h " + String.format("%02d", minutes) + " (temps de jeu)";
    }
    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xDD101018);
        g.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF5577AA);
        TrainerDataPayload data = ClientTrainerState.get();
        if (data == null) {
            g.drawString(this.font, "...", leftPos + 10, topPos + 50, GREY, false);
            return;
        }
        refreshButtons(data);
        // Temps de jeu écoulé depuis la réception : sert à décompter l'épreuve
        Minecraft mc = Minecraft.getInstance();
        long elapsed = mc.level == null ? 0 : Math.max(0, mc.level.getGameTime() - ClientTrainerState.getReceivedAtGameTime());
        if (tab == Tab.LEARNING) {
            renderLearning(g, data, elapsed);
        } else {
            renderRankUp(g, data, elapsed);
        }
    }
    private void renderLearning(GuiGraphics g, TrainerDataPayload data, long elapsed) {
        int listX = leftPos + 8;
        int listY = topPos + 44;
        List<TrainerDataPayload.TechniqueRow> rows = data.techniques();
        if (rows.isEmpty()) {
            line(g, "Je n'ai plus rien à t'enseigner.", listX, listY, GREY);
        }
        int maxScroll = Math.max(0, rows.size() - LIST_ROWS);
        listScroll = Mth.clamp(listScroll, 0, maxScroll);
        for (int i = 0; i < LIST_ROWS && listScroll + i < rows.size(); i++) {
            TrainerDataPayload.TechniqueRow row = rows.get(listScroll + i);
            int color = switch (row.status()) {
                case TrainerDataPayload.STATUS_IN_PROGRESS -> ORANGE;
                case TrainerDataPayload.STATUS_READY -> GREEN;
                case TrainerDataPayload.STATUS_RANK_TOO_LOW -> DIM;
                default -> WHITE;
            };
            if (row.id().equals(selected)) color = YELLOW;
            g.drawString(this.font, this.font.plainSubstrByWidth(row.id() + " [" + row.requiredRank() + "]", 150), listX, listY + i * LINE_H, color, false);
        }
        // Détail de la technique choisie
        int x = leftPos + 172;
        int y = topPos + 44;
        TrainerDataPayload.TechniqueRow row = selectedRow(data);
        if (row == null) {
            if (data.activeKind() == 1 && !data.activeTechnique().isEmpty()) {
                y = line(g, "Évaluation en cours : " + data.activeTechnique(), x, y, ORANGE);
            } else {
                line(g, "Choisis une technique.", x, y, GREY);
            }
        } else {
            y = line(g, row.id(), x, y, YELLOW);
            y = line(g, "Rang requis : " + row.requiredRank(), x, y, WHITE);
            y += 4;
            switch (row.status()) {
                case TrainerDataPayload.STATUS_RANK_TOO_LOW -> line(g, "Ton rang dans ce style est insuffisant.", x, y, RED);
                case TrainerDataPayload.STATUS_IN_PROGRESS -> {
                    y = line(g, "Évaluation en cours :", x, y, ORANGE);
                    drawObjectives(g, data, x, y);
                }
                case TrainerDataPayload.STATUS_READY -> {
                    y = line(g, "Évaluation réussie.", x, y, GREEN);
                    drawObjectives(g, data, x, y);
                }
                default -> {
                    y = line(g, "Je te donnerai des objectifs à accomplir.", x, y, GREY);
                    if (data.activeKind() != 0) {
                        line(g, "Une autre évaluation est en cours.", x, y, RED);
                    } else if (data.cooldownTicks() > 0) {
                        line(g, "Attends encore " + duration(Math.max(0, data.cooldownTicks() - elapsed)), x, y, RED);
                    }
                }
            }
        }
    }
    private void renderRankUp(GuiGraphics g, TrainerDataPayload data, long elapsed) {
        int x = leftPos + 10;
        int y = topPos + 46;
        y = line(g, "Style : " + data.style() + "   Rang : " + data.styleRank()
                + (data.nextRank().isEmpty() ? "" : "  ->  " + data.nextRank()), x, y, WHITE);
        y += 6;
        if (data.nextRank().isEmpty()) {
            line(g, "Tu as atteint le rang maximal.", x, y, GREEN);
        } else if (!data.needsTrial()) {
            line(g, "Ce rang se gagne par la maîtrise, sans épreuve.", x, y, GREY);
        } else if (data.activeKind() == 2) {
            if (data.activeCompleted()) {
                y = line(g, "Épreuve réussie : reviens la valider.", x, y, GREEN);
            } else {
                long remaining = Math.max(0, data.activeRemainingTicks() - elapsed);
                y = line(g, "Épreuve du rang " + data.activeTargetRank() + " en cours.", x, y, ORANGE);
                y = line(g, "Temps restant : " + duration(remaining), x, y, remaining < 2000 ? RED : WHITE);
            }
            y += 4;
            drawObjectives(g, data, x, y);
        } else if (data.activeKind() != 0) {
            line(g, "Une autre évaluation est en cours : abandonne-la d'abord.", x, y, RED);
        } else if (!data.qualified()) {
            line(g, "Tu n'es pas encore qualifié pour monter en rang.", x, y, RED);
        } else if (data.cooldownTicks() > 0) {
            y = line(g, "Tu es qualifié, mais je ne te reçois pas tout de suite.", x, y, GREY);
            line(g, "Attends encore " + duration(Math.max(0, data.cooldownTicks() - elapsed)), x, y, RED);
        } else {
            y = line(g, "Tu es qualifié pour le rang " + data.nextRank() + ".", x, y, GREEN);
            line(g, "Je te donnerai des objectifs à accomplir dans un temps limité.", x, y, GREY);
        }
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        TrainerDataPayload data = ClientTrainerState.get();
        if (data != null && tab == Tab.LEARNING && button == 0) {
            int listX = leftPos + 8;
            int listY = topPos + 44;
            if (mouseX >= listX && mouseX < listX + 156 && mouseY >= listY && mouseY < listY + LIST_ROWS * LINE_H) {
                int index = listScroll + (int) ((mouseY - listY) / LINE_H);
                if (index < data.techniques().size()) {
                    selected = data.techniques().get(index).id();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (tab == Tab.LEARNING) {
            listScroll -= (int) Math.signum(scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }
}