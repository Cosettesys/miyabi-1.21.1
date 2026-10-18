package net.cosette.miyabi.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(AQUA_HEARTIA);
        }
    }
}