package com.zj.runtimetest.ui.json;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.PlainTextLanguage;

import java.util.Objects;

/**
 * Json语言
 * 低版本或社区版好像找不到官方的，所以自定义一个
 * @author jie.zhou
 */
public class JsonLanguage {
    public static final Language INSTANCE = getJsonLanguage();

    private static Language getJsonLanguage() {
        Language jsonLanguage = Language.findLanguageByID("JSON");
        if (Objects.isNull(jsonLanguage)) {
            return PlainTextLanguage.INSTANCE;
        }
        return jsonLanguage;
    }
}
