package com.rcell.majobroom.init;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.entity.BroomEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MajoBroom.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BroomEntity>> BROOM = ENTITIES.register(
            "broom",
            () -> EntityType.Builder
                    .of(BroomEntity::new, MobCategory.MISC)
                    .sized(1.0F, 0.5F)
                    .clientTrackingRange(10)
                    .build("broom")
    );

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
