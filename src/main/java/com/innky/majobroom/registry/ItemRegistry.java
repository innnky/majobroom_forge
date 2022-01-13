package com.innky.majobroom.registry;

import com.innky.majobroom.MajoBroom;
//import com.innky.majobroom.armors.MajoWearableItem;
//import com.innky.majobroom.armors.ModArmorMaterial;
//import com.innky.majobroom.armors.Modelinit;
import com.innky.majobroom.armors.MajoWearableItem;
import com.innky.majobroom.armors.ModArmorMaterial;
import com.innky.majobroom.armors.Modelinit;
import com.innky.majobroom.item.BroomItem;
import com.innky.majobroom.itemgroup.ModGroup;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    public static  DeferredRegister<Item> ITEMS ;
    public static RegistryObject<Item> broomItem;
    public static  RegistryObject<Item> majoHat ;
    public static Map<String ,RegistryObject<Item>> itemMap = new HashMap<>();

    public static void registry()  {
        ITEMS= DeferredRegister.create(ForgeRegistries.ITEMS, "majobroom");
        broomItem = ITEMS.register("broom_item", () ->{
            return new BroomItem(new Item.Properties().tab(ModGroup.itemGroup));
        });
        boolean isRemote = true;
        try {
            System.out.println(HumanoidModel.class);
        }catch (NoClassDefFoundError e){
            isRemote = false;
        }
        try {
            InputStream in = MajoBroom.class.getClassLoader().getResourceAsStream("/assets/majobroom/jsonmodels/model_list.txt");
            if (in!=null) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(in));
                String temp = "";
                while ((temp = bf.readLine())!= null) {
                    String[] results = temp.split(",");
                    switch (results[1]){
                        case "3":
                            itemMap.put(results[0],ITEMS.register(results[0], () -> new MajoWearableItem(ModArmorMaterial.CLOTH, EquipmentSlot.HEAD, (new Item.Properties()).tab(ModGroup.itemGroup))));
                            break;
                        case "2":
                            itemMap.put(results[0],ITEMS.register(results[0], () -> new MajoWearableItem(ModArmorMaterial.CLOTH, EquipmentSlot.CHEST, (new Item.Properties()).tab(ModGroup.itemGroup))));
                            break;
                        case "1":
                            itemMap.put(results[0],ITEMS.register(results[0], () -> new MajoWearableItem(ModArmorMaterial.CLOTH, EquipmentSlot.LEGS, (new Item.Properties()).tab(ModGroup.itemGroup))));
                            break;
                        case "0":
                            itemMap.put(results[0],ITEMS.register(results[0], () -> new MajoWearableItem(ModArmorMaterial.CLOTH, EquipmentSlot.FEET, (new Item.Properties()).tab(ModGroup.itemGroup))));
                            break;
                    }
                    Modelinit.reg(results[0],isRemote);

                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}

