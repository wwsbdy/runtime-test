package com.zj.runtimetest.json;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.PlainTextLanguage;

import java.util.Objects;

/**
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
