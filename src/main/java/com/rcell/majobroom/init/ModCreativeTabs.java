package com.rcell.majobroom.init;

import com.rcell.majobroom.MajoBroom;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MajoBroom.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.majobroom.main"))
                    .icon(() -> new ItemStack(ModItems.BROOM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BROOM.get());
                        output.accept(ModItems.MAJO_HAT.get());
                        output.accept(ModItems.MAJO_CLOTH.get());
                    })
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
