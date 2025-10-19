package com.rcell.majobroom.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 兼容管理器 - 统一管理所有模组兼容
 * 简化设计：直接提供功能方法，内部按需加载兼容模组
 */
public class CompatManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompatManager.class);
    
    private static BackpackCompat backpackCompat;
    private static CosmeticArmorCompat cosmeticArmorCompat;
    
    /**
     * 初始化所有兼容模组
     */
    public static void init() {
        LOGGER.info("[MajoBroom] 初始化模组兼容...");
        
        // 背包兼容
        if (ModList.get().isLoaded("sophisticatedbackpacks")) {
            try {
                backpackCompat = new BackpackCompat();
                if (backpackCompat.isAvailable()) {
                    LOGGER.info("[MajoBroom] ✓ SophisticatedBackpacks 兼容已加载");
                } else {
                    backpackCompat = null;
                }
            } catch (Exception e) {
                LOGGER.error("[MajoBroom] ✗ SophisticatedBackpacks 兼容加载失败: {}", e.getMessage());
                backpackCompat = null;
            }
        }
        
        // 装饰盔甲兼容
        if (ModList.get().isLoaded("cosmeticarmorreworked")) {
            try {
                cosmeticArmorCompat = new CosmeticArmorCompat();
                if (cosmeticArmorCompat.isAvailable()) {
                    LOGGER.info("[MajoBroom] ✓ CosmeticArmorReworked 兼容已加载");
                } else {
                    cosmeticArmorCompat = null;
                }
            } catch (Exception e) {
                LOGGER.error("[MajoBroom] ✗ CosmeticArmorReworked 兼容加载失败: {}", e.getMessage());
                cosmeticArmorCompat = null;
            }
        }
        
        LOGGER.info("[MajoBroom] 兼容初始化完成");
    }
    
    /**
     * 在玩家所有物品栏中查找物品（包括原版物品栏和兼容的背包）
     * 同时返回来源背包的 UUID（如果是从背包中找到的）
     * 注意：这个方法只查找，不移除物品
     * @param player 玩家
     * @param item 要查找的物品
     * @return 查找结果，包含物品和来源背包 UUID
     */
    @Nonnull
    public static FindItemResult findItemInAllInventories(@Nonnull ServerPlayer player, @Nonnull Item item) {
        // 1. 先检查原版物品栏
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                return new FindItemResult(stack, null);  // 从原版物品栏找到，没有背包 UUID
            }
        }

        // 2. 检查背包模组（如果已加载）
        if (backpackCompat != null) {
            BackpackCompat.FindResult result = backpackCompat.findItemWithSource(player, item);
            if (!result.stack.isEmpty()) {
                return new FindItemResult(result.stack, result.backpackUUID);
            }
        }

        return FindItemResult.EMPTY;
    }

    /**
     * 从玩家的物品栏中移除物品（包括原版物品栏和兼容的背包）
     * 会正确触发背包的保存机制
     * @param player 玩家
     * @param item 要移除的物品
     * @return 移除结果，包含移除的物品（副本）和来源背包 UUID
     */
    @Nonnull
    public static FindItemResult removeItemFromAllInventories(@Nonnull ServerPlayer player, @Nonnull Item item) {
        // 1. 先检查原版物品栏
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                // 先复制一份物品（用于返回和读取配置）
                ItemStack copy = stack.copy();
                // 然后从物品栏移除
                stack.shrink(1);
                return new FindItemResult(copy, null);  // 没有背包 UUID
            }
        }

        // 2. 检查并从背包中移除（如果已加载）
        if (backpackCompat != null) {
            BackpackCompat.FindResult result = backpackCompat.removeItemFromBackpack(player, item);
            if (!result.stack.isEmpty()) {
                return new FindItemResult(result.stack, result.backpackUUID);
            }
        }

        return FindItemResult.EMPTY;
    }
    
    /**
     * 尝试将物品存储到指定 UUID 的背包中
     * @param player 玩家
     * @param backpackUUID 目标背包的 UUID
     * @param stack 要存储的物品堆栈
     * @return 是否成功存储
     */
    public static boolean storeItemToBackpack(@Nonnull ServerPlayer player, @Nullable java.util.UUID backpackUUID, @Nonnull ItemStack stack) {
        if (stack.isEmpty() || backpackUUID == null) {
            return false;
        }
        
        // 尝试按 UUID 存储到背包
        if (backpackCompat != null) {
            return backpackCompat.storeItemToBackpackByUUID(player, backpackUUID, stack);
        }
        
        return false;
    }
    
    /**
     * 获取生物在扩展装备槽中的所有装备（如装饰盔甲模组）
     * @param entity 生物实体
     * @return 扩展装备槽中的装备列表，如果没有兼容模组则返回空列表
     */
    @Nonnull
    public static List<ItemStack> getExtraArmorItems(@Nonnull LivingEntity entity) {
        List<ItemStack> extraArmor = new ArrayList<>();
        
        // 检查装饰盔甲模组（如果已加载）
        if (cosmeticArmorCompat != null) {
            extraArmor.addAll(cosmeticArmorCompat.getExtraArmorItems(entity));
        }
        
        return extraArmor;
    }
    
    /**
     * 查找物品的结果，包含物品和来源背包 UUID
     */
    public static class FindItemResult {
        public static final FindItemResult EMPTY = new FindItemResult(ItemStack.EMPTY, null);
        
        public final ItemStack stack;
        @Nullable
        public final java.util.UUID sourceBackpackUUID;
        
        public FindItemResult(ItemStack stack, @Nullable java.util.UUID sourceBackpackUUID) {
            this.stack = stack;
            this.sourceBackpackUUID = sourceBackpackUUID;
        }
    }
}
