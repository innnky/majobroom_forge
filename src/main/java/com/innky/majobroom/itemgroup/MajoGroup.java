package com.innky.majobroom.itemgroup;


import com.innky.majobroom.registry.ItemRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class MajoGroup extends CreativeModeTab {
    public MajoGroup() {
        super("majo_group");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ItemRegistry.broomItem.get());
    }
}
