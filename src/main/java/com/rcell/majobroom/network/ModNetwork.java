package com.rcell.majobroom.network;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.network.packet.BroomConfigPayload;
import com.rcell.majobroom.network.packet.BroomDismountPayload;
import com.rcell.majobroom.network.packet.BroomInputPayload;
import com.rcell.majobroom.network.packet.BroomSummonPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络通信管理
 * 注册所有自定义网络包
 */
@EventBusSubscriber(modid = MajoBroom.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {
    private ModNetwork() {}

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        
        // 输入控制包（客户端→服务端）
        registrar.playToServer(
            BroomInputPayload.TYPE,
            BroomInputPayload.STREAM_CODEC,
            BroomInputPayload::handle
        );
        
        // 召唤/收回包（客户端→服务端）
        registrar.playToServer(
            BroomSummonPayload.TYPE,
            BroomSummonPayload.STREAM_CODEC,
            BroomSummonPayload::handle
        );
        
        // 配置同步包（客户端→服务端）
        registrar.playToServer(
            BroomConfigPayload.TYPE,
            BroomConfigPayload.STREAM_CODEC,
            BroomConfigPayload::handle
        );
        
        // 下马包（客户端→服务端）
        registrar.playToServer(
            BroomDismountPayload.TYPE,
            BroomDismountPayload.STREAM_CODEC,
            BroomDismountPayload::handle
        );
    }
}
