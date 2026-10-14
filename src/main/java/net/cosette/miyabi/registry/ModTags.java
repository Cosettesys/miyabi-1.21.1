package net.cosette.miyabi.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.cosette.miyabi.Miyabi;

public final class ModTags {
    private ModTags() {}
    /**
     * Armes utilisables pour les techniques du style du dieu de l'épée. Par défaut : toutes les épées
     * (#minecraft:swords, que les mods ajoutant des épées utilisent) ; un modpack peut y ajouter d'autres objets.
     */
    public static final TagKey<Item> SWORD_GOD_WEAPONS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "sword_god_weapons"));
}