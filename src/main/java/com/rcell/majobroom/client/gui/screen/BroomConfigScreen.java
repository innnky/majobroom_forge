package com.rcell.majobroom.client.gui.screen;

import com.rcell.majobroom.client.gui.util.GuiItemRenderer;
import com.rcell.majobroom.common.PerspectiveMode;
import com.rcell.majobroom.entity.BroomEntity;
import com.rcell.majobroom.client.gui.texture.GuiIcons;
import com.rcell.majobroom.client.gui.texture.GuiTextures;
import com.rcell.majobroom.client.gui.widget.IconButton;
import com.rcell.majobroom.client.gui.widget.ValueSlider;
import com.rcell.majobroom.init.ModItems;
import com.rcell.majobroom.network.packet.BroomConfigPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * 扫帚配置界面
 * 显示和修改扫帚的个性化配置
 */
public class BroomConfigScreen extends AbstractBroomScreen {
    private final BroomEntity broomEntity;
    
    // 布局常量 - 右侧padding多8像素用于视觉平衡
    private static final int LEFT_PADDING = 15;
    private static final int RIGHT_PADDING = 23;  // 15 + 8
    private static final int BUTTON_WIDTH = 18;
    
    // 配置控件
    private IconButton perspectiveFirstButton;   // 第一人称按钮
    private IconButton perspectiveSecondButton;  // 第二人称按钮
    private IconButton perspectiveThirdButton;   // 第三人称按钮
    private IconButton sidewaysSittingButton;
    private IconButton autoHoverButton;
    private ValueSlider speedSlider;
    
    // 配置数据（本地副本）
    private PerspectiveMode perspectiveMode;
    private boolean sidewaysSitting;
    private boolean autoHover;
    private int speedPercent = 70;  // 速度百分比 (0-100)
    
    // 用于渲染的物品
    private final ItemStack broomItem = new ItemStack(ModItems.BROOM.get());

    public BroomConfigScreen(BroomEntity broom) {
        super(Component.translatable("gui.majobroom.broom_config.title"));
        this.broomEntity = broom;
        
        // 从扫帚实体读取当前配置
        this.perspectiveMode = broom.getPerspectiveMode();
        this.sidewaysSitting = broom.isSidewaysSitting();
        this.autoHover = broom.isAutoHover();
        this.speedPercent = broom.getSpeedPercent();
        
        // 设置窗口大小
        this.background = GuiTextures.BROOM_CONFIG;
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        setWindowOffset(-20, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop;
        
        // 计算布局位置
        int contentWidth = background.getWidth() - LEFT_PADDING - RIGHT_PADDING;  // 144
        int textX = x + LEFT_PADDING;  // 文本左对齐位置
        
        // 计算按钮居中位置（在文本右侧到右边界之间居中）
        // 文本区域预留80像素，按钮在剩余空间居中，然后向左偏移8像素
        int textAreaWidth = 80;
        int buttonAreaWidth = contentWidth - textAreaWidth;
        int buttonCenterX = x + LEFT_PADDING + textAreaWidth + (buttonAreaWidth - BUTTON_WIDTH) / 2 - 8;

        // 自动切换视角三选一按钮（第一行：y + 21）
        // 参考Create mod的Attribute Filter实现，三个按钮水平排列
        int perspectiveY = y + 21;
        int perspectiveStartX = buttonCenterX;  // 第一个按钮与下方开关按钮左对齐
        
        perspectiveFirstButton = new IconButton(perspectiveStartX, perspectiveY, GuiIcons.I_PERSPECTIVE_FIRST);
        perspectiveFirstButton.withCallback(() -> {
            this.perspectiveMode = PerspectiveMode.FIRST_PERSON;
        });
        perspectiveFirstButton.setToolTip(
            Component.translatable("gui.majobroom.config.perspective.first_person")
        );
        
        perspectiveSecondButton = new IconButton(perspectiveStartX + 18, perspectiveY, GuiIcons.I_PERSPECTIVE_SECOND);
        perspectiveSecondButton.withCallback(() -> {
            this.perspectiveMode = PerspectiveMode.SECOND_PERSON;
        });
        perspectiveSecondButton.setToolTip(
            Component.translatable("gui.majobroom.config.perspective.second_person")
        );
        
        perspectiveThirdButton = new IconButton(perspectiveStartX + 36, perspectiveY, GuiIcons.I_PERSPECTIVE_THIRD);
        perspectiveThirdButton.withCallback(() -> {
            this.perspectiveMode = PerspectiveMode.THIRD_PERSON;
        });
        perspectiveThirdButton.setToolTip(
            Component.translatable("gui.majobroom.config.perspective.third_person")
        );

        // 侧坐开关（第二行：y + 41）
        sidewaysSittingButton = new IconButton(buttonCenterX, y + 41, GuiIcons.I_SIDEWAYS_SITTING);
        sidewaysSittingButton.withCallback(() -> {
            this.sidewaysSitting = !this.sidewaysSitting;
        });
        sidewaysSittingButton.setToolTip(
            Component.translatable("gui.majobroom.config.sideways_sitting.tooltip")
        );

        // 自动悬浮开关（第三行：y + 61）
        autoHoverButton = new IconButton(buttonCenterX, y + 61, GuiIcons.I_AUTO_HOVER);
        autoHoverButton.withCallback(() -> {
            this.autoHover = !this.autoHover;
        });
        autoHoverButton.setToolTip(
            Component.translatable("gui.majobroom.config.auto_hover.tooltip")
        );

        // 速度滑动条（第四行：y + 86，独立一行，左侧对齐文本）
        int sliderY = y + 86;
        speedSlider = new ValueSlider(
            textX, 
            sliderY + 12,  // 滑条在标签下方
            contentWidth,  // 使用内容区域宽度
            Component.translatable("gui.majobroom.config.speed"),
            speedPercent
        );
        speedSlider.withCallback(newValue -> {
            this.speedPercent = newValue;
        });

        // 确认按钮（右下角，考虑右侧padding，向右移10像素）
        confirmButton = new IconButton(x + background.getWidth() - RIGHT_PADDING - BUTTON_WIDTH + 8, 
            y + background.getHeight() - 24, 
            GuiIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            // 立即应用视角切换（仅在玩家正在骑乘时）
            applyPerspectiveChange();
            onClose();
        });
        confirmButton.setToolTip(
            Component.translatable("gui.majobroom.config.confirm.tooltip")
        );

        addRenderableWidget(perspectiveFirstButton);
        addRenderableWidget(perspectiveSecondButton);
        addRenderableWidget(perspectiveThirdButton);
        addRenderableWidget(sidewaysSittingButton);
        addRenderableWidget(autoHoverButton);
        addRenderableWidget(speedSlider);
        addRenderableWidget(confirmButton);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 每帧更新按钮的显示状态
        // 三选一按钮：选中的按钮显示为绿色高亮，未选中的按钮显示为普通按钮
        perspectiveFirstButton.green = perspectiveMode == PerspectiveMode.FIRST_PERSON;
        perspectiveSecondButton.green = perspectiveMode == PerspectiveMode.SECOND_PERSON;
        perspectiveThirdButton.green = perspectiveMode == PerspectiveMode.THIRD_PERSON;
        
        // 开关按钮：开启时显示绿色背景，关闭时显示普通背景，图标保持不变
        sidewaysSittingButton.green = sidewaysSitting;
        autoHoverButton.green = autoHover;
    }

