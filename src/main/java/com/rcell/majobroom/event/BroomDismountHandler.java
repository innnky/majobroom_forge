package com.rcell.majobroom.event;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.entity.BroomEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 扫帚下马事件处理器
 * 阻止玩家通过shift键快速下马，必须长按达到要求的时间
 */
@Mod.EventBusSubscriber(modid = MajoBroom.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BroomDismountHandler {
    
    /**
     * 处理实体下马事件
     * Minecraft原版的shift下马机制会触发此事件
     * 只取消未授权的主动shift下马，死亡和实体移除等自动下马必须放行
     */
    @SubscribeEvent
    public static void onEntityDismount(EntityMountEvent event) {
        if (!event.isDismounting() || event.getLevel().isClientSide) {
            return;
        }
        
        if (!(event.getEntityBeingMounted() instanceof BroomEntity broom)) {
            return;
        }

        if (!(event.getEntityMounting() instanceof Player player)) {
            return;
        }

        // 只阻止存活玩家主动按Shift下马，死亡和实体移除必须正常清理骑乘关系。
        if (player.isAlive()
                && !player.isRemoved()
                && !broom.isRemoved()
                && player.isShiftKeyDown()
                && !broom.isAllowDismount()) {
            event.setCanceled(true);
        }
    }
}
