package com.fall.utils;

import java.math.BigInteger;

/**
 * @author FAll
 * @date 2024年12月12日 11:31
 */
public class NetUtil {
    private final static char[] digits = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int c = 0;
        for (byte b : bytes) {
            buf[c++] = digits[(b >> 4) & 0x0F];
            buf[c++] = digits[b & 0x0F];
        }
        return new String(buf);
    }

    public static byte[] hexStringToByteArray(String s) {
        s = s.replace(" ","").toUpperCase();
        return new BigInteger(s, 16).toByteArray();
    }
}
