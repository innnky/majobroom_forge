package com.innky.majobroom.registry;

import com.innky.majobroom.entity.MajoBroom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityTypeRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "majobroom");
    public static RegistryObject<EntityType<MajoBroom>> majoBroom = ENTITY_TYPES.register("majo_broom", () -> {
        return EntityType.Builder.of(MajoBroom::new, MobCategory.MISC).sized(1.0f,0.5f).build("majo_broom");
    });
}