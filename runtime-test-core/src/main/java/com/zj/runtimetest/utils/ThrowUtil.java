package com.zj.runtimetest.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author : jie.zhou
 * @date : 2025/7/10
 */
public class ThrowUtil {

    public static String printStackTrace(Throwable throwable) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            LogUtil.alwaysErr("failed: " + throwable.getMessage());
        }
        return "";
    }
}
