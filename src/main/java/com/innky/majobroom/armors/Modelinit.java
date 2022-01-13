package com.innky.majobroom.armors;

import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class Modelinit {
    public static Map<String,MajoWearableModel> modelMap = new HashMap<>();
    public static MajoWearableModel defaultModel = null;
    public static void reg(String name,boolean isRemote){
        MajoWearableModel item = null;
        if (isRemote){
            item = new MajoWearableModel(name);
            defaultModel = item;
        }

        modelMap.put(name,item);
    }
}
