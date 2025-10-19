package com.rcell.majobroom.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Curios API 背包辅助类
 * 用于从 Curios 饰品槽中获取背包
 */
class CuriosBackpackHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CuriosBackpackHelper.class);
    
    private final Method getCuriosInventoryMethod;
    private final Method getStacksHandlerMethod;
    private final Method getCurioStacksMethod;
    private final Method getSlotsMethod;
    private final Method getStackInSlotMethod;
    private final Class<?> backpackItemClass;
    private boolean available = false;
    
    CuriosBackpackHelper(Class<?> backpackItemClass) {
        this.backpackItemClass = backpackItemClass;
        
        Method tempGetCuriosInventoryMethod = null;
        Method tempGetStacksHandlerMethod = null;
        Method tempGetCurioStacksMethod = null;
        Method tempGetSlotsMethod = null;
        Method tempGetStackInSlotMethod = null;
        
        try {
            LOGGER.info("[MajoBroom] 开始初始化 Curios API 兼容...");
            
            // CuriosApi.getCuriosInventory(LivingEntity)
            LOGGER.info("[MajoBroom] 尝试加载 CuriosApi 类...");
            Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            LOGGER.info("[MajoBroom] ✓ CuriosApi 类加载成功");
            tempGetCuriosInventoryMethod = curiosApiClass.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
            LOGGER.info("[MajoBroom] ✓ getCuriosInventory() 方法找到");
            
            // ICuriosItemHandler.getStacksHandler(String)
            Class<?> curiosHandlerClass = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
            LOGGER.info("[MajoBroom] ✓ ICuriosItemHandler 类加载成功");
            tempGetStacksHandlerMethod = curiosHandlerClass.getMethod("getStacksHandler", String.class);
            LOGGER.info("[MajoBroom] ✓ getStacksHandler() 方法找到");
            
            // ICurioStacksHandler.getStacks()
            Class<?> curiosStacksHandlerClass = Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler");
            LOGGER.info("[MajoBroom] ✓ ICurioStacksHandler 类加载成功");
            tempGetCurioStacksMethod = curiosStacksHandlerClass.getMethod("getStacks");
            LOGGER.info("[MajoBroom] ✓ getStacks() 方法找到");
            
            // IItemHandlerModifiable.getSlots() 和 getStackInSlot(int)
            Class<?> itemHandlerClass = Class.forName("net.minecraftforge.items.IItemHandlerModifiable");
            tempGetSlotsMethod = itemHandlerClass.getMethod("getSlots");
            tempGetStackInSlotMethod = itemHandlerClass.getMethod("getStackInSlot", int.class);
            LOGGER.info("[MajoBroom] ✓ IItemHandlerModifiable 方法找到");
            
            available = true;
            LOGGER.info("[MajoBroom] ✓✓✓ Curios API 兼容初始化成功！");
        } catch (ClassNotFoundException e) {
            LOGGER.error("[MajoBroom] ✗ Curios API 类未找到: {}", e.getMessage());
            LOGGER.error("[MajoBroom] 请确认已安装 Curios API 模组");
            available = false;
        } catch (NoSuchMethodException e) {
            LOGGER.error("[MajoBroom] ✗ Curios API 方法未找到: {}", e.getMessage());
            LOGGER.error("[MajoBroom] Curios API 版本可能不兼容");
            available = false;
        } catch (Exception e) {
            LOGGER.error("[MajoBroom] ✗ Curios API 初始化失败: ", e);
            available = false;
        }
        
        this.getCuriosInventoryMethod = tempGetCuriosInventoryMethod;
        this.getStacksHandlerMethod = tempGetStacksHandlerMethod;
        this.getCurioStacksMethod = tempGetCurioStacksMethod;
        this.getSlotsMethod = tempGetSlotsMethod;
        this.getStackInSlotMethod = tempGetStackInSlotMethod;
    }
    
    boolean isAvailable() {
        return available;
    }
    
    /**
     * 从 Curios 饰品槽中获取所有背包
     * @param player 玩家
     * @return 背包物品列表
     */
    @Nonnull
    List<ItemStack> getCuriosBackpacks(@Nonnull Player player) {
        List<ItemStack> backpacks = new ArrayList<>();
        if (!available) {
            return backpacks;
        }
        
        try {
            // CuriosApi.getCuriosInventory(player) 返回 LazyOptional<ICuriosItemHandler>
            Object lazyOptional = getCuriosInventoryMethod.invoke(null, player);
            
            // 检查 LazyOptional 是否有值
            Class<?> lazyOptionalClass = lazyOptional.getClass();
            java.lang.reflect.Method isPresentMethod = lazyOptionalClass.getMethod("isPresent");
            Boolean isPresent = (Boolean) isPresentMethod.invoke(lazyOptional);
            
            if (!isPresent) {
                return backpacks;
            }
            
            // 获取 ICuriosItemHandler
            java.lang.reflect.Method resolveMethod = lazyOptionalClass.getMethod("resolve");
            Object optionalHandler = resolveMethod.invoke(lazyOptional);
            
            if (!(optionalHandler instanceof Optional)) {
                return backpacks;
            }
            @SuppressWarnings("unchecked")
            Optional<Object> handlerOpt = (Optional<Object>) optionalHandler;
            if (!handlerOpt.isPresent()) {
                return backpacks;
            }
            
            Object curiosHandler = handlerOpt.get();
            
            // 遍历所有可能的 Curios 槽位类型
            for (String slotType : new String[]{"back", "curio", "belt", "body", "charm"}) {
                try {
                    // getCuriosHandler().getStacksHandler(slotType)
                    Object optionalStacksHandler = getStacksHandlerMethod.invoke(curiosHandler, slotType);
                    
                    if (!(optionalStacksHandler instanceof Optional)) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Optional<Object> stacksHandlerOpt = (Optional<Object>) optionalStacksHandler;
                    if (!stacksHandlerOpt.isPresent()) {
                        continue;
                    }
                    
                    Object stacksHandler = stacksHandlerOpt.get();
                    
                    // getStacksHandler().getStacks() 返回 IItemHandlerModifiable
                    Object itemHandler = getCurioStacksMethod.invoke(stacksHandler);
                    
                    // 遍历所有槽位
                    int slots = (Integer) getSlotsMethod.invoke(itemHandler);
                    
                    for (int i = 0; i < slots; i++) {
                        ItemStack stack = (ItemStack) getStackInSlotMethod.invoke(itemHandler, i);
                        if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                            backpacks.add(stack);
                        }
                    }
                } catch (Exception e) {
                    // 该槽位类型不存在或出错，继续下一个
                    LOGGER.debug("检查 Curios 槽位 {} 时出错: {}", slotType, e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("从 Curios 槽位获取背包时出错: ", e);
        }
        
        return backpacks;
    }
}

