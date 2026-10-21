package net.cosette.miyabi.npc;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.cosette.miyabi.registry.ModMenus;

/** Menu de l'entraîneur (vide pour l'instant). Une instance par joueur : plusieurs joueurs peuvent l'ouvrir en même temps. */
public class TrainerMenu extends AbstractContainerMenu {
    private final TrainerEntity trainer; // null côté client
    /** Côté serveur. */
    public TrainerMenu(int containerId, Inventory inventory, TrainerEntity trainer) {
        super(ModMenus.TRAINER.get(), containerId);
        this.trainer = trainer;
    }
    /** Côté client : retrouve l'entraîneur par son identifiant. */
    public TrainerMenu(int containerId, Inventory inventory, int entityId) {
        this(containerId, inventory, inventory.player.level().getEntity(entityId) instanceof TrainerEntity found ? found : null);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    /** Le menu se ferme si l'entraîneur disparaît ou si le joueur s'en éloigne. */
    @Override
    public boolean stillValid(Player player) {
        return trainer == null || (trainer.isAlive() && player.distanceToSqr(trainer) < 64.0);
    }
}