package com.rcell.majobroom.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * SophisticatedBackpacks 模组兼容（NeoForge 1.21.1）
 * 使用反射实现，不需要编译时依赖
 * 
 * NeoForge 1.21.1 不再使用 LazyOptional，而是直接使用 BackpackWrapper.fromStack()
 */
class BackpackCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackpackCompat.class);
    
    private final Class<?> backpackItemClass;
    private final Method backpackWrapperFromStackMethod;
    private final Method getInventoryHandlerMethod;
    private final Method getContentsUuidMethod;
    private boolean available = false;
    
    // Curios API 辅助类（可选）
    @Nullable
    private final CuriosBackpackHelper curiosHelper;
    
    BackpackCompat() {
        Class<?> tempBackpackItemClass = null;
        Method tempBackpackWrapperFromStackMethod = null;
        Method tempGetInventoryHandlerMethod = null;
        Method tempGetContentsUuidMethod = null;
        
        try {
            LOGGER.info("[MajoBroom] 初始化 SophisticatedBackpacks 兼容...");
            
            // 加载 BackpackItem 类
            tempBackpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");
            LOGGER.info("[MajoBroom] ✓ BackpackItem 类加载成功");
            
            // 加载 BackpackWrapper 类（NeoForge 1.21.1 新方式）
            Class<?> backpackWrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper");
            tempBackpackWrapperFromStackMethod = backpackWrapperClass.getMethod("fromStack", ItemStack.class);
            LOGGER.info("[MajoBroom] ✓ BackpackWrapper.fromStack() 方法找到");
            
            // 加载 IBackpackWrapper 接口
            Class<?> iBackpackWrapperClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper");
            tempGetInventoryHandlerMethod = iBackpackWrapperClass.getMethod("getInventoryHandler");
            tempGetContentsUuidMethod = iBackpackWrapperClass.getMethod("getContentsUuid");
            LOGGER.info("[MajoBroom] ✓ IBackpackWrapper 方法找到");
            
            available = true;
            LOGGER.info("[MajoBroom] ✓✓✓ SophisticatedBackpacks 兼容初始化成功！");
        } catch (ClassNotFoundException e) {
            LOGGER.debug("[MajoBroom] SophisticatedBackpacks 类未找到: {}", e.getMessage());
            available = false;
        } catch (NoSuchMethodException e) {
            LOGGER.error("[MajoBroom] SophisticatedBackpacks 方法未找到: {}", e.getMessage());
            LOGGER.error("[MajoBroom] 可能是版本不兼容，请检查 SophisticatedBackpacks 版本");
            available = false;
        } catch (Exception e) {
            LOGGER.error("[MajoBroom] SophisticatedBackpacks 兼容初始化失败: ", e);
            available = false;
        }
        
        this.backpackItemClass = tempBackpackItemClass;
        this.backpackWrapperFromStackMethod = tempBackpackWrapperFromStackMethod;
        this.getInventoryHandlerMethod = tempGetInventoryHandlerMethod;
        this.getContentsUuidMethod = tempGetContentsUuidMethod;
        
        // 尝试初始化 Curios API 辅助类（可选）
        CuriosBackpackHelper tempCuriosHelper = null;
        if (available && tempBackpackItemClass != null) {
            tempCuriosHelper = new CuriosBackpackHelper(tempBackpackItemClass);
            if (tempCuriosHelper.isAvailable()) {
                LOGGER.info("[MajoBroom] ✓ Curios API 辅助类已加载");
            } else {
                LOGGER.debug("[MajoBroom] Curios API 辅助类未加载（可选）");
                tempCuriosHelper = null;
            }
        }
        this.curiosHelper = tempCuriosHelper;
    }
    
    boolean isAvailable() {
        return available;
    }
    
    /**
     * 获取背包的 UUID
     * @param backpackStack 背包物品堆栈
     * @return 背包的 UUID，如果无法获取则返回 null
     */
    @Nullable
    java.util.UUID getBackpackUUID(@Nonnull ItemStack backpackStack) {
        if (!isBackpackItem(backpackStack)) {
            return null;
        }
        
        try {
            Object backpackWrapper = getBackpackWrapper(backpackStack);
            if (backpackWrapper == null) {
                return null;
            }
            
            // 调用 getContentsUuid() 返回 Optional<UUID>
            @SuppressWarnings("unchecked")
            java.util.Optional<java.util.UUID> optional = (java.util.Optional<java.util.UUID>) getContentsUuidMethod.invoke(backpackWrapper);
            return optional.orElse(null);
        } catch (Exception e) {
            LOGGER.error("获取背包 UUID 时出错: ", e);
            return null;
        }
    }
    
    /**
     * 从玩家装备的背包中移除指定物品
     */
    @Nonnull
    FindResult removeItemFromBackpack(@Nonnull ServerPlayer player, @Nonnull Item item) {
        FindResult result = forEachBackpack(player, backpackStack -> {
            ItemStack extracted = extractFromBackpack(backpackStack, item);
            return extracted.isEmpty() ? null : new FindResult(extracted, getBackpackUUID(backpackStack));
        });
        return result != null ? result : FindResult.EMPTY;
    }

    /**
     * 在玩家装备的所有背包中查找物品
     */
    @Nonnull
    FindResult findItemWithSource(@Nonnull ServerPlayer player, @Nonnull Item item) {
        FindResult result = forEachBackpack(player, backpackStack -> {
            ItemStack found = findInBackpack(backpackStack, item);
            return found.isEmpty() ? null : new FindResult(found, getBackpackUUID(backpackStack));
        });
        return result != null ? result : FindResult.EMPTY;
    }
    
    /**
     * 按 UUID 查找玩家身上的背包并存储物品
     */
    boolean storeItemToBackpackByUUID(@Nonnull ServerPlayer player, @Nonnull java.util.UUID backpackUUID, @Nonnull ItemStack stack) {
        if (stack.isEmpty() || backpackUUID == null) {
            return false;
        }
        
        Boolean result = forEachBackpack(player, backpackStack -> {
            if (backpackUUID.equals(getBackpackUUID(backpackStack))) {
                return storeInBackpack(backpackStack, stack) ? true : null;
            }
            return null;
        });
        
        return result != null && result;
    }
    
    /**
     * 遍历玩家身上所有背包的通用方法
     * 按优先级顺序：胸甲槽 -> 主物品栏 -> 副手 -> Curios饰品槽
     * @param processor 处理函数，返回非null则停止遍历并返回结果
     * @return 处理函数的返回值
     */
    @Nullable
    private <T> T forEachBackpack(@Nonnull ServerPlayer player, @Nonnull BackpackProcessor<T> processor) {
        try {
            // 1. 胸甲槽
            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            if (isBackpackItem(chestStack)) {
                T result = processor.process(chestStack);
                if (result != null) {
                    return result;
                }
            }
            
            // 2. 主物品栏
            for (ItemStack stack : player.getInventory().items) {
                if (isBackpackItem(stack)) {
                    T result = processor.process(stack);
                    if (result != null) {
                        return result;
                    }
                }
            }
            
            // 3. 副手
            ItemStack offhandStack = player.getOffhandItem();
            if (isBackpackItem(offhandStack)) {
                T result = processor.process(offhandStack);
                if (result != null) {
                    return result;
                }
            }
            
            // 4. Curios 饰品槽（如果可用）
            CuriosBackpackHelper helper = curiosHelper;
            if (helper != null) {
                List<ItemStack> curiosBackpacks = helper.getCuriosBackpacks(player);
                for (ItemStack stack : curiosBackpacks) {
                    T result = processor.process(stack);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("遍历背包时出错: ", e);
        }
        
        return null;
    }
    
    @FunctionalInterface
    private interface BackpackProcessor<T> {
        T process(ItemStack backpackStack);
    }
    
    private boolean isBackpackItem(ItemStack stack) {
        return !stack.isEmpty() && backpackItemClass != null && backpackItemClass.isInstance(stack.getItem());
    }
    
    /**
     * 从背包中提取指定物品（使用 extractItem，会正确触发保存）
     * @param backpackStack 背包物品堆栈
     * @param item 要提取的物品
     * @return 提取的物品堆栈，如果没找到则返回空
     */
    @Nonnull
    private ItemStack extractFromBackpack(ItemStack backpackStack, Item item) {
        if (!isBackpackItem(backpackStack)) {
            return ItemStack.EMPTY;
        }
        
        try {
            IItemHandler inventoryHandler = getBackpackInventoryHandler(backpackStack);
            if (inventoryHandler == null) {
                return ItemStack.EMPTY;
            }
            
            int slots = inventoryHandler.getSlots();
            
            for (int slot = 0; slot < slots; slot++) {
                ItemStack slotStack = inventoryHandler.getStackInSlot(slot);
                if (slotStack.is(item)) {
                    // 使用 extractItem 正确移除物品（simulate = false）
                    return inventoryHandler.extractItem(slot, 1, false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("从背包提取物品时出错: ", e);
        }
        
        return ItemStack.EMPTY;
    }

    @Nonnull
    private ItemStack findInBackpack(ItemStack backpackStack, Item item) {
        if (!isBackpackItem(backpackStack)) {
            return ItemStack.EMPTY;
        }
        
        try {
            IItemHandler inventoryHandler = getBackpackInventoryHandler(backpackStack);
            if (inventoryHandler == null) {
                return ItemStack.EMPTY;
            }
            
            int slots = inventoryHandler.getSlots();
            
            for (int slot = 0; slot < slots; slot++) {
                ItemStack slotStack = inventoryHandler.getStackInSlot(slot);
                if (slotStack.is(item)) {
                    return slotStack;
                }
            }
        } catch (Exception e) {
            LOGGER.error("访问背包内容时出错: ", e);
        }
        
        return ItemStack.EMPTY;
    }
    
    private boolean storeInBackpack(ItemStack backpackStack, ItemStack itemToStore) {
        if (!isBackpackItem(backpackStack) || itemToStore.isEmpty()) {
            return false;
        }
        
        try {
            IItemHandler inventoryHandler = getBackpackInventoryHandler(backpackStack);
            if (inventoryHandler == null) {
                return false;
            }
            
            int slots = inventoryHandler.getSlots();
            
            // 尝试插入到任意槽位
            for (int slot = 0; slot < slots; slot++) {
                ItemStack remainingStack = inventoryHandler.insertItem(slot, itemToStore.copy(), false);
                
                // 如果完全插入成功
                if (remainingStack.isEmpty()) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("存储到背包时出错: ", e);
        }
        
        return false;
    }
    
    /**
     * 获取背包的 Wrapper（NeoForge 1.21.1 新方式）
     */
    @Nullable
    private Object getBackpackWrapper(ItemStack backpackStack) {
        try {
            // 在 NeoForge 1.21.1 中，直接调用 BackpackWrapper.fromStack(stack)
            return backpackWrapperFromStackMethod.invoke(null, backpackStack);
        } catch (Exception e) {
            LOGGER.error("获取背包 Wrapper 时出错: ", e);
            return null;
        }
    }
    
    @Nullable
    private IItemHandler getBackpackInventoryHandler(ItemStack backpackStack) {
        try {
            Object backpackWrapper = getBackpackWrapper(backpackStack);
            if (backpackWrapper == null) {
                return null;
            }
            // 反射调用 IBackpackWrapper.getInventoryHandler() 返回的是 InventoryHandler (extends IItemHandler)
            Object handler = getInventoryHandlerMethod.invoke(backpackWrapper);
            return (IItemHandler) handler;
        } catch (Exception e) {
            LOGGER.error("获取背包物品处理器时出错: ", e);
            return null;
        }
    }
    
    /**
     * 查找结果，包含找到的物品和来源背包 UUID
     */
    static class FindResult {
        static final FindResult EMPTY = new FindResult(ItemStack.EMPTY, null);
        
        final ItemStack stack;
        @Nullable
        final java.util.UUID sourceBackpackUUID;
        
        FindResult(ItemStack stack, @Nullable java.util.UUID sourceBackpackUUID) {
            this.stack = stack;
            this.sourceBackpackUUID = sourceBackpackUUID;
        }
    }
}
