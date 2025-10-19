package com.rcell.majobroom.client.gui.widget;

import com.rcell.majobroom.client.gui.texture.GuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * 滑动条组件
 * 用于调节 0-100 的数值，显示为百分比
 * 参考 Create 的 ValueSettingsScreen 实现
 */
public class ValueSlider extends AbstractWidget {
    private static final int MILESTONE_SIZE = 4;  // 刻度标记大小
    private static final int MILESTONE_INTERVAL = 20;  // 刻度间隔（调整为20以适应更小的宽度）
    private static final int SCALE = 1;  // 像素缩放（调整为1以适应更小的宽度）
    
    private int value;  // 当前值 (0-100)
    private final int maxValue = 100;
    private boolean dragging = false;
    private int lastHoveredValue = -1;
    private int soundCoolDown = 0;
    
    private Consumer<Integer> onValueChanged;
    private final Component label;
    
    // 滑动条的实际宽度
    private final int barWidth;
    
    public ValueSlider(int x, int y, int width, Component label, int initialValue) {
        super(x, y, width, 14, label);  // 高度14像素（光标高度）
        this.label = label;
        this.value = Mth.clamp(initialValue, 0, maxValue);
        
        // 计算滑动条宽度：刻度数量 * 刻度大小 + (最大值+1) * 缩放 + 1
        int milestoneCount = maxValue / MILESTONE_INTERVAL + 1;
        this.barWidth = milestoneCount * MILESTONE_SIZE + (maxValue + 1) * SCALE + 1;
    }
    
    public ValueSlider withCallback(Consumer<Integer> callback) {
        this.onValueChanged = callback;
        return this;
    }
    
    public void setValue(int value) {
        int oldValue = this.value;
        this.value = Mth.clamp(value, 0, maxValue);
        if (oldValue != this.value && onValueChanged != null) {
            onValueChanged.accept(this.value);
        }
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    protected void renderWidget(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (soundCoolDown > 0) {
            soundCoolDown--;
        }
        
        // 检查鼠标按钮状态，如果正在拖动但鼠标已释放，则停止拖动
        if (dragging && GLFW.glfwGetMouseButton(
                Minecraft.getInstance().getWindow().getWindow(), 
                GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
            // 鼠标已释放，播放确认音效（与按钮音效相同）
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f)
            );
            dragging = false;
        }
        
        // 计算滑动条实际开始位置（居中）
        int barStartX = getX() + (width - barWidth) / 2;
        int barY = getY();
        
        // 渲染背景
        GuiTextures.VALUE_SETTINGS_BAR_BG.renderStretched(graphics, barStartX, barY + 3, barWidth, 8);
        
        // 渲染滑动条主体（平铺）
        for (int w = 0; w < barWidth; w += GuiTextures.VALUE_SETTINGS_BAR.getWidth() - 1) {
            int renderWidth = Math.min(GuiTextures.VALUE_SETTINGS_BAR.getWidth() - 1, barWidth - w);
            GuiTextures.VALUE_SETTINGS_BAR.renderCropped(graphics, barStartX + w, barY + 3, renderWidth, 8);
        }
        
        // 渲染刻度标记
        int milestoneCount = maxValue / MILESTONE_INTERVAL + 1;
        int milestoneX = barStartX;
        for (int milestone = 0; milestone < milestoneCount; milestone++) {
            GuiTextures.VALUE_SETTINGS_MILESTONE.render(graphics, milestoneX, barY + 3);
            milestoneX += MILESTONE_SIZE + MILESTONE_INTERVAL * SCALE;
        }
        
        // 如果正在拖动，计算鼠标下的值（即使鼠标离开了组件区域）
        int displayValue = value;
        if (dragging) {
            displayValue = getValueFromX(mouseX, barStartX);
            if (displayValue != lastHoveredValue) {
                playScrollSound(displayValue);
                lastHoveredValue = displayValue;
            }
        }
        
        // 渲染光标
        renderCursor(graphics, barStartX, barY, displayValue);
    }
    
    private void renderCursor(GuiGraphics graphics, int barStartX, int barY, int value) {
        // 计算光标位置
        float xOffset = getXOffsetForValue(value);
        int cursorX = barStartX + (int) xOffset;
        
        // 格式化文本
        String valueText = value + "%";
        int textWidth = Minecraft.getInstance().font.width(valueText);
        
        // 计算光标宽度（文本宽度+边距，确保偶数）
        int cursorWidth = ((textWidth / 2) * 2 + 3);
        cursorX -= cursorWidth / 2;
        
        // 渲染光标
        GuiTextures.VALUE_SETTINGS_CURSOR_LEFT.render(graphics, cursorX - 3, barY);
        GuiTextures.VALUE_SETTINGS_CURSOR.renderCropped(graphics, cursorX, barY, cursorWidth, 14);
        GuiTextures.VALUE_SETTINGS_CURSOR_RIGHT.render(graphics, cursorX + cursorWidth, barY);
        
        // 渲染文本
        graphics.drawString(Minecraft.getInstance().font, valueText, cursorX + 2, barY + 3, 0x442000, false);
    }
    
    /**
     * 根据值计算X偏移
     */
    private float getXOffsetForValue(int value) {
        if (value == 0) return 0.5f;
        
        int milestonesPassed = (value - 1) / MILESTONE_INTERVAL;
        float xOut = milestonesPassed * MILESTONE_SIZE + value * SCALE + 1.5f;
        
        if (value % MILESTONE_INTERVAL == 0) {
            xOut += MILESTONE_SIZE / 2f;
        }
        if (value > 0) {
            xOut += MILESTONE_SIZE;
        }
        
        return xOut;
    }
    
    /**
     * 根据X坐标计算值
     */
    private int getValueFromX(int mouseX, int barStartX) {
        int relativeX = mouseX - barStartX;
        
        // 找最近的值
        int bestValue = 0;
        double bestDiff = Double.MAX_VALUE;
        
        for (int v = 0; v <= maxValue; v++) {
            float xOffset = getXOffsetForValue(v);
            double diff = Math.abs(xOffset - relativeX);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestValue = v;
            } else {
                break;  // 已经开始变远了
            }
        }
        
        return Mth.clamp(bestValue, 0, maxValue);
    }
    
    private void playScrollSound(int value) {
        if (soundCoolDown > 0) return;
        
        float pitch = value / (float) maxValue;
        pitch = Mth.lerp(pitch, 1.15f, 1.5f);
        
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), pitch, 0.25F)
        );
        
        soundCoolDown = 1;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered) {
            dragging = true;
            int barStartX = getX() + (width - barWidth) / 2;
            int newValue = getValueFromX((int) mouseX, barStartX);
            setValue(newValue);
            lastHoveredValue = newValue;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            
            // 播放确认音效（与按钮音效相同）
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f)
            );
            
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            int barStartX = getX() + (width - barWidth) / 2;
            int newValue = getValueFromX((int) mouseX, barStartX);
            setValue(newValue);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isHovered) {
            int change = (int) Math.signum(delta);
            setValue(value + change);
            playScrollSound(value);
            return true;
        }
        return false;
    }
    
    @Override
    protected void updateWidgetNarration(@Nonnull NarrationElementOutput output) {
        output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, 
            Component.translatable("narration.slider", label, value));
    }
}
