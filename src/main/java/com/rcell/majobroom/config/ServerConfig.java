package com.rcell.majobroom.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端配置（SERVER类型）
 * - 配置文件存储在: world/serverconfig/majobroom-server.toml
 * - 会自动从服务端同步到客户端
 * - 适用于游戏规则、平衡性等需要统一的设置
 */
public class ServerConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 飞行设置
    static {
        BUILDER.comment("飞行设置").push("flight");
        MAX_SPEED_SPEC = BUILDER
                .comment("最大水平速度（方块/tick）(需搭配加速度一起修改)")
                .defineInRange("maxSpeed", 0.9D, 0.3D, 2.0D);
        ACCELERATION_SPEC = BUILDER
                .comment("水平加速度")
                .defineInRange("acceleration", 0.08D, 0.04D, 0.1D);
        MAX_VERTICAL_SPEED_SPEC = BUILDER
                .comment("最大垂直速度（方块/tick）")
                .defineInRange("maxVerticalSpeed", 0.5D, 0.1D, 2.0D);
        VERTICAL_ACCELERATION_SPEC = BUILDER
                .comment("垂直加速度")
                .defineInRange("verticalAcceleration", 0.05D, 0.001D, 0.5D);
        MOMENTUM_SPEC = BUILDER
                .comment("动量保持系数（值越大惯性越大）")
                .defineInRange("momentum", 0.92D, 0.5D, 0.99D);
        GROUND_CHECK_OFFSET_SPEC = BUILDER
                .comment("地面检测向下偏移距离（方块）")
                .defineInRange("groundCheckOffset", 0.02D, 0.0D, 0.1D);
        GROUND_REPULSION_SPEC = BUILDER
                .comment("地面排斥力强度")
                .defineInRange("groundRepulsion", 0.005D, 0.0D, 0.1D);
        NO_ARMOR_SPEED_PENALTY_SPEC = BUILDER
                .comment("未穿戴魔女装备时的速度惩罚系数（1.0表示无惩罚，0.6表示速度降低至60%）")
                .defineInRange("noArmorSpeedPenalty", 0.38D, 0.1D, 1.0D);
        BUILDER.pop();

    }

    // 配置规范
    public static final ModConfigSpec SPEC = BUILDER.build();

    // ============ ModConfigSpec 值（配置定义） ============
    private static ModConfigSpec.DoubleValue MAX_SPEED_SPEC;
    private static ModConfigSpec.DoubleValue ACCELERATION_SPEC;
    private static ModConfigSpec.DoubleValue MAX_VERTICAL_SPEED_SPEC;
    private static ModConfigSpec.DoubleValue VERTICAL_ACCELERATION_SPEC;
    private static ModConfigSpec.DoubleValue MOMENTUM_SPEC;
    private static ModConfigSpec.DoubleValue GROUND_CHECK_OFFSET_SPEC;
    private static ModConfigSpec.DoubleValue GROUND_REPULSION_SPEC;
    private static ModConfigSpec.DoubleValue NO_ARMOR_SPEED_PENALTY_SPEC;
    

    // ============ 缓存值（实际使用，性能优化） ============
    // 飞行参数
    public static double maxSpeed;
    public static double acceleration;
    public static double maxVerticalSpeed;
    public static double verticalAcceleration;
    public static double momentum;
    public static double groundCheckOffset;
    public static double groundRepulsion;
    public static double noArmorSpeedPenalty;
    
    
    /**
     * 将配置值烘焙到缓存变量中（由 ConfigEvents 调用）
     * 这样在运行时使用缓存值，避免每次都调用 .get()
     */
    public static void bake() {
        // 飞行参数
        maxSpeed = MAX_SPEED_SPEC.get();
        acceleration = ACCELERATION_SPEC.get();
        maxVerticalSpeed = MAX_VERTICAL_SPEED_SPEC.get();
        verticalAcceleration = VERTICAL_ACCELERATION_SPEC.get();
        momentum = MOMENTUM_SPEC.get();
        groundCheckOffset = GROUND_CHECK_OFFSET_SPEC.get();
        groundRepulsion = GROUND_REPULSION_SPEC.get();
        noArmorSpeedPenalty = NO_ARMOR_SPEED_PENALTY_SPEC.get();
        
    }
}
