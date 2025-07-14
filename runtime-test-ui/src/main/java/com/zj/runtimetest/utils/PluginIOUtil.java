package com.zj.runtimetest.utils;

import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author : jie.zhou
 * @date : 2025/7/11
 */
public class PluginIOUtil {
    private static final Logger log = Logger.getInstance(PluginIOUtil.class);

    /**
     * rewrite content to file
     */
    public static boolean flushFile(String filePath, String content) {
        File file = new File(filePath);
        try {
            com.intellij.openapi.util.io.FileUtil.writeToFile(file, content);
            return true;
        } catch (IOException e) {
            log.error("flushFile error [filePath:{} content:{} errMsg:{}]", filePath, content, e.getMessage());
        }
        return false;
    }
}
