package com.rcell.majobroom.client.tooltip;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Tooltip 修饰器接口
 * 用于修改物品的 tooltip 显示内容
 */
@FunctionalInterface
public interface TooltipModifier {
    /**
     * 全局注册表
     */
    Map<Item, TooltipModifier> REGISTRY = new HashMap<>();

    /**
     * 空修饰器
     */
    TooltipModifier EMPTY = new TooltipModifier() {
        @Override
        public void modify(ItemTooltipEvent context) {
        }

        @Override
        public TooltipModifier andThen(TooltipModifier after) {
            return after;
        }
    };

    /**
     * 修改 tooltip
     * @param context tooltip 事件上下文
     */
    void modify(ItemTooltipEvent context);

    /**
     * 链式组合多个修饰器
     */
    default TooltipModifier andThen(TooltipModifier after) {
        if (after == EMPTY) {
            return this;
        }
        return tooltip -> {
            modify(tooltip);
            after.modify(tooltip);
        };
    }

    /**
     * 将 null 转换为 EMPTY
     */
    static TooltipModifier mapNull(@Nullable TooltipModifier modifier) {
        if (modifier == null) {
            return EMPTY;
        }
        return modifier;
    }

    /**
     * 注册修饰器
     */
    static void register(Item item, TooltipModifier modifier) {
        REGISTRY.put(item, modifier);
    }

    /**
     * 获取修饰器
     */
    @Nullable
    static TooltipModifier get(Item item) {
        return REGISTRY.get(item);
    }
}

