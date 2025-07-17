package com.zj.runtimetest.ui;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.debug.ui.PreMethodExpressionDialog;
import com.zj.runtimetest.json.JsonEditorField;
import com.zj.runtimetest.json.JsonLanguage;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.utils.BreakpointUtil;
import com.zj.runtimetest.utils.ExecutorUtil;
import com.zj.runtimetest.vo.CacheVo;
import com.zj.runtimetest.vo.ProcessVo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/**
 * @author arthur_zhou
 */
@Setter
@Getter
public class RuntimeTestDialog extends DialogWrapper {

    private static final Logger log = Logger.getInstance(RuntimeTestDialog.class);


    private final Project project;

    /**
     * swing样式类，定义在4.3.2
     */
    private final JsonEditorField jsonContent;

    private boolean disposed = false;
    private String cacheKey;
    private CacheVo cache;
    private String defaultJson;

    private ComboBox<Long> pidComboBox;
    private JButton resetButton;
    private ComboBox<String> historyComboBox;
    private JButton preMethodButton;

    private final @NotNull XLineBreakpoint<?> bp;

    public RuntimeTestDialog(Project project, String cacheKey, CacheVo cache, String defaultJson, @NotNull XLineBreakpoint<?> bp) {
        super(true);
        // 是否允许拖拽的方式扩大或缩小
        setResizable(true);
        String content = cache.getRequestJson();
        if (Objects.isNull(content)) {
            content = "";
        }
        // 设置会话框标题
        setTitle(PluginBundle.get("dialog.title") + " (" + cache.getMethodName() + ")");
        // 获取到当前项目的名称
        this.project = project;
        this.cacheKey = cacheKey;
        this.cache = cache;
        this.defaultJson = defaultJson;
        this.bp = bp;
        jsonContent = new JsonEditorField(JsonLanguage.INSTANCE, project, content);
        jsonContent.setPreferredSize(new Dimension(500, 700));
        // 触发一下init方法，否则swing样式将无法展示在会话框
        Disposer.register(getDisposable(), jsonContent);
        init();
    }

    @Override
    protected JComponent createNorthPanel() {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, jPanel.getBackground().darker()));
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
        Long pid = cache.getPid();
        if (Objects.nonNull(pid) && runtimeTestState.containsPid(pid)) {
            pidComboBox.setSelectedItem(pid);
        }
        pidComboBox.setToolTipText(PluginBundle.get("dialog.pid.title"));
        jPanel.add(pidComboBox);

        this.historyComboBox = new ComboBox<>();

        cache.forEachHistory(historyComboBox::addItem);
        historyComboBox.setToolTipText(PluginBundle.get("dialog.history.title"));
        historyComboBox.addActionListener(event -> {
            if (Objects.isNull(historyComboBox.getSelectedItem())) {
                return;
            }
            jsonContent.setText(historyComboBox.getSelectedItem().toString());
        });
        jPanel.add(historyComboBox);

        this.resetButton = new JButton(AllIcons.General.Reset);
        resetButton.setToolTipText(PluginBundle.get("dialog.reset.title"));
        resetButton.addActionListener(event -> jsonContent.setText(defaultJson));
        jPanel.add(resetButton);

        this.preMethodButton = new JButton(AllIcons.Nodes.Function);
        preMethodButton.setToolTipText(PluginBundle.get("dialog.preMethodFunction.title"));
        preMethodButton.addActionListener(event -> {
            PreMethodExpressionDialog<?> expressionDialog = new PreMethodExpressionDialog<>(project, bp);
            Disposer.register(getDisposable(), expressionDialog.getDisposable());
            expressionDialog.show();
        });
        jPanel.add(preMethodButton);
        return jPanel;
    }


    @Override
    protected JComponent createCenterPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        contentPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel center = new JPanel();
        center.setLayout(new BorderLayout(0, 0));
        center.setPreferredSize(new Dimension(750, 300));
        panel1.add(center, BorderLayout.CENTER);
        center.add(jsonContent, BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        panel1.add(panel2, BorderLayout.SOUTH);
        return contentPanel;
    }

    @Override
    protected void doOKAction() {
        Long pid = pidComboBox.getItem();
        if (Objects.isNull(pid)) {
            Messages.showErrorDialog(PluginBundle.get("notice.error.no-process-selected"), PluginBundle.get("notice.error"));
            return;
        }
        if (!RuntimeTestState.getInstance(project).containsPid(pid)) {
            Messages.showErrorDialog(PluginBundle.get("notice.error.no-such-process") + " " + pid, PluginBundle.get("notice.error"));
            return;
        }
        String jsonContentText = jsonContent.getText();
        cache.setPid(pid);
        cache.setRequestJson(jsonContentText);
        cache.addHistory(jsonContentText);
        cache.setExpression(bp.getConditionExpression());
        if (Optional.ofNullable(bp.getConditionExpression()).map(XExpression::getExpression).filter(StringUtils::isNotBlank).isEmpty()) {
            ApplicationManager.getApplication()
                    .runWriteAction(() ->
                            BreakpointUtil.removeBreakpoint(project, bp)
                    );
        }
        RuntimeTestState.getInstance(project).putCache(cacheKey, cache);
        toFrontRunContent(pid);
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        Long pid = pidComboBox.getItem();
        String jsonContentText = jsonContent.getText();
        cache.setPid(pid);
        cache.setRequestJson(jsonContentText);
        cache.setExpression(bp.getConditionExpression());
        RuntimeTestState.getInstance(project).putCache(cacheKey, cache);
        ApplicationManager.getApplication()
                .runWriteAction(() ->
                        BreakpointUtil.removeBreakpoint(project, bp)
                );
        super.doCancelAction();
    }

    /**
     * 跳转指定的Run/Debug窗口
     * @param pid 进程id
     */
    private void toFrontRunContent(Long pid) {
        if (Objects.isNull(pid)) {
            log.info("toFrontRunContent pid is null");
            return;
        }
        ProcessVo process = RuntimeTestState.getInstance(project).getProcess(pid);
        if (Objects.isNull(process) || Objects.isNull(process.getExecutionId()) || StringUtils.isEmpty(process.getExecutorId())) {
            log.info("toFrontRunContent process is null");
            return;
        }
        Executor executor = ExecutorRegistry.getInstance().getExecutorById(process.getExecutorId());
        if (Objects.isNull(executor)) {
            log.info("toFrontRunContent executor is null");
            return;
        }
        RunContentManager runContentManager = RunContentManager.getInstance(project);
        RunContentDescriptor runContentDescriptor = runContentManager.getAllDescriptors().stream()
                .filter(descriptor -> process.getExecutionId().equals(descriptor.getExecutionId()))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(runContentDescriptor)) {
            log.info("toFrontRunContent runContentDescriptor is null");
            return;
        }
        runContentManager.toFrontRunContent(executor, runContentDescriptor);
        ExecutionConsole console = runContentDescriptor.getExecutionConsole();
        if (console instanceof ConsoleView) {
            ConsoleView consoleView = (ConsoleView) console;
            consoleView.requestScrollingToEnd();
        }
    }

    @Override
    protected void dispose() {
        if (!disposed) {
            disposed = true;
            ExecutorUtil.removeListener(pidComboBox);
            ExecutorUtil.removeListener(resetButton);
            ExecutorUtil.removeListener(historyComboBox);
            ExecutorUtil.removeListener(preMethodButton);
            pidComboBox = null;
            resetButton = null;
            historyComboBox = null;
            cacheKey = null;
            cache = null;
            defaultJson = null;
            preMethodButton = null;
        }
        super.dispose();
    }
}

