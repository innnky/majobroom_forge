package com.rcell.majobroom.client;

import com.rcell.majobroom.client.gui.base.SimpleScreenOpener;
import com.rcell.majobroom.client.input.KeyBindings;
import com.rcell.majobroom.client.particle.BroomTrailParticles;
import com.rcell.majobroom.entity.BroomEntity;
import com.rcell.majobroom.client.gui.screen.BroomConfigScreen;
import com.rcell.majobroom.network.packet.BroomDismountPayload;
import com.rcell.majobroom.network.packet.BroomSummonPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端输入处理器（简化版）
 * 仅处理召唤按键和粒子效果，其他输入已集成到BroomEntity中
 * 音效由 BroomFlyingSound 处理
 */
@EventBusSubscriber(value = Dist.CLIENT)
public final class InputHandler {
    private InputHandler() {}
    
    // Shift键按下的tick计数（-1表示未按下）
    private static int shiftPressedTicks = -1;
    // 需要长按的tick数（0.5秒 = 10 ticks）
    private static final int REQUIRED_HOLD_TICKS = 10;
    // 上次显示提示的时间，避免频繁显示
    private static long lastMessageTime = 0;
    
    /**
     * 获取shift键是否已按下足够时间
     */
    public static boolean hasHeldShiftLongEnough() {
        return shiftPressedTicks >= REQUIRED_HOLD_TICKS;
    }
    
    /**
     * 重置shift键计时器
     */
    public static void resetShiftTimer() {
        shiftPressedTicks = -1;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // 处理召唤/收回扫帚
        if (KeyBindings.SUMMON_BROOM.consumeClick()) {
            PacketDistributor.sendToServer(new BroomSummonPayload());
        }

        // 处理打开配置界面（仅在骑乘扫帚时）
        if (KeyBindings.OPEN_CONFIG.consumeClick()) {
            if (player.getVehicle() instanceof BroomEntity broom) {
                SimpleScreenOpener.open(new BroomConfigScreen(broom));
            }
        }

        // 客户端粒子效果
        if (player.getVehicle() instanceof BroomEntity broom) {
            if (broom.isFlying()) {
                BroomTrailParticles.spawn(broom.level(), broom.position());
            }
            
            // 追踪shift键长按时间（仅在骑扫帚时）
            if (mc.options.keyShift.isDown()) {
                if (shiftPressedTicks < 0) {
                    shiftPressedTicks = 0;
                } else {
                    shiftPressedTicks++;
                }
                
                // 当达到要求时间时，发送下马包
                if (shiftPressedTicks == REQUIRED_HOLD_TICKS) {
                    PacketDistributor.sendToServer(new BroomDismountPayload(broom.getId()));
                }
            } else {
                // 松开shift键，检查是否按的时间不够
                if (shiftPressedTicks > 0 && shiftPressedTicks < REQUIRED_HOLD_TICKS) {
                    // 显示提示消息（避免频繁显示）
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastMessageTime > 1000) { // 至少间隔1秒
                        player.displayClientMessage(
                            Component.translatable("message.majobroom.hold_shift_to_dismount")
                                .withStyle(ChatFormatting.YELLOW),
                            true  // actionBar显示
                        );
                        lastMessageTime = currentTime;
                    }
                }
                // 重置计数器
                shiftPressedTicks = -1;
            }
        } else {
            // 不在骑扫帚时重置计数器
            shiftPressedTicks = -1;
        }
    }
}
