package com.zj.runtimetest.language;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * 语言绑定
 * @author : jie.zhou
 * @date : 2025/4/18
 */
public class PluginBundle {
    private static final String BUNDLE_NAME = "messages.MessagesBundle";
    private static ResourceBundle bundle;

    static {
        reloadBundle();
    }

    public static void reloadBundle() {
        // 系统语言是中文时，使用中文语言，否则用英文语言
        Locale ideLocale = Optional.ofNullable(Locale.getDefault())
                .filter(v -> "zh".equalsIgnoreCase(v.getLanguage()))
                .orElse(Locale.US);
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, ideLocale);
    }

    public static String get(@NotNull String key) {
        return bundle.getString(key);
    }

    public static String get(@NotNull String key, Object... params) {
        return String.format(get(key), params);
    }
}
