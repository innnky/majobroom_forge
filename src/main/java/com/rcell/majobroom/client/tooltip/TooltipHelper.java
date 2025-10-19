package com.rcell.majobroom.client.tooltip;

import com.google.common.base.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Tooltip 工具类
 * 提供文本格式化和自动换行功能
 */
public class TooltipHelper {
    
    /**
     * 每行最大宽度（像素）
     */
    public static final int MAX_WIDTH_PER_LINE = 200;

    /**
     * 主文本颜色
     */
    public static final ChatFormatting PRIMARY_COLOR = ChatFormatting.GRAY;

    /**
     * 高亮颜色
     */
    public static final ChatFormatting HIGHLIGHT_COLOR = ChatFormatting.WHITE;

    /**
     * 提示文本颜色
     */
    public static final ChatFormatting HINT_COLOR = ChatFormatting.DARK_GRAY;

    /**
     * 切割字符串文本组件（自动换行）
     * 
     * @param s 要切割的字符串
     * @param primaryColor 主要颜色
     * @param highlightColor 高亮颜色
     * @param indent 缩进空格数
     * @return 切割后的文本组件列表
     */
    public static List<Component> cutStringTextComponent(String s, ChatFormatting primaryColor, 
                                                          ChatFormatting highlightColor, int indent) {
        return cutTextComponent(Component.literal(s), 
                                Style.EMPTY.applyFormat(primaryColor),
                                Style.EMPTY.applyFormat(highlightColor),
                                indent);
    }

    /**
     * 切割字符串文本组件（无缩进）
     */
    public static List<Component> cutStringTextComponent(String s, ChatFormatting primaryColor, 
                                                          ChatFormatting highlightColor) {
        return cutStringTextComponent(s, primaryColor, highlightColor, 0);
    }

    /**
     * 切割文本组件（自动换行，支持下划线高亮）
     * 
     * @param c 文本组件
     * @param primaryStyle 主要样式
     * @param highlightStyle 高亮样式
     * @param indent 缩进
     * @return 切割后的文本组件列表
     */
    public static List<Component> cutTextComponent(Component c, Style primaryStyle,
                                                     Style highlightStyle, int indent) {
        String s = c.getString();

        // 分割单词
        List<String> words = new LinkedList<>();
        BreakIterator iterator = BreakIterator.getLineInstance(Minecraft.getInstance().getLocale());
        iterator.setText(s);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String word = s.substring(start, end);
            words.add(word);
        }

        // 应用硬换行
        Font font = Minecraft.getInstance().font;
        List<String> lines = new LinkedList<>();
        StringBuilder currentLine = new StringBuilder();
        int width = 0;
        for (String word : words) {
            int newWidth = font.width(word.replaceAll("_", ""));
            if (width + newWidth > MAX_WIDTH_PER_LINE) {
                if (width > 0) {
                    String line = currentLine.toString();
                    lines.add(line);
                    currentLine = new StringBuilder();
                    width = 0;
                } else {
                    lines.add(word);
                    continue;
                }
            }
            currentLine.append(word);
            width += newWidth;
        }
        if (width > 0) {
            lines.add(currentLine.toString());
        }

        // 格式化（支持下划线包裹的高亮文本）
        MutableComponent lineStart = Component.literal(Strings.repeat(" ", indent));
        lineStart.withStyle(primaryStyle);
        List<Component> formattedLines = new ArrayList<>(lines.size());

        boolean currentlyHighlighted = false;
        for (String string : lines) {
            MutableComponent currentComponent = lineStart.plainCopy();
            String[] split = string.split("_", -1);
            for (String part : split) {
                currentComponent.append(Component.literal(part)
                        .withStyle(currentlyHighlighted ? highlightStyle : primaryStyle));
                currentlyHighlighted = !currentlyHighlighted;
            }

            formattedLines.add(currentComponent);
            currentlyHighlighted = !currentlyHighlighted;
        }

        return formattedLines;
    }
}

