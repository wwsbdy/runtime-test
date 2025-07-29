package com.zj.runtimetest.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author 19242
 */
public class Base64Util {

    public static String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String str) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(str);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[Agent] Base64 decode fail: " + str);
            return null;
        }
    }
}
