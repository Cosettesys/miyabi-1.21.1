package net.cosette.miyabi.registry;

import net.cosette.miyabi.item.SpellScrollItem;
import net.cosette.miyabi.skills.SpellRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.item.StaffItem;
import net.cosette.miyabi.skills.magic.MagicSchool;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ModItems {
    private ModItems() {}
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Miyabi.MODID);
    /**
     * Aqua Heartia : eau -80 % de coût, autres écoles -25 %, et vitesse d'incantation x2 (temps divisé par deux)
     * dans les deux mains. Le modificateur est "+100 % de la valeur de base" : 1 x (1 + 1,0) = 2.
     */
    public static final DeferredItem<StaffItem> AQUA_HEARTIA = ITEMS.register("aqua_heartia", () -> new StaffItem(
            new Item.Properties().stacksTo(1).attributes(ItemAttributeModifiers.builder()
                    .add(ModAttributes.CASTING_SPEED,
                            new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "aqua_heartia_casting_speed"),
                                    1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                            EquipmentSlotGroup.HAND)
                    .build()),
            MagicSchool.WATER, 0.80f, 0.25f));
    /** Parchemin de sort : le sort enseigné est dans le composant miyabi:taught_spell. */
    public static final DeferredItem<SpellScrollItem> SPELL_SCROLL =
            ITEMS.register("spell_scroll", () -> new SpellScrollItem(new Item.Properties().stacksTo(16)));
    /** Oeuf d'apparition de l'entraîneur. */
    public static final DeferredItem<DeferredSpawnEggItem> TRAINER_SPAWN_EGG =
            ITEMS.register("trainer_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.TRAINER, 0x3355AA, 0xE8E0C0, new Item.Properties()));
    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(AQUA_HEARTIA);
            // Un parchemin par sort
            for (String spellId : SpellRegistry.ids()) {
                event.accept(SpellScrollItem.create(spellId));
            }
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(TRAINER_SPAWN_EGG);
        }
    }
}