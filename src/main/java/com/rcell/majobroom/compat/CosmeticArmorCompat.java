package com.rcell.majobroom.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * CosmeticArmorReworked 模组兼容
 * 使用反射实现，不需要编译时依赖
 */
class CosmeticArmorCompat {
    
    private final Method getCAStacksMethod;
    private final Method getStackInSlotMethod;
    private boolean available = false;
    
    CosmeticArmorCompat() {
        Method tempGetCAStacksMethod = null;
        Method tempGetStackInSlotMethod = null;
        
        try {
            // 加载 CosArmorAPI
            Class<?> cosArmorAPIClass = Class.forName("lain.mods.cos.api.CosArmorAPI");
            tempGetCAStacksMethod = cosArmorAPIClass.getMethod("getCAStacks", java.util.UUID.class);
            
            // CAStacksBase 继承自 ItemStackHandler
            Class<?> itemStackHandlerClass = Class.forName("net.minecraftforge.items.ItemStackHandler");
            tempGetStackInSlotMethod = itemStackHandlerClass.getMethod("getStackInSlot", int.class);
            
            available = true;
        } catch (Exception e) {
            available = false;
        }
        
        this.getCAStacksMethod = tempGetCAStacksMethod;
        this.getStackInSlotMethod = tempGetStackInSlotMethod;
    }
    
    boolean isAvailable() {
        return available;
    }
    
    /**
     * 获取生物在装饰盔甲槽位的装备
     * 装饰盔甲有 4 个槽位：0=脚, 1=腿, 2=胸, 3=头
     */
    @Nonnull
    List<ItemStack> getExtraArmorItems(@Nonnull LivingEntity entity) {
        List<ItemStack> extraArmor = new ArrayList<>();
        
        // 只有玩家才有装饰盔甲
        if (!(entity instanceof Player player)) {
            return extraArmor;
        }
        
        try {
            // 获取玩家的装饰盔甲物品栏
            Object caStacks = getCAStacksMethod.invoke(null, player.getUUID());
            
            if (caStacks != null) {
                // 遍历 4 个装饰盔甲槽位
                for (int slot = 0; slot < 4; slot++) {
                    ItemStack stack = (ItemStack) getStackInSlotMethod.invoke(caStacks, slot);
                    if (stack != null && !stack.isEmpty()) {
                        extraArmor.add(stack);
                    }
                }
            }
        } catch (Exception e) {
            // 静默处理异常
        }
        
        return extraArmor;
    }
}
