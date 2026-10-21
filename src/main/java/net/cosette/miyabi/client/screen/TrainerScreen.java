package net.cosette.miyabi.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.cosette.miyabi.npc.TrainerMenu;

/** Écran de l'entraîneur : vide pour l'instant. */
public class TrainerScreen extends AbstractContainerScreen<TrainerMenu> {
    public TrainerScreen(TrainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 200;
        this.imageHeight = 110;
        this.inventoryLabelY = 10000; // pas d'inventaire dans ce menu : on cache son libellé
    }
    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xDD101018);
        g.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF5577AA);
        g.drawString(this.font, "Je n'ai rien à t'enseigner pour l'instant.", leftPos + 10, topPos + 34, 0xFFAAAAAA, false);
    }
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }
}