    @Override
    protected void renderWindow(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;
        
        // 计算文本位置
        int textX = x + LEFT_PADDING;

        // 渲染背景
        background.render(graphics, x, y);

        // 渲染标题（居中，考虑不对称padding）
        Component title = Component.translatable("gui.majobroom.broom_config.title");
        int titleX = x + LEFT_PADDING + (background.getWidth() - LEFT_PADDING - RIGHT_PADDING - font.width(title)) / 2;
        graphics.drawString(font, title, titleX, y + 4, GuiTextures.TITLE_FONT_COLOR, false);

        // 渲染配置项标签（统一左对齐，垂直居中按钮）
        // 按钮高度18，文本高度8，所以文本向下偏移 (18-8)/2 = 5
        graphics.drawString(font, 
            Component.translatable("gui.majobroom.config.perspective_mode"),
            textX, y + 21 + 5, 
            GuiTextures.CONTENT_FONT_COLOR, false);
        
        graphics.drawString(font, 
            Component.translatable("gui.majobroom.config.sideways_sitting"),
            textX, y + 41 + 5, 
            GuiTextures.CONTENT_FONT_COLOR, false);
        
        graphics.drawString(font, 
            Component.translatable("gui.majobroom.config.auto_hover"),
            textX, y + 61 + 5, 
            GuiTextures.CONTENT_FONT_COLOR, false);

        // 渲染速度滑动条标签（左对齐）
        graphics.drawString(font, 
            Component.translatable("gui.majobroom.config.speed"),
            textX, y + 86, 
            GuiTextures.CONTENT_FONT_COLOR, false);

        // 渲染扫帚 3D 模型（右侧，垂直对齐确认按钮中心）
        // 使用自定义的 GuiItemRenderer，参数与原 GuiGameElement 调用一致
        // 原调用：GuiGameElement.of(broomItem).at(x + width + 10, y + height - 40, -50).scale(3).render(graphics)
        GuiItemRenderer.renderItemAt(
            graphics, 
            broomItem, 
            x + background.getWidth() + 10,      // x 位置
            y + background.getHeight() - 40,     // y 位置
            -50,                                  // z 深度
            3.0f                                  // 缩放比例
        );
    }

    @Override
    public void removed() {
        super.removed();
        // 关闭界面时发送配置到服务端
        sendConfigToServer();
    }

    private void sendConfigToServer() {
        if (broomEntity != null) {
            PacketDistributor.sendToServer(new BroomConfigPayload(
                broomEntity.getId(),
                perspectiveMode.ordinal(),  // 发送枚举序号
                sidewaysSitting,
                autoHover,
                speedPercent
            ));
        }
    }
    
    /**
     * 立即应用视角切换（客户端立即生效）
     */
    private void applyPerspectiveChange() {
        var mc = minecraft;
        if (mc == null) {
            return;
        }
        
        var player = mc.player;
        if (player == null) {
            return;
        }
        
        // 只有玩家正在骑乘扫帚时才应用视角
        if (player.getVehicle() == broomEntity) {
            // 根据perspectiveMode切换视角
            switch (perspectiveMode) {
                case FIRST_PERSON:
                    mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                    break;
                case SECOND_PERSON:
                    mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
                    break;
                case THIRD_PERSON:
                    mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                    break;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        // 不暂停游戏
        return false;
    }
}

