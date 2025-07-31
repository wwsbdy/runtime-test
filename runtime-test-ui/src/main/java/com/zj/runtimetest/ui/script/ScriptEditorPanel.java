package com.zj.runtimetest.ui.script;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.ui.expression.ExpressionEditorFactory;
import com.zj.runtimetest.utils.RunUtil;
import com.zj.runtimetest.vo.CacheVo;
import com.zj.runtimetest.vo.ProcessVo;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/**
 * @author : jie.zhou
 * @date : 2025/7/31
 */
public class ScriptEditorPanel {
    @Getter
    private final JPanel mainPanel;
    private final ComboBox<Long> pidComboBox;

    public ScriptEditorPanel(Project project) {
        mainPanel = new JPanel(new BorderLayout());

        // 顶部按钮 + Popup 菜单
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.pidComboBox = new ComboBox<>();
        RuntimeTestState runtimeTestState = RuntimeTestState.getInstance(project);
        runtimeTestState.getPids().forEach(pidComboBox::addItem);
        pidComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel();
                if (Objects.nonNull(value) && value instanceof Long) {
                    label.setText(
                            Optional.ofNullable(runtimeTestState.getProcess((Long) value))
                                    .map(ProcessVo::getEnv)
                                    .orElse("")
                                    + " - " + value
                    );
                }
                return label;
            }
        });
        pidComboBox.setToolTipText(PluginBundle.get("dialog.pid.title"));
        topPanel.add(pidComboBox);

        // 编辑器
        XDebuggerExpressionEditor expressionField =
                ExpressionEditorFactory.createExpressionEditor(project, null, CacheVo.EmptyXExpression.INSTANCE);
        if (Objects.isNull(expressionField)) {
            return;
        }

        // 图标按钮（启动/运行）
        AnAction runAction = new AnAction("运行", "执行模板操作", AllIcons.Actions.Execute) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                CacheVo cacheVo = new CacheVo();
                cacheVo.setPid(pidComboBox.getItem());
                cacheVo.setExpression(expressionField.getExpression());
                RunUtil.run(project, cacheVo);
            }
        };

        ActionButton runButton = new ActionButton(
                runAction,
                runAction.getTemplatePresentation(),
                "JsonEditor",
                new Dimension(20, 20)
        );
        topPanel.add(runButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(expressionField.getComponent(), BorderLayout.CENTER);
    }

}