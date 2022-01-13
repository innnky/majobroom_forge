package com.innky.majobroom.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyBoardInput {
    public static final KeyMapping UP_KEY = new KeyMapping("key.up",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            "key.category.majobroom");
    public static final KeyMapping DOWN_KEY = new KeyMapping("key.down",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.category.majobroom");

    public static boolean up = false;
    public static boolean down = false;
    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (UP_KEY.getKey().getValue() == event.getKey()) {
            if (event.getAction() == 1){
                up = true;
            }else if (event.getAction() == 0){
                up = false;
            }
        }
        if (DOWN_KEY.getKey().getValue() == event.getKey()) {
            if (event.getAction() == 1){
                down = true;
            }else if (event.getAction() == 0){
                down = false;
            }
        }
    }
}
