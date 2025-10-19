package com.rcell.majobroom.client.tooltip;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.ChatFormatting.*;

/**
 * 物品描述类
 * 用于管理物品的详细说明信息
 * 支持按 Shift/Ctrl 显示不同内容
 */
public record ItemDescription(ImmutableList<Component> lines, 
                               ImmutableList<Component> linesOnShift,
                               ImmutableList<Component> linesOnCtrl) {

    /**
     * 创建物品描述
     * @param item 物品
     * @return 物品描述，如果没有翻译则返回 null
     */
    @Nullable
    public static ItemDescription create(Item item) {
        return create(getTooltipTranslationKey(item));
    }

    /**
     * 通过翻译键创建物品描述
     */
    @Nullable
    public static ItemDescription create(String translationKey) {
        if (!I18n.exists(translationKey + ".summary")) {
            return null;
        }

        Builder builder = new Builder();
        fillBuilder(builder, translationKey);
        return builder.build();
    }

    /**
     * 填充构建器
     */
    public static void fillBuilder(Builder builder, String translationKey) {
        // 摘要
        String summaryKey = translationKey + ".summary";
        if (I18n.exists(summaryKey)) {
            builder.addSummary(I18n.get(summaryKey));
        }

        // 行为说明（按住 Shift 显示）
        for (int i = 1; i < 100; i++) {
            String conditionKey = translationKey + ".condition" + i;
            String behaviourKey = translationKey + ".behaviour" + i;
            if (!I18n.exists(conditionKey))
                break;
            builder.addBehaviour(I18n.get(conditionKey), I18n.get(behaviourKey));
        }

        // 控制说明（按住 Ctrl 显示）
        for (int i = 1; i < 100; i++) {
            String controlKey = translationKey + ".control" + i;
            String actionKey = translationKey + ".action" + i;
            if (!I18n.exists(controlKey))
                break;
            builder.addAction(I18n.get(controlKey), I18n.get(actionKey));
        }
    }

    /**
     * 获取 tooltip 翻译键
     */
    public static String getTooltipTranslationKey(Item item) {
        return item.getDescriptionId() + ".tooltip";
    }

    /**
     * 根据当前按键状态获取对应的行
     */
    public ImmutableList<Component> getCurrentLines() {
        if (Screen.hasShiftDown()) {
            return linesOnShift;
        } else if (Screen.hasControlDown()) {
            return linesOnCtrl;
        } else {
            return lines;
        }
    }

    /**
     * 构建器类
     */
    public static class Builder {
        protected final List<String> summary = new ArrayList<>();
        protected final List<Pair<String, String>> behaviours = new ArrayList<>();
        protected final List<Pair<String, String>> actions = new ArrayList<>();

        public Builder addSummary(String summaryLine) {
            summary.add(summaryLine);
            return this;
        }

        public Builder addBehaviour(String condition, String behaviour) {
            behaviours.add(Pair.of(condition, behaviour));
            return this;
        }

        public Builder addAction(String control, String action) {
            actions.add(Pair.of(control, action));
            return this;
        }

        public ItemDescription build() {
            List<Component> lines = new ArrayList<>();
            List<Component> linesOnShift = new ArrayList<>();
            List<Component> linesOnCtrl = new ArrayList<>();

            // 添加摘要
            for (String summaryLine : summary) {
                linesOnShift.addAll(TooltipHelper.cutStringTextComponent(
                        summaryLine, TooltipHelper.PRIMARY_COLOR, TooltipHelper.HIGHLIGHT_COLOR));
            }

            if (!behaviours.isEmpty()) {
                linesOnShift.add(CommonComponents.EMPTY);
            }

            // 添加行为说明
            for (Pair<String, String> behaviourPair : behaviours) {
                String condition = behaviourPair.getLeft();
                String behaviour = behaviourPair.getRight();
                linesOnShift.add(Component.literal(condition).withStyle(GRAY));
                linesOnShift.addAll(TooltipHelper.cutStringTextComponent(
                        behaviour, TooltipHelper.PRIMARY_COLOR, TooltipHelper.HIGHLIGHT_COLOR, 2));
            }

            // 添加控制说明
            for (Pair<String, String> actionPair : actions) {
                String control = actionPair.getLeft();
                String action = actionPair.getRight();
                linesOnCtrl.add(Component.literal(control).withStyle(GRAY));
                linesOnCtrl.addAll(TooltipHelper.cutStringTextComponent(
                        action, TooltipHelper.PRIMARY_COLOR, TooltipHelper.HIGHLIGHT_COLOR, 2));
            }

            boolean hasDescription = !linesOnShift.isEmpty();
            boolean hasControls = !linesOnCtrl.isEmpty();

            // 添加提示信息（支持多语言）
            if (hasDescription || hasControls) {
                String[] holdDesc = I18n.get("tooltip.majobroom.holdForDescription", "$").split("\\$");
                String[] holdCtrl = I18n.get("tooltip.majobroom.holdForControls", "$").split("\\$");
                MutableComponent keyShift = Component.translatable("tooltip.majobroom.keyShift");
                MutableComponent keyCtrl = Component.translatable("tooltip.majobroom.keyCtrl");
                
                for (List<Component> list : java.util.Arrays.asList(lines, linesOnShift, linesOnCtrl)) {
                    boolean shift = list == linesOnShift;
                    boolean ctrl = list == linesOnCtrl;

                    if (holdDesc.length != 2 || holdCtrl.length != 2) {
                        list.add(0, Component.literal("Invalid lang formatting!"));
                        continue;
                    }

                    if (hasControls) {
                        MutableComponent ctrlBuilder = Component.empty();
                        ctrlBuilder.append(Component.literal(holdCtrl[0]).withStyle(DARK_GRAY));
                        ctrlBuilder.append(keyCtrl.plainCopy()
                                .withStyle(ctrl ? WHITE : GRAY));
                        ctrlBuilder.append(Component.literal(holdCtrl[1]).withStyle(DARK_GRAY));
                        list.add(0, ctrlBuilder);
                    }

                    if (hasDescription) {
                        MutableComponent shiftBuilder = Component.empty();
                        shiftBuilder.append(Component.literal(holdDesc[0]).withStyle(DARK_GRAY));
                        shiftBuilder.append(keyShift.plainCopy()
                                .withStyle(shift ? WHITE : GRAY));
                        shiftBuilder.append(Component.literal(holdDesc[1]).withStyle(DARK_GRAY));
                        list.add(0, shiftBuilder);
                    }

                    // 添加空行
                    if (shift || ctrl)
                        list.add(hasDescription && hasControls ? 2 : 1, CommonComponents.EMPTY);
                }
            }

            // 如果没有详细信息，复制默认行
            if (!hasDescription) {
                linesOnShift.clear();
                linesOnShift.addAll(lines);
            }
            if (!hasControls) {
                linesOnCtrl.clear();
                linesOnCtrl.addAll(lines);
            }

            return new ItemDescription(
                    ImmutableList.copyOf(lines), 
                    ImmutableList.copyOf(linesOnShift), 
                    ImmutableList.copyOf(linesOnCtrl));
        }
    }

    /**
     * Tooltip 修饰器实现
     */
    public static class Modifier implements TooltipModifier {
        protected final Item item;
        protected String cachedLanguage;
        protected ItemDescription description;

        public Modifier(Item item) {
            this.item = item;
        }

        @Override
        public void modify(ItemTooltipEvent context) {
            if (checkLocale()) {
                description = create(item);
            }
            if (description == null) {
                return;
            }
            // 在第二行插入（第一行是物品名称）
            context.getToolTip().addAll(1, description.getCurrentLines());
        }

        /**
         * 检查语言环境是否变化
         */
        protected boolean checkLocale() {
            String currentLanguage = Minecraft.getInstance()
                    .getLanguageManager()
                    .getSelected();
            if (!currentLanguage.equals(cachedLanguage)) {
                cachedLanguage = currentLanguage;
                return true;
            }
            return false;
        }
    }
}

