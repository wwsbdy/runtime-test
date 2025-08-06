package com.zj.runtimetest.ui.script;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.ui.expression.ExpressionEditorFactory;
import com.zj.runtimetest.utils.ExecutorUtil;
import com.zj.runtimetest.utils.ExpressionUtil;
import com.zj.runtimetest.utils.RunUtil;
import com.zj.runtimetest.vo.CacheVo;
import com.zj.runtimetest.vo.ProcessVo;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author : jie.zhou
 * @date : 2025/7/31
 */
public class ScriptEditorPanel implements Disposable {

    private static final Logger log = Logger.getInstance(ScriptEditorPanel.class);
    private boolean disposed = false;
    @Getter
    private JPanel mainPanel;
    private ComboBox<Long> pidComboBox;
    private JBCheckBox logDetailCheckBox;

    public ScriptEditorPanel(Project project, CacheVo cacheVo) {
        mainPanel = new JPanel(new BorderLayout());
        // 顶部按钮 + Popup 菜单
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.pidComboBox = new ComboBox<>();
        Dimension fixedSize = new Dimension(150, 28);
        pidComboBox.setPreferredSize(fixedSize);
        pidComboBox.setMaximumSize(fixedSize);
        pidComboBox.setMinimumSize(fixedSize);
        pidComboBox.setPrototypeDisplayValue(10000000000L);
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
        runtimeTestState.addListener(pids -> {
            pidComboBox.removeAllItems();
            pids.forEach(pidComboBox::addItem);
        });

        // 编辑器
        XDebuggerExpressionEditor expressionField =
                ExpressionEditorFactory.createExpressionEditor(project, null, ExpressionUtil.EmptyXExpression.INSTANCE);
        if (Objects.isNull(expressionField)) {
            return;
        }
        // 修改脚本保存到缓存
        Optional.ofNullable(expressionField.getEditor())
                .map(Editor::getDocument)
                .ifPresent(document -> document.addDocumentListener(new DocumentListener() {
                    @Override
                    public void documentChanged(@NotNull DocumentEvent event) {
                        cacheVo.setExpression(expressionField.getExpression());
                    }
                }));
        this.logDetailCheckBox = new JBCheckBox();
        logDetailCheckBox.setToolTipText(PluginBundle.get("dialog.logDetail.title"));
        logDetailCheckBox.setSelected(cacheVo.isDetailLog());
        logDetailCheckBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cacheVo.setDetailLog(logDetailCheckBox.isSelected());
            }
        });
        topPanel.add(logDetailCheckBox);

        expressionField.setExpression(cacheVo.getExpression());
        // 图标按钮（启动/运行）
        AnAction runAction = new AnAction(AllIcons.Actions.Execute) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (Objects.nonNull(pidComboBox.getItem())
                        && Objects.nonNull(expressionField.getExpression())
                        && StringUtils.isNotBlank(expressionField.getExpression().getExpression())) {
                    cacheVo.setPid(pidComboBox.getItem());
                    cacheVo.setExpression(expressionField.getExpression());
                    ExecutorUtil.toFrontRunContent(project, cacheVo.getPid());
                    CompletableFuture.runAsync(() -> RunUtil.run(project, cacheVo))
                            .exceptionally(throwable -> {
                                log.error("run error", throwable);
                                return null;
                            });
                }
            }
        };

        ActionButton runButton = new ActionButton(
                runAction,
                runAction.getTemplatePresentation(),
                "ScriptEditor",
                new Dimension(20, 20)
        );
        runButton.setEnabled(Objects.nonNull(pidComboBox.getSelectedItem()));
        pidComboBox.addActionListener(e -> runButton.setEnabled(Objects.nonNull(pidComboBox.getSelectedItem())));
        topPanel.add(runButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(expressionField.getComponent(), BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            ExecutorUtil.removeListener(pidComboBox);
            ExecutorUtil.removeListener(logDetailCheckBox);
            mainPanel.removeAll();
            pidComboBox = null;
            logDetailCheckBox = null;
            mainPanel = null;
        }
    }
}