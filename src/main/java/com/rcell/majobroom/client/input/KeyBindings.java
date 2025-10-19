package com.rcell.majobroom.client.input;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class KeyBindings {
    private KeyBindings() {}

    public static final KeyMapping SUMMON_BROOM = new KeyMapping(
            "key.majobroom.summon",
            GLFW.GLFW_KEY_R,
            "key.category.majobroom"
    );

    public static final KeyMapping FLY_UP = new KeyMapping(
            "key.majobroom.fly_up",
            GLFW.GLFW_KEY_SPACE,
            "key.category.majobroom"
    );

    public static final KeyMapping FLY_DOWN = new KeyMapping(
            "key.majobroom.fly_down",
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.category.majobroom"
    );

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.majobroom.open_config",
            GLFW.GLFW_KEY_Y,
            "key.category.majobroom"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SUMMON_BROOM);
        event.register(FLY_UP);
        event.register(FLY_DOWN);
        event.register(OPEN_CONFIG);
    }
}



