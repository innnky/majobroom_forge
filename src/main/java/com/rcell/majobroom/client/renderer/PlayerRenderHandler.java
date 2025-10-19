package com.rcell.majobroom.client.renderer;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.entity.BroomEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 处理玩家骑乘扫帚时的客户端渲染效果
 * 注意：玩家的实际坐标已在服务端的 positionRider 中更新，
 * 此处不需要额外的渲染偏移（原版会自动处理玩家坐标的渲染）
 */
@EventBusSubscriber(modid = MajoBroom.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class PlayerRenderHandler {

    /**
     * 第三人称：在玩家渲染前的事件处理
     * 由于玩家的实际坐标已在 BroomEntity.positionRider 中应用浮动偏移，
     * 这里不需要额外操作，玩家会自动跟随扫帚浮动
     */
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        Entity vehicle = player.getVehicle();

        // 玩家骑乘扫帚时，坐标已由服务端更新，无需额外处理
        if (vehicle instanceof BroomEntity) {
            // 预留：如果需要额外的渲染效果，可以在这里添加
        }
    }
}
