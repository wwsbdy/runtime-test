package com.zj.runtimetest.json;

import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.LanguageTextField;
import com.zj.runtimetest.utils.ExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 语言编辑框
 *
 * @author 19242
 */
public class JsonEditorField extends LanguageTextField implements Disposable {


    private boolean disposed = false;

    public JsonEditorField(Language language, @Nullable Project project, @NotNull String value, boolean enabled) {
        super(language, project, value);
        TabComponentAdapter tabComponentAdapter = new TabComponentAdapter(this);
        addComponentListener(tabComponentAdapter);
        Disposer.register(this, tabComponentAdapter);
        // 关闭只能一行编辑
        setOneLineMode(false);
        setEnabled(enabled);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        // 垂直滚动条
        editor.setVerticalScrollbarVisible(true);
        // 水平滚动条
        editor.setHorizontalScrollbarVisible(true);

        EditorSettings settings = editor.getSettings();
        // 显示行数
        settings.setLineNumbersShown(true);
        // 自动折叠
        settings.setAutoCodeFoldingEnabled(true);
        // 开启折叠
        settings.setFoldingOutlineShown(true);
        // 允许折叠单个逻辑行
        settings.setAllowSingleLogicalLineFolding(true);
        // 显示右边缘线
        settings.setRightMarginShown(true);
        editor.setFile(new LightVirtualFile("edit.json", JsonLanguage.INSTANCE, ""));
        return editor;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            ExecutorUtil.removeListener(this);
        }
    }
}
