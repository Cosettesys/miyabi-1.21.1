package net.cosette.miyabi.client.screen;

import net.cosette.miyabi.skills.battleaura.AuraCriterion;
import net.cosette.miyabi.skills.battleaura.BattleAuraLevels;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.network.payload.MenuDataPayload;
import net.cosette.miyabi.network.payload.SlotAssignPayload;
import net.cosette.miyabi.progression.MasteryManager;
import net.cosette.miyabi.progression.SwordStyle;

import java.util.ArrayList;
import java.util.List;
import net.cosette.miyabi.client.input.ClientMenuHandler;
import net.cosette.miyabi.client.input.MiyabiKeyMappings;

public class MiyabiMenuScreen extends Screen {
    private enum View { MAIN, MAGIC_SCHOOLS, MAGIC_LIST, SWORD_STYLES, SWORD_LIST, AURA }
    private record ListEntry(String id, String text, boolean dim) {}
    private record EntryRow(String id, int x1, int y1, int x2, int y2) {}
    private static final int PANEL_W = 400;
    private static final int PANEL_MAX_H = 250;
    private static final int LINE_H = 10;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFFCCCCCC;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int DIM = 0xFF999999; // sort débloqué mais pas encore appris
    private MenuDataPayload data;
    private View view = View.MAIN;
    private MagicSchool selectedSchool = null;
    private SwordStyle selectedStyle = null;
    private String selectedEntry = null;
    private int scroll = 0;
    private boolean draggingScrollbar = false;
    private int panelH, left, top, slotY, mainY;
    private Button magicButton;
    private Button swordButton;
    private Button backButton;
    private Button auraButton;
    private final List<Button> slotButtons = new ArrayList<>();
    private final List<Button> schoolButtons = new ArrayList<>();
    private final List<Button> styleButtons = new ArrayList<>();
    private final List<EntryRow> entryRows = new ArrayList<>();
    public MiyabiMenuScreen(MenuDataPayload data) {
        super(Component.literal("Miyabi"));
        this.data = data;
    }
    public void updateData(MenuDataPayload newData) {
        this.data = newData;
        if (magicButton != null) {
            refreshButtons(); // met à jour les rangs affichés sur les boutons
        }
    }
    private static String keyLabel(int slot) {
        return MiyabiKeyMappings.SPELL_KEYS[slot].getTranslatedKeyMessage().getString();
    }
    private boolean isList() {
        return view == View.MAGIC_LIST || view == View.SWORD_LIST;
    }
    private int listTop() { return top + 22; }
    private int listBottom() { return slotY - 26; }
    private int visibleRows() { return Math.max(1, (listBottom() - listTop()) / LINE_H); }
    private int maxScroll(int total) { return Math.max(0, total - visibleRows()); }
    private int trackX() { return left + PANEL_W - 10; }
    private int currentTotal() {
        int n = 0;
        if (view == View.MAGIC_LIST && selectedSchool != null) {
            for (MenuDataPayload.SpellEntry s : data.spells()) {
                if (s.school().equals(selectedSchool.name())) n++;
            }
        } else if (view == View.SWORD_LIST && selectedStyle != null) {
            for (MenuDataPayload.TechniqueEntry t : data.techniques()) {
                if (t.style().equals(selectedStyle.name())) n++;
            }
        }
        return n;
    }
    @Override
    protected void init() {
        panelH = Math.min(PANEL_MAX_H, this.height - 10);
        left = (this.width - PANEL_W) / 2;
        top = (this.height - panelH) / 2;
        mainY = top + panelH - 26;
        slotY = top + panelH - 52;
        // Menu principal
        magicButton = Button.builder(Component.literal("Magies"), b -> goTo(View.MAGIC_SCHOOLS))
                .bounds(left + 10, mainY, 190, 20).build();
        swordButton = Button.builder(Component.literal("Techniques d'épée"), b -> goTo(View.SWORD_STYLES))
                .bounds(left + PANEL_W - 200, mainY, 190, 20).build();
        auraButton = Button.builder(Component.literal("Battle aura"), b -> goTo(View.AURA))
                .bounds(left + 10, mainY, 100, 20).build();
        addRenderableWidget(magicButton);
        addRenderableWidget(swordButton);
        addRenderableWidget(auraButton); // position et largeur réglées dans refreshButtons
        // Retour (tous les sous-menus)
        backButton = Button.builder(Component.literal("Retour"), b -> goBack())
                .bounds(left + 10, mainY, 190, 20).build();
        addRenderableWidget(backButton);
        // Choix de l'école : grille 2 colonnes x 4 lignes
        schoolButtons.clear();
        MagicSchool[] schools = MagicSchool.values();
        for (int i = 0; i < schools.length; i++) {
            final MagicSchool school = schools[i];
            int col = i % 3;
            int row = i / 3;
            Button b = Button.builder(Component.literal(school.name()), btn -> selectSchool(school))
                    .bounds(left + 10 + col * 130, top + 24 + row * 26, 120, 20).build();
            schoolButtons.add(b);
            addRenderableWidget(b);
        }
        // Choix du style : colonne de 3
        styleButtons.clear();
        SwordStyle[] styles = SwordStyle.values();
        for (int i = 0; i < styles.length; i++) {
            final SwordStyle style = styles[i];
            Button b = Button.builder(Component.literal(style.name()), btn -> selectStyle(style))
                    .bounds(left + 10, top + 24 + i * 26, 160, 20).build();
            styleButtons.add(b);
            addRenderableWidget(b);
        }
        slotButtons.clear();
        for (int i = 0; i < MiyabiKeyMappings.SPELL_KEYS.length; i++) {
            final int slot = i;
            Button b = Button.builder(Component.literal(keyLabel(i)), btn -> assignSelected(slot))
                    .bounds(left + 135 + i * 34, slotY, 30, 20).build();
            slotButtons.add(b);
            addRenderableWidget(b);
        }
        refreshButtons();
    }
    private void goTo(View target) {
        view = target;
        resetListState();
        refreshButtons();
    }
    private void selectSchool(MagicSchool school) {
        selectedSchool = school;
        view = View.MAGIC_LIST;
        resetListState();
        refreshButtons();
    }
    private void selectStyle(SwordStyle style) {
        selectedStyle = style;
        view = View.SWORD_LIST;
        resetListState();
        refreshButtons();
    }
    private void goBack() {
        switch (view) {
            case MAGIC_LIST -> view = View.MAGIC_SCHOOLS;
            case SWORD_LIST -> view = View.SWORD_STYLES;
            default -> view = View.MAIN;
        }
        resetListState();
        refreshButtons();
    }
    private void resetListState() {
        scroll = 0;
        selectedEntry = null;
        draggingScrollbar = false;
    }
    private void assignSelected(int slot) {
        if (selectedEntry != null) {
            PacketDistributor.sendToServer(new SlotAssignPayload(slot, selectedEntry));
        }
    }
    private void refreshButtons() {
        boolean main = (view == View.MAIN);
        // Le bouton Battle aura n'existe qu'une fois l'aura éveillée : les boutons se partagent la largeur.
        boolean aura = data.auraAwakened();
        int gap = 8;
        int count = aura ? 3 : 2;
        int width = (PANEL_W - 20 - gap * (count - 1)) / count;
        layoutMainButton(magicButton, 0, width, gap, main);
        layoutMainButton(swordButton, 1, width, gap, main);
        layoutMainButton(auraButton, 2, width, gap, main && aura);
        backButton.visible = !main;
        MagicSchool[] schools = MagicSchool.values();
        for (int i = 0; i < schoolButtons.size(); i++) {
            Button b = schoolButtons.get(i);
            b.visible = (view == View.MAGIC_SCHOOLS);
            MenuDataPayload.SchoolEntry info = findSchool(schools[i]);
            b.setMessage(Component.literal(schools[i].name() + (info != null ? " : " + info.rank() : "")));
        }
        SwordStyle[] styles = SwordStyle.values();
        for (int i = 0; i < styleButtons.size(); i++) {
            Button b = styleButtons.get(i);
            b.visible = (view == View.SWORD_STYLES);
            MenuDataPayload.StyleEntry info = findStyle(styles[i]);
            b.setMessage(Component.literal(styles[i].name() + (info != null ? " : " + info.rank() : "")));
        }
        for (Button b : slotButtons) {
            b.visible = isList();
        }
    }
    private void layoutMainButton(Button button, int index, int width, int gap, boolean visible) {
        button.setX(left + 10 + index * (width + gap));
        button.setWidth(width);
        button.visible = visible;
    }
    /** Écran de la battle aura : quatre jauges sans valeurs chiffrées. */
    private void renderAura(GuiGraphics g, int x) {
        int y = top + 8;
        y = line(g, "Battle aura : niveau " + data.auraLevel(), x, y, WHITE);
        if (data.auraLevel() >= BattleAuraLevels.MAX_LEVEL) {
            line(g, "Mon corps a atteint sa limite.", x, y + 4, GREY);
            return;
        }
        y += 8;
        AuraCriterion[] criteria = AuraCriterion.values();
        for (int i = 0; i < criteria.length; i++) {
            float ratio = i < data.auraProgress().size() ? Mth.clamp(data.auraProgress().get(i), 0f, 1f) : 0f;
            g.drawString(this.font, criteria[i].getLabel(), x, y, WHITE);
            int barX = x + 170;
            int barWidth = 150;
            g.fill(barX - 1, y - 1, barX + barWidth + 1, y + 9, 0xFF000000);
            g.fill(barX, y, barX + barWidth, y + 8, 0xFF333333);
            g.fill(barX, y, barX + Math.round(barWidth * ratio), y + 8, ratio >= 1f ? 0xFF55DD55 : 0xFFCC9933);
            y += 18;
        }
        line(g, "Je dois renforcer au moins trois de ces domaines pour progresser.", x, y + 6, GREY);
    }
    private MenuDataPayload.SchoolEntry findSchool(MagicSchool school) {
        for (MenuDataPayload.SchoolEntry e : data.schools()) {
            if (e.school().equals(school.name())) return e;
        }
        return null;
    }
    private MenuDataPayload.StyleEntry findStyle(SwordStyle style) {
        for (MenuDataPayload.StyleEntry e : data.styles()) {
            if (e.style().equals(style.name())) return e;
        }
        return null;
    }
    private int line(GuiGraphics g, String text, int x, int y, int color) {
        g.drawString(this.font, text, x, y, color);
        return y + LINE_H;
    }
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x88000000);
        g.fill(left - 2, top - 2, left + PANEL_W + 2, top + panelH + 2, 0xFF222222);
        g.fill(left, top, left + PANEL_W, top + panelH, 0xFF666666);
        int x = left + 10;
        entryRows.clear();
        switch (view) {
            case MAIN -> renderMain(g, x);
            case MAGIC_SCHOOLS -> line(g, "Écoles de magie", x, top + 8, WHITE);
            case MAGIC_LIST -> renderMagicList(g, x);
            case SWORD_STYLES -> line(g, "Styles de combat", x, top + 8, WHITE);
            case SWORD_LIST -> renderSwordList(g, x);
            case AURA -> renderAura(g, x);
        }
        magicButton.render(g, mouseX, mouseY, partialTick);
        swordButton.render(g, mouseX, mouseY, partialTick);
        backButton.render(g, mouseX, mouseY, partialTick);
        auraButton.render(g, mouseX, mouseY, partialTick);
        for (Button b : schoolButtons) if (b.visible) b.render(g, mouseX, mouseY, partialTick);
        for (Button b : styleButtons) if (b.visible) b.render(g, mouseX, mouseY, partialTick);
        for (Button b : slotButtons) if (b.visible) b.render(g, mouseX, mouseY, partialTick);
    }
    private void renderMain(GuiGraphics g, int x) {
        int y = top + 8;
        y = line(g, "Nom : " + data.playerName(), x, y, WHITE);
        y = line(g, String.format("Mana : %.0f / %.0f", data.mana(), data.maxMana()), x, y, WHITE);
        line(g, "Battle aura : niveau " + data.auraLevel()
                + (data.auraAwakened() ? " (éveillée)" : " (non éveillée)"), x, y, WHITE);
    }
    private void renderMagicList(GuiGraphics g, int x) {
        if (selectedSchool == null) return;
        MenuDataPayload.SchoolEntry info = findSchool(selectedSchool);
        if (info != null) {
            line(g, selectedSchool.name() + " : " + info.rank(), x, top + 8, WHITE);
        }
        List<ListEntry> entries = new ArrayList<>();
        for (MenuDataPayload.SpellEntry s : data.spells()) {
            if (!s.school().equals(selectedSchool.name())) continue;
            entries.add(new ListEntry(s.id(), s.id() + " [" + s.rank() + "] - " + String.format("%.1f", s.manaCost()) + " mana", !s.learned()));
        }
        renderEntries(g, x, entries, "Aucun sort pour cette école pour l'instant.");
        renderAssignFooter(g, x, "sort");
    }
    private void renderSwordList(GuiGraphics g, int x) {
        if (selectedStyle == null) return;
        MenuDataPayload.StyleEntry info = findStyle(selectedStyle);
        if (info != null) {
            line(g, selectedStyle.name() + " : " + info.rank(), x, top + 8, WHITE);
        }
        List<ListEntry> entries = new ArrayList<>();
        for (MenuDataPayload.TechniqueEntry t : data.techniques()) {
            if (!t.style().equals(selectedStyle.name())) continue;
            entries.add(new ListEntry(t.id(), t.id() + " [" + t.rank() + "]", false));
        }
        renderEntries(g, x, entries, "Aucune technique pour ce style pour l'instant.");
        renderAssignFooter(g, x, "technique");
    }
    private void renderEntries(GuiGraphics g, int x, List<ListEntry> entries, String emptyMessage) {
        scroll = Mth.clamp(scroll, 0, maxScroll(entries.size()));
        int rows = visibleRows();
        int rowRight = left + PANEL_W - 14;
        if (entries.isEmpty()) {
            line(g, emptyMessage, x, listTop(), GREY);
        }
        for (int i = 0; i < rows && scroll + i < entries.size(); i++) {
            ListEntry e = entries.get(scroll + i);
            int rowY = listTop() + i * LINE_H;
            boolean selected = e.id().equals(selectedEntry);
            int slotIndex = data.slots().indexOf(e.id());
            String slotLabel = slotIndex >= 0 ? " [" + keyLabel(slotIndex) + "]" : "";
            if (selected) {
                g.fill(x - 2, rowY - 1, rowRight, rowY + LINE_H - 1, 0x66FFFFFF);
            }
            entryRows.add(new EntryRow(e.id(), x - 2, rowY - 1, rowRight, rowY + LINE_H - 1));

            line(g, e.text() + slotLabel, x + 4, rowY, selected ? YELLOW : (e.dim() ? DIM : WHITE));
        }
        drawScrollbar(g, entries.size());
    }
    private void renderAssignFooter(GuiGraphics g, int x, String noun) {
        line(g, selectedEntry == null
                ? "Clique un " + noun + ", puis une touche ci-dessous pour l'assigner."
                : "Sélection : " + selectedEntry + " - clique la touche du slot.", x, slotY - 22, GREY);
        String bar = buildBar(Integer.MAX_VALUE);
        if (this.font.width(bar) > PANEL_W - 20) {
            bar = buildBar(9); // ids raccourcis si la ligne déborde du cadre
        }
        line(g, bar, x, slotY - 11, WHITE);
        g.drawString(this.font, "Assigner à la touche :", x, slotY + 6, WHITE);
    }
    private String buildBar(int maxLen) {
        StringBuilder bar = new StringBuilder("Barre :");
        for (int i = 0; i < data.slots().size(); i++) {
            String id = data.slots().get(i);
            if (id.isEmpty()) {
                id = "-";
            } else if (id.length() > maxLen) {
                id = id.substring(0, maxLen - 1) + "…";
            }
            bar.append(' ').append(keyLabel(i)).append('=').append(id);
        }
        return bar.toString();
    }
    private void drawScrollbar(GuiGraphics g, int total) {
        int rows = visibleRows();
        if (total <= rows) return;

        int trackTop = listTop();
        int trackBottom = listBottom();
        int trackH = trackBottom - trackTop;

        g.fill(trackX(), trackTop, trackX() + 4, trackBottom, 0xFF444444);

        int thumbH = Math.max(8, trackH * rows / total);
        int thumbY = trackTop + (trackH - thumbH) * scroll / maxScroll(total);
        g.fill(trackX(), thumbY, trackX() + 4, thumbY + thumbH, draggingScrollbar ? 0xFFFFFFFF : 0xFFBBBBBB);
    }
    private boolean isOnScrollbar(double mx, double my, int total) {
        return total > visibleRows()
                && mx >= trackX() - 3 && mx <= trackX() + 7
                && my >= listTop() && my <= listBottom();
    }
    private void scrollToMouse(double my, int total) {
        int trackTop = listTop();
        int trackH = listBottom() - trackTop;
        int rows = visibleRows();
        int thumbH = Math.max(8, trackH * rows / total);
        int range = trackH - thumbH;
        if (range <= 0 || maxScroll(total) == 0) return;

        double ratio = (my - trackTop - thumbH / 2.0) / range;
        scroll = Mth.clamp((int) Math.round(ratio * maxScroll(total)), 0, maxScroll(total));
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isList() && button == 0) {
            int total = currentTotal();
            if (isOnScrollbar(mouseX, mouseY, total)) {
                draggingScrollbar = true;
                scrollToMouse(mouseY, total);
                return true;
            }
            for (EntryRow row : entryRows) {
                if (mouseX >= row.x1() && mouseX <= row.x2() && mouseY >= row.y1() && mouseY <= row.y2()) {
                    selectedEntry = row.id();
                    return true;
                }
            }
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        this.setFocused(null);
        return handled;
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            scrollToMouse(mouseY, currentTotal());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isList()) {
            scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll(currentTotal()));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientMenuHandler.MENU.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        if (isList()) {
            int step = switch (keyCode) {
                case GLFW.GLFW_KEY_DOWN -> 1;
                case GLFW.GLFW_KEY_UP -> -1;
                case GLFW.GLFW_KEY_PAGE_DOWN -> visibleRows();
                case GLFW.GLFW_KEY_PAGE_UP -> -visibleRows();
                default -> 0;
            };
            if (step != 0) {
                scroll = Mth.clamp(scroll + step, 0, maxScroll(currentTotal()));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}