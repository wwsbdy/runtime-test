package com.zj.runtimetest.json;

import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actions.IndentSelectionAction;
import com.intellij.openapi.editor.actions.UnindentSelectionAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.ui.EditorTextField;
import com.zj.runtimetest.utils.ExecutorUtil;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tab键 适配器
 *
 * @author 19242
 */
public class TabComponentAdapter extends ComponentAdapter implements Disposable {
    private final EditorTextField editor;
    private boolean initDone = false;

    private boolean disposed = false;
    private JButton submit;

    public TabComponentAdapter(EditorTextField editor) {
        this.editor = editor;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        componentShown(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        EditorEx editor = (EditorEx) this.editor.getEditor();
        if (Objects.isNull(editor) || initDone) {
            return;
        }
        JComponent content = editor.getContentComponent();
        List<String> indentKeys = getKeys(content, KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, "pressed TAB");
        List<String> unindentKeys = getKeys(content, KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, "shift pressed TAB");

        editor.getSettings().setLineNumbersShown(true);
        content.setFocusTraversalKeysEnabled(false);
        content.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String keyStroke = Objects.isNull(e) ? null : KeyStroke.getKeyStrokeForEvent(e).toString();
                if (indentKeys.contains(keyStroke)) {
                    performAction(editor, new IndentSelectionAction());
                } else if (unindentKeys.contains(keyStroke)) {
                    performAction(editor, new UnindentSelectionAction());
                }
            }
        });
        initDone = true;
    }

    private static List<String> getKeys(JComponent component, int whichKeys, String def) {
        List<String> keys = component.getFocusTraversalKeys(whichKeys).stream()
                .map(AWTKeyStroke::toString)
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(keys) ? Collections.singletonList(def) : keys;
    }

    private static void performAction(EditorEx editor, EditorAction action) {
        for (Caret caret : editor.getCaretModel().getAllCarets()) {
            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> action.getHandler().execute(editor, caret, DataManager.getInstance().getDataContext(editor.getContentComponent())));
        }
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            ExecutorUtil.removeListener(submit);
            submit  = null;
        }
//        if (Objects.nonNull(this.editor.getEditor()) && !this.editor.getEditor().isDisposed()) {
//            EditorFactory.getInstance().releaseEditor(this.editor.getEditor());
//        }
    }
}
