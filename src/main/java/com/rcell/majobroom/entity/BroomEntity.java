package com.rcell.majobroom.entity;

import com.rcell.majobroom.client.input.KeyBindings;
import com.rcell.majobroom.compat.CompatManager;
import com.rcell.majobroom.init.ModItems;
import com.rcell.majobroom.item.armor.MajoHatItem;
import com.rcell.majobroom.item.armor.MajoRobeItem;
import com.rcell.majobroom.network.packet.BroomInputPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;

import javax.annotation.Nonnull;
import java.util.List;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 扫帚实体 - 简化版，参考Hexerei和原版船的设计
 * 使用GeckoLib渲染，集成式输入处理
 */
public class BroomEntity extends Entity implements GeoEntity {
    
    
    // ============ 浮动效果常量 ============
    private static final float FLOAT_PHASE_INCREMENT = 0.05F;  // 浮动相位增量
    private static final float FLOAT_AMPLITUDE = 0.1F;         // 浮动振幅
    private static final float FLOAT_FREQUENCY = 2.0F;         // 浮动频率
    
    // ============ GeckoLib 缓存（仅用于模型渲染） ============
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    
    // ============ 同步数据（EntityDataAccessor） ============
    private static final EntityDataAccessor<Float> SPEED_MODIFIER = 
        SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.FLOAT);
    
    // 个性化配置（使用Byte存储boolean: 0=false, 1=true）
    private static final EntityDataAccessor<Byte> AUTO_PERSPECTIVE = 
        SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> SIDEWAYS_SITTING = 
        SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> AUTO_HOVER = 
        SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> SPEED_PERCENT = 
        SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PERSPECTIVE_MODE = 
        SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.INT);
    
    // 魔女装备状态（服务端检查，客户端同步）
    private static final EntityDataAccessor<Byte> WEARING_MAJO_ARMOR = 
        SynchedEntityData.defineId(BroomEntity.class, EntityDataSerializers.BYTE);
    
    // ============ 状态变量（不同步） ============
    // 浮动动画
    private float floatPhase = 0.0F;
    private float floatOffset = 0.0F;
    private float prevFloatOffset = 0.0F;
    
    // 转向
    private float deltaRotation = 0.0F;
    
    // 位置同步（用于客户端平滑插值）
    private int lerpSteps = 0;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    
    // 输入状态（客户端设置，服务端接收）
    private boolean inputLeft = false;
    private boolean inputRight = false;
    private boolean inputUp = false;
    private boolean inputDown = false;
    private boolean inputForward = false;  // 上升（Jump/Space）
    private boolean inputBack = false;     // 下降（Ctrl）
    private boolean inputBrake = false;    // 刹车（Shift）
    
    // 来源背包 UUID（用于回收时放回原背包）
    @Nullable
    private java.util.UUID sourceBackpackUUID = null;
    
    // 允许下马标记（仅服务端使用，用于区分授权下马和自动下马）
    private boolean allowDismount = false;
    
    // ============ 构造函数 ============
    public BroomEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }
    
    // ============ GeckoLib 接口实现 ============
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 不注册动画控制器 - 浮动效果在渲染器中应用
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
    
    // ============ 实体数据同步 ============
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPEED_MODIFIER, 1.0F);
        builder.define(AUTO_PERSPECTIVE, (byte) 1);  // 默认开启
        builder.define(SIDEWAYS_SITTING, (byte) 1);  // 默认开启
        builder.define(AUTO_HOVER, (byte) 0);  // 默认关闭
        builder.define(SPEED_PERCENT, 70);  // 默认100%速度
        builder.define(PERSPECTIVE_MODE, 2);  // 默认第三人称（THIRD_PERSON的ordinal）
        builder.define(WEARING_MAJO_ARMOR, (byte) 0);  // 默认没有魔女装备
    }
    
    // ============ 核心tick循环（参考原版船的结构） ============
    @Override
    public void tick() {
        super.tick();
        
        // 强制乘客保持站立姿势（防止shift键触发蹲伏视角下移）
        Entity passenger = getFirstPassenger();
        if (passenger instanceof Player player) {
            player.setForcedPose(Pose.STANDING);
        }
        
        // 更新浮动动画（所有端都执行）
        updateFloatAnimation();
        
        // 处理位置同步插值（参考原版船的tickLerp）
        tickLerp();
        
        if (!this.level().isClientSide) {
            updateMajoArmorStatus();
        }


        // 本地控制时处理运动（参考原版船的isControlledByLocalInstance）
        if (this.isControlledByLocalInstance()) {
            // 客户端：读取输入并发送网络包
            if (this.level().isClientSide) {
                readAndSendInputs();
            }
            
            // 两端都执行：运动计算和应用
            updateMotion();
            move(MoverType.SELF, getDeltaMovement());
            
            if (this.getControllingPassenger() == null && !this.isAutoHover()) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x, motion.y - 0.04D, motion.z);  // 应用重力
                move(MoverType.SELF, getDeltaMovement());
                applyGroundRepulsion();
            }
            // 地面碰撞约束
            applyGroundRepulsion();
        }
    }
    
    /**
     * 处理位置同步插值（参考原版船）
     */
    private void tickLerp() {
        // 如果本地控制，清空插值（使用客户端预测）
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
        
        // 如果有插值步数，执行平滑插值
        if (this.lerpSteps > 0) {
            double x = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
            double y = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
            double z = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
            double yRot = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
            
            this.setYRot(this.getYRot() + (float)yRot / (float)this.lerpSteps);
            this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
            --this.lerpSteps;
            this.setPos(x, y, z);
            this.setRot(this.getYRot(), this.getXRot());
        }
    }
    
    
    // ============ 浮动动画（保留原实现） ============
    private void updateFloatAnimation() {
        // 保存上一帧的浮动偏移
        prevFloatOffset = floatOffset;
        
        // 更新浮动相位
        floatPhase += FLOAT_PHASE_INCREMENT;
        
        // 保持相位在 [0, 2π) 范围内
        if (floatPhase >= Mth.TWO_PI) {
            floatPhase -= Mth.TWO_PI;
        }
        
        // 计算浮动偏移（加入entityId作为相位偏移，避免所有扫帚同步）
        float phaseOffset = (this.getId() % 100) * 0.1F;
        floatOffset = FLOAT_AMPLITUDE * Mth.sin(FLOAT_FREQUENCY * (floatPhase + phaseOffset));
    }
    
    // ============ 输入处理（客户端，每tick发送，参考原版船） ============
    @OnlyIn(Dist.CLIENT)
    private void readAndSendInputs() {
        Entity rider = getFirstPassenger();
        if (!(rider instanceof LocalPlayer player)) {
            return;
        }
        
        // 读取Minecraft原生输入
        this.inputLeft = player.input.left;
        this.inputRight = player.input.right;
        this.inputUp = player.input.up;
        this.inputDown = player.input.down;
        
        // 读取自定义按键
        this.inputForward = player.input.jumping;  // Space/Jump
        this.inputBack = KeyBindings.FLY_DOWN.isDown();  // Ctrl
        
        // 检测刹车键（Shift）- 注意：这里直接读取shift键状态
        Minecraft mc = Minecraft.getInstance();
        this.inputBrake = mc.options.keyShift.isDown();
        
        // 打包所有输入到一个字节（位标志）
        byte flags = (byte)(
            (this.inputLeft ? 0x01 : 0) |      // bit 0: 左
            (this.inputRight ? 0x02 : 0) |     // bit 1: 右
            (this.inputUp ? 0x04 : 0) |        // bit 2: 前
            (this.inputDown ? 0x08 : 0) |      // bit 3: 后
            (this.inputForward ? 0x10 : 0) |   // bit 4: 上升
            (this.inputBack ? 0x20 : 0) |      // bit 5: 下降
            (this.inputBrake ? 0x40 : 0)       // bit 6: 刹车
        );
        
        // 发送统一的输入包到服务端（每tick都发送）
        PacketDistributor.sendToServer(new BroomInputPayload(this.getId(), flags));
    }
    
    // ============ 运动计算（类似船） ============
    private void updateMotion() {
        Vec3 motion = this.getDeltaMovement();
        
        // 刹车模式：按下shift键时立即刹车，忽略所有其他输入
        if (this.inputBrake) {
            // 强力刹车：每tick减速到原速度的30%，快速停止
            double brakeForce = 0.6;
            motion = new Vec3(
                motion.x * brakeForce,
                motion.y * brakeForce,
                motion.z * brakeForce
            );
            // 如果速度已经很小，直接设为0
            if (motion.length() < 0.01) {
                motion = Vec3.ZERO;
            }
            
            // 应用刹车后的运动向量并返回（忽略其他输入）
            this.setDeltaMovement(motion);
            return;
        }
        
        // 正常模式：处理所有输入
        // 1. 水平转向
        if (this.inputLeft) {
            --this.deltaRotation;
        }
        if (this.inputRight) {
            ++this.deltaRotation;
        }
        
        this.setYRot(this.getYRot() + this.deltaRotation);
        this.deltaRotation *= com.rcell.majobroom.config.ServerConfig.momentum;
        
        // 2. 前进速度
        float forwardSpeed = 0.0F;
        if (this.inputUp) {
            forwardSpeed += (float)com.rcell.majobroom.config.ServerConfig.acceleration;
        }
        if (this.inputDown) {
            forwardSpeed -= (float)com.rcell.majobroom.config.ServerConfig.acceleration * 0.5F;  // 后退较慢
        }
        
        // 3. 垂直速度
        double verticalSpeed = motion.y;
        if (this.inputForward) {
            verticalSpeed = Math.min(verticalSpeed + com.rcell.majobroom.config.ServerConfig.verticalAcceleration, 
                                    com.rcell.majobroom.config.ServerConfig.maxVerticalSpeed);
        } else if (this.inputBack) {
            verticalSpeed = Math.max(verticalSpeed - com.rcell.majobroom.config.ServerConfig.verticalAcceleration, 
                                    -com.rcell.majobroom.config.ServerConfig.maxVerticalSpeed);
        }
        
        // 4. 应用前进方向
        double forwardX = -Mth.sin(this.getYRot() * Mth.DEG_TO_RAD) * forwardSpeed;
        double forwardZ = Mth.cos(this.getYRot() * Mth.DEG_TO_RAD) * forwardSpeed;
        
        // 5. 合成并应用动量衰减
        double momentum = com.rcell.majobroom.config.ServerConfig.momentum;
        motion = new Vec3(
            (motion.x + forwardX) * momentum,
            verticalSpeed * momentum,
            (motion.z + forwardZ) * momentum
        );
        
        // 6. 限制最大水平速度，并根据装备情况调整速度
        double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        double maxSpeed = getEffectiveMaxSpeed();  // 使用计算后的有效最大速度
        if (horizontalSpeed > maxSpeed) {
            double scale = maxSpeed / horizontalSpeed;
            motion = new Vec3(motion.x * scale, motion.y, motion.z * scale);
        }
        
        // 7. 更新运动向量
        this.setDeltaMovement(motion);
    }
    
    /**
     * 获取有效的最大速度，根据乘客是否穿戴魔女装备和配置的速度百分比进行调整
     * @return 有效的最大速度
     */
    private double getEffectiveMaxSpeed() {
        Entity passenger = getFirstPassenger();
        double baseMaxSpeed = com.rcell.majobroom.config.ServerConfig.maxSpeed;
        
        // 如果没有乘客，返回基础速度
        if (!(passenger instanceof LivingEntity)) {
            return baseMaxSpeed * (getSpeedPercent() / 100.0);
        }
        
        // 直接使用同步的魔女装备状态（服务端已检查并同步）
        boolean wearingMajoArmor = this.entityData.get(WEARING_MAJO_ARMOR) != 0;
        
        // 如果没有穿戴任何魔女装备，应用速度惩罚
        if (!wearingMajoArmor) {
            baseMaxSpeed *= com.rcell.majobroom.config.ServerConfig.noArmorSpeedPenalty;
        }
        
        // 应用配置的速度百分比
        return baseMaxSpeed * (getSpeedPercent() / 100.0);
    }
    
    /**
     * 服务端：更新魔女装备状态并同步到客户端
     */
    private void updateMajoArmorStatus() {
        Entity passenger = getFirstPassenger();
        if (passenger instanceof LivingEntity living) {
            boolean wearingMajoArmor = isWearingMajoArmor(living);
            byte currentStatus = this.entityData.get(WEARING_MAJO_ARMOR);
            byte newStatus = wearingMajoArmor ? (byte) 1 : (byte) 0;
            
            // 只有状态改变时才更新，避免不必要的网络同步
            if (currentStatus != newStatus) {
                this.entityData.set(WEARING_MAJO_ARMOR, newStatus);
            }
        } else {
            // 没有乘客时，设置为没有魔女装备
            if (this.entityData.get(WEARING_MAJO_ARMOR) != 0) {
                this.entityData.set(WEARING_MAJO_ARMOR, (byte) 0);
            }
        }
    }
    
    /**
     * 检查生物是否穿戴了魔女装备（帽子或长袍）
     * 会检查原版装备槽和兼容模组的装饰槽位
     * @param entity 要检查的生物
     * @return 是否穿戴了至少一件魔女装备
     */
    private boolean isWearingMajoArmor(LivingEntity entity) {
        // 检查原版装备槽
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof MajoHatItem) {
            return true;
        }
        
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof MajoRobeItem) {
            return true;
        }
        
        // 检查装饰盔甲槽位（如果有兼容模组）
        List<ItemStack> extraArmor = CompatManager.getExtraArmorItems(entity);
        for (ItemStack stack : extraArmor) {
            if (stack.getItem() instanceof MajoHatItem || stack.getItem() instanceof MajoRobeItem) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 地面和水面碰撞约束：检测向下方块或水面是否会碰撞，并施加向上排斥力
     */
    private void applyGroundRepulsion() {
        // 创建向下偏移的碰撞箱
        AABB checkBox = this.getBoundingBox().move(0, -com.rcell.majobroom.config.ServerConfig.groundCheckOffset, 0);
        
        // 检测固体方块碰撞或液体碰撞（使用同一个碰撞箱）
        boolean hasCollision = !this.level().noCollision(this, checkBox);  // 固体方块
        boolean hasLiquid = this.level().containsAnyLiquid(checkBox);      // 液体（水、岩浆等）
        
        // 如果检测到碰撞或液体，施加向上的排斥力
        if (hasCollision || hasLiquid) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motion.y + com.rcell.majobroom.config.ServerConfig.groundRepulsion, motion.z);
        }
    }
    
    // ============ 位置同步（客户端接收服务端位置） ============
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = 10; // 10步平滑插值
    }
    
    // ============ 服务端输入接收 ============
    /**
     * 由自定义网络包调用，设置所有输入（统一接口）
     * 这个方法在服务端被调用，接收来自客户端的输入
     * 每tick都会被调用（原版船的做法）
     */
    public void setInput(boolean left, boolean right, boolean forward, boolean backward, boolean up, boolean down, boolean brake) {
        // 直接设置输入状态，不打印日志（每tick调用会刷屏）
        this.inputLeft = left;
        this.inputRight = right;
        this.inputUp = forward;
        this.inputDown = backward;
        this.inputForward = up;
        this.inputBack = down;
        this.inputBrake = brake;
    }
    
    // ============ 骑乘系统 ============
    public double getPassengersRidingOffset() {
        return 0.2D;
    }

    @Override
    public void positionRider(@Nonnull Entity passenger, 
                              @Nonnull MoveFunction moveFunction) {
        if (hasPassenger(passenger)) {
            // 应用浮动偏移
            double yOffset = this.getPassengersRidingOffset() + floatOffset;
            
            // 设置乘客位置
            moveFunction.accept(passenger, this.getX(), this.getY() + yOffset, this.getZ());
            
            // 同步旋转（参考原版船）
            passenger.setYRot(passenger.getYRot() + this.deltaRotation);
            passenger.setYHeadRot(passenger.getYHeadRot() + this.deltaRotation);
            
            // 限制乘客旋转，使身体跟随扫帚旋转（关键！）
            this.clampRotation(passenger);
        }
    }
    
    /**
     * 限制乘客旋转，使身体跟随扫帚旋转（参考原版船逻辑）
     */
    protected void clampRotation(Entity passenger) {
        // 根据配置计算身体旋转角度
        float bodyRotation = this.getYRot();
        if (this.isSidewaysSitting()) {
            // 侧坐：身体逆时针旋转90度
            bodyRotation -= 90.0F;
        }
        
        // 设置身体旋转
        passenger.setYBodyRot(bodyRotation);
        
        // 计算玩家视角和身体旋转的差值
        float angleDiff = Mth.wrapDegrees(passenger.getYRot() - bodyRotation);
        
        // 限制视角偏移在 [-105, 105] 度范围内
        float clampedDiff = Mth.clamp(angleDiff, -105.0F, 105.0F);
        
        // 调整旋转
        passenger.yRotO += clampedDiff - angleDiff;
        passenger.setYRot(passenger.getYRot() + clampedDiff - angleDiff);
        passenger.setYHeadRot(passenger.getYRot());
    }
    
    /**
     * 当乘客转头时调用（参考原版船）
     */
    @Override
    public void onPassengerTurned(@Nonnull Entity passenger) {
        this.clampRotation(passenger);
    }
    
    @Override
    protected void removePassenger(@Nonnull Entity passenger) {
        super.removePassenger(passenger);
        
        // 清空输入状态
        this.inputLeft = false;
        this.inputRight = false;
        this.inputUp = false;
        this.inputDown = false;
        this.inputForward = false;
        this.inputBack = false;
    }
    
    @Override
    public @Nonnull InteractionResult interact(
            @Nonnull Player player, 
            @Nonnull InteractionHand hand) {
        // 玩家按住Shift时不上扫帚（用于其他交互，比如打开GUI等）
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        
        // 客户端：告诉服务端要处理这个交互
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        // 服务端：实际执行骑乘逻辑
        boolean success = player.startRiding(this);
        return success ? InteractionResult.CONSUME : InteractionResult.PASS;
    }
    
    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity passenger = getFirstPassenger();
        if (passenger instanceof LivingEntity living) {
            return living;
        }
        return null;
    }
    
    /**
     * 设置允许下马标记
     * 用于区分授权的下马（通过网络包）和未授权的自动下马（shift键）
     */
    public void setAllowDismount(boolean allow) {
        this.allowDismount = allow;
    }
    
    /**
     * 检查是否允许下马
     */
    public boolean isAllowDismount() {
        return this.allowDismount;
    }
    
    // ============ 扫帚回收功能 ============
    /**
     * 处理玩家攻击扫帚：当玩家按住Shift键并左键攻击时，回收扫帚物品
     */
    @Override
    public boolean hurt(@javax.annotation.Nonnull DamageSource source, float amount) {
        // 如果扫帚已经被移除或处于无敌状态，拒绝伤害
        if (this.isRemoved() || this.isInvulnerableTo(source)) {
            return false;
        }
        
        // 检查伤害源是否来自玩家
        Entity sourceEntity = source.getEntity();
        if (sourceEntity instanceof Player player) {
            // 检查玩家是否按住Shift键
            if (player.isShiftKeyDown()) {
                // 仅在服务端处理
                if (!this.level().isClientSide) {
                    // 播放音效
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 生成回收粒子效果
                    spawnBroomParticles(this.level(), this.getX(), this.getY(), this.getZ());
                    
                    // 给玩家添加扫帚物品并保存配置
                    ItemStack broomStack = new ItemStack(ModItems.BROOM.get());
                    this.saveConfigToItemStack(broomStack);  // 保存配置到物品NBT
                    
                    if (!player.getInventory().add(broomStack)) {
                        // 如果物品栏满了，掉落在地上
                        player.drop(broomStack, false);
                    }
                    
                    // 如果有乘客，先卸下（需要设置允许下马标记）
                    this.setAllowDismount(true);
                    this.ejectPassengers();
                    this.setAllowDismount(false);
                    
                    // 移除扫帚实体
                    this.discard();
                }
                
                // 返回true表示伤害被处理
                return true;
            }
        }
        
        // 其他情况：扫帚不受伤害（类似船的行为）
        return false;
    }
    
    // ============ 数据持久化 ============
    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        if (tag.contains("SpeedModifier")) {
            entityData.set(SPEED_MODIFIER, tag.getFloat("SpeedModifier"));
        }
        
        // 读取配置到EntityData
        if (tag.contains("AutoPerspective")) {
            entityData.set(AUTO_PERSPECTIVE, tag.getBoolean("AutoPerspective") ? (byte) 1 : (byte) 0);
        }
        if (tag.contains("SidewaysSitting")) {
            entityData.set(SIDEWAYS_SITTING, tag.getBoolean("SidewaysSitting") ? (byte) 1 : (byte) 0);
        }
        if (tag.contains("AutoHover")) {
            entityData.set(AUTO_HOVER, tag.getBoolean("AutoHover") ? (byte) 1 : (byte) 0);
        }
        if (tag.contains("SpeedPercent")) {
            entityData.set(SPEED_PERCENT, tag.getInt("SpeedPercent"));
        }
        
        // 读取来源背包 UUID
        if (tag.hasUUID("SourceBackpackUUID")) {
            this.sourceBackpackUUID = tag.getUUID("SourceBackpackUUID");
        }
    }
    
    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putFloat("SpeedModifier", entityData.get(SPEED_MODIFIER));
        
        // 保存配置
        tag.putBoolean("AutoPerspective", entityData.get(AUTO_PERSPECTIVE) != 0);
        tag.putBoolean("SidewaysSitting", entityData.get(SIDEWAYS_SITTING) != 0);
        tag.putBoolean("AutoHover", entityData.get(AUTO_HOVER) != 0);
        tag.putInt("SpeedPercent", entityData.get(SPEED_PERCENT));
        
        // 保存来源背包 UUID
        if (this.sourceBackpackUUID != null) {
            tag.putUUID("SourceBackpackUUID", this.sourceBackpackUUID);
        }
    }
    
    // ============ Getter/Setter ============
    public float getSpeedModifier() {
        return entityData.get(SPEED_MODIFIER);
    }
    
    /**
     * 获取来源背包的 UUID
     * @return 来源背包的 UUID，如果不是从背包召唤的则返回 null
     */
    @Nullable
    public java.util.UUID getSourceBackpackUUID() {
        return this.sourceBackpackUUID;
    }
    
    /**
     * 设置来源背包的 UUID
     * @param uuid 背包的 UUID，可以为 null
     */
    public void setSourceBackpackUUID(@Nullable java.util.UUID uuid) {
        this.sourceBackpackUUID = uuid;
    }
    
    public void setSpeedModifier(float modifier) {
        entityData.set(SPEED_MODIFIER, modifier);
    }
    
    // 配置 Getter/Setter（使用EntityData自动同步）
    public boolean isAutoPerspective() {
        return entityData.get(AUTO_PERSPECTIVE) != 0;
    }
    
    public void setAutoPerspective(boolean autoPerspective) {
        entityData.set(AUTO_PERSPECTIVE, autoPerspective ? (byte) 1 : (byte) 0);
    }
    
    public boolean isSidewaysSitting() {
        return entityData.get(SIDEWAYS_SITTING) != 0;
    }
    
    public void setSidewaysSitting(boolean sidewaysSitting) {
        entityData.set(SIDEWAYS_SITTING, sidewaysSitting ? (byte) 1 : (byte) 0);
    }
    
    public boolean isAutoHover() {
        return entityData.get(AUTO_HOVER) != 0;
    }
    
    public void setAutoHover(boolean autoHover) {
        entityData.set(AUTO_HOVER, autoHover ? (byte) 1 : (byte) 0);
    }
    
    public int getSpeedPercent() {
        return entityData.get(SPEED_PERCENT);
    }
    
    public void setSpeedPercent(int speedPercent) {
        // 限制在 0-100 范围内
        int clamped = Math.max(0, Math.min(100, speedPercent));
        entityData.set(SPEED_PERCENT, clamped);
    }
    
    public com.rcell.majobroom.common.PerspectiveMode getPerspectiveMode() {
        return com.rcell.majobroom.common.PerspectiveMode.fromOrdinal(entityData.get(PERSPECTIVE_MODE));
    }
    
    public boolean isWearingMajoArmor() {
        return this.entityData.get(WEARING_MAJO_ARMOR) != 0;
    }
    
    public void setPerspectiveMode(com.rcell.majobroom.common.PerspectiveMode mode) {
        entityData.set(PERSPECTIVE_MODE, mode.ordinal());
    }
    
    // ============ NBT同步（实体<->物品） ============
    /**
     * 从物品NBT加载配置到实体
     */
    public void loadConfigFromItemStack(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(
            net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY
        );
        CompoundTag tag = customData.copyTag();
        if (tag.isEmpty()) return;
        
        if (tag.contains("AutoPerspective")) {
            setAutoPerspective(tag.getBoolean("AutoPerspective"));
        }
        if (tag.contains("SidewaysSitting")) {
            setSidewaysSitting(tag.getBoolean("SidewaysSitting"));
        }
        if (tag.contains("AutoHover")) {
            setAutoHover(tag.getBoolean("AutoHover"));
        }
        if (tag.contains("SpeedPercent")) {
            setSpeedPercent(tag.getInt("SpeedPercent"));
        }
        if (tag.contains("PerspectiveMode")) {
            setPerspectiveMode(com.rcell.majobroom.common.PerspectiveMode.fromOrdinal(tag.getInt("PerspectiveMode")));
        }
    }
    
    /**
     * 将实体配置保存到物品NBT
     */
    public void saveConfigToItemStack(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("AutoPerspective", isAutoPerspective());
        tag.putBoolean("SidewaysSitting", isSidewaysSitting());
        tag.putBoolean("AutoHover", isAutoHover());
        tag.putInt("SpeedPercent", getSpeedPercent());
        tag.putInt("PerspectiveMode", getPerspectiveMode().ordinal());
        
        // 使用新的DataComponents API保存数据
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.of(tag));
    }
    
    /**
     * 是否正在飞行（用于粒子效果等）
     */
    public boolean isFlying() {
        return getDeltaMovement().length() > 0.1;
    }
    
    /**
     * 获取插值后的浮动偏移值（用于渲染）
     */
    public float getInterpolatedFloatOffset(float partialTick) {
        return Mth.lerp(partialTick, prevFloatOffset, floatOffset);
    }

    
    // ============ 基础属性 ============
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    
    @Override
    public boolean isPickable() {
        // 允许玩家用光标选中并右键交互
        return !this.isRemoved();
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }
    
    @Override
    protected boolean canRide(@Nonnull Entity vehicle) {
        return false;
    }

    // ============ 摔落伤害保护 ============
    @Override
    protected void checkFallDamage(double y, boolean onGround, 
                                   @javax.annotation.Nonnull net.minecraft.world.level.block.state.BlockState state, 
                                   @javax.annotation.Nonnull net.minecraft.core.BlockPos pos) {
        // 扫帚不受摔落伤害（参考原版船）
        this.resetFallDistance();
    }

    // ============ 粒子效果工具方法 ============
    /**
     * 生成扫帚召唤/回收的粒子效果（环形烟雾）
     * @param level 世界对象
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     */
    public static void spawnBroomParticles(Level level, double x, double y, double z) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        
        // 环形烟雾效果
        for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2 * i) / 6;
            double offsetX = Math.cos(angle) * 0.5;
            double offsetZ = Math.sin(angle) * 0.5;
            serverLevel.sendParticles(ParticleTypes.POOF, 
                x + offsetX, y + 0.1, z + offsetZ, 
                1, 0, 0.1, 0, 0.06);  // 速度从0.02提高到0.1，消散更快
        }
    }
    
}
