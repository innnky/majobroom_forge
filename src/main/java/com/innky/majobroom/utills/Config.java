package com.innky.majobroom.utills;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.IntValue MAX_SPEED;
    public static ForgeConfigSpec.BooleanValue ADVANCED_MODE;
    public static ForgeConfigSpec.BooleanValue AUTO_PERSPECTIVE;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push("general");
        MAX_SPEED = COMMON_BUILDER.comment("The speed value refers to the percentage of the default maximum speed.The default value is 100%").defineInRange("max_speed", 100, 0, Integer.MAX_VALUE);
        ADVANCED_MODE = COMMON_BUILDER.comment("When this value equals false, you don't need experience to get on and fly the broom.").define("advanced_mode",true);
        AUTO_PERSPECTIVE = COMMON_BUILDER.comment("When this value equals false, perspective will not be changed automatically when you get on the broom.").define("auto_perspective",true);
        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
