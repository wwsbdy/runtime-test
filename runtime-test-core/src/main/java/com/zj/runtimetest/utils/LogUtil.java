package com.zj.runtimetest.utils;

/**
 * 日志工具类
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

    public static void log(Object message) {
        log(isDetailLogEnabled(), String.valueOf(message));
    }

    public static void err(Object message) {
        err(isDetailLogEnabled(), String.valueOf(message));
    }

    public static void alwaysLog(Object message) {
        System.out.println(message);
    }

    public static void alwaysErr(Object message) {
        System.out.println(RED_START + message + END);
    }

    private static void log(boolean detailLog, String message) {
        if (detailLog) {
            System.out.println(message);
        }
    }

    private static void err(boolean detailLog, String message) {
        if (detailLog) {
            System.out.println(RED_START + message + END);
        }
    }
}
