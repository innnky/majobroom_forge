package com.innky.majobroom.events;

import com.innky.majobroom.armors.MajoWearableItem;
import com.innky.majobroom.entity.renderer.BroomRenderer;
import com.innky.majobroom.registry.EntityTypeRegistry;
import com.innky.majobroom.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onClientSetUpEvent(EntityRenderersEvent.RegisterRenderers event) {
//        System.out.println(111);
//        RegistryObject a = EntityTypeRegistry.majoBroom;
//        IForgeRegistryEntry b = a.get();
        event.registerEntityRenderer(EntityTypeRegistry.majoBroom.get(), BroomRenderer::new);
        ItemColors itemColors = Minecraft.getInstance().itemColors;
        itemColors.register((stack, color) -> {
            return color > 0 ? -1 : ((MajoWearableItem)stack.getItem()).getColor(stack);
        }, ItemRegistry.itemMap.get("majo_hat").get());
    }

}