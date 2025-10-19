package com.rcell.majobroom.client.gui.util;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * 键盘和鼠标输入工具类
 * 参考 Create 的 AllKeys 实现
 */
public class AllKeys {
    
    /**
     * 检查鼠标按钮是否按下
     * @param button 鼠标按钮 (0=左键, 1=右键, 2=中键)
     */
    public static boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(
            Minecraft.getInstance().getWindow().getWindow(), 
            button
        ) == GLFW.GLFW_PRESS;
    }
}

