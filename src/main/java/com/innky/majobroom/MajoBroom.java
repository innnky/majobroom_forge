package com.innky.majobroom;

//import com.innky.majobroom.registry.EntityTypeRegistry;
import com.innky.majobroom.registry.EntityTypeRegistry;
import com.innky.majobroom.registry.ItemRegistry;
import com.innky.majobroom.utills.Config;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.File;

@Mod("majobroom")
public class MajoBroom {

    public MajoBroom() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        ItemRegistry.registry();

        EntityTypeRegistry.ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
