package com.rcell.majobroom.init;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.item.BroomItem;
import com.rcell.majobroom.item.armor.MajoHatItem;
import com.rcell.majobroom.item.armor.MajoRobeItem;
import com.rcell.majobroom.client.tooltip.ItemDescription;
import com.rcell.majobroom.client.tooltip.TooltipModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MajoBroom.MODID);

    public static final DeferredItem<Item> BROOM = ITEMS.register(
            "broom", () -> new BroomItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> MAJO_HAT = ITEMS.register(
            "majo_hat", () -> new MajoHatItem(new Item.Properties())
    );

    public static final DeferredItem<Item> MAJO_CLOTH = ITEMS.register(
            "majo_cloth", () -> new MajoRobeItem(new Item.Properties())
    );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    /**
     * 注册物品的 Tooltip 修饰器
     * 必须在物品注册完成后调用（在客户端设置阶段）
     */
    public static void registerTooltips() {
        // 为扫帚注册 Tooltip
        TooltipModifier.register(BROOM.get(), new ItemDescription.Modifier(BROOM.get()));
    }
}
