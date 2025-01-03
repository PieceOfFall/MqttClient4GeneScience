package com.fall.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.List;

/**
 * @author FAll
 * @date 2024年12月12日 11:31
 */
@Slf4j
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
        s = s.replace(" ", "").toUpperCase();
        return new BigInteger(s, 16).toByteArray();
    }

    public static Boolean validateAddress(String ip, Integer port) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return port >= 1 && port <= 65535;
    }

    public static Boolean validateAddress(List<String> addressList) {
        for (String address : addressList) {
            String[] splitAddress = address.split(":");
            if (splitAddress.length != 2) {
                log.error("Invalid Address: {}", address);
                return false;
            }
            if (!validateAddress(splitAddress[0], Integer.parseInt(splitAddress[1])))
                return false;
        }
        return true;
    }
}
