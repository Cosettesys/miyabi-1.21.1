package net.cosette.miyabi.registry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.npc.TrainerEntity;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ModEntityAttributes {
    private ModEntityAttributes() {}
    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TRAINER.get(), TrainerEntity.createAttributes().build());
    }
}