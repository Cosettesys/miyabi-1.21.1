package net.cosette.miyabi.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.npc.TrainerMenu;

public final class ModMenus {
    private ModMenus() {}
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Miyabi.MODID);
    /** Menu de l'entraîneur : reçoit l'identifiant de l'entité en données supplémentaires. */
    public static final DeferredHolder<MenuType<?>, MenuType<TrainerMenu>> TRAINER = MENUS.register("trainer",
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> new TrainerMenu(windowId, inventory, data.readVarInt())));
}