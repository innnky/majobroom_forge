package com.innky.majobroom.events;

import com.innky.majobroom.entity.MajoBroom;
import com.innky.majobroom.sound.FlyingSound;
import com.innky.majobroom.utills.Config;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber( value = Dist.CLIENT)
public class ClientHandler {
    @SubscribeEvent
    public static void entityMountHandler(EntityMountEvent event){
        if(event.getEntityBeingMounted() instanceof MajoBroom) {
            if (event.isMounting()) {

                if(Minecraft.getInstance().player != null) {
                    if (event.getEntityMounting().getUUID() == Minecraft.getInstance().player.getUUID()) {
                        if (Config.AUTO_PERSPECTIVE.get()) {
                            Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
                        }
                        Minecraft.getInstance().getSoundManager().play(new FlyingSound((MajoBroom) event.getEntityBeingMounted()));

                    }
                }
            } else {
                if (Minecraft.getInstance().player != null) {
                    if (event.getEntityMounting().getUUID() == Minecraft.getInstance().player.getUUID()) {
                        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                    }
                    ((MajoBroom) event.getEntityBeingMounted()).hasPassenger = false;
                }
            }
        }
    }
}
