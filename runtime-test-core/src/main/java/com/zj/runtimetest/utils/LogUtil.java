package com.zj.runtimetest.utils;

/**
 * @author : jie.zhou
 * @date : 2025/7/30
 */
public class LogUtil {
    public static final String RED_START = "\u001B[31m";
//    public static final String GREEN_START = "\u001B[32m";
    public static final String END = "\u001B[0m";
    private static final ThreadLocal<Boolean> DETAIL_LOG = ThreadLocal.withInitial(() -> false);

    public static void setDetailLog(boolean detailLog) {
        DETAIL_LOG.set(detailLog);
    }

    /**
     * 获取当前线程的详细日志状态
     *
     * @return true表示开启详细日志，false表示关闭
     */
    public static boolean isDetailLogEnabled() {
        return DETAIL_LOG.get();
    }

    /**
     * 清除当前线程的ThreadLocal状态（防止内存泄漏）
     */
    public static void clear() {
        DETAIL_LOG.remove();
    }

    public static void log(String message) {
        log(isDetailLogEnabled(), message);
    }

    public static void err(String message) {
        err(isDetailLogEnabled(), message);
    }

    public static void log(boolean detailLog, String message) {
        if (detailLog) {
            System.out.println(message);
        }
    }

    public static void err(boolean detailLog, String message) {
        if (detailLog) {
            System.out.println(RED_START + message + END);
        }
    }
}
