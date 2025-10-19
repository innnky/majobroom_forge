package com.rcell.majobroom.client;

import com.rcell.majobroom.client.sound.BroomFlyingSound;
import com.rcell.majobroom.entity.BroomEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 客户端骑乘事件处理器
 * 处理玩家骑乘/下马扫帚时的逻辑
 */
@EventBusSubscriber(value = Dist.CLIENT)
public final class BroomClientHandler {
    private BroomClientHandler() {}
    
    /**
     * 处理实体骑乘事件
     */
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        // 只处理扫帚实体
        if (!(event.getEntityBeingMounted() instanceof BroomEntity broom)) {
            return;
        }
        
        // 仅客户端处理
        if (!event.getLevel().isClientSide) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) {
            return;
        }
        
        // 只处理本地玩家的骑乘事件
        if (event.getEntityMounting().getUUID().equals(player.getUUID())) {
            if (event.isMounting()) {
                // 骑上扫帚
                onMountBroom(broom, mc);
            } else {
                // 下马
                onDismountBroom(broom, mc);
            }
        }
    }
    
    /**
     * 骑上扫帚时的处理
     */
    private static void onMountBroom(BroomEntity broom, Minecraft mc) {
        // 自动切换到配置的视角（从扫帚实体读取配置）
        if (broom.isAutoPerspective()) {
            var perspectiveMode = broom.getPerspectiveMode();
            switch (perspectiveMode) {
                case FIRST_PERSON:
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                    break;
                case SECOND_PERSON:
                    mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                    break;
                case THIRD_PERSON:
                    mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                    break;
            }
        }
        
        // 播放飞行音效
        mc.getSoundManager().play(new BroomFlyingSound(broom));
    }
    
    /**
     * 下马时的处理
     */
    private static void onDismountBroom(BroomEntity broom, Minecraft mc) {
        // 固定切换回第一人称视角（从扫帚实体读取配置）
        if (broom.isAutoPerspective()) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
        
        // 清除强制姿势，恢复正常的姿势控制
        if (mc.player != null) {
            mc.player.setForcedPose(null);
        }
        
        // 音效会在BroomFlyingSound的tick中自动停止
    }
}

